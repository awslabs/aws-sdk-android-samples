/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazonaws.demo.sns;

import java.util.List;

import com.amazonaws.demo.R;
import com.amazonaws.demo.AlertActivity;
import com.amazonaws.demo.sqs.SimpleQueue;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SnsSubscribe extends AlertActivity {

	protected ArrayAdapter<String> topicListAdapter;
	protected ArrayAdapter<String> queueListAdapter;
	protected Spinner topicSpinner;
	protected Spinner protocolSpinner;
	protected EditText endpointInput;
	protected TextView topicIntro;
	protected TextView protoIntro;
	protected TextView endpointIntro;
	protected Spinner endpointSpinner;
	protected Button submitButton;
	protected Handler mHandler;
	protected List<String> topicArns;
	protected List<String> queueUrls;
	private int inputTextStatus;

	public static final int INPUT_TEXT_ON = 0;
	public static final int INPUT_TEXT_OFF = 1;
	public static final int POLICY_PROPAGATION_TIME_IN_SECONDS = 12;

	private Runnable postResults = new Runnable() {
		@Override
		public void run() {
			updateUi();
		}
	};

	private Runnable postDone = new Runnable() {
		@Override
		public void run() {
			finish();
		}
	};

	private Runnable postProp = new Runnable() {
		@Override
		public void run() {
			clearUi();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sns_subscribe);
		topicSpinner = (Spinner) findViewById(R.id.sns_sub_topic_spinner);
		protocolSpinner = (Spinner) findViewById(R.id.sns_sub_protocol_spinner);
		endpointSpinner = (Spinner) findViewById(R.id.sns_sub_endpoint_spinner);
		endpointInput = (EditText) findViewById(R.id.sns_sub_endpoint_input);
		submitButton = (Button) findViewById(R.id.sns_sub_submit_button);
		topicIntro = (TextView) findViewById(R.id.sns_sub_topic_intro_text);
		protoIntro = (TextView) findViewById(R.id.sns_sub_protocol_intro_text);
		endpointIntro = (TextView) findViewById(R.id.sns_sub_endpoint_intro_text);
		mHandler = new Handler();
		inputTextStatus = INPUT_TEXT_ON;
		getTopicArn();
		wireOnSelect();
	}

	private void getTopicArn() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					topicArns = SimpleNotification.getTopicNames();
					queueUrls = SimpleQueue.getQueueUrls();
					mHandler.post(postResults);
				} catch (Throwable e) {
					setStackAndPost(e);
				}
			}
		};
		t.start();
	}

	private void subscribeQueue(String url, String arn) {
		final String tUrl = url;
		final String tArn = arn;
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					SimpleQueue.allowNotifications(tUrl, tArn);
					mHandler.post(postProp);
				} catch (Throwable e) {
					setStackAndPost(e);
				}
				try {
					Thread.sleep(POLICY_PROPAGATION_TIME_IN_SECONDS * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					String end = SimpleQueue.getQueueArn(tUrl);
					SimpleNotification.subscribe(tArn,
							(String) protocolSpinner.getSelectedItem(), end);
					mHandler.post(postDone);
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
				protocolSpinner.setVisibility(View.INVISIBLE);

				if (inputTextStatus == INPUT_TEXT_ON) {

					endpointInput.setVisibility(View.GONE);
					new SubscribeTask().execute();

				} else {

					String arn = (String) topicSpinner.getSelectedItem()
							.toString();

					endpointSpinner.setVisibility(View.GONE);
					String url = (String) endpointSpinner.getSelectedItem()
							.toString();
					subscribeQueue(url, arn);
				}

			}
		});
	}

	private void wireOnSelect() {
		protocolSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// Show Spinner for SQS
						if (position == 4) {
							swapField(INPUT_TEXT_OFF);
						} else {
							swapField(INPUT_TEXT_ON);
						}
						// Prefix web requests
						if (position == 0) {
							endpointInput.setText("http://");
						} else if (position == 1) {
							endpointInput.setText("https://");
						} else {
							endpointInput.setText("");
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}

				});
	}

	private void updateUi() {
		topicListAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, topicArns);
		queueListAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, queueUrls);
		topicSpinner.setAdapter(topicListAdapter);
		endpointSpinner.setAdapter(queueListAdapter);
		topicSpinner.setVisibility(View.VISIBLE);
		wireSubmitButton();
	}

	private void clearUi() {
		submitButton.setVisibility(View.INVISIBLE);
		topicIntro.setVisibility(View.INVISIBLE);
		protoIntro.setVisibility(View.INVISIBLE);
		endpointIntro
				.setText("Waiting for 12 Seconds For Subscribe to Propagate. . .");
		endpointIntro.setGravity(Gravity.CENTER);
	}

	private void swapField(int i) {
		if (i == INPUT_TEXT_ON && inputTextStatus == INPUT_TEXT_OFF) {
			endpointSpinner.setVisibility(View.GONE);
			endpointInput.setVisibility(View.VISIBLE);
			inputTextStatus = INPUT_TEXT_ON;
		} else if (i == INPUT_TEXT_OFF && inputTextStatus == INPUT_TEXT_ON) {
			endpointInput.setVisibility(View.GONE);
			endpointSpinner.setVisibility(View.VISIBLE);
			inputTextStatus = INPUT_TEXT_OFF;
		}
	}

	private class SubscribeTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			try {
				SimpleNotification.subscribe((String) topicSpinner
						.getSelectedItem().toString(), (String) protocolSpinner
						.getSelectedItem(), endpointInput.getText().toString());

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
