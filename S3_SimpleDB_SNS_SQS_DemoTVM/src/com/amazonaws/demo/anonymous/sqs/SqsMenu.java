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

import com.amazonaws.demo.anonymous.R;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SqsMenu extends Activity {
	
	Button createQueue;
	Button queueList;
	Button deleteQueueList;
	Button sendMessages;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sqs_menu);
        wireButtons();
    }
    
    public void wireButtons(){
    	createQueue = (Button) findViewById(R.id.sqs_create_queue_button);
    	queueList = (Button) findViewById(R.id.sqs_list_queues_button);
    	sendMessages = (Button) findViewById(R.id.sqs_send_message_button);
    	deleteQueueList = (Button) findViewById(R.id.sqs_delete_queue_button);
    	
    	createQueue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SqsMenu.this, SqsCreateQueue.class));
			}
		});
    	
    	queueList.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SqsMenu.this, SqsQueueList.class));
			}
		});
    	
    	deleteQueueList.setOnClickListener(new View.OnClickListener() {
    		
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SqsMenu.this, SqsDeleteQueueList.class));
			}
		});
    	
    	sendMessages.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SqsMenu.this, SqsSendMessages.class));
			}
		});
    	

    	
    }

}
