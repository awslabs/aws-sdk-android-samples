/*
 * Copyright 2015-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.s3transferutility;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.format.DateUtils;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestMainActivity {
    private static final String TAG = TestMainActivity.class.getSimpleName();

    private static final String TEST_FILE_NAME = "ui-test-file";
    private static final String TEST_FILE_CONTENTS = "AWS Samples test file contents.";

    private static AmazonS3Client s3;
    private static String bucket;
    private static String region;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Upload a test file to the S3 bucket
     */
    @BeforeClass
    public static void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(appContext, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });
        latch.await();

        final AWSConfiguration awsConfiguration = AWSMobileClient.getInstance().getConfiguration();

        // AWS Configuration file should have S3 bucket and region configured
        assertNotNull(awsConfiguration.optJsonObject("S3TransferUtility"));
        bucket = awsConfiguration.optJsonObject("S3TransferUtility").getString("Bucket");
        region = awsConfiguration.optJsonObject("S3TransferUtility").getString("Region");

        s3 = new AmazonS3Client(AWSMobileClient.getInstance(), Region.getRegion(region));
        s3.putObject(bucket, TEST_FILE_NAME, TEST_FILE_CONTENTS);
    }

    @After
    public void cleanAfter() {
        s3.deleteObject(bucket, TEST_FILE_NAME);
    }

    @Test
    public void mainActivityTest() {
        // Set the Idling Resource timeout to 1 second
        long waitingTime = DateUtils.SECOND_IN_MILLIS;
        Log.d(TAG,"setIdlingResourceTimeout");

        IdlingPolicies.setIdlingResourceTimeout(waitingTime, TimeUnit.MILLISECONDS);
        DownloadCompleteIdlingResource downloadCompleteIdlingResource = new DownloadCompleteIdlingResource();

        // Perform the test steps
        ViewInteraction button = onView(withId(R.id.buttonDownloadMain));
        Log.d(TAG,"click DownloadMain button");
        button.perform(click());

        Log.d(TAG,"click allow button");
        ViewInteraction button2 = onView(withId(R.id.buttonDownload));
        Log.d(TAG,"click Download button");
        button2.perform(click());

        DataInteraction linearLayout = onData(anything())
                .inAdapterView(withId(android.R.id.list))
                .atPosition(0);
        Log.d(TAG,"Select downloading file");
        linearLayout.perform(click());

        ViewInteraction textView = onView(withId(R.id.textState));
        IdlingRegistry.getInstance().register(downloadCompleteIdlingResource);
        Log.d(TAG,"textView.check COMPLETED");
        textView.check(matches(withText("COMPLETED")));

        Log.d(TAG,"unregister(downloadCompleteIdlingResource)");
        IdlingRegistry.getInstance().unregister(downloadCompleteIdlingResource);
        Log.d(TAG,"finished");
    }
}
