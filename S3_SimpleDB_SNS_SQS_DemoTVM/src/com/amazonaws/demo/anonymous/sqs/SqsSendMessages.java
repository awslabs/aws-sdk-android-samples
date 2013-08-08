/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.demo.anonymous.sqs;

import java.util.List;

import com.amazonaws.demo.anonymous.R;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.demo.anonymous.AlertActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SqsSendMessages extends AlertActivity {

	protected Handler mHandler;
	protected Spinner queueSpinner;
	protected ArrayAdapter<String> queueListAdapter;
	protected EditText messageInput;
	protected Button submitButton;
	protected List<String> queueUrls;

	private Runnable postResults = new Runnable() {
		@Override
		public void run() {
			updateUi();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sqs_send);
		queueSpinner = (Spinner) findViewById(R.id.sqs_send_queue_spinner);
		messageInput = (EditText) findViewById(R.id.sqs_send_message_input);
		submitButton = (Button) findViewById(R.id.sqs_send_submit_button);
		mHandler = new Handler();
		getTopicArn();
	}

	private void getTopicArn() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					queueUrls = SimpleQueue.getQueueUrls();
					mHandler.post(postResults);
				} catch (AmazonServiceException e) {
					if ("InvalidAccessKeyId".equals(e.getErrorCode())) {
						putRefreshError();
					} else {
						setStackAndPost(e);
					}
				} catch (Throwable e) {
					setStackAndPost(e);
				}
			}
		};
		t.start();
	}

	private void wireSubmitButton() {
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				queueSpinner.setVisibility(View.INVISIBLE);
				messageInput.setVisibility(View.INVISIBLE);
				new SendMessageTask().execute();
			}
		});
	}

	private void updateUi() {
		queueListAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, queueUrls);
		queueSpinner.setAdapter(queueListAdapter);
		wireSubmitButton();
	}

	private class SendMessageTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			try {
				SimpleQueue.sendMessage(queueSpinner.getSelectedItem()
						.toString(), messageInput.getText().toString());

			} catch (AmazonServiceException e) {
				if ("InvalidAccessKeyId".equals(e.getErrorCode())) {
					putRefreshError();
				} else {
					setStackAndPost(e);
				}
			} catch (Throwable e) {
				setStackAndPost(e);
			}

			return null;
		}

		protected void onPostExecute(Void result) {
			finish();
		}
	}
}