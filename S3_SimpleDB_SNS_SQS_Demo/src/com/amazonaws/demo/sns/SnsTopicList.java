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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;

public class SnsTopicList extends CustomListActivity{
	
	protected List<String> topicNameList;
	
	private static final String SUCCESS = "Topic List";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startPopulateList();
    }
    
    protected void obtainListItems(){
		new ObtainListItemsTask().execute();
    }
    
	protected void wireOnListClick(){
		getItemList().setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
		    	final String topicArn = topicNameList.get(position);
				Intent snsTopicIntent = new Intent(SnsTopicList.this, SnsTopicView.class);
				snsTopicIntent.putExtra( SimpleNotification.TOPIC_ARN, topicArn );
				startActivity(snsTopicIntent);
		    }
		 });
	}
    	
	private class ObtainListItemsTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {
			topicNameList = SimpleNotification.getTopicNames();
			return null;
		}

		protected void onPostExecute(Void result) {
			updateUi(topicNameList, SUCCESS);
		}
	}
}
