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

import android.content.SharedPreferences;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;

/**
* This utility class is used to store content in Android's Shared Preferences.
* For maximum security the preferences should be private. 
*/
public class AmazonSharedPreferencesWrapper {
	
    private static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY";
    private static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";
    private static final String AWS_SECURITY_TOKEN = "AWS_SECURITY_TOKEN";
    private static final String AWS_EXPIRATION_DATE = "AWS_EXPIRATION_DATE";
    
    private static final String AWS_DEVICE_UID = "AWS_DEVICE_UID";
    private static final String AWS_DEVICE_KEY = "AWS_DEVICE_KEY";
   
    /**
     * Set all of the Shared Preferences used by the Token Vending Machine to null.
     * This function is useful if the user needs/wants to log out to clear any user specific information.
     */
    public static void wipe( SharedPreferences sharedPreferences ) {
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_DEVICE_UID, null );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_DEVICE_KEY, null );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_ACCESS_KEY, null );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_SECRET_KEY, null );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_SECURITY_TOKEN, null );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_EXPIRATION_DATE, null );
    }
       
    /**
     * Stores the UID and Key that were registered in the Shared Preferences.
     * The UID and Key and used to encrypt/decrypt the Token that is returned from the Token Vending Machine.
     */       
    public static void registerDeviceId( SharedPreferences sharedPreferences, String uid, String key ) {
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_DEVICE_UID, uid );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_DEVICE_KEY, key );
    }
    
    /**
     * Returns the current UID stored in Shared Preferences.
     */
    public static String getUidForDevice( SharedPreferences sharedPreferences ) {
        return AmazonSharedPreferencesWrapper.getValueFromSharedPreferences( sharedPreferences, AWS_DEVICE_UID );
    }
    
    /**
     * Returns the current Key stored in Shared Preferences.
     */
    public static String getKeyForDevice( SharedPreferences sharedPreferences ) {
        return AmazonSharedPreferencesWrapper.getValueFromSharedPreferences( sharedPreferences, AWS_DEVICE_KEY );
    }
        
    /**
     * Gets the AWS Access Key, AWS Secret Key and Security Token currently stored in Shared Preferences.  Then creates a Credentials object
     * and returns that object.
     */
    public static AWSCredentials getCredentialsFromSharedPreferences( SharedPreferences sharedPreferences ) {
        String accessKey = AmazonSharedPreferencesWrapper.getValueFromSharedPreferences( sharedPreferences, AWS_ACCESS_KEY );
        String secretKey = AmazonSharedPreferencesWrapper.getValueFromSharedPreferences( sharedPreferences, AWS_SECRET_KEY );
        String securityToken = AmazonSharedPreferencesWrapper.getValueFromSharedPreferences( sharedPreferences, AWS_SECURITY_TOKEN );
        
        return new BasicSessionCredentials( accessKey, secretKey, securityToken );
    }
        
    /**
     * Checks if the current Token's expiration date stored in Shared Preferences has expired.
     * A token that has expired or will expire in fifteen minutes or less is considered expired.
     */
    public static boolean areCredentialsExpired( SharedPreferences sharedPreferences ) {
        String expirationDate = AmazonSharedPreferencesWrapper.getValueFromSharedPreferences( sharedPreferences, AWS_EXPIRATION_DATE );
        if ( expirationDate == null ) {
            return true;
        }
        else {
            long timeInSeconds = Long.parseLong( expirationDate );
            return ( timeInSeconds < ( System.currentTimeMillis() + 15 * 60 * 100 ) );
        }
    }
    
    /**
     * Stores the four pieces of information associated with a Token in the Shared Preferences.
     */
    public static void storeCredentialsInSharedPreferences( SharedPreferences sharedPreferences, String accessKey, String secretKey, String securityToken, String expirationDate ) {
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_ACCESS_KEY, accessKey );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_SECRET_KEY, secretKey );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_SECURITY_TOKEN, securityToken );
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences( sharedPreferences, AWS_EXPIRATION_DATE, expirationDate );        
    }
    
	protected static void storeValueInSharedPreferences( SharedPreferences sharedPreferences, String key, String value ) {
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    	editor.putString( key, value );
    	editor.commit();
	}
    
	protected static String getValueFromSharedPreferences( SharedPreferences sharedPreferences, String key ) {
		return sharedPreferences.getString( key, null );
	}
}
