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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** PhotoActivity will be launched after user clicked on an album.
 *  It defines actions on the Photo view.
 */
public class PhotoActivity extends AppCompatActivity {
    static TransferUtility transferUtility;
    static ArrayList<HashMap<String, Object>> transferRecordMaps;

    private Button btnDownload;

    private static final int UPLOAD_REQUEST_CODE = 0;
    private static final int DOWNLOAD_SELECTION_REQUEST_CODE = 1;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int DEFAULT_VALUE_OF_REQUEST_CODE = -1;
    private static final String PUBLIC_ACCESSTYPE = "public";
    private static final String PRIVATE_ACCESSTYPE = "private";
    private static final String PROTECTED_ACCESSTYPE = "protected";
    private static final String ALBUM_INDEX = "AlbumIndex";

    private static String bucket;
    public static String username;
    private static String userIdentityId;
    public static String accessType;

    // The SimpleAdapter adapts the data about transfers to rows in the UI
    public static SimpleAdapter simpleAdapter;

    private Context mContext = PhotoActivity.this;
    private static GridView photoGridView;
    private PhotoAdapter mAdapter;
    private ArrayList<Photo> photoList;

    private static StorageHelper storageHelper;
    private static AppSyncHelper appSyncHelper;
    public static int indexOfAlbumClicked;
    public static String currentAlbumGraphQLid;

    // A List of all transfers
    static List<TransferObserver> observers;

    private int[] photoCandidates = {
            R.drawable.photo1,
            R.drawable.photo2,
            R.drawable.photo3,
            R.drawable.photo4,
            R.drawable.photo5,
            R.drawable.photo6
    };
    HashMap<Integer, String> resToFileName;

    private static final String TAG = PhotoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Get bucket for this project from aws configuration
        bucket = new AWSConfiguration(this).optJsonObject("S3TransferUtility").optString("Bucket");
        accessType = PRIVATE_ACCESSTYPE;
        userIdentityId = getUserIdentityId();

        storageHelper = new StorageHelper(this);
        appSyncHelper = new AppSyncHelper(this);

        transferUtility = storageHelper.getTransferUtility(this);
        transferRecordMaps = new ArrayList<HashMap<String, Object>>();

        resToFileName = new HashMap<Integer, String>();
        resToFileName.put(R.drawable.photo1, "photo1.jpg");
        resToFileName.put(R.drawable.photo2, "photo2.jpg");
        resToFileName.put(R.drawable.photo3, "photo3.jpg");
        resToFileName.put(R.drawable.photo4, "photo4.jpg");
        resToFileName.put(R.drawable.photo5, "photo5.jpg");
        resToFileName.put(R.drawable.photo6, "photo6.jpg");

        btnDownload = (Button) findViewById(R.id.buttonDownload);
        photoGridView = findViewById(R.id.gw_lstPhoto);
        photoList = new ArrayList<Photo>();
        updatePhotoGridViewWithPhotos();

        initUI();

        // if this view comes from AlbumActivity
        if (getIntent().getIntExtra(ALBUM_INDEX, DEFAULT_VALUE_OF_REQUEST_CODE) != DEFAULT_VALUE_OF_REQUEST_CODE) {

            indexOfAlbumClicked = getIntent().getIntExtra(ALBUM_INDEX,0);
            currentAlbumGraphQLid = appSyncHelper.listAlbums().get(indexOfAlbumClicked).getId();
            photoList = appSyncHelper.listAlbums().get(indexOfAlbumClicked).getPhotos();

            updatePhotoGridViewWithPhotos();
        }
    }

    /**
     * Refresh the current view with the latest data.
     */
    public void updatePhotoGridViewWithPhotos() {
        mAdapter = new PhotoAdapter(this, photoList);
        photoGridView.setAdapter(mAdapter);
    }

    private void initUI() {

        btnDownload = (Button) findViewById(R.id.buttonDownload);

        // Launches an activity for the user to select an object in their S3 bucket to download
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhotoActivity.this, DownloadSelectionActivity.class);
                startActivityForResult(intent, DOWNLOAD_SELECTION_REQUEST_CODE);
            }
        });

        requestWriteExternalStoragePermission();
    }

    /**
     * Pop up a dialog to request permission to write to external storage.
     */
    private void requestWriteExternalStoragePermission() {
        //ask for the permission in android
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to store data in external storage is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the External Storage is required for this application to store the downloaded data from Amazon S3")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRequest();
            }
        }
        else {
            Log.i(TAG, "Permission to store data in external storage is granted.");
        }
    }

    /**
     * Make a request to get permission to write to external storage.
     */
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    /**
     * Create Menu for PhotoActivity.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.photo_menu, menu);
        return true;
    }

    /**
     * Define actions on the menu.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.photo_add:
                File imageFile = storageHelper.saveDrawableAsFile(this, R.drawable.photo1, resToFileName.get(R.drawable.photo1));
                beginUpload(imageFile);
                return true;

            case R.id.photo_sign_out:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Start downloading with the key they selected in the DownloadSelectionActivity screen.
                String key = data.getStringExtra("key");
                beginDownload(key);
            }
        }
    }

    /**
     * Begins to upload the file specified by the file path.
     */
    private void beginUpload(File file) {
        if (file == null) {
            Toast.makeText(this, "File is null!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        TransferNetworkLossHandler.getInstance(mContext);
        final String filename = file.getName();
        final String key = getKeyInBucket(filename, accessType);
        TransferObserver observer = transferUtility.upload(key, file);

        // Attach a listener to the observer to get state update and progress notifications
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED)) {

                    AlertDialog alertDialog = new AlertDialog.Builder(PhotoActivity.this)
                            .setTitle("Upload Succeed Message")
                            .setMessage("The file " + filename + " was uploaded successfully!")
                            .setPositiveButton("DONE", null)
                            .create();
                    alertDialog.show();

                    createPhoto(filename, currentAlbumGraphQLid, key, bucket);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d(TAG, "Progress = " + bytesCurrent + " / " + bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, "Error on transfer: " + ex);
                ex.printStackTrace();
            }
        });
    }

    /**
     * Begins to download the file specified by the key in the bucket.
     */
    private void beginDownload(final String key) {
        // Location to download files from S3 to. You can choose any accessible file.
        final File file = new File(Environment.getExternalStorageDirectory().toString() + "/Download/" + key);
        // Initiate the download
        TransferObserver observer = transferUtility.download(key, file);
        observer.getId();
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED)) {
                    photoList.add(new Photo("123", file.getName(), bucket, key, BitmapFactory.decodeFile(file.getPath())));
                    updatePhotoGridViewWithPhotos();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "Download Progress = " + bytesCurrent + " / " + bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    /**
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Sign out and go back to LoginActivity view.
     */
    public void signOut() {
        AWSMobileClient.getInstance().signOut();
        Intent intent = new Intent(PhotoActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /** Add a photo in AppSync.
     * @param name
     * @param bucket
     * @param key
     */
    public void createPhoto(String name, String albumID, String key, String bucket) {
        appSyncHelper.createPhoto(name, albumID, key, bucket);
    }

    /**
     * Automatically generate a key for a photo to store in the bucket.
     * @param photoName
     * @return
     */
    public String getKeyInBucket(String photoName, String accessType) {
        return accessType + "/" + userIdentityId + "/" + photoName;
    }

    /**
     * A TransferListener class that can listen to a download task and be
     * notified when the status changes.
     */
    private static class DownloadListener implements TransferListener, Serializable {
        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "onError: " + id, e);
            updateList();
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            updateList();
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "onStateChanged: " + id + ", " + state);
            updateList();
        }
    }

    /**
     * Updates the ListView according to observers, by making transferRecordMap
     * reflect the current data in observers.
     */
    static void updateList() {
        TransferObserver observer = null;
        HashMap<String, Object> map = null;
        for (int i = 0; i < observers.size(); i++) {
            observer = observers.get(i);
            observer.setTransferListener(new DownloadListener());
            map = transferRecordMaps.get(i);
            storageHelper.fillMap(map, observer, false);
        }
        simpleAdapter.notifyDataSetChanged();
    }

    /**
     * Get current userIdentityId.
     */
    public String getUserIdentityId() {
        String userIdentityId = AWSMobileClient.getInstance().getIdentityId();
        Log.e(TAG, "Successfully get userIdentityId: " + userIdentityId);
        return userIdentityId;
    }
}