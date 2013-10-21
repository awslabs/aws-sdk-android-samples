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

import com.amazonaws.demo.CustomListActivity;

import android.os.AsyncTask;
import android.os.Bundle;

public class SnsTopicView extends CustomListActivity {

	protected String topicArn;
	protected List<String> subscriptionListArray;

	private static final String SUCCESS = "Subscription List";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = this.getIntent().getExtras();
		topicArn = extras.getString(SimpleNotification.TOPIC_ARN);
		startPopulateList();

	}

	protected void obtainListItems() {
		new ObtainListItemsTask().execute();
	}

	private class ObtainListItemsTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {
			if (topicArn.equals("")) {
				subscriptionListArray = SimpleNotification
						.getSubscriptionNames();
			} else {
				subscriptionListArray = SimpleNotification
						.getSubscriptionNamesByTopic(topicArn);
			}

			return null;
		}

		protected void onPostExecute(Void result) {
			updateUi(subscriptionListArray, SUCCESS);
		}
	}
}
