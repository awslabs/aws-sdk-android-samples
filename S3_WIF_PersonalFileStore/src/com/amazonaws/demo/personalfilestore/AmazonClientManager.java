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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.auth.WebIdentityFederationSessionCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * This class is used to get clients to the various AWS services.  Before accessing a client 
 * the credentials should be checked to ensure validity.
 */
public class AmazonClientManager {
    private String LOG_TAG = "AmazonClientManager";

    private AmazonS3Client s3Client = null;
    private WebIdentityFederationSessionCredentialsProvider wif = null;
    private WIFIdentityProvider idp = null;
    private SharedPreferences sharedPreferences = null;

    private String bucketName = null;

    private String fbRoleARN = null;
    private String googleRoleARN = null;
    private String amazonRoleARN = null;
    private String googleClientID = null;

    public AmazonClientManager( SharedPreferences settings, Bundle bundle ) {
        this.sharedPreferences = settings;

        bucketName = bundle.getString("BucketName");

        fbRoleARN = bundle.getString("FBRoleARN");
        googleRoleARN = bundle.getString("GoogleRoleARN");
        amazonRoleARN = bundle.getString("AMZNRoleARN");

        googleClientID = bundle.getString("GoogleClientID");
    }

    public AmazonS3Client s3() {
        return s3Client;
    }

    public boolean hasCredentials() {
        return !(fbRoleARN.equals("ROLE_ARN") && googleRoleARN.equals("ROLE_ARN") && amazonRoleARN.equals("ROLE_ARN"));
    }

    public boolean isLoggedIn() {
        return ( s3Client != null );
    }

    public void login( WIFIdentityProvider wifIDP, final AlertActivity activity ) {
        idp = wifIDP;
        wif = new WebIdentityFederationSessionCredentialsProvider(idp.getToken(),idp.getProviderID(), idp.getRoleARN()); 

        //call refresh to login
        new AsyncTask<Void, Void, Throwable>() {
            @Override
            protected Throwable doInBackground(Void... arg0) {

                try {
                    wif.refresh();
                } catch (Throwable t) {
                    return t;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Throwable t) {
                if (t != null) {
                    Log.e(LOG_TAG, "Unable to login.", t);
                    activity.setResult(Activity.RESULT_CANCELED);
                    activity.alertUser(t);
                } else {
                    s3Client = new AmazonS3Client(wif);
                    AmazonSharedPreferencesWrapper.storeUsername(sharedPreferences, wif.getSubjectFromWIF());
                    Log.d(LOG_TAG, "Logged in with user id " + wif.getSubjectFromWIF());
                    activity.setResult(Activity.RESULT_OK);
                }
                activity.finish();
            }
        }.execute();

    }

    public String getBucketName() {
        return bucketName;
    }

    public String getUsername() {
        return AmazonSharedPreferencesWrapper.getUsername( this.sharedPreferences );
    }

    public String getAmazonRoleARN() {
        return amazonRoleARN;
    }

    public String getGoogleRoleARN() {
        return googleRoleARN;
    }

    public String getFacebookRoleARN() {
        return fbRoleARN;
    }

    public String getGoogleClientID() {
        return googleClientID;
    }

    public void clearCredentials() {
        s3Client = null;
        idp.logout();
        idp = null;
    }


    public void wipe() {
        AmazonSharedPreferencesWrapper.wipe( this.sharedPreferences );
    }
}
