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
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;
import java.util.Map;

/**
 * This class is used to communicate with the sample Cognito developer
 * authentication application sample.
 */
public class AmazonCognitoSampleDeveloperAuthenticationClient {
    private static final String LOG_TAG = "AmazonCognitoSampleDeveloperAuthenticationClient";

    /**
     * The endpoint for the sample Cognito developer authentication application.
     */
    private final String endpoint;

    /**
     * The appName declared by the sample Cognito developer authentication
     * application.
     */
    private final String appName;

    /**
     * Use SSL when making connections to the sample Cognito developer
     * authentication application.
     */
    private final boolean useSSL;

    /**
     * The shared preferences where user key is stored.
     */
    private final SharedPreferences sharedPreferences;

    public AmazonCognitoSampleDeveloperAuthenticationClient(
            SharedPreferences sharedPreferences, String endpoint,
            String appName, boolean useSSL) {
        this.endpoint = this.getEndpointDomainName(endpoint.toLowerCase());
        this.appName = appName.toLowerCase();
        this.useSSL = useSSL;
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Gets a token from the sample Cognito developer authentication
     * application. The registered key is used to secure the communication.
     */
    public Response getToken(Map<String, String> logins, String identityId) {
        String uid = AmazonSharedPreferencesWrapper
                .getUidForDevice(this.sharedPreferences);
        String key = AmazonSharedPreferencesWrapper
                .getKeyForDevice(this.sharedPreferences);

        Request getTokenRequest = new GetTokenRequest(this.endpoint,
                this.useSSL, uid, key, logins, identityId);
        ResponseHandler handler = new GetTokenResponseHandler(key);

        GetTokenResponse getTokenResponse = (GetTokenResponse) this
                .processRequest(getTokenRequest, handler);

        // TODO: You can cache the open id token as you will have the control
        // over the duration of the token when it is issued. Caching can reduce
        // the communication required between the app and your backend
        return getTokenResponse;
    }

    /**
     * Using the given username and password, securily communictes the Key for
     * the user's account.
     */
    public Response login(String username, String password) {
        Response response = Response.SUCCESSFUL;
        if (AmazonSharedPreferencesWrapper
                .getUidForDevice(this.sharedPreferences) == null) {
            String uid = AmazonCognitoSampleDeveloperAuthenticationClient
                    .generateRandomString();
            LoginRequest loginRequest = new LoginRequest(this.endpoint,
                    this.useSSL, this.appName, uid, username, password);
            ResponseHandler handler = new LoginResponseHandler(
                    loginRequest.getDecryptionKey());

            response = this.processRequest(loginRequest, handler);
            if (response.requestWasSuccessful()) {
                AmazonSharedPreferencesWrapper.registerDeviceId(
                        this.sharedPreferences, uid,
                        ((LoginResponse) response).getKey());
            }
        }

        return response;
    }

    /**
     * Process Request
     */
    protected Response processRequest(Request request, ResponseHandler handler) {
        Response response = null;
        int retries = 2;
        do {
            response = CognitoSampleDeveloperAuthenticationService.sendRequest(
                    request, handler);
            if (response.requestWasSuccessful()) {
                return response;
            } else {
                Log.w(LOG_TAG,
                        "Request to Cognito Sample Developer Authentication Application failed with Code: ["
                                + response.getResponseCode() + "] Message: ["
                                + response.getResponseMessage() + "]");
            }
        } while (retries-- > 0);

        return response;
    }

    /**
     * Creates a 128 bit random string..
     */
    public static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = random.generateSeed(16);
        String randomString = new String(Hex.encodeHex(randomBytes));
        return randomString;
    }

    private String getEndpointDomainName(String endpoint) {
        int startIndex = 0;
        int endIndex = 0;

        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            startIndex = endpoint.indexOf("://") + 3;
        } else {
            startIndex = 0;
        }

        if (endpoint.charAt(endpoint.length() - 1) == '/') {
            endIndex = endpoint.length() - 1;
        } else {
            endIndex = endpoint.length();
        }

        return endpoint.substring(startIndex, endIndex);
    }

}
