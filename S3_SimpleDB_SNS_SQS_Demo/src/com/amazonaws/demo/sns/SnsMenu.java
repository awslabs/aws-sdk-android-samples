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

import com.amazonaws.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SnsMenu extends Activity {
	
	Button topicListButton;
	Button topicDeleteButton;
	Button subscribeButton;
	Button subscriberListButton;
	Button publishButton;
	Button createButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sns_menu);
        wireButtons();
    }
    
    public void wireButtons(){
    	topicDeleteButton = (Button) findViewById(R.id.sns_main_delete_button);
    	topicListButton = (Button) findViewById(R.id.sns_main_topic_list_button);
    	subscribeButton = (Button) findViewById(R.id.sns_main_subscribe_button);
    	subscriberListButton = (Button) findViewById(R.id.sns_main_subscriber_list_button);
    	publishButton = (Button) findViewById(R.id.sns_main_publish_button);
    	createButton = (Button) findViewById(R.id.sns_main_create_button);
    	
    	topicDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SnsMenu.this, SnsDeleteTopicList.class));
			}
		});
    	
    	topicListButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SnsMenu.this, SnsTopicList.class));
			}
		});
    	
    	subscribeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SnsMenu.this, SnsSubscribe.class));
			}
		});
    	
    	subscriberListButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent subscriberListIntent = new Intent(SnsMenu.this, SnsTopicView.class);
				subscriberListIntent.putExtra(SimpleNotification.TOPIC_ARN, "");
				startActivity(subscriberListIntent);
				
			}
		});
    	
    	publishButton.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SnsMenu.this, SnsPublish.class));
			}
		});
    	
    	createButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SnsMenu.this, SnsCreateTopic.class));
			}
		});
    }

}
