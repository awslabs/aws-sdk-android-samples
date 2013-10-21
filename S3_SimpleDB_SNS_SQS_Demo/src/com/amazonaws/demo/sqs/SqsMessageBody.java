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

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.demo.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SqsMessageBody extends Activity {
	
	protected TextView loadingText;
	protected TextView bodyText;
	protected int messageIndex;
	protected String messageId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_view);
        Bundle extras = this.getIntent().getExtras();
        messageIndex = extras.getInt(SimpleQueue.MESSAGE_INDEX);
        messageId = extras.getString(SimpleQueue.MESSAGE_ID);
        loadingText = (TextView) findViewById(R.id.item_view_loading_text);
        bodyText = (TextView) findViewById(R.id.item_view_body_text);
        updateUi();
    }
        
    private void updateUi(){
    	loadingText.setText(messageId);
    	String message = SimpleQueue.getMessageBody(messageIndex);
    	String decodedMessage = new String(Base64.decodeBase64(message.getBytes()));
    	if(decodedMessage.charAt(0) == '{' && decodedMessage.endsWith("}"))
    		bodyText.setText(decodedMessage);
    	else
    		bodyText.setText(message);
    	loadingText.setTextSize(16);
    }


}
