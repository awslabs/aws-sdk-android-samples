/**
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.cognito.sync.devauth.client;

import android.content.SharedPreferences;

/**
 * This utility class is used to store content in Android's Shared Preferences.
 * For maximum security the preferences should be private.
 */
public class AmazonSharedPreferencesWrapper {

    private static final String AWS_DEVICE_UID = "AWS_DEVICE_UID";
    private static final String AWS_DEVICE_KEY = "AWS_DEVICE_KEY";

    /**
     * Set all of the Shared Preferences used by the sample Cognito developer
     * authentication application to null. This function is useful if the user
     * needs/wants to log out to clear any user specific information.
     */
    public static void wipe(SharedPreferences sharedPreferences) {
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences(
                sharedPreferences, AWS_DEVICE_UID, null);
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences(
                sharedPreferences, AWS_DEVICE_KEY, null);
    }

    /**
     * Stores the UID and Key that were registered in the Shared Preferences.
     * The UID and Key and used to encrypt/decrypt the Token that is returned
     * from the sample Cognito developer authentication application.
     */
    public static void registerDeviceId(SharedPreferences sharedPreferences,
            String uid, String key) {
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences(
                sharedPreferences, AWS_DEVICE_UID, uid);
        AmazonSharedPreferencesWrapper.storeValueInSharedPreferences(
                sharedPreferences, AWS_DEVICE_KEY, key);
    }

    /**
     * Returns the current UID stored in Shared Preferences.
     */
    public static String getUidForDevice(SharedPreferences sharedPreferences) {
        return AmazonSharedPreferencesWrapper.getValueFromSharedPreferences(
                sharedPreferences, AWS_DEVICE_UID);
    }

    /**
     * Returns the current Key stored in Shared Preferences.
     */
    public static String getKeyForDevice(SharedPreferences sharedPreferences) {
        return AmazonSharedPreferencesWrapper.getValueFromSharedPreferences(
                sharedPreferences, AWS_DEVICE_KEY);
    }

    protected static void storeValueInSharedPreferences(
            SharedPreferences sharedPreferences, String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected static String getValueFromSharedPreferences(
            SharedPreferences sharedPreferences, String key) {
        return sharedPreferences.getString(key, null);
    }
}
