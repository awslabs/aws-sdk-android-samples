/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.android.samples.photosharing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignInState;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.s3.AmazonS3Client;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class StorageUITest {

    private final int MAX_TIME_OUT = 60 * 1000;
    private static final int PERMISSIONS_DIALOG_DELAY = 1000;
    private static final int GRANT_BUTTON_INDEX = 1;
    private static final String ALBUM_NAME_FOR_TESTING = "TestStorage";
    private static final String PHOTO_NAME_FOR_TESTING = "photo1.jpg";
    private static AmazonS3Client s3;
    private static String bucket;

    private final String TAG = AppSyncMutationUITest.class.getSimpleName();
    private final static String ALBUM_ACTIVITY_CLASS_NAME = AlbumActivity.class.getSimpleName();
    private final static String PHOTO_ACTIVITY_CLASS_NAME = PhotoActivity.class.getSimpleName();

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    /**
     * Do sign in and add an album in setup.
     */
    @Before
    public void setUp() throws Exception {
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

        // AWS Configuration file should have S3TransferUtility configured.
        // Should be generated by Amplify CLI.
        assertNotNull(awsConfiguration.optJsonObject("S3TransferUtility"));
        Log.e(TAG, "S3TransferUtility has been configured.");

        bucket = awsConfiguration.optJsonObject("S3TransferUtility").getString("Bucket");

        s3 = new AmazonS3Client(AWSMobileClient.getInstance());

        // Check if goes back to LoginActivity
        int timeOut = 0;
        while (timeOut < MAX_TIME_OUT) {
            try {
                ViewInteraction button2 = onView(allOf(withId(R.id.user_pool_sign_in_view_id)));
                button2.check(matches(isDisplayed()));
                Log.e(TAG,"The view has gone back to LoginActivity.");
                break;
            } catch (NoMatchingViewException e) {
            }

            Thread.sleep(5000);
            timeOut += 5000;
        }

        // Do sign in first
        UIActionsUtil.signIn(UIActionsUtil.getUsername(), UIActionsUtil.getPassword());

        // Check if user successfully signed in.
        AWSMobileClient.getInstance().confirmSignIn("signInChallengeResponse", new Callback<SignInResult>() {
            @Override
            public void onResult(SignInResult result) {
                Log.e(TAG, "SignInResult: " + result);
                assertEquals(result.getSignInState(), SignInState.DONE);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "SignInResult Error: " + e);
            }
        });

        // Check if it jumps into AlbumActivity
        timeOut = 0;
        while (timeOut < MAX_TIME_OUT) {
            try {
                onView(allOf(withText(ALBUM_ACTIVITY_CLASS_NAME), childAtPosition(allOf(withId(R.id.action_bar), childAtPosition(
                        withId(R.id.action_bar_container),
                        0)), 0), isDisplayed()));
                break;
            } catch (NoMatchingViewException e) {
            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }

        UIActionsUtil.addAlbum(ALBUM_NAME_FOR_TESTING);

        UIActionsUtil.clickEdit();
    }

    @Test
    public void testUploadAndDownload() throws InterruptedException {
        UIActionsUtil.clickEdit();

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.album_image),
                        childAtPosition(
                                withParent(withId(R.id.gw_lstAlbum)),
                                0),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        // Check if it jumps into PhotoActivity
        int timeOut = 0;
        while (timeOut < MAX_TIME_OUT) {
            try {
                onView(allOf(withText(PHOTO_ACTIVITY_CLASS_NAME), childAtPosition(allOf(withId(R.id.action_bar), childAtPosition(
                        withId(R.id.action_bar_container),
                        0)), 0), isDisplayed()));
                break;
            } catch (NoMatchingViewException e) {
            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }

        Log.e(TAG,"click allow button");
        allowPermissionsIfNeeded();

        UIActionsUtil.clickUploadPhoto();

        timeOut = 0;
        while (timeOut < (MAX_TIME_OUT / 2)) {
            try {
                ViewInteraction textView = onView(
                        allOf(IsInstanceOf.<View>instanceOf(android.widget.TextView.class), withText("Upload Succeed Message"),
                                childAtPosition(
                                        allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                childAtPosition(
                                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                        0)),
                                        0),
                                isDisplayed()));
                textView.check(matches(withText("Upload Succeed Message")));
                Log.e(TAG, "Upload succeed.");
                break;
            } catch (NoMatchingViewException e) {
            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }

        timeOut = 0;
        while (timeOut < (MAX_TIME_OUT / 2)) {
            try {
                ViewInteraction appCompatButton = onView(
                        allOf(withId(android.R.id.button1), withText("DONE"),
                                childAtPosition(
                                        childAtPosition(
                                                withClassName(is("android.widget.ScrollView")),
                                                0),
                                        3)));
                appCompatButton.perform(scrollTo(), click());
                break;
            } catch (NoMatchingViewException e) {
            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }

        timeOut = 0;
        while (timeOut < (MAX_TIME_OUT / 2)) {
            try {
                ViewInteraction button = onView(allOf(withId(R.id.buttonDownload)));
                Log.e(TAG, "click Download button");
                button.perform(click());
                break;
            } catch (NoMatchingViewException e) {
            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }

        timeOut = 0;
        while (timeOut < (MAX_TIME_OUT / 2)) {
            try {
                DataInteraction linearLayout = onData(anything())
                        .inAdapterView(allOf(withId(android.R.id.list)))
                        .atPosition(0);
                Log.e(TAG, "Select downloading file from the 1st place.");
                linearLayout.perform(click());
                break;
            } catch (NoMatchingViewException e) {
            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }

        timeOut = 0;
        while (timeOut < MAX_TIME_OUT) {
            try {
                onView(withText(PHOTO_NAME_FOR_TESTING)).check(matches(isDisplayed()));
                Log.e(TAG, "Download succeed.");
                break;
            } catch (NoMatchingViewException e) {
            }
            Thread.sleep(5000);
            timeOut += 5000;
        }
    }

    /**
     * Delete the photo and album created. Then sign out.
     */
    @After
    public void tearDown() {
        try {
            pressBack();

            // Check if it jumps into AlbumActivity
            int timeOut = 0;
            while (timeOut < MAX_TIME_OUT) {
                try {
                    onView(allOf(withText(ALBUM_ACTIVITY_CLASS_NAME), childAtPosition(allOf(withId(R.id.action_bar), childAtPosition(
                            withId(R.id.action_bar_container),
                            0)), 0), isDisplayed()));
                    break;
                } catch (NoMatchingViewException e) {
                } finally {
                    Thread.sleep(5000);
                    timeOut += 5000;
                }
            }

            UIActionsUtil.clickEdit();

            // delete album
            timeOut = 0;
            while (timeOut < MAX_TIME_OUT) {
                try {
                    onView(UIActionsUtil.withIndex(withId(R.id.delete_album), 0)).perform(click());
                    break;
                } catch (NoMatchingViewException e) {
                } finally {
                    Thread.sleep(5000);
                    timeOut += 5000;
                }
            }

            // assert the album has been deleted successfully
            timeOut = 0;
            while (timeOut < MAX_TIME_OUT) {
                try {
                    onView(allOf(withId(R.id.album_name), withText(ALBUM_NAME_FOR_TESTING))).check(doesNotExist());
                    Log.e(TAG, "An album is deleted successfully!");
                    break;
                } catch (NoMatchingViewException e) {
                } finally {
                    Thread.sleep(5000);
                    timeOut += 5000;
                }
            }

            UIActionsUtil.clickSignOut();

            UIActionsUtil.signOut();

            // Check if user successfully signed out
            assertEquals(AWSMobileClient.getInstance().currentUserState().getUserState(), UserState.SIGNED_OUT);

            // Check if goes back to LoginActivity
            timeOut = 0;
            while (timeOut < MAX_TIME_OUT) {
                try {
                    onView(allOf(withId(R.id.user_pool_sign_in_view_id))).check(matches(isDisplayed()));
                    Log.e(TAG,"The view has gone back to LoginActivity.");
                    break;
                } catch (NoMatchingViewException e) {
                }

                Thread.sleep(5000);
                timeOut += 5000;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error on tear down: " + e);
        }
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
            System.out.println("There is no permissions dialog to interact with.");
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

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}