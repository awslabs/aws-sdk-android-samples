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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The MainActivity - first entrypoint for the application.  Displays
 * a list of notes to the user of the app.
 */
public class MainActivity extends Activity {
    /**
     * The List of Notes
     */
    @BindView(R.id.listView) ListView listView;

    /**
     * The current list of memos
     */
    List<Document> memos;

    /**
     * The date formatter to use when displaying dates
     */
    DateFormat formatter = new SimpleDateFormat("EEEEE MMMMM yyyy HH:mm:ss.SSSZ", Locale.US);

    /**
     * Entry-point and initializer for the activity
     * @param savedInstanceState the bundle from the Intent creating this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Install a custom UncaughtExceptionHandler so we can debug crashes
        CrashHandler.installHandler(this);

        // Bind the activity widgets to the variables
        ButterKnife.bind(this);

        // Install the event handler for clicking on a row
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document memo = memos.get(position);
                TextView txtMemo = (TextView) view.findViewById(R.id.txtMemo);
                txtMemo.setText(shortenString(memo.get("content").asString()));
            }
        });
    }

    /**
     * Lifecycle method - called when the app is resumed (including the first-time start)
     */
    @Override
    protected void onResume() {
        super.onResume();
        GetAllItemsAsyncTask task = new GetAllItemsAsyncTask();
        task.execute();
    }

    /**
     * Event Handler called when the Add button is clicked
     */
    public void onAddClicked(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
    }

    /**
     * Event Handler called when the Delete button is clicked in the list view
     * @param memo The document that was clicked on
     */
    public void onDeleteClicked(Document memo) {
        DeleteItemAsyncTask task = new DeleteItemAsyncTask();
        task.execute(memo);
    }

    /**
     * Event Handler called when the Edit button is clicked in the list view
     * @param memo the document that was clicked on
     */
    public void onEditClicked(Document memo) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("MEMO", memo.get("noteId").asString());
        startActivity(intent);
    }

    /**
     * Helper method to shorten a long note to something reasonable with ellipsis
     * @param text the text to shorten
     * @return the shortened text
     */
    private String shortenString(String text) {
        String temp = text.replaceAll("\n", " ");
        if (temp.length() > 25) {
            return temp.substring(0, 25) + "...";
        } else {
            return temp;
        }
    }

    /**
     * Given a list of memos as Documents, then populate the list view with the list of memos
     * @param memos the list of memos
     */
    protected void populateMemoList(List<Document> memos) {
        this.memos = memos;
        listView.setAdapter(new MemoAdapter(this, memos));
    }

    /**
     * Given a list of memos as Documents, remove the memos from the list of memos
     */
    protected void removeDocumentsFromMemoList(List<Document> memos) {
        MemoAdapter adapter = (MemoAdapter) listView.getAdapter();
        for (Document memo : memos) {
            adapter.remove(memo);
            memos.remove(memo);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Helper method to display the stored time (in milliseconds since the epoch) as
     * a human-readable string.
     * @param milliseconds the # of milliseconds since the epoch
     * @return a printable date
     */
    private String asDateString(long milliseconds) {
        Date date = new Date(milliseconds);
        return formatter.format(date);
    }

    /**
     * Async Task for handling the network retrieval of all the memos in DynamoDB
     */
    private class GetAllItemsAsyncTask extends AsyncTask<Void, Void, List<Document>> {
        @Override
        protected List<Document> doInBackground(Void... params) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MainActivity.this);
            return databaseAccess.getAllMemos();
        }

        @Override
        protected void onPostExecute(List<Document> documents) {
            if (documents != null) {
                populateMemoList(documents);
            }
        }
    }

    /**
     * Async Task for handling the network deletion of a memo within DynamoDB
     */
    private class DeleteItemAsyncTask extends AsyncTask<Document, Void, List<Document>> {
        @Override
        protected List<Document> doInBackground(Document... documents) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(MainActivity.this);
            ArrayList<Document> deletedDocuments = new ArrayList<>();
            for (Document memo : documents) {
                databaseAccess.delete(memo);
                deletedDocuments.add(memo);
            }
            return deletedDocuments;
        }

        @Override
        protected void onPostExecute(List<Document> documents) {
            removeDocumentsFromMemoList(documents);
        }
    }

    private class MemoAdapter extends ArrayAdapter<Document> {
        MemoAdapter(Context context, List<Document> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_list_item, parent, false);
            }

            ImageView btnEdit = (ImageView) convertView.findViewById(R.id.btnEdit);
            ImageView btnDelete = (ImageView) convertView.findViewById(R.id.btnDelete);
            TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
            TextView txtMemo = (TextView) convertView.findViewById(R.id.txtMemo);

            // Fill in the text fields
            final Document memo = memos.get(position);
            txtDate.setText(asDateString(memo.get("creationDate").asLong()));
            txtMemo.setText(shortenString(memo.get("content").asString()));

            // Wire up the event handlers for the buttons
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEditClicked(memo);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeleteClicked(memo);
                }
            });

            return convertView;
        }
    }
}
