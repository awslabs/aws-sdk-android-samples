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

package com.amazonaws.demo.numberguess;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;
import com.amazonaws.demo.numberguess.manager.CognitoClientManager;
import com.amazonaws.demo.numberguess.manager.CognitoSyncClientManager;
import com.amazonaws.demo.numberguess.manager.DynamoDBManager;
import com.amazonaws.demo.numberguess.manager.MobileAnalyticsClientManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Dataset.SyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;

import java.util.List;

public class ResultActivity extends Activity {

    private static final String TAG = "ResultActivity";
    private static final String[] APP_SCOPES = {
            "profile"
    };
    private static String sUserName;

    private int mNumberOfGuesses;
    private int mSecondsUsed;
    private int mBestScore;
    private int mCurrentScore;
    private boolean hasSentEvents;
    private boolean hasGotToken;
    private boolean hasGotProfile;

    private TextView textNumberOfGuess;
    private TextView textTime;
    private TextView textScore;
    private TextView textBestScore;
    private Button btnRecord;
    private Button btnPlayAgain;
    private Button btnLoginWithAmazon;
    private Button btnWipeData;
    private LinearLayout loginLayout;
    private AmazonAuthorizationManager mAuthManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        initData();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Resume Mobile Analytics client when Activity resumes.
         */
        MobileAnalyticsClientManager.resumeSession();
        if (!hasSentEvents) {
            MobileAnalyticsClientManager.recordGameResult(mSecondsUsed,
                    mNumberOfGuesses, mCurrentScore);
            hasSentEvents = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
         * Pause Mobile Analytics client when Activity pauses.
         */
        MobileAnalyticsClientManager.pauseAndSubmit();
    }

    private void initData() {
        hasSentEvents = false;
        hasGotProfile = false;
        hasGotToken = false;
        /*
         * Initializes CognitoSyncClientManager and DynamoDBManager,
         * initialization must be called before you can use them.
         */
        CognitoSyncClientManager.init(this);
        DynamoDBManager.init();
        mNumberOfGuesses = getIntent().getIntExtra(
                GuessActivity.TIMES_OF_GUESSES, 0);
        mSecondsUsed = getIntent().getIntExtra(GuessActivity.TIME_TO_COMPLETE,
                0);
        mBestScore = getIntent().getIntExtra(GuessActivity.BEST_SCORE, 0);
        mCurrentScore = calculateScore(mNumberOfGuesses, mSecondsUsed);
        Log.i(TAG, "Best score = " + mBestScore + ", Current score = "
                + mCurrentScore);
        if (mCurrentScore > mBestScore) {
            mBestScore = mCurrentScore;
            new InsertRecordTask().execute();
        }
        /*
         * Initializes MobileAnalyticsClientManager, it must be called before
         * you can use it.
         */
        MobileAnalyticsClientManager.init(ResultActivity.this);

        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Login with Amazon is disabled.",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Login with Amazon isn't configured correctly. "
                    + "Thus it's disabled in this demo.", e);
        }
    }

    private void initUI() {
        textNumberOfGuess = (TextView) findViewById(R.id.textViewNumberGuess);
        textNumberOfGuess.setText(getString(R.string.number_of_guess) + " "
                + mNumberOfGuesses);
        textTime = (TextView) findViewById(R.id.textViewTime);
        textTime.setText(getString(R.string.game_time) + " " + mSecondsUsed
                + " " + getString(R.string.seconds));
        textScore = (TextView) findViewById(R.id.textViewScore);
        textScore.setText(getString(R.string.my_score) + " " + mCurrentScore);
        textBestScore = (TextView) findViewById(R.id.textViewBestScore);
        textBestScore.setText(getString(R.string.my_best_score) + " "
                + mBestScore);
        btnRecord = (Button) findViewById(R.id.btnGlobalRecord);
        btnRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(ResultActivity.this, GlobalRecordActivity.class);
                startActivity(intent);
            }
        });
        btnPlayAgain = (Button) findViewById(R.id.btnPlayAgain);
        btnPlayAgain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(ResultActivity.this, GuessActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnLoginWithAmazon = (Button) findViewById(R.id.buttonLogin);
        btnLoginWithAmazon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mAuthManager.authorize(APP_SCOPES, Bundle.EMPTY,
                        new AuthorizeListener());
            }
        });
        btnWipeData = (Button) findViewById(R.id.buttonWipeData);
        btnWipeData.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new WipeDataTask().execute();
            }
        });
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        if (CognitoClientManager.isAuthenticated()) {
            loginLayout.setVisibility(View.GONE);
        } else {
            textBestScore.setVisibility(View.GONE);
        }
    }

    private void getBestScoreFromCognitoSync() {
        /* For unauthenticated users, will not sync user data */
        if (!CognitoClientManager.isAuthenticated()) {
            Log.d(TAG, "Unauthenticated user");
            return;
        }

        Dataset dataset = CognitoSyncClientManager
                .openOrCreateDataset(Constants.SYNC_DATASET_NAME);
        dataset.synchronize(new SyncCallback() {
            @Override
            public void onSuccess(Dataset ds, List<Record> list) {
                Log.d(TAG, "Sync success");
                String scoreStr = ds.get(Constants.SYNC_KEY_BEST);
                if (scoreStr == null)
                    scoreStr = "0";
                mBestScore = Integer.parseInt(scoreStr);
                if (mCurrentScore > mBestScore) {
                    mBestScore = mCurrentScore;
                    new InsertRecordTask().execute();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginLayout.setVisibility(View.GONE);
                        textBestScore.setText(getString(R.string.my_best_score) + " "
                                + mBestScore);
                        textBestScore.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onFailure(DataStorageException ex) {
                Log.e(TAG, "Sync fails", ex);
            }

            @Override
            public boolean onDatasetsMerged(Dataset ds, List<String> list) {
                Log.d(TAG, "Datasets merged");
                return false;
            }

            @Override
            public boolean onDatasetDeleted(Dataset ds, String str) {
                Log.d(TAG, "Dataset deleted");
                return false;
            }

            @Override
            public boolean onConflict(Dataset ds, List<SyncConflict> list) {
                Log.d(TAG, "Conflict");
                return false;
            }
        });
    }

    private void syncBestScore(int best) {
        /* For unauthenticated users, will not sync user data */
        if (!CognitoClientManager.isAuthenticated()) {
            return;
        }
        Dataset dataset = CognitoSyncClientManager
                .openOrCreateDataset(Constants.SYNC_DATASET_NAME);
        dataset.put(Constants.SYNC_KEY_BEST, String.valueOf(best));
        dataset.synchronize(new SyncCallback() {

            @Override
            public void onSuccess(Dataset ds, List<Record> list) {
                Log.d(TAG, "Sync success");
            }

            @Override
            public void onFailure(DataStorageException ex) {
                Log.e(TAG, "Sync error", ex);
            }

            @Override
            public boolean onDatasetsMerged(Dataset ds, List<String> list) {
                Log.d(TAG, "Datasets merged");
                return false;
            }

            @Override
            public boolean onDatasetDeleted(Dataset ds, String str) {
                Log.d(TAG, "Dataset deleted");
                return false;
            }

            @Override
            public boolean onConflict(Dataset ds, List<SyncConflict> list) {
                Log.i(TAG, "Datasets Conflict");
                return false;
            }
        });
    }

    private int calculateScore(int guess, int seconds) {
        return 100000 / (1 + guess * seconds);
    }

    private void updateBestScoreIfPossible() {
        if (hasGotProfile && hasGotToken && CognitoClientManager.isAuthenticated()) {
            getBestScoreFromCognitoSync();
        }
    }

    class InsertRecordTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            String userID = null;
            String userName = null;
            if (CognitoClientManager.isAuthenticated()) {
                userID = CognitoClientManager.getCredentials().getIdentityId();
                Log.i(TAG, "userID = " + userID);
                userName = sUserName;
            } else {
                return null;
            }
            DynamoDBManager.saveRecord(userID, mBestScore, userName);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (CognitoClientManager.isAuthenticated()) {
                syncBestScore(mBestScore);
            }
        }
    }

    private class WipeDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            CognitoSyncClientManager.wipeData();
            /*
             * Refresh the credential provider in case the Identity Id has
             * changed
             */
            CognitoClientManager.refresh();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hasGotProfile = false;
                    hasGotToken = false;
                    loginLayout.setVisibility(View.VISIBLE);
                    textBestScore.setVisibility(View.GONE);
                }
            });
        }
    }

    private class AuthorizeListener implements AuthorizationListener {

        /* Authorization was completed successfully. */
        @Override
        public void onSuccess(Bundle response) {
            Log.d(TAG, "Auth successful. Start to getToken and getProfile");
            mAuthManager.getToken(APP_SCOPES, new AuthTokenListener());
            mAuthManager.getProfile(new AuthProfileListener());
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

    private class AuthProfileListener implements APIListener {

        /* Got user's profile Successfully. */
        @Override
        public void onSuccess(Bundle response) {
            Bundle profileBundle = response
                    .getBundle(AuthzConstants.BUNDLE_KEY.PROFILE.val);
            sUserName = profileBundle
                    .getString(AuthzConstants.PROFILE_KEY.NAME.val);
            hasGotProfile = true;
            updateBestScoreIfPossible();
        }

        /* There was an error during the attempt to get user's profile. */
        @Override
        public void onError(AuthError ae) {
            Log.e(TAG, "AuthError during getProfile", ae);
        }
    }

    private class AuthTokenListener implements APIListener {

        /* Got token Successfully. */
        @Override
        public void onSuccess(Bundle response) {
            final String token = response
                    .getString(AuthzConstants.BUNDLE_KEY.TOKEN.val);
            Log.i(TAG, "amazon token: " + token);
            CognitoClientManager.addLogins("www.amazon.com", token);
            /*
             * Refresh the credential provider in case the Identity Id has
             * changed
             */
            CognitoClientManager.refresh();
            hasGotToken = true;
            updateBestScoreIfPossible();
        }

        /* There was an error during the attempt to get token. */
        @Override
        public void onError(AuthError ae) {
            Log.e(TAG, "Failed to get token", ae);
        }
    }
}
