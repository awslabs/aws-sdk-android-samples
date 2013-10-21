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

package com.amazonaws.demo.sqs;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.amazonaws.demo.CustomListActivity;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;

public class SqsDeleteQueueList extends CustomListActivity {

	protected List<String> queueListArray;
	protected String clickedQueueUrl;

	private static final String SUCCESS = "Delete Queue";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startPopulateList();
	}

	protected void obtainListItems() {
		new ObtainMoreItemsTask().execute();
	}

	protected void wireOnListClick() {
		getItemList().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View view,
					int position, long id) {
				clickedQueueUrl = ((TextView) view).getText().toString();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SqsDeleteQueueList.this);
				builder.setMessage(
						"Are you sure you want to delete: " + clickedQueueUrl)
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										new DeleteQueueTask().execute();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
				builder.show();
			}
		});
	}

	private class DeleteQueueTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			SimpleQueue.getInstance().deleteQueue(
					new DeleteQueueRequest(clickedQueueUrl));

			return null;
		}

		protected void onPostExecute(Void result) {
			startPopulateList();
		}
	}

	private class ObtainMoreItemsTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {
			queueListArray = SimpleQueue.getQueueUrls();
			return null;
		}

		protected void onPostExecute(Void result) {
			updateUi(queueListArray, SUCCESS);
		}
	}
}