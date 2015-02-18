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

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.cognito.sync.devauth.client.Response;

/**
 * A class which performs the task of authentication the user. For the sample it
 * validates a set of username and possword against the sample Cognito developer
 * authentication application
 */
public class DeveloperAuthenticationTask extends
        AsyncTask<LoginCredentials, Void, Void> {

    // The user name or the developer user identifier you will pass to the
    // Amazon Cognito in the GetOpenIdTokenForDeveloperIdentity API
    private String userName;

    private boolean isSuccessful;

    private final Context context;

    public DeveloperAuthenticationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(LoginCredentials... params) {

        Response response = DeveloperAuthenticationProvider
                .getDevAuthClientInstance()
                .login(params[0].getUsername(), params[0].getPassword());
        isSuccessful = response.requestWasSuccessful();
        userName = params[0].getUsername();

        if (isSuccessful) {
            CognitoSyncClientManager
                    .addLogins(
                            ((DeveloperAuthenticationProvider) CognitoSyncClientManager.credentialsProvider
                                    .getIdentityProvider()).getProviderName(),
                            userName);
            // Always remember to call refresh after updating the logins map
            ((DeveloperAuthenticationProvider) CognitoSyncClientManager.credentialsProvider
                    .getIdentityProvider()).refresh();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (!isSuccessful) {
            new AlertDialog.Builder(context).setTitle("Login error")
                    .setMessage("Username or password do not match!!").show();
        }
    }
}