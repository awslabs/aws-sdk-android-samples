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
package com.amazonaws.demo.identity.sdb;

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
import android.widget.TextView;

public class SdbItemCreate extends AlertActivity {

	protected Button submitButton;
	protected Spinner domainName;
	protected EditText itemName;
	protected ArrayAdapter<String> domainNameAdapter;
	protected List<String> domainNameList;
	protected Handler mHandler;
	protected TextView firstIntroText;
	protected TextView secondIntroText;

	protected Runnable postResults = new Runnable() {
		@Override
		public void run() {
			updateUi();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_item_with_spinner);
		submitButton = (Button) findViewById(R.id.create_sp_submit_button);
		itemName = (EditText) findViewById(R.id.create_sp_input_field);
		firstIntroText = (TextView) findViewById(R.id.create_sp_first_intro_text);
		secondIntroText = (TextView) findViewById(R.id.create_sp_second_intro_text);
		domainName = (Spinner) findViewById(R.id.create_sp_spinner);
		mHandler = new Handler();
		firstIntroText.setText("Enter Domain Name:");
		secondIntroText.setText("Enter Item Name:");
		getDomains();
	}

	public void getDomains() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					domainNameList = SimpleDB.getDomainNames();
					mHandler.post(postResults);
				} catch (AmazonServiceException e) {
					putRefreshError();
				} catch (Throwable e) {
					setStackAndPost(e);
				}
			}
		};
		t.start();
	}

	public void updateUi() {
		domainNameAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, domainNameList);
		domainName.setAdapter(domainNameAdapter);
		domainName.setClickable(true);
		wireSubmitButton();
	}

	public void wireSubmitButton() {
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				itemName.setVisibility(View.INVISIBLE);
				new CreateItemTask().execute();
			}
		});
	}

	private class CreateItemTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			try {
				SimpleDB.createItem(domainName.getSelectedItem().toString(),
						itemName.getText().toString());

			} catch (AmazonServiceException e) {
				if ("InvalidClientTokenId".equals(e.getErrorCode())) {
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