/**
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.numberguess;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.amazonaws.demo.numberguess.manager.DynamoDBManager;
import com.amazonaws.demo.numberguess.manager.DynamoDBManager.GameRecord;

import java.util.ArrayList;
import java.util.HashMap;

public class GlobalRecordActivity extends ListActivity {

    private static final String MAP_KEY_NAME = "name";
    private static final String MAP_KEY_SCORE = "score";

    private ProgressDialog dialog;
    private SimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, Object>> recordItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        initData();
        initUI();
    }

    private void initData() {
        /*
         * Initializes DynamoDBManager, it must be called before you can use it.
         */
        DynamoDBManager.init();
        recordItems = new ArrayList<HashMap<String, Object>>();
    }

    private void initUI() {
        simpleAdapter = new SimpleAdapter(this, recordItems,
                R.layout.record_item, new String[] {
                        MAP_KEY_NAME,
                        MAP_KEY_SCORE
                }, new int[] {
                        R.id.textViewUserName,
                        R.id.textViewScore
                });
        simpleAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                    String textRepresentation) {
                switch (view.getId()) {
                    case R.id.textViewUserName:
                        TextView name = (TextView) view;
                        name.setText((String) data);
                        return true;
                    case R.id.textViewScore:
                        TextView score = (TextView) view;
                        score.setText(String.valueOf((Integer) data));
                        return true;
                }
                return false;
            }
        });
        getListView().setAdapter(simpleAdapter);
        new GetRecordsTask().execute();
    }

    private class GetRecordsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(GlobalRecordActivity.this,
                    getApplicationContext().getString(R.string.loading),
                    getApplicationContext().getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ArrayList<GameRecord> list = DynamoDBManager.getGameRecords();
            for (GameRecord r : list) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(MAP_KEY_NAME, r.getUserName());
                map.put(MAP_KEY_SCORE, r.getScore());
                recordItems.add(map);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            simpleAdapter.notifyDataSetChanged();
        }
    }
}
