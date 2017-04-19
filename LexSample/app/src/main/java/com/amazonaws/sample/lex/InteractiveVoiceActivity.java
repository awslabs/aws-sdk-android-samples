
package com.amazonaws.sample.lex;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.lex.interactionkit.Response;
import com.amazonaws.mobileconnectors.lex.interactionkit.config.InteractionConfig;
import com.amazonaws.mobileconnectors.lex.interactionkit.ui.InteractiveVoiceView;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;

import java.util.Locale;
import java.util.Map;

public class InteractiveVoiceActivity extends Activity
        implements InteractiveVoiceView.InteractiveVoiceListener {
    private static final String TAG = "VoiceActivity";
    private Context appContext;
    private InteractiveVoiceView voiceView;
    private TextView transcriptTextView;
    private TextView responseTextView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive_voice);
        transcriptTextView = (TextView) findViewById(R.id.transcriptTextView);
        responseTextView = (TextView) findViewById(R.id.responseTextView);
        init();
        StringUtils.isBlank("notempty");
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void init() {
        appContext = getApplicationContext();
        voiceView = (InteractiveVoiceView) findViewById(R.id.voiceInterface);
        voiceView.setInteractiveVoiceListener(this);
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider(
                appContext.getResources().getString(R.string.identity_id_test),
                Regions.fromName(appContext.getResources().getString(R.string.aws_region)));
        voiceView.getViewAdapter().setCredentialProvider(credentialsProvider);
        voiceView.getViewAdapter().setInteractionConfig(
                new InteractionConfig(appContext.getString(R.string.bot_name),
                        appContext.getString(R.string.bot_alias)));
        voiceView.getViewAdapter().setAwsRegion(appContext.getString(R.string.aws_region));
    }

    private void exit() {
        finish();
    }

    @Override
    public void dialogReadyForFulfillment(final Map<String, String> slots, final String intent) {
        Log.d(TAG, String.format(
                Locale.US,
                "Dialog ready for fulfillment:\n\tIntent: %s\n\tSlots: %s",
                intent,
                slots.toString()));
    }

    @Override
    public void onResponse(Response response) {
        Log.d(TAG, "Bot response: " + response.getTextResponse());
        Log.d(TAG, "Transcript: " + response.getInputTranscript());

        responseTextView.setText(response.getTextResponse());
        transcriptTextView.setText(response.getInputTranscript());
    }

    @Override
    public void onError(final String responseText, final Exception e) {
        Log.e(TAG, "Error: " + responseText, e);
    }
}
