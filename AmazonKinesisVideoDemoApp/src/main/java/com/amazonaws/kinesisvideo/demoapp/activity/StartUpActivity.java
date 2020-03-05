package com.amazonaws.kinesisvideo.demoapp.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.kinesisvideo.demoapp.util.ActivityUtils;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.Tokens;

public class StartUpActivity extends AppCompatActivity {
    public static final String TAG = StartUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AWSMobileClient auth = AWSMobileClient.getInstance();
        final AppCompatActivity thisActivity = this;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (auth.isSignedIn()) {
                    auth.getTokens(new Callback<Tokens>() {
                        @Override
                        public void onResult(Tokens result) {
                            ActivityUtils.startActivity(thisActivity, SimpleNavActivity.class);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "onError: Sign in failure - Auth get token error", e);
                            Toast.makeText(StartUpActivity.this, "onError: Sign in failure - Auth get token error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    auth.showSignIn(thisActivity,
                            SignInUIOptions.builder()
                                    .nextActivity(SimpleNavActivity.class)
                                    .build(),
                            new Callback<UserStateDetails>() {
                                @Override
                                public void onResult(UserStateDetails result) {
                                    Log.d(TAG, "onResult: User signed-in " + result.getUserState());
                                }

                                @Override
                                public void onError(final Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e(TAG, "onError: User sign-in error", e);
                                            Toast.makeText(StartUpActivity.this, "User sign-in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                }
            }
        });
    }
}
