/*
 * Copyright 2013-2017 Amazon.com, Inc. or its affiliates.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.cognito.android.samples.authdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.cognito.android.samples.authdemo.fragments.AuthUserFragment;
import com.amazonaws.cognito.android.samples.authdemo.fragments.UnauthUserFragment;
import com.amazonaws.mobileconnectors.cognitoauth.Auth;
import com.amazonaws.mobileconnectors.cognitoauth.AuthUserSession;
import com.amazonaws.mobileconnectors.cognitoauth.handlers.AuthHandler;

public class MainActivity extends FragmentActivity
implements AuthUserFragment.OnFragmentInteractionListener,
        UnauthUserFragment.OnFragmentInteractionListener {
    private static final String TAG = "CognitoAuthDemo";
    private Auth auth;
    private AlertDialog userDialog;
    private Uri appRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCognito();
        setNewUserFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent activityIntent = getIntent();
        //  -- Call Auth.getTokens() to get Cognito JWT --
        if (activityIntent.getData() != null &&
                appRedirect.getHost().equals(activityIntent.getData().getHost())) {
            auth.getTokens(activityIntent.getData());
        }
    }

    /**
     * Sets new user fragment on the screen.
     */
    private void setNewUserFragment() {
        UnauthUserFragment newUserFragment = new UnauthUserFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayoutContainer, newUserFragment);
        transaction.commit();
        setScreenImages();
    }

    /**
     * Sets auth user fragment.
     * @param session {@link AuthUserSession} containing tokens for a user.
     */
    private void setAuthUserFragment(AuthUserSession session) {
        AuthUserFragment userFragment = new AuthUserFragment();

        Bundle fragArgs = new Bundle();
        fragArgs.putString(getString(R.string.app_access_token), session.getAccessToken().getJWTToken());
        fragArgs.putString(getString(R.string.app_id_token), session.getIdToken().getJWTToken());
        userFragment.setArguments(fragArgs);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayoutContainer, userFragment);
        transaction.commit();
        setScreenImages();
    }

    /**
     * Handles button press.
     * @param signIn When {@code True} this performs sign-in.
     */
    public void onButtonPress(boolean signIn) {
        Log.d(" -- ", "Button press: " + signIn);
        if (signIn) {
            this.auth.getSession();
        } else {
            this.auth.signOut();
        }
    }

    @Override
    public void showPopup(String title, String content) {
        showDialogMessage(title, content);
    }

    /**
     * Setup authentication with Cognito.
     */
    void initCognito() {
        //  -- Create an instance of Auth --
        Auth.Builder builder = new Auth.Builder().setAppClientId(getString(R.string.cognito_client_id))
                .setAppClientSecret(getString(R.string.cognito_client_secret))
                .setAppCognitoWebDomain(getString(R.string.cognito_web_domain))
                .setApplicationContext(getApplicationContext())
                .setAuthHandler(new callback())
                .setSignInRedirect(getString(R.string.app_redirect))
                .setSignOutRedirect(getString(R.string.app_redirect));
        this.auth = builder.build();
        appRedirect = Uri.parse(getString(R.string.app_redirect));
    }

    /**
     * Callback handler for Amazon Cognito.
     */
    class callback implements AuthHandler {

        @Override
        public void onSuccess(AuthUserSession authUserSession) {
            // Show tokens for the authenticated user
            setAuthUserFragment(authUserSession);
        }

        @Override
        public void onSignout() {
            // Back to new user screen.
            setNewUserFragment();
        }

        @Override
        public void onFailure(Exception e) {
            showDialogMessage("error", e.getMessage());
        }
    }

    /**
     * Show an popup dialog.
     * @param title
     * @param body
     */
    private void showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();

                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG,"Dialog failure", e);
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    /**
     * Sets images on the screen.
     */
    private void setScreenImages() {
        ImageView cognitoLogo = (ImageView) findViewById(R.id.imageViewCognito);
        cognitoLogo.setImageDrawable(getDrawable(R.drawable.ic_mobileservices_amazoncognito));
    }
}
