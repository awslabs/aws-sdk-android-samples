/**
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.cognito.sync.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.android.cognito.Dataset;

public class EditRecordActivity extends Activity {

    public static final String KEY_ACTION = "action";
    public static final String KEY_DATASET_NAME = "dataset_name";
    public static final String KEY_RECORD_KEY = "key";
    public static final String KEY_RECORD_VALUE = "value";

    private Dataset dataset;

    private String action;
    private String datasetName;
    private String key;
    private String value;

    private TextView tvTitle;
    private EditText etKey;
    private EditText etValue;
    private Button btnSave;
    private Button btnRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        action = bundle.getString(KEY_ACTION);
        datasetName = bundle.getString(KEY_DATASET_NAME);
        key = bundle.getString(KEY_RECORD_KEY);
        value = bundle.getString(KEY_RECORD_VALUE);

        dataset = CognitoSyncClientManager.getInstance().openOrCreateDataset(datasetName);

        tvTitle = (TextView) findViewById(R.id.tvEditTitle);
        etKey = (EditText) findViewById(R.id.etKey);
        etValue = (EditText) findViewById(R.id.etValue);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                key = etKey.getText().toString();
                value = etValue.getText().toString();
                dataset.put(key, value);
                finish();
            }
        });

        btnRemove = (Button) findViewById(R.id.btnRemove);
        btnRemove.setEnabled(action.equals("edit"));
        btnRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                key = etKey.getText().toString();
                dataset.remove(key);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_ACTION, action);
        outState.putString(KEY_DATASET_NAME, datasetName);
        key = etValue.getText().toString();
        outState.putString(KEY_RECORD_KEY, key);
        value = etValue.getText().toString();
        outState.putSerializable(KEY_RECORD_VALUE, value);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (action.equals("new")) {
            tvTitle.setText("New Record");
            etKey.setEnabled(true);
            if (key != null) {
                etKey.setText(key);
            }
            if (value != null) {
                etValue.setText(value);
            }
        } else if (action.equals("edit")) {
            tvTitle.setText("Edit Record");
            etKey.setText(key);
            etKey.setEnabled(false);
            if (value == null) {
                value = dataset.get(key);
            }
            etValue.setText(value);
        }
    }
}
