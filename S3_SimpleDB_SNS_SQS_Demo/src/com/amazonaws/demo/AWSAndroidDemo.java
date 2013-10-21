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
package com.amazonaws.demo;

import com.amazonaws.demo.R;
import com.amazonaws.demo.s3.S3Menu;
import com.amazonaws.demo.sdb.SdbMenu;
import com.amazonaws.demo.sns.SnsMenu;
import com.amazonaws.demo.sqs.SqsMenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AWSAndroidDemo extends Activity {
    
	private static final String success = "Welcome to The AWS Browser Demo!";
	private static final String fail = "Load Failed. Please Try Restarting the Application.";
	
	protected Button snsButton;
	protected Button sqsButton;
	protected Button s3Button;
	protected Button sdbButton;
	protected TextView welcomeText;
	
    public static AmazonClientManager clientManager = null;
    	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        snsButton = (Button) findViewById(R.id.main_notify_button);
        sqsButton = (Button) findViewById(R.id.main_queue_button);
        s3Button = (Button) findViewById(R.id.main_storage_button);
        sdbButton = (Button) findViewById(R.id.main_sdb_button);
        welcomeText = (TextView) findViewById(R.id.main_into_text);                                    

        clientManager = new AmazonClientManager();

     	if ( AWSAndroidDemo.clientManager.hasCredentials() ){
    		welcomeText.setText(success);
    		snsButton.setVisibility(View.VISIBLE);
    		sqsButton.setVisibility(View.VISIBLE);
    		s3Button.setVisibility(View.VISIBLE);
    		sdbButton.setVisibility(View.VISIBLE);
    		this.wireButtons();
    	} 
        else {
    		this.displayCredentialsIssueAndExit();
    		welcomeText.setText(fail);
    	}       
    }
        
    private void wireButtons(){
        snsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
    			startActivity(new Intent(AWSAndroidDemo.this, SnsMenu.class));
			}
		});
		
		sqsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
    			startActivity(new Intent(AWSAndroidDemo.this, SqsMenu.class));
			}
		});
		
		s3Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
    			startActivity(new Intent(AWSAndroidDemo.this, S3Menu.class));
			}
		});
		
		sdbButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
    			startActivity(new Intent(AWSAndroidDemo.this, SdbMenu.class));
			}
		});
        
    }
        
    protected void displayCredentialsIssueAndExit() {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle("Credential Problem!");
        confirm.setMessage( "AWS Credentials not configured correctly.  Please review the README file." );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                AWSAndroidDemo.this.finish();
            }
        } );
        confirm.show().show();                
    }
    
}
