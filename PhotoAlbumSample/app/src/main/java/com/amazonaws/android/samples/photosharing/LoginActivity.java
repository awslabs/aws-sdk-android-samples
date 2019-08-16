/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.android.samples.photosharing;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;


/**
 * LoginActivity displays the drop-in UI of AWS Auth for Android SDK.
 * Allows user sign-up and sign-in.
 * Each time when user sign out, the app will return LoginActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, "onResult: " + userStateDetails.getUserState());

                switch (userStateDetails.getUserState()) {
                    case GUEST:
                        Log.i(TAG, "user is in guest mode");
                        break;
                    case SIGNED_IN:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(LoginActivity.this, AlbumActivity.class));
                            }
                        });
                        break;
                    case SIGNED_OUT:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                presentSignIn();
                            }
                        });
                        break;
                    case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                        Log.i(TAG, "need to login again");
                        break;
                    case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                        Log.i(TAG, "user logged in via federation, but currently needs new tokens");
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("LoginError")
                                .setMessage("Error logging in. Please check your username, password and network connection.")
                                .show();
                    }
                });
            }
        });
    }

    /**
     * Present Drop-in UI.
     */
    private void presentSignIn() {
        AWSMobileClient.getInstance().showSignIn(
                LoginActivity.this,
                SignInUIOptions.builder()
                        .nextActivity(AlbumActivity.class)
                        .logo(R.drawable.logo)
                        .canCancel(true)
                        .build(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        Log.d(TAG, "onResult: " + result.getUserState());
                        switch (result.getUserState()){
                            case GUEST:
                                Log.i(TAG, "user is in guest mode");
                                break;
                            case SIGNED_IN:
                                startActivity(new Intent(LoginActivity.this, AlbumActivity.class));
                                Log.i(TAG, "logged in!");
                                break;
                            case SIGNED_OUT:
                                Log.i(TAG, "onResult: User did not choose to sign-in");
                                break;
                            case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                                Log.i(TAG, "need to login again");
                                break;
                            case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                                Log.i(TAG, "user logged in via federation, but currently needs new tokens");
                                break;
                            default:
                                AWSMobileClient.getInstance().signOut();
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: ", e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setTitle("LoginError")
                                        .setMessage("Error logging in. Please check your username, password and network connection.")
                                        .show();
                            }
                        });
                    }
                }
        );
    }
}