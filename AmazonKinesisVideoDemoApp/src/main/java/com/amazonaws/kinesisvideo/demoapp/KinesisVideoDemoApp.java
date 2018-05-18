package com.amazonaws.kinesisvideo.demoapp;

import android.app.Application;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.regions.Regions;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.amazonaws.util.StringUtils.isBlank;

public class KinesisVideoDemoApp extends Application {
    public static Regions KINESIS_VIDEO_REGION = Regions.US_WEST_2;

    @Override
    public void onCreate() {
        super.onCreate();

        // this sets the logging level
        // to actually enable logging you also need to run
        //    adb shell setprop log.tag.com.amazonaws.request DEBUG
        // see https://github.com/aws/aws-sdk-android/blob/master/Logging.html for details
        Logger.getLogger("org.apache.http").setLevel(Level.FINEST);
        Logger.getLogger("com.amazonaws").setLevel(Level.FINEST);

        if (IdentityManager.getDefaultIdentityManager() == null) {
            final IdentityManager identityManager = new IdentityManager(
                    getApplicationContext(),
                    new AWSConfiguration(this)
            );
            IdentityManager.setDefaultIdentityManager(identityManager);

            IdentityManager.getDefaultIdentityManager().addSignInProvider(CognitoUserPoolsSignInProvider.class);
        }
    }

    public static AWSCredentialsProvider getCredentialsProvider() {
        return IdentityManager.getDefaultIdentityManager().getCredentialsProvider();
    }

}
