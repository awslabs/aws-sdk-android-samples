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
import android.os.Bundle;
import android.util.Log;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;

public class AmazonLogin extends AlertActivity {

    private AmazonAuthorizationManager authManager;
    private static final String[] APP_SCOPES = { "profile" };
    private static final String LOG_TAG = "AMZN_LOGIN";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        try {
            authManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
            authManager.authorize(APP_SCOPES, Bundle.EMPTY, new AuthListener());
        }
        catch (Exception e) {
            alertUser(e);
        }
    }

    private class AuthListener implements AuthorizationListener{

        @Override
        public void onSuccess(Bundle response) {
            Log.d(LOG_TAG, "Auth succeeded, getting token");
            authManager.getToken(APP_SCOPES, new TokenListener());         
        }

        @Override
        public void onError(AuthError ae) {
            Log.e(LOG_TAG, "AuthError during authorization", ae);
            runOnUiThread(new Runnable() {
                public void run() {
                    setResult(Activity.RESULT_CANCELED, null);
                    finish();  
                }
            });
        }

        @Override
        public void onCancel(Bundle cause) {
            Log.e(LOG_TAG, "User cancelled authorization");
            runOnUiThread(new Runnable() {
                public void run() {
                    setResult(Activity.RESULT_CANCELED, null);
                    finish();  
                }
            });
        }
    }

    private class TokenListener implements AuthorizationListener{

        @Override
        public void onSuccess(Bundle response) {
            String authzToken = response.getString(AuthzConstants.BUNDLE_KEY.TOKEN.val);
            Log.d(LOG_TAG, "Token: " + authzToken);
            S3PersonalFileStore.clientManager.login(new AmazonIDP(authManager,authzToken), AmazonLogin.this);
        }

        @Override
        public void onError(AuthError ae) {
            Log.e(LOG_TAG, ae.getMessage(), ae);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResult(Activity.RESULT_CANCELED, null);
                    finish();
                }
            });
        }

        @Override
        public void onCancel(Bundle cause) {
            Log.e(LOG_TAG, "ProfileListener cancelled");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResult(Activity.RESULT_CANCELED, null);
                    finish();     
                }
            });
        }
    }

    protected class AmazonIDP implements WIFIdentityProvider {

        private String amazonToken;
        private AmazonAuthorizationManager authManager;

        public AmazonIDP(AmazonAuthorizationManager authManager, String token) {
            this.authManager = authManager;
            amazonToken = token;
        }

        @Override
        public String getToken() {
            return amazonToken;
        }

        @Override
        public String getProviderID() {
            return "www.amazon.com";
        }

        @Override
        public String getRoleARN() {
            return S3PersonalFileStore.clientManager.getAmazonRoleARN();
        }

        @Override
        public void logout() {
            authManager.clearAuthorizationState(new APIListener() {
                @Override
                public void onSuccess(Bundle results) {
                    Log.d(LOG_TAG, "Successfully logged out");
                }

                @Override
                public void onError(AuthError authError) {
                    Log.e(LOG_TAG, "Error clearing authorization state.", authError);
                }
            });
        }

    }
}
