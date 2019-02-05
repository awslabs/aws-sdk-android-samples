/*
 * Copyright 2016-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.sample.lex;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.lex.interactionkit.InteractionClient;
import com.amazonaws.mobileconnectors.lex.interactionkit.config.InteractionConfig;
import com.amazonaws.mobileconnectors.lex.interactionkit.Response;
import com.amazonaws.mobileconnectors.lex.interactionkit.continuations.LexServiceContinuation;
import com.amazonaws.mobileconnectors.lex.interactionkit.listeners.AudioPlaybackListener;
import com.amazonaws.mobileconnectors.lex.interactionkit.listeners.InteractionListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lexrts.model.DialogState;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class TextActivity extends Activity {
    private static final String TAG = "TextActivity";
    /**
     * Implementing {@link AudioPlaybackListener}.
     */
    final AudioPlaybackListener audioPlaybackListener = new AudioPlaybackListener() {
        @Override
        public void onAudioPlaybackStarted() {
            Log.d(TAG, " -- Audio playback started");
        }

        @Override
        public void onAudioPlayBackCompleted() {
            Log.d(TAG, " -- Audio playback ended");
        }

        @Override
        public void onAudioPlaybackError(Exception e) {
            Log.d(TAG, " -- Audio playback error", e);
        }
    };
    private EditText userTextInput;
    private Context appContext;
    private InteractionClient lexInteractionClient;
    private boolean inConversation;
    private LexServiceContinuation convContinuation;
    final InteractionListener interactionListener = new InteractionListener() {
        @Override
        public void onReadyForFulfillment(final Response response) {
            Log.d(TAG, "Transaction completed successfully");
            addMessage(new TextMessage(response.getTextResponse(), "rx", getCurrentTimeStamp()));
            inConversation = false;
        }

        @Override
        public void promptUserToRespond(final Response response,
                                        final LexServiceContinuation continuation) {
            addMessage(new TextMessage(response.getTextResponse(), "rx", getCurrentTimeStamp()));
            readUserText(continuation);
        }

        @Override
        public void onInteractionError(final Response response, final Exception e) {
            if (response != null) {
                if (DialogState.Failed.toString().equals(response.getDialogState())) {
                    addMessage(new TextMessage(response.getTextResponse(), "rx",
                            getCurrentTimeStamp()));
                    inConversation = false;
                } else {
                    addMessage(new TextMessage("Please retry", "rx", getCurrentTimeStamp()));
                }
            } else {
                showToast("Error: " + e.getMessage());
                Log.e(TAG, "Interaction error", e);
                inConversation = false;
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        init();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Initializes the application.
     */
    private void init() {
        Log.d(TAG, "Initializing text component: ");
        appContext = getApplicationContext();
        userTextInput = (EditText) findViewById(R.id.userInputEditText);

        // Set text edit listener.
        userTextInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    textEntered();
                    return true;
                }
                return false;
            }
        });
        userTextInput.setEnabled(false);

        initializeLexSDK();
        startNewConversation();
    }

    /**
     * Initializes Lex client.
     */
    private void initializeLexSDK() {
        Log.d(TAG, "Lex Client");

        // Initialize the mobile client
        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.d(TAG, "initialize.onResult, userState: " + result.getUserState().toString());

                // Identity ID is not available until we make a call to get credentials, which also
                // caches identity ID.
                AWSMobileClient.getInstance().getCredentials();

                String identityId = AWSMobileClient.getInstance().getIdentityId();
                Log.d(TAG, "identityId: " + identityId);
                String botName = null;
                String botAlias = null;
                String botRegion = null;
                JSONObject lexConfig;
                try {
                    lexConfig = AWSMobileClient.getInstance().getConfiguration().optJsonObject("Lex");
                    lexConfig = lexConfig.getJSONObject(lexConfig.keys().next());

                    botName = lexConfig.getString("Name");
                    botAlias = lexConfig.getString("Alias");
                    botRegion = lexConfig.getString("Region");
                } catch (JSONException e) {
                    Log.e(TAG, "onResult: Failed to read configuration", e);
                }

                InteractionConfig lexInteractionConfig = new InteractionConfig(
                        botName,
                        botAlias,
                        identityId);

                lexInteractionClient = new InteractionClient(getApplicationContext(),
                        AWSMobileClient.getInstance(),
                        Regions.fromName(botRegion),
                        lexInteractionConfig);

                lexInteractionClient.setAudioPlaybackListener(audioPlaybackListener);
                lexInteractionClient.setInteractionListener(interactionListener);
                userTextInput.setEnabled(true);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "initialize.onError: ", e);
            }
        });
    }

    /**
     * Read user text input.
     */
    private void textEntered() {
        String text = userTextInput.getText().toString();
        if (!inConversation) {
            Log.d(TAG, " -- New conversation started");
            startNewConversation();
            addMessage(new TextMessage(text, "tx", getCurrentTimeStamp()));
            lexInteractionClient.textInForTextOut(text, null);
            inConversation = true;
        } else {
            Log.d(TAG, " -- Responding with text: " + text);
            addMessage(new TextMessage(text, "tx", getCurrentTimeStamp()));
            convContinuation.continueWithTextInForTextOut(text);
        }
        clearTextInput();
    }

    /**
     * Pass user input to Lex client.
     *
     * @param continuation
     */
    private void readUserText(final LexServiceContinuation continuation) {
        convContinuation = continuation;
        inConversation = true;
    }

    /**
     * Clears the current conversation history and closes the current request.
     */
    private void startNewConversation() {
        Log.d(TAG, "Starting new conversation");
        Conversation.clear();
        inConversation = false;
        clearTextInput();
    }

    /**
     * Clear text input field.
     */
    private void clearTextInput() {
        userTextInput.setText("");
    }

    /**
     * Show the text message on the screen.
     *
     * @param message
     */
    private void addMessage(final TextMessage message) {
        Conversation.add(message);
        final MessagesListAdapter listAdapter = new MessagesListAdapter(getApplicationContext());
        final ListView messagesListView = (ListView) findViewById(R.id.conversationListView);
        messagesListView.setDivider(null);
        messagesListView.setAdapter(listAdapter);
        messagesListView.setSelection(listAdapter.getCount() - 1);
    }

    /**
     * Current time stamp.
     *
     * @return
     */
    private String getCurrentTimeStamp() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    /**
     * Show a toast.
     *
     * @param message - Message text for the toast.
     */
    private void showToast(final String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        Log.d(TAG, message);
    }
}
