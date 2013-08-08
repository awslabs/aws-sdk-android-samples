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
package com.amazonaws.demo.anonymous;

import com.amazonaws.demo.anonymous.R;

import com.amazonaws.demo.anonymous.AmazonClientManager;
import com.amazonaws.demo.anonymous.s3.S3Menu;
import com.amazonaws.demo.anonymous.sdb.SdbMenu;
import com.amazonaws.demo.anonymous.sns.SnsMenu;
import com.amazonaws.demo.anonymous.sqs.SqsMenu;
import com.amazonaws.tvmclient.Response;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AWSAndroidDemoTVM extends Activity {

	private static final String success = "Welcome to The AWS Browser Demo!";
	private static final String fail = "Load Failed. Please Try Restarting the Application.";

	protected Button snsButton;
	protected Button sqsButton;
	protected Button s3Button;
	protected Button sdbButton;
	protected Button logoutButton;
	protected TextView welcomeText;

	public static AmazonClientManager clientManager = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		snsButton = (Button) findViewById(R.id.main_notify_button);
		sqsButton = (Button) findViewById(R.id.main_queue_button);
		s3Button = (Button) findViewById(R.id.main_storage_button);
		sdbButton = (Button) findViewById(R.id.main_sdb_button);
		logoutButton = (Button) findViewById(R.id.main_logout_button);
		welcomeText = (TextView) findViewById(R.id.main_into_text);

		clientManager = new AmazonClientManager(getSharedPreferences(
				"com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE));

		if (AWSAndroidDemoTVM.clientManager.hasCredentials()) {
			welcomeText.setText(success);
			snsButton.setVisibility(View.VISIBLE);
			sqsButton.setVisibility(View.VISIBLE);
			s3Button.setVisibility(View.VISIBLE);
			sdbButton.setVisibility(View.VISIBLE);
			logoutButton.setVisibility(View.VISIBLE);
			this.wireButtons();
		} else {
			this.displayCredentialsIssueAndExit();
			welcomeText.setText(fail);
		}
	}

	private void wireButtons() {
		snsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new ValidateCredentialsTask().execute(SnsMenu.class);
			}
		});

		sqsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new ValidateCredentialsTask().execute(SqsMenu.class);
			}
		});

		s3Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new ValidateCredentialsTask().execute(S3Menu.class);
			}
		});

		sdbButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new ValidateCredentialsTask().execute(SdbMenu.class);
			}
		});

		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clientManager.clearCredentials();
				displayLogoutSuccess();
			}
		});
	}

	protected void displayCredentialsIssueAndExit() {
		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setTitle("Credential Problem!");
		confirm.setMessage("AWS Credentials not configured correctly.  Please review the README file.");
		confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				AWSAndroidDemoTVM.this.finish();
			}
		});
		confirm.show().show();
	}

	protected void displayErrorAndExit(Response response) {
		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		if (response == null) {
			confirm.setTitle("Error Code Unkown");
			confirm.setMessage("Please review the README file.");
		} else {
			confirm.setTitle("Error Code [" + response.getResponseCode() + "]");
			confirm.setMessage(response.getResponseMessage()
					+ "\nPlease review the README file.");
		}

		confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				AWSAndroidDemoTVM.this.finish();
			}
		});
		confirm.show().show();
	}

	protected void displayLogoutSuccess() {
		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setTitle("Logout");
		confirm.setMessage("You have successfully cleared the credentials.");
		confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		confirm.show().show();
	}

	private class ValidateCredentialsTask extends
			AsyncTask<Class<?>, Void, Response> {

		Class<?> cls;

		protected Response doInBackground(Class<?>... classes) {

			cls = classes[0];
			return AWSAndroidDemoTVM.clientManager.validateCredentials();
		}

		protected void onPostExecute(Response response) {
			if (response != null && response.requestWasSuccessful()) {
				startActivity(new Intent(AWSAndroidDemoTVM.this, cls));
			} else {
				AWSAndroidDemoTVM.this.displayErrorAndExit(response);
			}
		}
	}
}
