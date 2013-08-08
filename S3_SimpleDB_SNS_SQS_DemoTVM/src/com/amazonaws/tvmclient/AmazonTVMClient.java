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
package com.amazonaws.tvmclient;

import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;

import android.content.SharedPreferences;
import android.util.Log;

/**
* This class is uzsed to communicate with the Token Vending Machine specific for this application. 
*/
public class AmazonTVMClient {
    private static final String LOG_TAG = "AmazonTVMClient";

    /**
     * The endpoint for the Token Vending Machine to connect to.
     */
    private String endpoint;
        
    /**
     * Use SSL when making connections to the Token Vending Machine.
     */
    private boolean useSSL;
    
    /**
     * The shared preferences where credentials are other aws access information is stored.
     */
    private SharedPreferences sharedPreferences;
    
    
    public AmazonTVMClient( SharedPreferences sharedPreferences, String endpoint, boolean useSSL ) {
        this.endpoint = this.getEndpointDomainName( endpoint.toLowerCase() );
        this.useSSL = useSSL;
        this.sharedPreferences = sharedPreferences;
    }
    
    /**
     * Anonymously register the current application/device with the Token Vending Machine.
     */
    public Response anonymousRegister() {
        Response response = Response.SUCCESSFUL;
        if ( AmazonSharedPreferencesWrapper.getUidForDevice( this.sharedPreferences ) == null ) {
            String uid = this.generateRandomString();
            String key = this.generateRandomString();

            RegisterDeviceRequest registerDeviceRequest = new RegisterDeviceRequest( this.endpoint, this.useSSL, uid, key );
            ResponseHandler handler = new ResponseHandler();

            response = this.processRequest( registerDeviceRequest, handler );
            if ( response.requestWasSuccessful() ) {
                AmazonSharedPreferencesWrapper.registerDeviceId( this.sharedPreferences, uid, key );
            }
        }
        
        return response;
    }
    
    /**
     * Gets a token from the Token Vending Machine.  The registered key is used to secure the communication.
     */
    public Response getToken() {
        String uid = AmazonSharedPreferencesWrapper.getUidForDevice( this.sharedPreferences );
        String key = AmazonSharedPreferencesWrapper.getKeyForDevice( this.sharedPreferences );

        Request getTokenRequest = new GetTokenRequest( this.endpoint, this.useSSL, uid, key );
        ResponseHandler handler = new GetTokenResponseHandler( key );

        GetTokenResponse getTokenResponse = (GetTokenResponse)this.processRequest( getTokenRequest, handler );
        if ( getTokenResponse.requestWasSuccessful() ) {
            AmazonSharedPreferencesWrapper.storeCredentialsInSharedPreferences( this.sharedPreferences, getTokenResponse.getAccessKey(), getTokenResponse.getSecretKey(), getTokenResponse.getSecurityToken(), getTokenResponse.getExpirationDate() );
        }

        return getTokenResponse;
    }
    
    /**
     *  Process Request
     */
    protected Response processRequest( Request request, ResponseHandler handler ) {
        Response response = null;
        int retries = 2;
        do {            
            response = TokenVendingMachineService.sendRequest( request, handler );
            if ( response.requestWasSuccessful() ) {
                return response;
            }
            else {
                Log.w( LOG_TAG, "Request to Token Vending Machine failed with Code: [" + response.getResponseCode() + "] Message: [" + response.getResponseMessage() + "]" );
            }
        }
        while ( retries-- > 0 );       
        
        return response;                     
    } 
     
    
    /**
     * Creates a 128-bit random string.
     */
    public String generateRandomString() {
		SecureRandom random = new SecureRandom();
		byte[] randomBytes = random.generateSeed( 16 );
		String randomString = new String( Hex.encodeHex( randomBytes ) );
		return randomString;
	}     

    private String getEndpointDomainName( String endpoint ) {
    	int startIndex = 0;
    	int endIndex = 0;
    	
    	if ( endpoint.startsWith( "http://") || endpoint.startsWith( "https://") ) {
    		startIndex = endpoint.indexOf( "://" ) + 3;
    	}
    	else {
    		startIndex = 0;
    	}
    	
    	if ( endpoint.charAt( endpoint.length() - 1 ) == '/' ) {
    		endIndex = endpoint.length() - 1;
    	}
    	else {
    		endIndex = endpoint.length();
    	}
    	
    	return endpoint.substring( startIndex, endIndex );
    }
}
