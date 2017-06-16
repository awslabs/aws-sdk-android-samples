/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
 * in compliance with the License. A copy of the License is located at
 *
 *   http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.amazonaws.samples.notes;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import butterknife.BindView;
import butterknife.ButterKnife;import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;

/**
 * Activity that displays the Edit Memo Screen
 */
public class EditActivity extends Activity {
    /**
     * The Text Editor
     */
    @BindView(R.id.etText) EditText etText;

    /**
     * The Memo being edited
     */
    private Document memo;

    /**
     * Lifecycle method called when the activity is created
     * @param savedInstanceState the bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Bind the view variables
        ButterKnife.bind(this);

        // If this activity was passed an intent, then receive the noteId of
        // the memo to edit and populate the UI with the content
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            GetItemAsyncTask task = new GetItemAsyncTask();
            task.execute((String) bundle.get("MEMO"));
        }
    }

    /**
     * Event Handler called when the Save button is clicked
     * @param view the initiating view
     */
    public void onSaveClicked(View view) {
        if (memo == null) {
            Document newMemo = new Document();
            newMemo.put("content", etText.getText().toString());
            CreateItemAsyncTask task = new CreateItemAsyncTask();
            task.execute(newMemo);
        } else {
            memo.put("content", etText.getText().toString());
            UpdateItemAsyncTask task = new UpdateItemAsyncTask();
            task.execute(memo);
        }
        // Finish this activity and return to the prior activity
        this.finish();
    }

    /**
     * Event Handler called when the Cancel button is clicked
     * @param view the initiating view
     */
    public void onCancelClicked(View view) {
        this.finish();
    }

    /**
     * Async Task to retrieve a Memo by its noteId from the DynamoDB table
     */
    private class GetItemAsyncTask extends AsyncTask<String, Void, Document> {
        @Override
        protected Document doInBackground(String... documents) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditActivity.this);
            return databaseAccess.getMemoById(documents[0]);
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result != null) {
                memo = result;
                etText.setText(memo.get("content").asString());
            }
        }
    }

    /**
     * Async Task to create a new memo into the DynamoDB table
     */
    private class CreateItemAsyncTask extends AsyncTask<Document, Void, Void> {
        @Override
        protected Void doInBackground(Document... documents) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditActivity.this);
            databaseAccess.create(documents[0]);
            return null;
        }
    }

    /**
     * Async Task to save an existing memo into the DynamoDB table
     */
    private class UpdateItemAsyncTask extends AsyncTask<Document, Void, Void> {
        @Override
        protected Void doInBackground(Document... documents) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(EditActivity.this);
            databaseAccess.update(documents[0]);
            return null;
        }
    }
}
