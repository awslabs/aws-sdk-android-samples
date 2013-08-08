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
package com.amazonaws.demo.identity;

import com.amazonaws.demo.identity.R;
import com.amazonaws.tvmclient.Response;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.net.Uri;

public class Login extends AlertActivity {

	protected Button okButton;
	protected Button registerButton;
	protected EditText username;
	protected EditText password;
	protected TextView introText;
	protected TextView usernameHeader;
	protected TextView passwordHeader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_menu);

		introText = (TextView) findViewById(R.id.login_intro_text);
		introText.setText("Login");

		usernameHeader = (TextView) findViewById(R.id.login_username_header);
		usernameHeader.setText("Username:");

		username = (EditText) findViewById(R.id.login_username_input_field);

		passwordHeader = (TextView) findViewById(R.id.login_password_header);
		passwordHeader.setText("Password:");

		password = (EditText) findViewById(R.id.login_password_input_field);

		okButton = (Button) findViewById(R.id.login_main_ok_button);
		registerButton = (Button) findViewById(R.id.login_main_register_button);

		wireButtons();
	}

	public void wireButtons() {
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new TVMLoginTask().execute();
			}
		});

		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String registerURL = (PropertyLoader.getInstance().useSSL() ? "https://"
							: "http://")
							+ PropertyLoader.getInstance()
									.getTokenVendingMachineURL()
							+ "/register.jsp";
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(registerURL)));
				} catch (Throwable e) {
				}

			}
		});
	}

	protected void displayCredentialsIssueAndExit() {
		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setTitle("Credential Problem!");
		confirm.setMessage("AWS Credentials not configured correctly.  Please review the README file.");
		confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Login.this.finish();
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
				Login.this.finish();
			}
		});
		confirm.show().show();
	}

	private class TVMLoginTask extends AsyncTask<Void, Void, Response> {

		protected Response doInBackground(Void... voids) {

			try {
				return AWSAndroidDemoTVMIdentity.clientManager.login(username
						.getText().toString(), password.getText().toString());

			} catch (Throwable e) {
				setStackAndPost(e);
			}

			return null;
		}

		protected void onPostExecute(Response response) {

			if (response != null && response.getResponseCode() == 404) {
				Login.this.displayErrorAndExit(response);
			} else if (response != null && response.getResponseCode() != 200) {
				Login.this.displayCredentialsIssueAndExit();
			} else {
				finish();
			}
		}
	}
}
