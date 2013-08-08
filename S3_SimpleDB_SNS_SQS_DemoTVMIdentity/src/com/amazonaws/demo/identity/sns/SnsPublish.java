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
package com.amazonaws.demo.identity.sns;

import java.util.List;

import com.amazonaws.demo.identity.R;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.demo.identity.AlertActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SnsPublish extends AlertActivity {

	protected Handler mHandler;
	protected Spinner topicSpinner;
	protected ArrayAdapter<String> topicListAdapter;
	protected EditText messageInput;
	protected Button submitButton;
	protected List<String> topicArns;

	private Runnable postResults = new Runnable() {
		@Override
		public void run() {
			updateUi();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sns_publish);
		topicSpinner = (Spinner) findViewById(R.id.sns_pub_topic_spinner);
		messageInput = (EditText) findViewById(R.id.sns_pub_message_input);
		submitButton = (Button) findViewById(R.id.sns_pub_submit_button);
		mHandler = new Handler();
		getTopicArn();
	}

	private void getTopicArn() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					topicArns = SimpleNotification.getTopicNames();
					mHandler.post(postResults);
				} catch (AmazonServiceException e) {
					if ("ExpiredToken".equals(e.getErrorCode())) {
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
				topicSpinner.setVisibility(View.INVISIBLE);
				messageInput.setVisibility(View.INVISIBLE);
				new PublishTask().execute();
			}
		});
	}

	private void updateUi() {
		topicListAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, topicArns);
		topicSpinner.setAdapter(topicListAdapter);
		wireSubmitButton();
	}

	private class PublishTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			try {
				SimpleNotification.publish(topicSpinner.getSelectedItem()
						.toString(), messageInput.getText().toString());

			} catch (AmazonServiceException e) {
				if ("ExpiredToken".equals(e.getErrorCode())) {
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