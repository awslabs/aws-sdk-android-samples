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
package com.amazonaws.demo.feedback;

import com.amazonaws.demo.feedback.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.EditText;

public class FeedbackFormDemoActivity extends Activity {

	protected Button submitButton;
	protected RatingBar ratingBar;
	protected EditText nameField;
	protected EditText commentsField;

	public static AmazonClientManager clientManager = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		submitButton = (Button) findViewById(R.id.submitButton);
		ratingBar = (RatingBar) findViewById(R.id.ratingBar);
		nameField = (EditText) findViewById(R.id.nameField);
		commentsField = (EditText) findViewById(R.id.commentsField);

		clientManager = new AmazonClientManager();

		if (FeedbackFormDemoActivity.clientManager.hasCredentials()) {
			submitButton.setVisibility(View.VISIBLE);
			this.wireButtons();
		} else {
			this.displayCredentialsIssueAndExit();
		}
	}

	/** Configure onClick handlers for buttons */
	private void wireButtons() {
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendFeedback();
			}
		});
	}

	/** Check data that was entered and send email if valid */
	protected void sendFeedback() {

		if (commentsField.getText().length() == 0
				|| nameField.getText().length() == 0) {

			AlertDialog.Builder confirm = new AlertDialog.Builder(this);

			confirm.setTitle("Feedback Not Sent!");
			confirm.setMessage("Please fill out the form before submitting.");
			confirm.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			confirm.show();
		} else {
			new SendFeedbackTask().execute();
		}
	}

	/** Display an error and exit if credentials aren't provided */
	protected void displayCredentialsIssueAndExit() {
		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setTitle("Credential Problem!");
		confirm.setMessage("AWS Credentials not configured correctly.  Please review the README file.");
		confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				FeedbackFormDemoActivity.this.finish();
			}
		});
		confirm.show().show();
	}

	private class SendFeedbackTask extends AsyncTask<Void, Void, Boolean> {

		protected Boolean doInBackground(Void... voids) {

			boolean didSucceed = SESManager.sendFeedbackEmail(commentsField
					.getText().toString(), nameField.getText().toString(),
					ratingBar.getRating());

			return didSucceed;
		}

		protected void onPostExecute(Boolean didSucceed) {

			AlertDialog.Builder confirm = new AlertDialog.Builder(
					FeedbackFormDemoActivity.this);

			if (didSucceed) {
				confirm.setTitle("Feedback Success!");
				confirm.setMessage("Thank you for your feedback.");
				confirm.setNegativeButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
			} else {
				confirm.setTitle("Feedback Failed!");
				confirm.setMessage("Unable to send feedback at this time.");
				confirm.setNegativeButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								FeedbackFormDemoActivity.this.finish();
							}
						});
			}

			confirm.show();
		}
	}
}