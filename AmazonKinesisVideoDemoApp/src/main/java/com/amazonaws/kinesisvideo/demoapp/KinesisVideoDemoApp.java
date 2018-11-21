package com.amazonaws.kinesisvideo.demoapp;

import android.app.Application;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.regions.Regions;

public class KinesisVideoDemoApp extends Application {
    public static final String TAG = KinesisVideoDemoApp.class.getSimpleName();
    public static Regions KINESIS_VIDEO_REGION = Regions.US_WEST_2;

    public static AWSCredentialsProvider getCredentialsProvider() {
        return AWSMobileClient.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.d(TAG, "onResult: user state: " + result.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Initialization error of the mobile client", e);
            }
        });
    }

}
