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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Dataset.SyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;

public class ListRecordsActivity extends ListActivity {

    private static final String TAG = "ListRecordsActivity";
    public static final String KEY_DATASET_NAME = "dataset_name";

    private Dataset dataset;
    private RecordsAdapter adapter;
    private String datasetName;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            datasetName = bundle.getString(KEY_DATASET_NAME);
        }
        if (datasetName == null) {
            datasetName = "dataset";
        }

        dataset = CognitoSyncClientManager.getInstance().openOrCreateDataset(datasetName);

        // add header
        View header = getLayoutInflater()
                .inflate(R.layout.record_list_item, getListView(), false);
        getListView().addHeaderView(header, null, false);

        adapter = new RecordsAdapter(this, R.layout.record_list_item);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // offset by 1 due to header
                Record record = adapter.getItem(position - 1);
                Intent intent = new Intent(ListRecordsActivity.this, EditRecordActivity.class);
                intent.putExtra(EditRecordActivity.KEY_ACTION, "edit");
                intent.putExtra(EditRecordActivity.KEY_DATASET_NAME, datasetName);
                intent.putExtra(EditRecordActivity.KEY_RECORD_KEY, record.getKey());
                startActivityForResult(intent, 0);
            }
        });

        tvTitle = ((TextView) findViewById(R.id.tvTitle));

        findViewById(R.id.btnSync).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                synchronize(false);
            }
        });

        findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListRecordsActivity.this, EditRecordActivity.class);
                intent.putExtra(EditRecordActivity.KEY_ACTION, "new");
                intent.putExtra(EditRecordActivity.KEY_DATASET_NAME, datasetName);
                startActivityForResult(intent, 0);
            }
        });

        Button btnPopulate = (Button) findViewById(R.id.btnPopulate);
        btnPopulate.setVisibility(View.VISIBLE);
        btnPopulate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = 20;
                Log.i(TAG, String.format("populated %d random records", num));
                Map<String, String> values = new HashMap<String, String>();
                for (int i = 0; i < num; i++) {
                    String key = "key" + (int) (Math.random() * 100000);
                    String value = "value" + (int) (Math.random() * 100000);
                    values.put(key, value);
                }
                // dataset.putAll is much more efficient when adding records in
                // a batch
                dataset.putAll(values);

                refreshListData();
            }
        });

        Button btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ListRecordsActivity.this)
                        .setTitle("Delete dataset")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(
                                "All records will be deleted and can't be undone. "
                                        + "Do you want to proceed?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dataset.delete();
                                dialog.dismiss();
                                synchronize(true);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });

        synchronize(false);
    }

    private void synchronize(final boolean finish) {
        final ProgressDialog dialog = ProgressDialog.show(ListRecordsActivity.this,
                "Syncing", "Please wait");
        Log.i("Sync", "synchronize: " + finish);
        dataset.synchronize(new SyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, final List<Record> newRecords) {
                Log.i("Sync", "success");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (finish) {
                            finish();
                        }
                        refreshListData();
                        Log.i("Sync", String.format("%d records synced", newRecords.size()));
                        Toast.makeText(ListRecordsActivity.this,
                                "Successful!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(final DataStorageException dse) {
                Log.i("Sync", "failure: ", dse);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Log.e("Sync", "failed: " + dse);
                        Toast.makeText(ListRecordsActivity.this,
                                "Failed due to\n" + dse.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            @Override
            public boolean onConflict(final Dataset dataset,
                    final List<SyncConflict> conflicts) {
                Log.i("Sync", "conflict: " + conflicts);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Log.i(TAG,
                                String.format("%s records in conflict", conflicts.size()));
                        List<Record> resolvedRecords = new ArrayList<Record>();
                        for (SyncConflict conflict : conflicts) {
                            Log.i(TAG,
                                    String.format("remote: %s; local: %s",
                                            conflict.getRemoteRecord(),
                                            conflict.getLocalRecord()));
                            /* resolve by taking remote records */
                            resolvedRecords.add(conflict.resolveWithRemoteRecord());

                            /* resolve by taking local records */
                            // resolvedRecords.add(conflict.resolveWithLocalRecord());

                            /*
                             * resolve with customized logic, e.g. concatenate
                             * strings
                             */
                            // String newValue =
                            // conflict.getRemoteRecord().getValue()
                            // + conflict.getLocalRecord().getValue();
                            // resolvedRecords.add(conflict.resolveWithValue(newValue));
                        }
                        dataset.resolve(resolvedRecords);
                        refreshListData();
                        Toast.makeText(
                                ListRecordsActivity.this,
                                String.format(
                                        "%s records in conflict. Resolve by taking remote records",
                                        conflicts.size()),
                                Toast.LENGTH_LONG).show();
                    }
                });
                return true;
            }

            @Override
            public boolean onDatasetDeleted(Dataset dataset, String datasetName) {
                Log.i("Sync", "delete: " + datasetName);
                return true;
            }

            @Override
            public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                Log.i("Sync", "merge: " + datasetNames);
                return false;
            }
        });
    }

    private void updateTitle() {
        tvTitle.setText(String.format("Dataset: %s", datasetName));
    }

    private void refreshListData() {
        adapter.clear();
        for (Record record : dataset.getAllRecords()) {
            adapter.add(record);
        }
        adapter.notifyDataSetChanged();
        updateTitle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            refreshListData();
        }
    }

    /**
     * A list adapter for the records
     */
    static class RecordsAdapter extends ArrayAdapter<Record> {

        public RecordsAdapter(Context context, int resource) {
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

            Record record = getItem(position);
            // record key
            holder.tvKey.setText(record.getKey());
            // if the record is modified, mark it blue
            holder.tvKey.setTextColor(record.isModified() ? Color.BLUE : Color.BLACK);
            // if the record is deleted, put a strike
            if (record.isDeleted()) {
                holder.tvKey.setPaintFlags(holder.tvKey.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvKey.setPaintFlags(holder.tvKey.getPaintFlags()
                        & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
            // record value
            holder.tvValue.setText(record.getValue() == null ? "" : record.getValue());
            // record sync count
            holder.tvSyncCount.setText(String.valueOf(record.getSyncCount()));

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
