package com.amazonaws.kinesisvideo.demoapp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.kinesisvideo.demoapp.util.ActivityUtils;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

public class StartUpActivity extends AppCompatActivity {
    public static final String TAG = StartUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AWSMobileClient auth = AWSMobileClient.getInstance();

        if (auth.isSignedIn()) {
            ActivityUtils.startActivity(this, SimpleNavActivity.class);
        } else {
            auth.showSignIn(this,
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
}
