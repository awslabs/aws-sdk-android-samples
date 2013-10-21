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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.demo.personalfilestore.s3.S3;
import com.amazonaws.demo.personalfilestore.s3.S3BucketView;

public class S3PersonalFileStore extends Activity {

    private static final String success = "Welcome to the S3 personal file store.";
    private static final String fail = "Load failed. Please try restarting the application.";

    protected Button s3Button;
    protected Button loginButton;
    protected Button logoutButton;
    protected TextView welcomeText;

    public static AmazonClientManager clientManager = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        java.util.logging.Logger.getLogger("com.amazonaws").setLevel(java.util.logging.Level.FINEST);
        java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.FINEST);
        setContentView(R.layout.main);
        s3Button = (Button) findViewById(R.id.main_storage_button);
        logoutButton = (Button) findViewById(R.id.main_logout_button);
        loginButton = (Button) findViewById(R.id.main_login_button);
        welcomeText = (TextView) findViewById(R.id.main_into_text);                                    

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            clientManager = new AmazonClientManager( getSharedPreferences( "com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE ), bundle);
        } catch (NameNotFoundException e) {
            displayErrorAndExit("Unable to load application bundle: " + e.getMessage());
        }

        if ( !S3PersonalFileStore.clientManager.hasCredentials() ) {
            this.displayCredentialsIssueAndExit();
            welcomeText.setText(fail);
        }
        else if ( !S3PersonalFileStore.clientManager.isLoggedIn() ) {
            welcomeText.setText(success);
            loginButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        }
        else {
            welcomeText.setText(success);
            s3Button.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        } 
    }

    protected void onResume() {
        super.onResume();
        if ( !S3PersonalFileStore.clientManager.isLoggedIn() ) {
            welcomeText.setText(success);
            loginButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        }
        else {
            loginButton.setVisibility(View.INVISIBLE);
            welcomeText.setText(success);
            s3Button.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        } 

    }

    private void wireButtons(){
        s3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent bucketViewIntent = new Intent(S3PersonalFileStore.this, S3BucketView.class);
                bucketViewIntent.putExtra( S3.BUCKET_NAME, S3PersonalFileStore.clientManager.getBucketName() );
                bucketViewIntent.putExtra( S3.PREFIX, S3PersonalFileStore.clientManager.getUsername() );
                startActivity(bucketViewIntent);

            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientManager.clearCredentials();  
                clientManager.wipe();              
                displayLogoutSuccess();

                s3Button.setVisibility(View.INVISIBLE);
                logoutButton.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.VISIBLE);                
            }
        });  

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(S3PersonalFileStore.this, Login.class));                
            }
        });        
    }

    protected void displayCredentialsIssueAndExit() {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle("AWS Credentials Issue");
        confirm.setMessage( "AWS Credentials not configured correctly.  Please review the README file." );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                S3PersonalFileStore.this.finish();
            }
        } );
        confirm.show();                
    }



    protected void displayErrorAndExit( String msg ) {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        if ( msg == null ) { 
            confirm.setTitle("Error Code Unknown" );
            confirm.setMessage( "Please review the README file." );
        } 
        else {
            confirm.setTitle( "Error" );
            confirm.setMessage( msg + "\nPlease review the README file."  );
        }

        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                S3PersonalFileStore.this.finish();
            }
        } );
        confirm.show();                
    }

    protected void displayLogoutSuccess() {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle("Logout");
        confirm.setMessage( "You have successfully logged out." );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
            }
        } );
        confirm.show();                
    }
    
    /**
     * Display an alert dialog with the content the error message of the throwable.  Logs throwable.  Calls context.finish()
     * if it is an Activity when user dismisses alert.
     * @param throwable Throwable to log and alert user of.
     * @param context context to finish on dismissal of alert.
     */
    public static void displayAlert(Throwable throwable, final Context context) {
        Log.e(S3PersonalFileStore.class.getName(), throwable.getMessage(), throwable);
        AlertDialog.Builder confirm = new AlertDialog.Builder(context);
        confirm.setTitle("An error occured.");
        confirm.setMessage(throwable.getMessage() + "\nPlease review the README file.");
        confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        });
        confirm.show();
    }

}
