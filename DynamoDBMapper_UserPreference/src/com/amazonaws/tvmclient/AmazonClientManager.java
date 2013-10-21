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

package com.amazonaws.tvmclient;

import android.content.SharedPreferences;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.demo.userpreferencesom.PropertyLoader;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import android.util.Log;

/**
 * This class is used to get clients to the various AWS services. Before accessing a client the credentials should be checked to ensure validity.
 */
public class AmazonClientManager {
	
	private static final String LOG_TAG = "AmazonClientManager";
	
	private AmazonDynamoDBClient ddb = null;
	private SharedPreferences sharedPreferences = null;
	
	public AmazonClientManager( SharedPreferences settings ) {
		this.sharedPreferences = settings;
	}
	
	public AmazonDynamoDBClient ddb() {
		validateCredentials();
		return ddb;
	}
	
	public boolean hasCredentials() {
		return PropertyLoader.getInstance().hasCredentials();
	}
	
	public Response validateCredentials() {

		Response ableToGetToken = Response.SUCCESSFUL;

		if (AmazonSharedPreferencesWrapper
				.areCredentialsExpired(this.sharedPreferences)) {

			synchronized (this) {

				if (AmazonSharedPreferencesWrapper
						.areCredentialsExpired(this.sharedPreferences)) {

					Log.i(LOG_TAG, "Credentials were expired.");

					AmazonTVMClient tvm = new AmazonTVMClient( this.sharedPreferences, PropertyLoader.getInstance().getTokenVendingMachineURL(), PropertyLoader.getInstance().useSSL() );

					ableToGetToken = tvm.anonymousRegister();

					if (ableToGetToken.requestWasSuccessful()) {

						ableToGetToken = tvm.getToken();

						if (ableToGetToken.requestWasSuccessful()) {
							Log.i(LOG_TAG, "Creating New Credentials.");
							initClients();
						}
					}
				}
			}

		} else if (ddb == null) {

			synchronized (this) {

				if (ddb == null) {

					Log.i(LOG_TAG, "Creating New Credentials.");
					initClients();
				}
			}
		}

		return ableToGetToken;
	}

	private void initClients() {
		AWSCredentials credentials = AmazonSharedPreferencesWrapper
				.getCredentialsFromSharedPreferences(this.sharedPreferences);

		ddb = new AmazonDynamoDBClient( credentials );
		ddb.setRegion(Region.getRegion(Regions.US_WEST_2));
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
}
