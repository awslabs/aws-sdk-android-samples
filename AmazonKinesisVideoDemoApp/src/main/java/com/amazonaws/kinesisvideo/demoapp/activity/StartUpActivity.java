package com.amazonaws.kinesisvideo.demoapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.amazonaws.kinesisvideo.demoapp.util.ActivityUtils;
import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.amazonaws.mobile.auth.core.StartupAuthResult;
import com.amazonaws.mobile.auth.core.StartupAuthResultHandler;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInActivity;

public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IdentityManager.getDefaultIdentityManager().doStartupAuth(this, new StartupAuthResultHandler() {
            @Override
            public void onComplete(StartupAuthResult authResults) {
                if (authResults.isUserSignedIn()) {
                    ActivityUtils.startActivity(StartUpActivity.this, SimpleNavActivity.class);
                } else {
                    IdentityManager.getDefaultIdentityManager().setUpToAuthenticate(StartUpActivity.this, new DefaultSignInResultHandler() {
                        @Override
                        public void onSuccess(Activity callingActivity, IdentityProvider provider) {
                            ActivityUtils.startActivity(StartUpActivity.this, SimpleNavActivity.class);
                        }

                        @Override
                        public boolean onCancel(Activity callingActivity) {
                            return false; // false = User cannot cancel login
                        }
                    });
                    SignInActivity.startSignInActivity(StartUpActivity.this, new AuthUIConfiguration.Builder().userPools(true).build());
                }
            }
        });
    }
}
