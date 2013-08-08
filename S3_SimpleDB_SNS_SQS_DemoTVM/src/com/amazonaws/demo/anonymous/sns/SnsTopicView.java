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
package com.amazonaws.demo.anonymous.sns;

import java.util.List;

import com.amazonaws.demo.anonymous.CustomListActivity;

import android.os.Bundle;


public class SnsTopicView extends CustomListActivity{
	
	protected String topicArn;
	protected List<String> subscriptionListArray;
	
	private static final String SUCCESS = "Subscription List";

	private Runnable postResults = new Runnable(){
		@Override
		public void run(){
			updateUi(subscriptionListArray, SUCCESS);
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        topicArn = extras.getString(SimpleNotification.TOPIC_ARN);
        startPopulateList();

    }
    
    protected void obtainListItems(){
    	if(topicArn.equals(""))
    		subscriptionListArray = SimpleNotification.getSubscriptionNames();
    	else
    		subscriptionListArray = SimpleNotification.getSubscriptionNamesByTopic(topicArn);

		getHandler().post(postResults);
    }

}
