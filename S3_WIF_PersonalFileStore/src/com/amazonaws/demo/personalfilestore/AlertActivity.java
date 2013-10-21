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
package com.amazonaws.demo.personalfilestore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class AlertActivity extends Activity {
    private static String TAG = AlertActivity.class.getName();
    
    private Throwable error;
    private Handler mHandler;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }
    protected Runnable displayError = new Runnable(){
        public void run(){
            AlertDialog.Builder confirm = new AlertDialog.Builder( AlertActivity.this );
            confirm.setTitle( "An error occured.");
            confirm.setMessage( error.getMessage() + ". Please review the README and check LogCat for details.");
            confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                    AlertActivity.this.finish();
                }
            } );
            confirm.show();   
        }
    };

    protected Runnable refreshFailed = new Runnable(){
        public void run(){
            AlertDialog.Builder confirm = new AlertDialog.Builder( AlertActivity.this );
            confirm.setTitle( "Failed to refresh credentials");
            confirm.setMessage( "Try again");
            confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                    AlertActivity.this.finish();
                }
            } );
            confirm.show();   
        }
    };

   
    public void alertUser(Throwable aThrowable){
        Log.e(TAG, aThrowable.getMessage(), aThrowable);
        error = aThrowable;
        mHandler.post(displayError);
    }

    public void putRefreshError(){
        mHandler.post(refreshFailed);
    }
}
