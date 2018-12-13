/**
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * <p>
 * http://aws.amazon.com/apache2.0
 * <p>
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.demo.androidpubsubwebsocket;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class PubSubActivity extends Activity {

    static final String LOG_TAG = PubSubActivity.class.getCanonicalName();

    // --- Constants to modify per your configuration ---

    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "REPLACE_ME";

    EditText txtSubscribe;
    EditText txtTopic;
    EditText txtMessage;

    TextView tvLastMessage;
    TextView tvClientId;
    TextView tvStatus;

    Button btnConnect;

    AWSIotMqttManager mqttManager;
    String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSubscribe = findViewById(R.id.txtSubscribe);
        txtTopic = findViewById(R.id.txtTopic);
        txtMessage = findViewById(R.id.txtMessage);

        tvLastMessage = findViewById(R.id.tvLastMessage);
        tvClientId = findViewById(R.id.tvClientId);
        tvStatus = findViewById(R.id.tvStatus);

        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setEnabled(false);

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString();
        tvClientId.setText(clientId);

        // Initialize the credentials provider
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(
                getApplicationContext(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        latch.countDown();
                        Log.e(LOG_TAG, "onError: ", e);
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_IOT_ENDPOINT);

        // Enable button once all clients are ready
        btnConnect.setEnabled(true);
    }

    public void connect(final View view) {
        Log.d(LOG_TAG, "clientId = " + clientId);

        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText(status.toString());
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error.", throwable);
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
            tvStatus.setText("Error! " + e.getMessage());
        }
    }

    public void subscribe(final View view) {
        final String topic = txtSubscribe.getText().toString();

        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                        Log.d(LOG_TAG, " Message: " + message);

                                        tvLastMessage.setText(message);

                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }

    public void publish(final View view) {
        final String topic = txtTopic.getText().toString();
        final String msg = txtMessage.getText().toString();

        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }
    }

    public void disconnect(final View view) {
        try {
            mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error.", e);
        }
    }
}
