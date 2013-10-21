/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazonaws.demo.userpreferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

public class FacebookLogin extends AlertActivity {

	/**
	 * For help understanding the Facebook login flow, and objects such as Session and UiLifecycleHelper please see 
	 * Facebook's getting started guide for Android, in particular https://developers.facebook.com/docs/android/login-with-facebook/
	 * is a quick resource to understand this sample.
	 */
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    private static final String LOG_TAG = "FB_LOGIN";
	private UiLifecycleHelper uiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate()");
		setContentView(R.layout.facebook_login);
		uiHelper = new UiLifecycleHelper(this, statusCallback);
		uiHelper.onCreate(savedInstanceState);
    }

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");

		// We do not have a UI for this activity but the Login widget provided
		// by Facebook can still be used to control the session
		Button loginButton = (Button) findViewById(R.id.facebookAuth);

		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened())) {
			onSessionStateChange(session, session.getState(), null);
		} else if (session != null && session.isClosed()) {
			onSessionStateChange(session, session.getState(), null);
			loginButton.performClick();
		} else {
			loginButton.performClick();
		}

		uiHelper.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause");
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy");
		uiHelper.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(LOG_TAG, "onActivityResult");
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(LOG_TAG, "onSaveInstanceState()");
		uiHelper.onSaveInstanceState(outState);
	}
	
	public void onSessionStateChange(Session session, SessionState state, Exception exception){
		if (session.isOpened()) {
            Log.d(LOG_TAG, "session is open");
                UserPreferenceDemoActivity.clientManager.login(new FacebookIDP(),FacebookLogin.this);
                setResult(Activity.RESULT_OK, null);
        }
        else if (state == SessionState.CLOSED_LOGIN_FAILED) {
            setStackAndPost(exception);
        }
	}


    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        	onSessionStateChange(session,state,exception);
        }
    }

    protected class FacebookIDP implements WIFIdentityProvider {

        @Override
        public String getToken() {
            return Session.getActiveSession().getAccessToken();
        }

        @Override
        public String getProviderID() {
            return "graph.facebook.com";
        }

        @Override
        public String getRoleARN() {
            return UserPreferenceDemoActivity.clientManager.getFacebookRoleARN();
        }

        @Override
        public void logout() {
            Session.getActiveSession().closeAndClearTokenInformation();
        }

    }
}
