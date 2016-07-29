/*
 *  Copyright 2013-2016 Amazon.com,
 *  Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package com.amazonaws.youruserpools;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

public class MFAActivity extends AppCompatActivity {
    private TextView mfaScreenText;
    private EditText mfaInput;
    private Button sendMfa;
    private AlertDialog userDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mfa);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_mfa);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView main_title = (TextView) findViewById(R.id.mfa_toolbar_title);
        main_title.setText("Verification");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit(null);
            }
        });

        init();
    }

    @Override
    public void onBackPressed() {
        exit(null);
    }

    public void sendMFA(View view) {
        getCode();
    }

    private void init(){
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            if(extras.containsKey("mode")) {
                String mode = extras.getString("mode");
                mfaScreenText = (TextView) findViewById(R.id.textViewMFASubTitle);
                mfaScreenText.setText("Verification code has been sent via "+mode);
            }
        }

        mfaInput = (EditText) findViewById(R.id.editTextMFACode);
        mfaInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewMFACodeLabel);
                    label.setText(mfaInput.getHint());
                    mfaInput.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewMFACodeMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewMFACodeLabel);
                    label.setText("");
                }
            }
        });
    }

    private void getCode() {
        String MFACode = mfaInput.getText().toString();

        if (MFACode == null || MFACode.length() < 1) {
            TextView label = (TextView) findViewById(R.id.textViewMFACodeMessage);
            label.setText(mfaInput.getHint() + " cannot be empty");
            mfaInput.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }
        exit(MFACode);
    }

    private void exit(String MFACode) {
        Intent intent = new Intent();
        if(MFACode == null)
            MFACode = "";
        intent.putExtra("mfacode", MFACode);
        setResult(RESULT_OK, intent);
        finish();
    }
}
