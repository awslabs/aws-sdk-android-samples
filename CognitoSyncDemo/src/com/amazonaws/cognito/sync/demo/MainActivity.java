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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final String[] APP_SCOPES = {
            "profile"
    };

    private Button btnLoginFacebook;
    private Button btnLoginLWA;
    private Button btnLoginDevAuth;

    private CallbackManager callbackManager;
    private AmazonAuthorizationManager mAuthManager;

    static OAuthProvider mOauthProvider = null;
    static DefaultOAuthConsumer mOauthConsumer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Log.i(TAG, "onCreate");

        /**
         * Initialize Facebook SDK
         */
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        //Twitter
        if (mOauthConsumer == null) {
            mOauthProvider = new DefaultOAuthProvider("https://api.twitter.com/oauth/request_token", "https://api.twitter.com/oauth/access_token", "https://api.twitter.com/oauth/authorize");
            mOauthConsumer = new DefaultOAuthConsumer(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));
        }
        retrieveTwitterCredentials(getIntent());

        //If access token is already here, set fb session
        final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        if (fbAccessToken != null) {
            setFacebookSession(fbAccessToken);
            btnLoginFacebook.setVisibility(View.GONE);
        }

        /**
         * Initializes the sync client. This must be call before you can use it.
         */
        CognitoSyncClientManager.init(this);

        btnLoginFacebook = (Button) findViewById(R.id.btnLoginFacebook);
        btnLoginFacebook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // start Facebook Login
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        btnLoginFacebook.setVisibility(View.GONE);
                        new GetFbName(loginResult).execute();
                        setFacebookSession(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this, "Facebook login cancelled",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(MainActivity.this, "Error in Facebook login " +
                                        error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        btnLoginFacebook.setEnabled(getString(R.string.facebook_app_id) != "facebook_app_id");

        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Login with Amazon isn't configured correctly. "
                    + "Thus it's disabled in this demo.");
        }
        btnLoginLWA = (Button) findViewById(R.id.btnLoginLWA);
        btnLoginLWA.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthManager.authorize(APP_SCOPES, Bundle.EMPTY,
                        new AuthorizeListener());
            }
        });
        btnLoginLWA.setEnabled(mAuthManager != null);

        Button btnWipedata = (Button) findViewById(R.id.btnWipedata);
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
                                        if (fbAccessToken != null) {
                                            LoginManager.getInstance().logOut();
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

        /**
         * Button that leaves the app and launches the Twitter site to get an authorization.
         * If the user grants permissions to our app, it will be redirected to us again through
         * a callback.
         */
        Button btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
        btnLoginTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String callbackUrl = "callback://" + getString(R.string.twitter_callback_url);
                            String authUrl = mOauthProvider.retrieveRequestToken(mOauthConsumer, callbackUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.w("oauth fail", e);
                        }
                    }
                }).start();
            }
        });
        btnLoginTwitter.setEnabled(getString(R.string.twitter_consumer_secret) != "twitter_consumer_secret");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setFacebookSession(AccessToken accessToken) {
        Log.i(TAG, "facebook token: " + accessToken.getToken());
        CognitoSyncClientManager.addLogins("graph.facebook.com",
                accessToken.getToken());
        btnLoginFacebook.setVisibility(View.GONE);
    }

    private void setTwitterSession(String token, String secret) {
        Log.i(TAG, "Twitter token: " + TwitterLoginFromTokenAndSecret(token, secret));
        CognitoSyncClientManager.addLogins("api.twitter.com", TwitterLoginFromTokenAndSecret(token, secret));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
                if (btnLoginTwitter != null) {
                    btnLoginTwitter.setVisibility(View.GONE);
                }
            }
        });
    }

    private static String TwitterLoginFromTokenAndSecret(String key, String secret) {
        // Concatenate token and secret using a semicolon as it's the format Amazon Cognito expects.
        return key+";"+secret;
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

    /**
     * If the app was launched via a Twitter callback, we read the auth token and secret here
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
        retrieveTwitterCredentials(intent);
    }

    void retrieveTwitterCredentials(Intent intent) {

        final Uri uri = intent.getData();
        String callbackUrl = "callback://" + getString(R.string.twitter_callback_url);
        if (uri == null || !uri.toString().startsWith(callbackUrl)) {
            Log.e(TAG, "This is not our Twitter callback");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Get token and secret
                    String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
                    mOauthProvider.retrieveAccessToken(mOauthConsumer, verifier);
                    String token = mOauthConsumer.getToken();
                    String tokenSecret = mOauthConsumer.getTokenSecret();

                    setTwitterSession(token, tokenSecret);

                } catch (Exception e) {
                    Log.e("Exception", e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private class GetFbName extends AsyncTask<Void, Void, String> {
        private final LoginResult loginResult;
        private ProgressDialog dialog;

        public GetFbName(LoginResult loginResult) {
            this.loginResult = loginResult;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "Wait", "Getting user name");
        }

        @Override
        protected String doInBackground(Void... params) {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                            Log.v("LoginActivity", response.toString());
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "name");
            request.setParameters(parameters);
            GraphResponse graphResponse = request.executeAndWait();
            try {
                return graphResponse.getJSONObject().getString("name");
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            dialog.dismiss();
            if (response != null) {
                Toast.makeText(MainActivity.this, "Hello " + response, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Unable to get user name from Facebook",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
