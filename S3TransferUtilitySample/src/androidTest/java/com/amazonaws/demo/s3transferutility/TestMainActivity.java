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

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestMainActivity {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private static AmazonS3Client s3;
    private static String bucket;
    private static String testFileName = "ui-test-file";
    private static String testFileContent = "AWS Samples test file contents. ";
    private static int NUM_OF_RETRIES = 3;
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

        s3 = new AmazonS3Client(AWSMobileClient.getInstance());

        // Attempt to upload the file NUM_OF_RETRIES times
        for (int i = 0; i < NUM_OF_RETRIES; i++) {
            try {
                s3.putObject(bucket, testFileName, testFileContent);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                if(i == NUM_OF_RETRIES - 1) fail(e.getMessage());
            }
        }
    }

    @After
    public void cleanAfter() {
        s3.deleteObject(bucket, testFileName);
    }

    @Test
    public void mainActivityTest() {
        // Set the Idling Resource timeout to 1 second
        long waitingTime = DateUtils.SECOND_IN_MILLIS;
        IdlingPolicies.setIdlingResourceTimeout(
                waitingTime, TimeUnit.MILLISECONDS);
        DownloadCompleteIdlingResource downloadCompleteIdlingResource = new DownloadCompleteIdlingResource();

        // Perform the test steps
        ViewInteraction button = onView(
                allOf(withId(R.id.buttonDownloadMain)));
        button.perform(click());
        ViewInteraction button2 = onView(
                allOf(withId(R.id.buttonDownload)));
        button2.perform(click());

        DataInteraction linearLayout = onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list)))
                .atPosition(0);
        linearLayout.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.textState)));

        IdlingRegistry.getInstance().register(downloadCompleteIdlingResource);
        textView.check(matches(withText("COMPLETED")));
        IdlingRegistry.getInstance().unregister(downloadCompleteIdlingResource);
    }
}