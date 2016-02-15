/**
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.numberguess.manager;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.demo.numberguess.Constants;
import com.amazonaws.regions.Regions;

import java.util.HashMap;
import java.util.Map;

public class CognitoClientManager {

    private static CognitoCachingCredentialsProvider provider;

    /**
     * Initializes the CognitoClient. This must be called before
     * getCredentials().
     * 
     * @param context a context of the app
     */
    public static void init(Context context) {
        if (provider == null)
            provider = new CognitoCachingCredentialsProvider(context,
                    Constants.IDENTITY_POOL_ID, Regions.US_EAST_1);
    }

    /**
     * Sets the logins map used to authenticated with Amazon Cognito. Note: You
     * should manually call refresh on the credentials provider after adding
     * logins to the provider as your Identity Id may have changed.
     * 
     * @param providerName the name of 3rd identity provider
     * @param token openId token
     */
    public static void addLogins(String providerName, String token) {
        checkCredentialAvailability();
        Map<String, String> logins = provider.getLogins();
        if (logins == null) {
            logins = new HashMap<String, String>();
        }
        logins.put(providerName, token);
        provider.setLogins(logins);
    }

    /**
     * Clears the logins map used to authenticated with Amazon Cognito. Note:
     * You should manually call refresh on the credentials provider after
     * clearing logins to the provider as your Identity Id may have changed.
     */
    public static void clearLogins() {
        checkCredentialAvailability();
        provider.setLogins(new HashMap<String, String>());
    }

    /**
     * Forces this credentials provider to refresh its credentials.
     */
    public static void refresh() {
        checkCredentialAvailability();
        provider.refresh();
    }

    /**
     * @return whether the current user is authenticated.
     */
    public static boolean isAuthenticated() {
        checkCredentialAvailability();
        Map<String, String> logins = provider.getLogins();
        if (logins == null || logins.isEmpty()) {
            return false;
        }
        return true;
    }

    private static void checkCredentialAvailability() {
        if (provider == null) {
            throw new IllegalStateException("provider not initialized yet");
        }
    }

    /**
     * Gets the singleton instance of the CredentialsProvider. init() must be
     * call prior to this.
     * 
     * @return an instance of CognitoCachingCredentialsProvider
     */
    public static CognitoCachingCredentialsProvider getCredentials() {
        checkCredentialAvailability();
        return provider;
    }
}
