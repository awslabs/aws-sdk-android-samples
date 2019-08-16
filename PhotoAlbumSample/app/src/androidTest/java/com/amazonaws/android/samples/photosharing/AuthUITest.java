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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignInState;
import com.amazonaws.mobile.config.AWSConfiguration;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AuthUITest {

    private final static String ALBUM_ACTIVITY_CLASS_NAME = AlbumActivity.class.getSimpleName();
    private static final String TAG = AuthUITest.class.getSimpleName();
    private static final int MAX_TIME_OUT = 60 * 1000;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void setUp() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        UIActionsUtil.signOut();
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(appContext, new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails result) {
                Log.i(TAG, "onResult: " + result.getUserState());
                // Check whether user is signed out
                assertEquals(result.getUserState(), UserState.SIGNED_OUT);
                Log.e(TAG, "Now the userstate is SIGNED_OUT!");
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: " + e);
                latch.countDown();
            }
        });
        latch.await();

        AWSMobileClient.getInstance().addUserStateListener(new UserStateListener() {
            @Override
            public void onUserStateChanged(UserStateDetails details) {
                Log.i(TAG, "User state has been changed.");
            }
        });

        final AWSConfiguration awsConfiguration = AWSMobileClient.getInstance().getConfiguration();

        // AWS Configuration file should have credential provider and cognito user pools configured.
        assertNotNull(awsConfiguration.optJsonObject("CognitoUserPool"));
        Log.e(TAG, "Cognito User Pool has been configured.");

        assertNotNull(awsConfiguration.optJsonObject("CredentialsProvider"));
        Log.e(TAG, "Credentials Provider has been configured.");
    }

    /**
     * This test first do sign in, then sign out.
     */
    @Test
    public void testSignInAndSignOut() throws InterruptedException {

        // Do sign in
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
        int timeOut = 0;
        while (timeOut < MAX_TIME_OUT) {
            try {
                onView(allOf(withText(ALBUM_ACTIVITY_CLASS_NAME), childAtPosition(allOf(withId(R.id.action_bar), childAtPosition(
                        withId(R.id.action_bar_container),
                        0)), 0), isDisplayed()));
                break;
            } catch (NoMatchingViewException e) {
                Log.e(TAG, "Can not find matching view: " + e);
            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }

        // Do sign out
        UIActionsUtil.clickSignOut();

        UIActionsUtil.signOut();
        // Check if goes back to LoginActivity
        timeOut = 0;
        while (timeOut < MAX_TIME_OUT) {
            try {
                ViewInteraction button2 = onView(allOf(withId(R.id.user_pool_sign_in_view_id)));
                button2.check(matches(isDisplayed()));
                Log.e(TAG,"The view has gone back to LoginActivity.");
                break;
            } catch (NoMatchingViewException e) {
                Log.e(TAG, "Can not find matching view: " + e);
            }

            Thread.sleep(5000);
            timeOut += 5000;
        }
    }

    @After
    public void tearDown() {
        try {
            UIActionsUtil.signOut();

            // Check if user successfully signed out
            assertEquals(AWSMobileClient.getInstance().currentUserState().getUserState(), UserState.SIGNED_OUT);

        } catch (Exception e) {
            Log.e(TAG, "Error on tear down: " + e);
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
