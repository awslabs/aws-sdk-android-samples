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
package com.amazonaws.demo.identity.s3;

import com.amazonaws.demo.identity.R;


import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;


public class S3ObjectView extends Activity{
	
	protected Handler mHandler;
	protected TextView loadingText;
	protected TextView bodyText;
	protected String bucketName;
	protected String objectName;
	protected String objectData;
	
	private final Runnable postResults = new Runnable() {
		@Override
		public void run(){
			updateUi();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_view);
        Bundle extras = this.getIntent().getExtras();
        bucketName = extras.getString(S3.BUCKET_NAME);
        objectName = extras.getString(S3.OBJECT_NAME);
        mHandler = new Handler();
        loadingText = (TextView) findViewById(R.id.item_view_loading_text);
        bodyText = (TextView) findViewById(R.id.item_view_body_text);
        startPopulateText();
    }
    
    private void startPopulateText(){
    	Thread t = new Thread() {
    		@Override
    		public void run(){
    			objectData = S3.getDataForObject(bucketName, objectName);
    	        mHandler.post(postResults);
    		}
    	};
    	t.start();
    }
    
    private void updateUi(){
    	loadingText.setText(objectName);
    	bodyText.setText(objectData);
    	loadingText.setTextSize(16);
    }	
}