
package com.amazonaws.sample.lex;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.sample.lex.R;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Button textDemoButton;
    private Button speechDemoButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public void onBackPressed() {
        finish();
        moveTaskToBack(true);
    }

    /**
     * Initializes the application.
     */
    private void init() {
        Log.e(TAG, "Initializing app");
        textDemoButton = (Button) findViewById(R.id.button_select_text);
        speechDemoButton = (Button) findViewById(R.id.button_select_voice);
        textDemoButton.setOnClickListener(this);
        speechDemoButton.setOnClickListener(this);
    }

    /**
     * On-click listener for buttons text and voice buttons.
     *
     * @param v {@link View}, instance of the button component.
     */
    @Override
    public void onClick(final View v) {
        switch ((v.getId())) {
            case R.id.button_select_text:
                Intent textIntent = new Intent(this, TextActivity.class);
                startActivity(textIntent);
                break;
            case R.id.button_select_voice:
                Intent voiceIntent = new Intent(this, InteractiveVoiceActivity.class);
                startActivity(voiceIntent);
                break;
        }
    }
}
