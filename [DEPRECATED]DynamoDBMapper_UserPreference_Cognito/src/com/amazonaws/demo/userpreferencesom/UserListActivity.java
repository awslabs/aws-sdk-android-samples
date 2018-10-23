/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.userpreferencesom;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.demo.userpreferencesom.DynamoDBManager.UserPreference;

import java.util.ArrayList;

public class UserListActivity extends ListActivity {

    private ArrayList<UserPreference> items = null;
    private ArrayList<String> labels = null;
    private int currentPosition = 0;
    private ArrayAdapter<String> arrayAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new GetUserListTask().execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Intent intent = new Intent(UserListActivity.this, UserActivity.class);
        intent.putExtra("USER_NO", items.get(position).getUserNo() + "");
        startActivity(intent);
    }

    protected boolean onLongListItemClick(View v, int position, long id) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        new DeleteUserTask().execute();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this user?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

        currentPosition = position;

        return true;
    }

    private class GetUserListTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... inputs) {

            labels = new ArrayList<String>();

            items = DynamoDBManager.getUserList();

            for (UserPreference up : items) {
                labels.add(up.getFirstName() + " " + up.getLastName());
            }

            return null;
        }

        protected void onPostExecute(Void result) {

            arrayAdapter = new ArrayAdapter<String>(UserListActivity.this,
                    R.layout.user_list_item, labels);
            setListAdapter(arrayAdapter);

            ListView lv = getListView();
            lv.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> av, View v,
                        int pos, long id) {
                    return onLongListItemClick(v, pos, id);
                }
            });

            Toast toast = Toast.makeText(UserListActivity.this,
                    "Tap and hold to delete users", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private class DeleteUserTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... types) {

            DynamoDBManager.deleteUser(items.get(currentPosition));
            items.remove(currentPosition);
            labels.remove(currentPosition);

            return null;
        }

        protected void onPostExecute(Void result) {

            arrayAdapter.notifyDataSetChanged();

        }
    }
}
