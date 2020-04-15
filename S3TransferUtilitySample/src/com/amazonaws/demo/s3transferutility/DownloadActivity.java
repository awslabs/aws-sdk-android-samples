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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DownloadActivity displays a list of download records and a bunch of buttons
 * for managing the downloads.
 */
public class DownloadActivity extends ListActivity {
    private static final String TAG = "DownloadActivity";

    private static final int DOWNLOAD_SELECTION_REQUEST_CODE = 1;

    private static final int DOWNLOAD_IN_BACKGROUND_SELECTION_REQUEST_CODE = 2;

    // Indicates no row element has been selected
    private static final int INDEX_NOT_CHECKED = -1;

    private Button btnDownload;
    private Button btnDownloadInBackground;
    private Button btnPause;
    private Button btnResume;
    private Button btnCancel;
    private Button btnDelete;
    private Button btnPauseAll;
    private Button btnCancelAll;

    // This is the main class for interacting with the Transfer Manager
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
    static int checkedIndex;
    static Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
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
        // Uses TransferUtility to get all previous download records.
        observers = transferUtility.getTransfersWithType(TransferType.DOWNLOAD);
        TransferListener listener = new DownloadListener();
        for (TransferObserver observer : observers) {
            observer.refresh();
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
        // This adapter takes the data in transferRecordMaps and displays it,
        // with the keys of the map being related to the columns in the adapter
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

        btnDownload = findViewById(R.id.buttonDownload);
        btnDownloadInBackground = findViewById(R.id.buttonDownloadInBackground);
        btnPause = findViewById(R.id.buttonPause);
        btnResume = findViewById(R.id.buttonResume);
        btnCancel = findViewById(R.id.buttonCancel);
        btnDelete = findViewById(R.id.buttonDelete);
        btnPauseAll = findViewById(R.id.buttonPauseAll);
        btnCancelAll = findViewById(R.id.buttonCancelAll);

        // Launches an activity for the user to select an object in their S3
        // bucket to download
        btnDownload.setOnClickListener(view -> {
            Intent intent = new Intent(DownloadActivity.this, DownloadSelectionActivity.class);
            startActivityForResult(intent, DOWNLOAD_SELECTION_REQUEST_CODE);
        });

        // Launches an activity for the user to select an object in their S3
        // bucket to download in the background
        btnDownloadInBackground.setOnClickListener(view -> {
            Intent intent = new Intent(DownloadActivity.this, DownloadSelectionActivity.class);
            startActivityForResult(intent, DOWNLOAD_IN_BACKGROUND_SELECTION_REQUEST_CODE);
        });

        btnPause.setOnClickListener(view -> {
            // Make sure the user has selected a transfer
            if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                Boolean paused = transferUtility.pause(observers.get(checkedIndex)
                        .getId());
                /*
                 * If paused does not return true, it is likely because the
                 * user is trying to pause a download that is not in a
                 * pausable state (For instance it is already paused, or
                 * canceled).
                 */
                if (!paused) {
                    Toast.makeText(
                            DownloadActivity.this,
                            "Cannot Pause transfer.  You can only pause transfers in a WAITING or IN_PROGRESS state.",
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
                observers.get(checkedIndex).setTransferListener(new DownloadListener());

                /*
                 * If resume returns null, it is likely because the transfer
                 * is not in a resumable state (For instance it is already
                 * running).
                 */
                if (resumed == null) {
                    Toast.makeText(
                            DownloadActivity.this,
                            "Cannot resume transfer.  You can only resume transfers in a PAUSED state.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(view -> {
            // Make sure a transfer is selected
            if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                boolean canceled = transferUtility.cancel(observers.get(checkedIndex).getId());
                /*
                 * If cancel returns false, it is likely because the
                 * transfer is already canceled
                 */
                if (!canceled) {
                    Toast.makeText(
                            DownloadActivity.this,
                            "Cannot cancel transfer.  You can only resume transfers in a PAUSED, WAITING, or IN_PROGRESS state.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(view -> {
            // Make sure a transfer is selected
            if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                // Deletes a record but the file is not deleted.
                transferUtility.deleteTransferRecord(observers.get(checkedIndex).getId());
                observers.remove(checkedIndex);
                transferRecordMaps.remove(checkedIndex);
                checkedIndex = INDEX_NOT_CHECKED;
                updateButtonAvailability();
                updateList();
            }
        });

        btnPauseAll.setOnClickListener(view -> transferUtility.pauseAllWithType(TransferType.DOWNLOAD));

        btnCancelAll.setOnClickListener(view -> transferUtility.cancelAllWithType(TransferType.DOWNLOAD));

        updateButtonAvailability();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DOWNLOAD_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Start downloading with the key they selected in the
                // DownloadSelectionActivity screen.
                String key = data.getStringExtra("key");
                beginDownload(key);
            }
        } else if (requestCode == DOWNLOAD_IN_BACKGROUND_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Start downloading with the key they selected in the
                // DownloadSelectionActivity screen.
                String key = data.getStringExtra("key");
                beginDownloadInBackground(key);
            }
        }
    }

    /*
     * Begins to download the file specified by the key in the bucket.
     */
    private void beginDownload(String key) {
        // Location to download files from S3 to. You can choose any accessible
        // file.

        File file = new File(getExternalFilesDir(null).toString() + "/" + key);

        // Initiate the download
        TransferObserver observer = transferUtility.download(key, file);

        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new DownloadListener());
    }

    /*
     * Begins to download the file specified by the key in the bucket.
     */
    private void beginDownloadInBackground(String key) {
        // Location to download files from S3 to. You can choose any accessible
        // file.
        File file = new File(getExternalFilesDir(null).toString() + "/" + key);

        // Wrap the download call from a background service to
        // support long-running downloads. Uncomment the following
        // code in order to start a download from the background
        // service.
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MyService.class);
        intent.putExtra(MyService.INTENT_KEY_NAME, key);
        intent.putExtra(MyService.INTENT_TRANSFER_OPERATION, MyService.TRANSFER_OPERATION_DOWNLOAD);
        intent.putExtra(MyService.INTENT_FILE, file);
        context.startService(intent);

        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new DownloadListener());
    }

    /*
     * Updates the ListView according to observers, by making transferRecordMap
     * reflect the current data in observers.
     */
    static void updateList() {
        TransferObserver observer;
        HashMap<String, Object> map;
        for (int i = 0; i < observers.size(); i++) {
            observer = observers.get(i);
            observer.setTransferListener(new DownloadListener());
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

    /*
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
}
