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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;
import com.amazonaws.cognito.sync.devauth.client.AmazonSharedPreferencesWrapper;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class MainActivity extends Activity implements Session.StatusCallback {

    private static final String TAG = "MainActivity";

    private static final String[] APP_SCOPES = {
            "profile"
    };

    private Button btnLoginFacebook;
    private Button btnLoginLWA;
    private Button btnLoginDevAuth;
    private Button btnWipedata;
    private AmazonAuthorizationManager mAuthManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        /**
         * Initializes the sync client. This must be call before you can use it.
         */
        CognitoSyncClientManager.init(this);

        btnLoginFacebook = (Button) findViewById(R.id.btnLoginFacebook);
        btnLoginFacebook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start Facebook Login
                Session.openActiveSession(MainActivity.this, true,
                        MainActivity.this);
            }
        });
        final Session session = Session
                .openActiveSessionFromCache(MainActivity.this);
        if (session != null) {
            setFacebookSession(session);
        }

        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Login with Amazon is disabled.",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Login with Amazon isn't configured correctly. "
                    + "Thus it's disabled in this demo.", e);
        }
        btnLoginLWA = (Button) findViewById(R.id.btnLoginLWA);
        btnLoginLWA.setVisibility(View.VISIBLE);
        btnLoginLWA.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthManager.authorize(APP_SCOPES, Bundle.EMPTY,
                        new AuthorizeListener());
            }
        });
        btnLoginLWA.setEnabled(mAuthManager != null);

        btnWipedata = (Button) findViewById(R.id.btnWipedata);
        btnWipedata.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Wipe data?")
                        .setMessage(
                                "This will log off your current session and wipe all user data. "
                                        + "Any data not synchronized will be lost.")
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        // clear login status
                                        if (session != null) {
                                            session.closeAndClearTokenInformation();
                                        }
                                        btnLoginFacebook
                                                .setVisibility(View.VISIBLE);
                                        if (mAuthManager != null) {
                                            mAuthManager
                                                    .clearAuthorizationState(null);
                                        }
                                        btnLoginLWA.setVisibility(View.VISIBLE);
                                        // wipe data
                                        CognitoSyncClientManager.getInstance()
                                                .wipeData();

                                        // Wipe shared preferences
                                        AmazonSharedPreferencesWrapper.wipe(PreferenceManager
                                                .getDefaultSharedPreferences(MainActivity.this));
                                    }

                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.cancel();
                                    }
                                }).show();
            }
        });

        findViewById(R.id.btnListDatasets).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this,
                                ListDatasetsActivity.class);
                        startActivity(intent);
                    }
                });

        btnLoginDevAuth = (Button) findViewById(R.id.btnLoginDevAuth);
        if ((CognitoSyncClientManager.credentialsProvider.getIdentityProvider()) instanceof DeveloperAuthenticationProvider) {
            btnLoginDevAuth.setEnabled(true);
            Log.w(TAG, "Developer authentication feature configured correctly. ");
        } else {
            btnLoginDevAuth.setEnabled(false);
            Toast.makeText(this, "Developer authentication feature is disabled.",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Developer authentication feature configured incorrectly. "
                    + "Thus it's disabled in this demo.");
        }
        btnLoginDevAuth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // username and password dialog
                final Dialog login = new Dialog(MainActivity.this);
                login.setContentView(R.layout.login_dialog);
                login.setTitle("Sample developer login");
                final TextView txtUsername = (TextView) login
                        .findViewById(R.id.txtUsername);
                txtUsername.setHint("Username");
                final TextView txtPassword = (TextView) login
                        .findViewById(R.id.txtPassword);
                txtPassword.setHint("Password");
                Button btnLogin = (Button) login.findViewById(R.id.btnLogin);
                Button btnCancel = (Button) login.findViewById(R.id.btnCancel);

                btnCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        login.dismiss();
                    }
                });

                btnLogin.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Validate the username and password
                        if (txtUsername.getText().toString().isEmpty()
                                || txtPassword.getText().toString().isEmpty()) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Login error")
                                    .setMessage(
                                            "Username or password cannot be empty!!")
                                    .show();
                        } else {
                            // Clear the existing credentials
                            CognitoSyncClientManager.credentialsProvider
                                    .clearCredentials();
                            // Initiate user authentication against the
                            // developer backend in this case the sample Cognito
                            // developer authentication application.
                            ((DeveloperAuthenticationProvider) CognitoSyncClientManager.credentialsProvider
                                    .getIdentityProvider()).login(
                                    txtUsername.getText().toString(),
                                    txtPassword.getText().toString(),
                                    MainActivity.this);
                        }
                        login.dismiss();
                    }
                });
                login.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
    }

    @Override
    public void call(Session session, SessionState state, Exception exception) {
        if (session.isOpened()) {
            setFacebookSession(session);
            // make request to the /me API
            Request.newMeRequest(session, new Request.GraphUserCallback() {

                // callback after Graph API response with user object
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        Toast.makeText(MainActivity.this,
                                "Hello " + user.getName(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }).executeAsync();
        }
    }

    private void setFacebookSession(Session session) {
        Log.i(TAG, "facebook token: " + session.getAccessToken());
        CognitoSyncClientManager.addLogins("graph.facebook.com",
                session.getAccessToken());
        btnLoginFacebook.setVisibility(View.GONE);
    }

    private class AuthorizeListener implements AuthorizationListener {

        /* Authorization was completed successfully. */
        @Override
        public void onSuccess(Bundle response) {
            Log.i(TAG, "Auth successful. Start to getToken");
            mAuthManager.getToken(APP_SCOPES, new AuthTokenListener());
            mAuthManager.getProfile(new APIListener() {
                @Override
                public void onSuccess(Bundle response) {
                    Bundle profileBundle = response
                            .getBundle(AuthzConstants.BUNDLE_KEY.PROFILE.val);
                    final String name = profileBundle
                            .getString(AuthzConstants.PROFILE_KEY.NAME.val);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Hello " + name,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(AuthError ae) {
                    Log.e(TAG, "AuthError during getProfile", ae);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnLoginLWA.setVisibility(View.GONE);
                }
            });

        }

        /* There was an error during the attempt to authorize the application. */
        @Override
        public void onError(AuthError ae) {
            Log.e(TAG, "AuthError during authorization", ae);
        }

        /* Authorization was cancelled before it could be completed. */
        @Override
        public void onCancel(Bundle cause) {
            Log.e(TAG, "User cancelled authorization");
        }
    }

    private class AuthTokenListener implements APIListener {

        @Override
        public void onSuccess(Bundle response) {
            final String token = response
                    .getString(AuthzConstants.BUNDLE_KEY.TOKEN.val);
            Log.i(TAG, "amazon token: " + token);
            CognitoSyncClientManager.addLogins("www.amazon.com", token);
        }

        @Override
        public void onError(AuthError ae) {
            Log.e(TAG, "Failed to get token", ae);
        }
    }
}
