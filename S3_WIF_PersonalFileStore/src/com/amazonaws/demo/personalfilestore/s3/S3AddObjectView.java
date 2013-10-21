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
package com.amazonaws.demo.personalfilestore.s3;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.demo.personalfilestore.R;
import com.amazonaws.demo.personalfilestore.S3PersonalFileStore;

public class S3AddObjectView extends Activity {

    protected String bucketName;
    protected String prefix;

    protected EditText objectName;
    protected EditText objectData;

    protected Button okButton;
    protected Button cancelButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_view);
        Bundle extras = this.getIntent().getExtras();
        bucketName = extras.getString(S3.BUCKET_NAME);
        prefix = extras.getString(S3.PREFIX);

        objectName = (EditText) findViewById(R.id.object_name);        
        objectData = (EditText) findViewById(R.id.object_data);

        okButton = (Button) findViewById(R.id.ok_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);

        wireButtons();
    }

    public void wireButtons() {
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Throwable>() {

                    @Override
                    protected Throwable doInBackground(Void... arg0) {
                        try {
                            String key = S3PersonalFileStore.clientManager.getUsername() + "/"
                                    + objectName.getText().toString();
                            S3.createObjectForBucket(bucketName, key, objectData.getText().toString());
                        } catch (Throwable t) {
                            return t;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Throwable result) {
                        if (result != null) {
                            S3PersonalFileStore.displayAlert(result, S3AddObjectView.this);
                        } else {
                            finish();
                        }
                    }
                }.execute();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
