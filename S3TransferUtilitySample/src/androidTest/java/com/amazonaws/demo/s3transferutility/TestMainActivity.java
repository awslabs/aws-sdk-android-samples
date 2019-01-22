package com.amazonaws.demo.s3transferutility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
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
import android.util.Log;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
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
    private static final int PERMISSIONS_DIALOG_DELAY = 1000;
    private static final int GRANT_BUTTON_INDEX = 1;

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
        String TAG = "mainActivityTest";
        Log.e(TAG,"setIdlingResourceTimeout");

        IdlingPolicies.setIdlingResourceTimeout(
                waitingTime, TimeUnit.MILLISECONDS);
        DownloadCompleteIdlingResource downloadCompleteIdlingResource = new DownloadCompleteIdlingResource();

        // Perform the test steps
        ViewInteraction button = onView(
                allOf(withId(R.id.buttonDownloadMain)));
        Log.e(TAG,"click DownloadMain button");
        button.perform(click());

        Log.e(TAG,"click allow button");
        allowPermissionsIfNeeded();
        ViewInteraction button2 = onView(
                allOf(withId(R.id.buttonDownload)));
        Log.e(TAG,"click Download button");
        button2.perform(click());

        DataInteraction linearLayout = onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list)))
                .atPosition(0);
        Log.e(TAG,"Select downloading file");
        linearLayout.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.textState)));

        IdlingRegistry.getInstance().register(downloadCompleteIdlingResource);
        Log.e(TAG,"textView.check COMPLETED");
        textView.check(matches(withText("COMPLETED")));
        Log.e(TAG,"unregister(downloadCompleteIdlingResource)");
        IdlingRegistry.getInstance().unregister(downloadCompleteIdlingResource);
        Log.e(TAG,"finished");
    }

    public static void allowPermissionsIfNeeded() {
        try {
            String permissionNeeded = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasNeededPermission(permissionNeeded)) {
                sleep(PERMISSIONS_DIALOG_DELAY);
                UiDevice device = UiDevice.getInstance(getInstrumentation());
                UiObject allowPermissions = device.findObject(new UiSelector()
                        .clickable(true)
                        .checkable(false)
                        .index(GRANT_BUTTON_INDEX));
                if (allowPermissions.exists()) {
                    allowPermissions.click();
                }
            }
        } catch (UiObjectNotFoundException e) {
            System.out.println("There is no permissions dialog to interact with");
        }
    }

    private static boolean hasNeededPermission(String permissionNeeded) {
        Context context = InstrumentationRegistry.getTargetContext();
        int permissionStatus = ContextCompat.checkSelfPermission(context, permissionNeeded);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot execute Thread.sleep()");
        }
    }
}
