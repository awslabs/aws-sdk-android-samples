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
package com.amazonaws.demo.anonymous.sqs;

import java.util.List;

import com.amazonaws.demo.anonymous.CustomListActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class SqsRecieveMessages extends CustomListActivity {
	
	protected List<String> queueMessageArray;
	private String queueUrl;
	
	private static final String SUCCESS = "Recieved Messages";
	
	
	private Runnable postResults = new Runnable(){
		@Override
		public void run(){
			updateUi(queueMessageArray, SUCCESS, CustomListActivity.LEFT);
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        Bundle extras = this.getIntent().getExtras();
        queueUrl = extras.getString(SimpleQueue.QUEUE_URL);
        startPopulateList();
    }
    
    protected void obtainListItems(){
    	
		queueMessageArray = SimpleQueue.recieveMessageIds(queueUrl);
		getHandler().post(postResults);
    }
    
    @Override
    protected void wireOnListClick(){
		getItemList().setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
				Intent messageBodyIntent = new Intent(SqsRecieveMessages.this, SqsMessageBody.class);
				messageBodyIntent.putExtra( SimpleQueue.MESSAGE_INDEX, position );
				messageBodyIntent.putExtra( SimpleQueue.MESSAGE_ID, queueMessageArray.get(position));
				startActivity(messageBodyIntent);
		    }
		 });
    }
    
    
}
