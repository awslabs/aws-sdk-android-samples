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

package com.amazonaws.demo.userpreferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.WebIdentityFederationSessionCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * This class is used to get clients to the various AWS services. Before
 * accessing a client the credentials should be checked to ensure validity.
 */
public class AmazonClientManager {

	private static final String LOG_TAG = "AmazonClientManager";

	private AmazonDynamoDBClient ddb = null;
	private SharedPreferences sharedPreferences = null;

	private WebIdentityFederationSessionCredentialsProvider wif = null;
	private WIFIdentityProvider idp = null;

	private String fbRoleARN = null;
	private String googleRoleARN = null;
	private String amazonRoleARN = null;
	private String googleClientID = null;

	public AmazonClientManager(SharedPreferences settings, Bundle bundle) {
		this.sharedPreferences = settings;
		if(PropertyLoader.getInstance().hasCredentials()){
			this.initWithEmbeddedCredentials();
		}
		fbRoleARN = bundle.getString("FBRoleARN");
		googleRoleARN = bundle.getString("GoogleRoleARN");
		amazonRoleARN = bundle.getString("AMZNRoleARN");
		googleClientID = bundle.getString("GoogleClientID");
	}

	public AmazonDynamoDBClient ddb() {
		return ddb;
	}

	public boolean hasCredentials() {
		if (PropertyLoader.getInstance().hasCredentials()){
			return true;
		}
		return !(fbRoleARN.equals("ROLE_ARN") && googleRoleARN.equals("ROLE_ARN") && amazonRoleARN.equals("ROLE_ARN"));
	}

	public boolean isLoggedIn() {
		return ( ddb != null );
	}

	public void clearCredentials() {

		synchronized (this) {
			AmazonSharedPreferencesWrapper.wipe(this.sharedPreferences);
			ddb = null;
		}
	}

	public boolean wipeCredentialsOnAuthError(AmazonServiceException ex) {
		if (
				// STS
				// http://docs.amazonwebservices.com/STS/latest/APIReference/CommonErrors.html
				ex.getErrorCode().equals("IncompleteSignature")
				|| ex.getErrorCode().equals("InternalFailure")
				|| ex.getErrorCode().equals("InvalidClientTokenId")
				|| ex.getErrorCode().equals("OptInRequired")
				|| ex.getErrorCode().equals("RequestExpired")
				|| ex.getErrorCode().equals("ServiceUnavailable")

				// DynamoDB
				// http://docs.amazonwebservices.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIErrorTypes
				|| ex.getErrorCode().equals("AccessDeniedException")
				|| ex.getErrorCode().equals("IncompleteSignatureException")
				|| ex.getErrorCode().equals(
						"MissingAuthenticationTokenException")
						|| ex.getErrorCode().equals("ValidationException")
						|| ex.getErrorCode().equals("InternalFailure")
						|| ex.getErrorCode().equals("InternalServerError")) {

			clearCredentials();

			return true;
		}

		return false;
	}


	public void login( WIFIdentityProvider wifIDP, final AlertActivity activity ) {
		idp = wifIDP;
		wif = new WebIdentityFederationSessionCredentialsProvider(idp.getToken(),idp.getProviderID(), idp.getRoleARN()); 
		
		//call refresh to login from an AsyncTask because refreshing requires the network
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
                    activity.setStackAndPost(t);
                } else {
            		ddb = new AmazonDynamoDBClient( wif );
            		ddb.setRegion(Region.getRegion(Regions.US_WEST_2));
                    AmazonSharedPreferencesWrapper.storeUsername(sharedPreferences, wif.getSubjectFromWIF());
                    Log.d(LOG_TAG, "Logged in with user id " + wif.getSubjectFromWIF());
                    activity.setResult(Activity.RESULT_OK);
                }
                activity.finish();
            }
        }.execute();
	}

	private void initWithEmbeddedCredentials(){
		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// This sample App is for demonstration purposes only.
		// It is not secure to embed your credentials into source code.
		// DO NOT EMBED YOUR CREDENTIALS IN PRODUCTION APPS.
		// We offer two solutions for getting credentials to your mobile App.
		// Please read the following article to learn about Token Vending Machine:
		//  * http://aws.amazon.com/articles/Mobile/4611615499399490
		// Or consider using web identity federation:
		// * http://aws.amazon.com/articles/Mobile/4617974389850313
		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		if (ddb == null){
			AWSCredentials credentials = new BasicAWSCredentials( PropertyLoader.getInstance().getAccessKeyID(), PropertyLoader.getInstance().getSecretKey());

			ddb = new AmazonDynamoDBClient( credentials );
			ddb.setRegion(Region.getRegion(Regions.US_WEST_2));
			Log.d(LOG_TAG, "Logged in with embedded credentials");
		}
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
	public void wipe() {
		AmazonSharedPreferencesWrapper.wipe( this.sharedPreferences );
	}
}
