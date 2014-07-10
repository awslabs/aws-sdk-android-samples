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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.android.cognito.CognitoSyncClient;
import com.amazonaws.android.cognito.DatasetMetadata;
import com.amazonaws.android.cognito.exceptions.DataStorageException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ListDatasetsActivity extends ListActivity {

    private static final String TAG = "ListDatasetsActivity";

    private DatasetsAdapter adapter;
    private CognitoSyncClient client;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        client = CognitoSyncClientManager.getInstance();

        // add header
        View header = getLayoutInflater()
                .inflate(R.layout.record_list_item, getListView(), false);
        getListView().addHeaderView(header, null, false);
        ((TextView) header.findViewById(R.id.tvKey)).setText("name");
        ((TextView) header.findViewById(R.id.tvValue)).setText("last modified date");
        ((TextView) header.findViewById(R.id.tvSyncCount)).setText("count");

        adapter = new DatasetsAdapter(this, R.layout.record_list_item);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DatasetMetadata dataset = adapter.getItem(position - 1);

                Intent intent = new Intent(ListDatasetsActivity.this, ListRecordsActivity.class);
                intent.putExtra(ListRecordsActivity.KEY_DATASET_NAME, dataset.getDatasetName());
                startActivity(intent);
            }
        });

        tvTitle = ((TextView) findViewById(R.id.tvTitle));

        findViewById(R.id.btnSync).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDatasetMetadata();
            }
        });

        findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set up the input
                final EditText input = new EditText(ListDatasetsActivity.this);
                input.setSingleLine(true);

                new AlertDialog.Builder(ListDatasetsActivity.this)
                        .setTitle("Add new dataset")
                        .setView(input)

                        // Set up the buttons
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String datasetName = input.getText().toString();
                                if (datasetName != null && !datasetName.trim().isEmpty()) {
                                    client.openOrCreateDataset(datasetName);
                                    refreshListData();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        refreshDatasetMetadata();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshListData();
    }

    private void refreshListData() {
        List<DatasetMetadata> datasets = client.listDatasets();
        adapter.clear();
        for (DatasetMetadata dataset : datasets) {
            adapter.add(dataset);
        }
        adapter.notifyDataSetChanged();

        tvTitle.setText("Datasets");
    }

    private void refreshDatasetMetadata() {
        new RefreshDatasetMetadataTask().execute();
    }

    private class RefreshDatasetMetadataTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(ListDatasetsActivity.this,
                    "Syncing", "Please wait");
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                client.refreshDatasetMetadata();
            } catch (DataStorageException dse) {
                Log.e(TAG, "failed to fresh dataset metadata", dse);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            refreshListData();
        }
    }

    /**
     * A list adapter for datasets
     */
    private class DatasetsAdapter extends ArrayAdapter<DatasetMetadata> {
        DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        public DatasetsAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.record_list_item,
                        parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DatasetMetadata dataset = getItem(position);
            // dataset name
            holder.tvKey.setText(dataset.getDatasetName());
            // if the dataset is deleted, put a strike
            holder.tvKey.setPaintFlags(holder.tvKey.getPaintFlags()
                    & ~Paint.STRIKE_THRU_TEXT_FLAG);
            // last modified
            holder.tvValue.setText(df.format(dataset.getLastModifiedDate()));
            holder.tvSyncCount.setText(String.valueOf(dataset.getRecordCount()));

            return convertView;
        }
    }

    static class ViewHolder {
        TextView tvKey;
        TextView tvValue;
        TextView tvSyncCount;

        public ViewHolder(View rootView) {
            tvKey = (TextView) rootView.findViewById(R.id.tvKey);
            tvValue = (TextView) rootView.findViewById(R.id.tvValue);
            tvSyncCount = (TextView) rootView.findViewById(R.id.tvSyncCount);
        }
    }
}
