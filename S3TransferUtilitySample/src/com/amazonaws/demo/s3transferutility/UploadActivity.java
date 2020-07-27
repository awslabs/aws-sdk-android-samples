/*
 * Copyright 2015-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.s3transferutility;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * UploadActivity is a ListActivity of uploading, and uploaded records as well
 * as buttons for managing the uploads and creating new ones.
 */
public class UploadActivity extends ListActivity {

    // Indicates that no upload is currently selected
    private static final int INDEX_NOT_CHECKED = -1;

    // TAG for logging;
    private static final String TAG = "UploadActivity";

    private static final int UPLOAD_REQUEST_CODE = 0;

    private static final int UPLOAD_IN_BACKGROUND_REQUEST_CODE = 1;

    // Button for upload operations
    private Button btnUploadFile;
    private Button btnUploadFileInBackground;
    private Button btnUploadImage;
    private Button btnPause;
    private Button btnResume;
    private Button btnCancel;
    private Button btnDelete;
    private Button btnPauseAll;
    private Button btnCancelAll;

    // The TransferUtility is the primary class for managing transfer to S3
    static TransferUtility transferUtility;

    // The SimpleAdapter adapts the data about transfers to rows in the UI
    static SimpleAdapter simpleAdapter;

    // A List of all transfers
    static List<TransferObserver> observers;

    /**
     * This map is used to provide data to the SimpleAdapter above. See the
     * fillMap() function for how it relates observers to rows in the displayed
     * activity.
     */
    static ArrayList<HashMap<String, Object>> transferRecordMaps;

    // Which row in the UI is currently checked (if any)
    static int checkedIndex;

    // Reference to the utility class
    static Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initializes TransferUtility, always do this before using it.
        util = new Util();
        transferUtility = util.getTransferUtility(this);
        checkedIndex = INDEX_NOT_CHECKED;
        transferRecordMaps = new ArrayList<>();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get the data from any transfer's that have already happened,
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear transfer listeners to prevent memory leak, or
        // else this activity won't be garbage collected.
        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }
    }

    /**
     * Gets all relevant transfers from the Transfer Service for populating the
     * UI
     */
    static void initData() {
        transferRecordMaps.clear();
        // Use TransferUtility to get all upload transfers.
        observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        TransferListener listener = new UploadListener();
        for (TransferObserver observer : observers) {
            observer.refresh();

            // For each transfer we will will create an entry in
            // transferRecordMaps which will display
            // as a single row in the UI
            HashMap<String, Object> map = new HashMap<>();
            util.fillMap(map, observer, false);
            transferRecordMaps.add(map);

            // Sets listeners to in progress transfers
            if (TransferState.WAITING.equals(observer.getState())
                    || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                    || TransferState.IN_PROGRESS.equals(observer.getState())) {
                observer.setTransferListener(listener);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    private void initUI() {
        /*
         * This adapter takes the data in transferRecordMaps and displays it,
         * with the keys of the map being related to the columns in the adapter
         */
        simpleAdapter = new SimpleAdapter(this, transferRecordMaps,
                R.layout.record_item, new String[] {
                        "checked", "fileName", "progress", "bytes", "state", "percentage"
                },
                new int[] {
                        R.id.radioButton1, R.id.textFileName, R.id.progressBar1, R.id.textBytes,
                        R.id.textState, R.id.textPercentage
                });
        simpleAdapter.setViewBinder((view, data, textRepresentation) -> {
            switch (view.getId()) {
                case R.id.radioButton1:
                    RadioButton radio = (RadioButton) view;
                    radio.setChecked((Boolean) data);
                    return true;
                case R.id.progressBar1:
                    ProgressBar progress = (ProgressBar) view;
                    progress.setProgress((Integer) data);
                    return true;
                case R.id.textFileName:
                case R.id.textBytes:
                case R.id.textState:
                case R.id.textPercentage:
                    TextView text = (TextView) view;
                    text.setText(data.toString());
                    return true;
            }
            return false;
        });
        setListAdapter(simpleAdapter);

        // Updates checked index when an item is clicked
        getListView().setOnItemClickListener((adapterView, view, pos, id) -> {
            if (checkedIndex != pos) {
                transferRecordMaps.get(pos).put("checked", true);
                if (checkedIndex >= 0) {
                    transferRecordMaps.get(checkedIndex).put("checked", false);
                }
                checkedIndex = pos;
                updateButtonAvailability();
                simpleAdapter.notifyDataSetChanged();
            }
        });

        btnUploadFile = findViewById(R.id.buttonUploadFile);
        btnUploadFileInBackground = findViewById(R.id.buttonUploadFileInBackground);
        btnUploadImage = findViewById(R.id.buttonUploadImage);
        btnPause = findViewById(R.id.buttonPause);
        btnResume = findViewById(R.id.buttonResume);
        btnCancel = findViewById(R.id.buttonCancel);
        btnDelete = findViewById(R.id.buttonDelete);
        btnPauseAll = findViewById(R.id.buttonPauseAll);
        btnCancelAll = findViewById(R.id.buttonCancelAll);

        btnUploadFile.setOnClickListener(view -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= 19) {
                // For Android KitKat, we use a different intent to ensure
                // we can
                // get the file path from the returned intent URI
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setType("*/*");
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
            }

            startActivityForResult(intent, UPLOAD_REQUEST_CODE);
        });

        btnUploadFileInBackground.setOnClickListener(view -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= 19) {
                // For Android KitKat, we use a different intent to ensure
                // we can
                // get the file path from the returned intent URI
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setType("*/*");
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
            }

            startActivityForResult(intent, UPLOAD_IN_BACKGROUND_REQUEST_CODE);
        });

        btnUploadImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= 19) {
                // For Android versions of KitKat or later, we use a
                // different intent to ensure
                // we can get the file path from the returned intent URI
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }

            intent.setType("image/*");
            startActivityForResult(intent, UPLOAD_REQUEST_CODE);
        });

        btnPause.setOnClickListener(view -> {
            // Make sure the user has selected a transfer
            if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                Boolean paused = transferUtility.pause(observers.get(checkedIndex).getId());
                /**
                 * If paused does not return true, it is likely because the
                 * user is trying to pause an upload that is not in a
                 * pausable state (For instance it is already paused, or
                 * canceled).
                 */
                if (!paused) {
                    Toast.makeText(
                            UploadActivity.this,
                            "Cannot pause transfer.  You can only pause transfers in a IN_PROGRESS or WAITING state.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnResume.setOnClickListener(view -> {
            // Make sure the user has selected a transfer
            if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                TransferObserver resumed = transferUtility.resume(observers.get(checkedIndex)
                        .getId());
                // Sets a new transfer listener to the original observer.
                // This will overwrite existing listener.
                observers.get(checkedIndex).setTransferListener(new UploadListener());
                /**
                 * If resume returns null, it is likely because the transfer
                 * is not in a resumable state (For instance it is already
                 * running).
                 */
                if (resumed == null) {
                    Toast.makeText(
                            UploadActivity.this,
                            "Cannot resume transfer.  You can only resume transfers in a PAUSED state.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(view -> {
            // Make sure a transfer is selected
            if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                Boolean canceled = transferUtility.cancel(observers.get(checkedIndex).getId());
                /*
                 * If cancel returns false, it is likely because the
                 * transfer is already canceled
                 */
                if (!canceled) {
                    Toast.makeText(
                            UploadActivity.this,
                            "Cannot cancel transfer.  You can only resume transfers in a PAUSED, WAITING, or IN_PROGRESS state.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(view -> {
            // Make sure a transfer is selected
            if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                transferUtility.deleteTransferRecord(observers.get(checkedIndex).getId());
                observers.remove(checkedIndex);
                transferRecordMaps.remove(checkedIndex);
                checkedIndex = INDEX_NOT_CHECKED;
                updateButtonAvailability();
                updateList();
            }
        });

        btnPauseAll.setOnClickListener(view -> transferUtility.pauseAllWithType(TransferType.UPLOAD));

        btnCancelAll.setOnClickListener(view -> transferUtility.cancelAllWithType(TransferType.UPLOAD));

        updateButtonAvailability();
    }

    /*
     * Updates the ListView according to the observers.
     */
    static void updateList() {
        TransferObserver observer;
        HashMap<String, Object> map;
        for (int i = 0; i < observers.size(); i++) {
            observer = observers.get(i);
            map = transferRecordMaps.get(i);
            util.fillMap(map, observer, i == checkedIndex);
        }
        simpleAdapter.notifyDataSetChanged();

    }

    /*
     * Enables or disables buttons according to checkedIndex.
     */
    private void updateButtonAvailability() {
        boolean availability = checkedIndex >= 0;
        btnPause.setEnabled(availability);
        btnResume.setEnabled(availability);
        btnCancel.setEnabled(availability);
        btnDelete.setEnabled(availability);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPLOAD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                try {
                    File file = readContentToFile(uri);
                    beginUpload(file);
                } catch (IOException e) {
                    Toast.makeText(this,
                            "Unable to find selected file. See error log for details",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Unable to upload file from the given uri", e);
                }
            }
        } else if (requestCode == UPLOAD_IN_BACKGROUND_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();

                try {
                    File file = readContentToFile(uri);
                    beginUploadInBackground(file);
                } catch (IOException e) {
                    Toast.makeText(this,
                            "Unable to find selected file. See error log for details",
                            Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Unable to upload file from the given uri", e);
                }
            }
        }
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginUpload(File file) {
        TransferObserver observer = transferUtility.upload(
                file.getName(),
                file
        );

        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new UploadListener());
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginUploadInBackground(File file) {
        // Wrap the upload call from a background service to
        // support long-running downloads. Uncomment the following
        // code in order to start a upload from the background
        // service.
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra(MyService.INTENT_KEY_NAME, file.getName());
        intent.putExtra(MyService.INTENT_TRANSFER_OPERATION, MyService.TRANSFER_OPERATION_UPLOAD);
        intent.putExtra(MyService.INTENT_FILE, file);
        context.startService(intent);

        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new UploadListener());
    }

    /**
     * Copies the resource associated with the Uri to a new File in the cache directory, and returns the File
     * @param uri the Uri
     * @return a copy of the Uri's content as a File in the cache directory
     * @throws IOException if openInputStream fails or writing to the OutputStream fails
     */
    private File readContentToFile(Uri uri) throws IOException {
        final File file = new File(getCacheDir(), getDisplayName(uri));
        try (
            final InputStream in = getContentResolver().openInputStream(uri);
            final OutputStream out = new FileOutputStream(file, false);
        ) {
            byte[] buffer = new byte[1024];
            for (int len; (len = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, len);
            }
            return file;
        }
    }

    /**
     * Returns the filename for the given Uri
     * @param uri the Uri
     * @return String representing the file name (DISPLAY_NAME)
     */
    private String getDisplayName(Uri uri) {
        final String[] projection = { MediaStore.Images.Media.DISPLAY_NAME };
        try (
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        ){
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        }
        // If the display name is not found for any reason, use the Uri path as a fallback.
        Log.w(TAG, "Couldnt determine DISPLAY_NAME for Uri.  Falling back to Uri path: " + uri.getPath());
        return uri.getPath();
    }

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    static class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "Error during upload: " + id, e);
            updateList();
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            updateList();
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(TAG, "onStateChanged: " + id + ", " + newState);
            updateList();
        }
    }
}
