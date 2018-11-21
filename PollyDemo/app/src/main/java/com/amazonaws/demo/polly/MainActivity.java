/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.polly;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "PollyDemo";

    private static final String KEY_SELECTED_VOICE_POSITION = "SelectedVoicePosition";
    private static final String KEY_VOICES = "Voices";
    private static final String KEY_SAMPLE_TEXT = "SampleText";
    SpinnerVoiceAdapter spinnerVoiceAdapter;
    View progressBar;
    // Media player
    MediaPlayer mediaPlayer;
    // Backend resources
    private AmazonPollyPresigningClient client;
    private List<Voice> voices;
    // UI
    private Spinner voicesSpinner;
    private EditText sampleTextEditText;
    private Button playButton;
    private ImageButton defaultTextButton;
    private int selectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");

        playButton = findViewById(R.id.readButton);
        progressBar = findViewById(R.id.voicesProgressBar);
        voicesSpinner = findViewById(R.id.voicesSpinner);
        defaultTextButton = findViewById(R.id.defaultTextButton);
        sampleTextEditText = findViewById(R.id.sampleText);

        progressBar.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
        setupVoiceSpinner();
        initPollyClient();
        setupNewMediaPlayer();
        setupSampleTextEditText();
        setupDefaultTextButton();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_VOICE_POSITION, voicesSpinner.getSelectedItemPosition());
        outState.putSerializable(KEY_VOICES, (Serializable) voices);
        outState.putString(KEY_SAMPLE_TEXT, sampleTextEditText.getText().toString());

        super.onSaveInstanceState(outState);
    }

    void restoreInstanceState(Bundle savedInstanceState) {
        selectedPosition = savedInstanceState.getInt(KEY_SELECTED_VOICE_POSITION);
        voices = (List<Voice>) savedInstanceState.getSerializable(KEY_VOICES);

        String sampleText = savedInstanceState.getString(KEY_SAMPLE_TEXT);
        if (sampleText.isEmpty()) {
            defaultTextButton.setVisibility(View.GONE);
        } else {
            sampleTextEditText.setText(sampleText);
            defaultTextButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupVoiceSpinner() {
        spinnerVoiceAdapter = new SpinnerVoiceAdapter(MainActivity.this, new ArrayList<Voice>());
        voicesSpinner.setAdapter(spinnerVoiceAdapter);
        voicesSpinner.setVisibility(View.VISIBLE);

        voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    setDefaultTextForSelectedVoice();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    void initPollyClient() {
        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                // Create a client that supports generation of presigned URLs.
                client = new AmazonPollyPresigningClient(AWSMobileClient.getInstance());
                Log.d(TAG, "onResult: Created polly pre-signing client");

                if (voices == null) {
                    // Create describe voices request.
                    DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

                    try {
                        // Synchronously ask the Polly Service to describe available TTS voices.
                        DescribeVoicesResult describeVoicesResult = client.describeVoices(describeVoicesRequest);

                        // Get list of voices from the result.
                        voices = describeVoicesResult.getVoices();

                        // Log a message with a list of available TTS voices.
                        Log.i(TAG, "Available Polly voices: " + voices);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Unable to get available voices.", e);
                        return;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinnerVoiceAdapter.setVoices(voices);
                        voicesSpinner.setSelection(selectedPosition);
                        progressBar.setVisibility(View.INVISIBLE);
                        playButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Initialization error", e);
            }
        });
    }

    void setupSampleTextEditText() {
        sampleTextEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                defaultTextButton.setVisibility(sampleTextEditText.getText().toString().isEmpty() ?
                        View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        sampleTextEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                playButton.performClick();
                return false;
            }
        });
    }

    void setupNewMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                setupNewMediaPlayer();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                playButton.setEnabled(true);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                playButton.setEnabled(true);
                return false;
            }
        });
    }

    String getSampleText(Voice voice) {
        if (voice == null) {
            return "";
        }

        String resourceName = "sample_" +
                voice.getLanguageCode().replace("-", "_").toLowerCase() + "_" +
                voice.getId().toLowerCase();
        int sampleTextResourceId =
                getResources().getIdentifier(resourceName, "string", getPackageName());
        if (sampleTextResourceId == 0)
            return "";

        return getString(sampleTextResourceId);
    }

    void setDefaultTextForSelectedVoice() {
        Voice selectedVoice = (Voice) voicesSpinner.getSelectedItem();
        if (selectedVoice == null) {
            return;
        }

        String sampleText = getSampleText(selectedVoice);

        sampleTextEditText.setHint(sampleText);
    }

    void setupDefaultTextButton() {
        defaultTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sampleTextEditText.setText(null);
            }
        });
    }

    public void playVoice(View view) {
        playButton.setEnabled(false);

        Voice selectedVoice = (Voice) voicesSpinner.getSelectedItem();

        String textToRead = sampleTextEditText.getText().toString();

        // Use voice's sample text if user hasn't provided any text to read.
        if (textToRead.trim().isEmpty()) {
            textToRead = getSampleText(selectedVoice);
        }

        // Create speech synthesis request.
        SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                new SynthesizeSpeechPresignRequest()
                        // Set text to synthesize.
                        .withText(textToRead)
                        // Set voice selected by the user.
                        .withVoiceId(selectedVoice.getId())
                        // Set format to MP3.
                        .withOutputFormat(OutputFormat.Mp3);

        // Get the presigned URL for synthesized speech audio stream.
        URL presignedSynthesizeSpeechUrl =
                client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

        Log.i(TAG, "Playing speech from presigned URL: " + presignedSynthesizeSpeechUrl);

        // Create a media player to play the synthesized audio stream.
        if (mediaPlayer.isPlaying()) {
            setupNewMediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Set media player's data source to previously obtained URL.
            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
        } catch (IOException e) {
            Log.e(TAG, "Unable to set data source for the media player! " + e.getMessage());
        }

        // Start the playback asynchronously (since the data source is a network stream).
        mediaPlayer.prepareAsync();
    }
}
