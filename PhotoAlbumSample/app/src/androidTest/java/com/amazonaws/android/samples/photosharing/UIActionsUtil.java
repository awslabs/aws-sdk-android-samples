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

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.CountDownLatch;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

public class UIActionsUtil {

    private static final String username = "test01";
    private static final String password = "The#test1";
    private static final String AUTH_FORM_VIEW = "com.amazonaws.mobile.auth.userpools.FormView";
    private static final String AUTH_FORM_EDIT_TEXT = "com.amazonaws.mobile.auth.userpools.FormEditText";
    private static final String AUTH_UI_SIGN_VIEW = "com.amazonaws.mobile.auth.ui.SignInView";
    private static final int MAX_TIME_OUT = 60 * 1000;
    private static final String TAG = UIActionsUtil.class.getSimpleName();

    public static void signIn(String username, String password) throws InterruptedException {
        int timeOut = 0;
        while (timeOut < MAX_TIME_OUT) {
            try {
                onView(allOf(withText("Sign-in"), childAtPosition(allOf(withId(R.id.action_bar), childAtPosition(
                        withId(R.id.action_bar_container),
                        0)), 0), isDisplayed()));
                ViewInteraction editText = onView(
                        allOf(childAtPosition(
                                childAtPosition(
                                        withClassName(is(AUTH_FORM_VIEW)),
                                        0),
                                1),
                                isDisplayed()));
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
                ViewInteraction editText = onView(
                        allOf(childAtPosition(
                                childAtPosition(
                                        withClassName(is(AUTH_FORM_VIEW)),
                                        0),
                                1),
                                isDisplayed()));
                Log.e(TAG, "Click Drop-in UI form.");
                editText.perform(click());

                // Enter username
                ViewInteraction editText2 = onView(
                        allOf(childAtPosition(
                                childAtPosition(
                                        withClassName(is(AUTH_FORM_VIEW)),
                                        0),
                                1),
                                isDisplayed()));
                Log.e(TAG, "Enter username.");
                editText2.perform(replaceText(username), closeSoftKeyboard());

                // Enter password
                ViewInteraction editText3 = onView(
                        allOf(childAtPosition(
                                childAtPosition(
                                        withClassName(is(AUTH_FORM_EDIT_TEXT)),
                                        1),
                                0),
                                isDisplayed()));
                Log.e(TAG, "Enter password.");
                editText3.perform(replaceText(password), closeSoftKeyboard());

                // Click sign in button
                ViewInteraction button = onView(
                        allOf(withText("Sign In"),
                                childAtPosition(
                                        allOf(withId(R.id.user_pool_sign_in_view_id),
                                                childAtPosition(
                                                        withClassName(is(AUTH_UI_SIGN_VIEW)),
                                                        1)),
                                        1),
                                isDisplayed()));
                Log.e(TAG, "Click sign in button.");
                button.perform(click());

                break;
            } catch (NoMatchingViewException e) {

            } finally {
                Thread.sleep(5000);
                timeOut += 5000;
            }
        }
    }

    public static void addAlbum(String albumName) {
        ViewInteraction addAlbumButton = onView(allOf(withId(R.id.album_add)));
        Log.e(TAG, "Click button to add an album - do AppSync create mutation.");
        addAlbumButton.perform(click());

        ViewInteraction editText = onView(
                allOf(UIActionsUtil.childAtPosition(
                        allOf(withId(R.id.custom),
                                UIActionsUtil.childAtPosition(
                                        withId(R.id.customPanel),
                                        0)),
                        0),
                        isDisplayed()));
        Log.e(TAG, "Enter album name.");
        editText.perform(replaceText(albumName), closeSoftKeyboard());

        ViewInteraction confirmButton = onView(
                allOf(withId(android.R.id.button1), withText("Add"),
                        UIActionsUtil.childAtPosition(
                                UIActionsUtil.childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        Log.e(TAG, "Click confirm button.");
        confirmButton.perform(click());
    }

    public static void clickSignOut() {
        // Click sign out button
        ViewInteraction signOutButton = onView(
                allOf(withId(R.id.album_sign_out)));
        Log.e(TAG,"Click sign out button.");
        signOutButton.perform(click());
    }

    public static void clickEdit() {
        // Click edit button
        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.album_edit), withContentDescription("Edit"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                0),
                        isDisplayed()));
        Log.e(TAG, "Click edit button.");
        actionMenuItemView2.perform(click());
    }

    public static void clickUploadPhoto() {
        // Click upload photo button
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.photo_add), withContentDescription("Add photo"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                1),
                        isDisplayed()));
        Log.e(TAG, "Click upload photo button.");
        actionMenuItemView.perform(click());
    }

    public static void signOut() {
        // Global sign out for tear down
        try {
            final CountDownLatch signOutLatch = new CountDownLatch(1);
            // Global sign out
            AWSMobileClient.getInstance().signOut(SignOutOptions.builder().invalidateTokens(true).signOutGlobally(true).build(), new Callback<Void>() {
                @Override
                public void onResult(Void result) {
                    signOutLatch.countDown();
                }

                @Override
                public void onError(Exception e) {
                    signOutLatch.countDown();
                }
            });
            signOutLatch.await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static Matcher<View> childAtPosition (
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

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}
