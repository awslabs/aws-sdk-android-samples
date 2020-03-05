package com.amazonaws.kinesisvideo.demoapp;

import android.app.Application;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.kinesisvideo.auth.KinesisVideoCredentialsProvider;
import com.amazonaws.kinesisvideo.common.logging.LogLevel;
import com.amazonaws.kinesisvideo.common.logging.OutputChannel;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.kinesisvideo.auth.AwsIoTKinesisVideoCredentialsProvider;
import com.amazonaws.mobileconnectors.kinesisvideo.util.AndroidLogOutputChannel;
import com.amazonaws.regions.Regions;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;

public class KinesisVideoDemoApp extends Application {
    public static final String TAG = KinesisVideoDemoApp.class.getSimpleName();
    public static Regions KINESIS_VIDEO_REGION = Regions.US_WEST_2;

    public static AWSCredentialsProvider getCredentialsProvider() {
        return AWSMobileClient.getInstance();
    }

    public static KinesisVideoCredentialsProvider getKvsCredentialsProvider() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final OutputChannel outputChannel = new AndroidLogOutputChannel();
        final com.amazonaws.kinesisvideo.common.logging.Log log =
                new com.amazonaws.kinesisvideo.common.logging.Log(outputChannel, LogLevel.VERBOSE, TAG);
        return new AwsIoTKinesisVideoCredentialsProvider("REPLACE_ME_IoT_ThingName", "REPLACE_ME_awsIotAuthUrl", "REPLACE_ME_keyStoreLocation",
                "REPLACE_ME_keyStorePassword", log);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.d(TAG, "onResult: user state: " + result.getUserState());
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Initialization error of the mobile client", e);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
