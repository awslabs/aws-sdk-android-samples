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

package com.amazonaws.cognito.sync.demo;

import android.content.Context;
import android.preference.PreferenceManager;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.cognito.sync.devauth.client.AmazonCognitoSampleDeveloperAuthenticationClient;
import com.amazonaws.cognito.sync.devauth.client.GetTokenResponse;
import com.amazonaws.regions.Regions;

/**
 * A class used for communicating with developer backend, in this sample it is
 * used to communicate with the sample Cognito developer authentication
 * application.
 */
public class DeveloperAuthenticationProvider extends
        AWSAbstractCognitoDeveloperIdentityProvider {

    private static AmazonCognitoSampleDeveloperAuthenticationClient devAuthClient;

    private static final String developerProvider = "";
    private static final String cognitoSampleDeveloperAuthenticationAppEndpoint = "";
    private static final String cognitoSampleDeveloperAuthenticationAppName = "AWSCognitoDeveloperAuthenticationSample";

    public DeveloperAuthenticationProvider(String accountId,
            String identityPoolId, Context context, Regions region) {
        super(accountId, identityPoolId, region);
        /*
         * Initialize the client using which you will communicate with your
         * backend for user authentication. Here we initialize a client which
         * communicates with sample Cognito developer authentication
         * application.
         */
        if (isDeveloperAuthenticatedAppConfigured()) {
            devAuthClient = new AmazonCognitoSampleDeveloperAuthenticationClient(
                    PreferenceManager.getDefaultSharedPreferences(context),
                    cognitoSampleDeveloperAuthenticationAppEndpoint,
                    cognitoSampleDeveloperAuthenticationAppName, false);
        }

    }

    /*
     * (non-Javadoc)
     * @see com.amazonaws.auth.AWSCognitoIdentityProvider#refresh() In refresh
     * method, you will have two flows:
     */
    /*
     * 1. When the app user uses developer authentication . In this case, make
     * the call to your developer backend, from where call the
     * GetOpenIdTokenForDeveloperIdentity API of Amazon Cognito service. For
     * this sample the GetToken request to the sample Cognito developer
     * authentication application is made. Be sure to call update(), so as to
     * set the identity id and the token received.
     */
    /*
     * 2.When the app user is not using the developer authentication, just call
     * the refresh method of the AWSAbstractCognitoDeveloperIdentityProvider
     * class which actually calls GetId and GetOpenIDToken API of Amazon
     * Cognito.
     */
    @Override
    public String refresh() {
        setToken(null);
        // If there is a key with developer provider name in the logins map, it
        // means the app user has used developer credentials
        if (getProviderName() != null && !this.loginsMap.isEmpty()
                && this.loginsMap.containsKey(getProviderName())) {
            GetTokenResponse getTokenResponse = (GetTokenResponse) devAuthClient.getToken(
                    this.loginsMap,
                    identityId);
            update(getTokenResponse.getIdentityId(),
                    getTokenResponse.getToken());
            return getTokenResponse.getToken();
        } else {
            this.getIdentityId();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.amazonaws.auth.AWSBasicCognitoIdentityProvider#getIdentityId()
     */
    /*
     * This method again has two flows as mentioned above depending on whether
     * the app user is using developer authentication or not. When using
     * developer authentication system, the identityId should be retrieved from
     * the developer backend. In the other case the identityId will be retrieved
     * using the getIdentityId() method which in turn calls Cognito GetId and
     * GetOpenIdToken APIs.
     */
    @Override
    public String getIdentityId() {
        identityId = CognitoSyncClientManager.credentialsProvider.getCachedIdentityId();
      if (identityId == null) {
            if (getProviderName() != null && !this.loginsMap.isEmpty()
                    && this.loginsMap.containsKey(getProviderName())) {
                GetTokenResponse getTokenResponse = (GetTokenResponse) devAuthClient.getToken(
                        this.loginsMap, identityId);
                update(getTokenResponse.getIdentityId(),
                        getTokenResponse.getToken());
                return getTokenResponse.getIdentityId();
            } else {
                return super.getIdentityId();
            }
        } else {
            return identityId;
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.amazonaws.auth.AWSAbstractCognitoIdentityProvider#getProviderName()
     * Return the developer provider name which you choose while setting up the
     * identity pool in the Amazon Cognito Console
     */
    @Override
    public String getProviderName() {
        return developerProvider;
    }

    /**
     * This function validates the user credentials against the sample Cognito
     * developer authentication application. After that it stores the key and
     * token received from sample Cognito developer authentication application
     * for all further communication with the application.
     * 
     * @param userName
     * @param password
     */
    public void login(String userName, String password, Context context) {
        new DeveloperAuthenticationTask(context).execute(new LoginCredentials(
                userName, password));
    }

    /**
     * This function checks if the app is configured for developer authenticated
     * identities. It needs the app name, developer provider name and app
     * endpoint to be configured for developer authenticated identities.
     * 
     * @return
     */
    public static boolean isDeveloperAuthenticatedAppConfigured() {
        return (cognitoSampleDeveloperAuthenticationAppEndpoint != null
                && !cognitoSampleDeveloperAuthenticationAppEndpoint.equals("")
                && cognitoSampleDeveloperAuthenticationAppName != null
                && !cognitoSampleDeveloperAuthenticationAppName
                        .equals("") && developerProvider != null && !developerProvider.equals(""));
    }

    public static AmazonCognitoSampleDeveloperAuthenticationClient getDevAuthClientInstance() {
        if (devAuthClient == null) {
            throw new IllegalStateException(
                    "Dev Auth Client not initialized yet");
        }
        return devAuthClient;
    }
}
