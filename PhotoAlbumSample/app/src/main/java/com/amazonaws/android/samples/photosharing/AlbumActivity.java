/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.android.samples.photosharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridView;

import com.amazonaws.mobile.client.AWSMobileClient;

import java.util.ArrayList;

/** AlbumActivity will be launched after user sign-in.
 *  It defines actions on the Album view.
 */
public class AlbumActivity extends AppCompatActivity {

    private static GridView albumGridView;
    private AlbumAdapter mAdapter;
    private ArrayList<Album> albumList;
    public Album albumClicked;
    public static int indexOfAlbumClicked;

    private static AppSyncHelper appSyncHelper;

    private static final String TAG = AlbumActivity.class.getSimpleName();
    private static boolean EDIT_FLAG = false;

    private static String username;

    private static final String INDEX = "index";
    private static final String REQUEST_CODE = "ReqCode";
    private static final String NEW_ALBUM_NAME = "newAlbumName";
    private static final int DEFAULT_VALUE_OF_REQUEST_CODE = -1;
    private static final int DELETE_REQUEST_CODE = 1;
    private static final int UPDATE_REQUEST_CODE = 2;

    private static final String PUBLIC_ACCESSTYPE = "public";
    private static final String PRIVATE_ACCESSTYPE = "private";
    private static final String PROTECTED_ACCESSTYPE = "protected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        appSyncHelper = new AppSyncHelper(AlbumActivity.this);

        username = getUsername();

        albumGridView = (GridView) findViewById(R.id.gw_lstAlbum);

        updateAlbumGridViewWithAlbums();

        if (getIntent().getIntExtra(INDEX, DEFAULT_VALUE_OF_REQUEST_CODE) != DEFAULT_VALUE_OF_REQUEST_CODE) {

            indexOfAlbumClicked = getIntent().getIntExtra(INDEX,0);

            // Get delete request
            if (getIntent().getIntExtra(REQUEST_CODE, DEFAULT_VALUE_OF_REQUEST_CODE) == DELETE_REQUEST_CODE) {
                deleteAlbum();
            }

            // Get update request
            else if (getIntent().getIntExtra(REQUEST_CODE, DEFAULT_VALUE_OF_REQUEST_CODE) == UPDATE_REQUEST_CODE) {
                String newAlbumName = getIntent().getStringExtra(NEW_ALBUM_NAME);
                updateAlbum(newAlbumName);
            }
        }
        updateAlbumGridViewWithAlbums();
        updateAlbumGridViewWithAlbums();
    }

    /**
     * Refresh the current view with the latest data.
     */
    private void updateAlbumGridViewWithAlbums() {
        albumList = appSyncHelper.listAlbums();
        mAdapter = new AlbumAdapter(this, albumList, EDIT_FLAG);
        albumGridView.setAdapter(mAdapter);
    }

    /**
     * Specify the options menu for an AlbumActivity.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Define actions on the menu - add album, edit album and sign out.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.album_add:
                final EditText albumEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add an album:")
                        .setView(albumEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nameOfAlbum = String.valueOf(albumEditText.getText());
                                createAlbum(nameOfAlbum);
                                updateAlbumGridViewWithAlbums();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;
            case R.id.album_sign_out:
                signOut();
                return true;
            case R.id.album_edit:
                if (EDIT_FLAG) {
                    EDIT_FLAG = false;
                } else {
                    EDIT_FLAG = true;
                }
                updateAlbumGridViewWithAlbums();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create an album.
     * @param albumName
     */
    private void createAlbum(String albumName) {
        appSyncHelper.createAlbum(albumName, username, PRIVATE_ACCESSTYPE);
    }

    /**
     * Delete an album.
     */
    public void deleteAlbum() {
        albumClicked = albumList.get(indexOfAlbumClicked);
        String id = albumClicked.getId();
        appSyncHelper.deleteAlbum(id);
    }

    /**
     * Update an album.
     * @param name
     */
    public void updateAlbum(String name) {
        albumClicked = albumList.get(indexOfAlbumClicked);
        String id = albumClicked.getId();
        appSyncHelper.updateAlbum(name, id);
    }

    /**
     * User sign out and go into LoginActivity.
     */
    public void signOut() {
        AWSMobileClient.getInstance().signOut();
        Log.e(TAG, "Successfully sign out.");
        Intent intent = new Intent(AlbumActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Get current username.
     */
    public String getUsername() {
        Log.e(TAG, "Successfully get username.");
        return AWSMobileClient.getInstance().getUsername();
    }
}