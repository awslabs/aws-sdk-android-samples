/*
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

package com.amazonaws.demo.s3_transfer_manager.network;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.amazonaws.demo.s3_transfer_manager.Util;
import com.amazonaws.demo.s3_transfer_manager.models.DownloadModel;
import com.amazonaws.demo.s3_transfer_manager.models.TransferModel;
import com.amazonaws.demo.s3_transfer_manager.models.UploadModel;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;

/*
 * This class handles starting all the downloads/uploads. We use a service to do this
 * so that the transfers will continue even though the activity has ended(ie due to
 * orientation change).
 *
 */
public class NetworkService extends IntentService {
    public static final String S3_KEYS_EXTRA = "keys";
    public static final String ACTION_ABORT = "abort";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_RESUME = "resume";
    public static final String NOTIF_ID_EXTRA = "notification_id";

    private static final String TAG = "NetworkService";
    private static final int DEFAULT_INT = -1;

    private TransferManager mTransferManager;

    public NetworkService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTransferManager = new TransferManager(Util.getCredProvider(this));
    }

    @Override
    public void onHandleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_GET_CONTENT) &&
                    intent.getStringArrayExtra(S3_KEYS_EXTRA) != null) {
                download(intent.getStringArrayExtra(S3_KEYS_EXTRA));
            } else if (intent.getAction().equals(Intent.ACTION_SEND) &&
                    intent.getData() != null) {
                upload(intent.getData());
            } else if (intent.getIntExtra(NOTIF_ID_EXTRA, DEFAULT_INT)
                != DEFAULT_INT) {
                int notifId = intent.getIntExtra(NOTIF_ID_EXTRA, DEFAULT_INT);
                if (intent.getAction().equals(ACTION_PAUSE)) {
                    pause(notifId);
                } else if (intent.getAction().equals(ACTION_ABORT)) {
                    abort(notifId);
                } else if (intent.getAction().equals(ACTION_RESUME)) {
                    resume(notifId);
                }
            }
        }
    }

    private void abort(int notifId) {
        TransferModel model = TransferModel.getTransferModel(notifId);
        model.abort();
    }

    private void download(String[] keys) {
        for (int i = 0; i < keys.length; i++) {
            DownloadModel model = new DownloadModel(this, keys[i],
                    mTransferManager);
            model.download();
        }
    }

    private void pause(int notifId) {
        TransferModel model = TransferModel.getTransferModel(notifId);
        model.pause();
    }

    private void resume(int notifId) {
        TransferModel model = TransferModel.getTransferModel(notifId);
        model.resume();
    }

    /* We use a new thread for upload because we have to copy the file */
    private void upload(Uri uri) {
        UploadModel model = new UploadModel(this, uri, mTransferManager);
        new Thread(model.getUploadRunnable()).run();
    }
}
