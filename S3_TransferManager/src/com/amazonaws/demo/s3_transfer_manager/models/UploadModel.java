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

package com.amazonaws.demo.s3_transfer_manager.models;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amazonaws.demo.s3_transfer_manager.Constants;
import com.amazonaws.demo.s3_transfer_manager.Util;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.PersistableUpload;
import com.amazonaws.mobileconnectors.s3.transfermanager.Transfer;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.mobileconnectors.s3.transfermanager.exception.PauseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/* UploadModel handles the interaction between the Upload and TransferManager.
 * This also makes sure that the file that is uploaded has the same file extension
 *
 * One thing to note is that we always create a copy of the file we are given. This
 * is because we wanted to demonstrate pause/resume which is only possible with a
 * File parameter, but there is no reliable way to get a File from a Uri(mainly
 * because there is no guarantee that the Uri has an associated File).
 *
 * You can easily avoid this by directly using an InputStream instead of a Uri.
 */
public class UploadModel extends TransferModel {
    private static final String TAG = "UploadModel";

    private Upload mUpload;
    private PersistableUpload mPersistableUpload;
    private ProgressListener mListener;
    private Status mStatus;
    private File mFile;
    private String mExtension;

    public UploadModel(Context context, Uri uri, TransferManager manager) {
        super(context, uri, manager);
        mStatus = Status.IN_PROGRESS;
        mExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(
                context.getContentResolver().getType(uri));
        ;
        mListener = new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent event) {
                if (event.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                    mStatus = Status.COMPLETED;
                    if (mFile != null) {
                        mFile.delete();
                    }
                }
            }
        };
    }

    public Runnable getUploadRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                upload();
            }
        };
    }

    @Override
    public void abort() {
        if (mUpload != null) {
            mStatus = Status.CANCELED;
            mUpload.abort();
            if (mFile != null) {
                mFile.delete();
            }
        }
    }

    @Override
    public Status getStatus() {
        return mStatus;
    }

    @Override
    public Transfer getTransfer() {
        return mUpload;
    }

    @Override
    public void pause() {
        if (mStatus == Status.IN_PROGRESS) {
            if (mUpload != null) {
                mStatus = Status.PAUSED;
                try {
                    mPersistableUpload = mUpload.pause();
                } catch (PauseException e) {
                    Log.d(TAG, "", e);
                }
            }
        }
    }

    @Override
    public void resume() {
        if (mStatus == Status.PAUSED) {
            mStatus = Status.IN_PROGRESS;
            if (mPersistableUpload != null) {
                // if it paused fine, resume
                mUpload = getTransferManager().resumeUpload(mPersistableUpload);
                mUpload.addProgressListener(mListener);
                mPersistableUpload = null;
            } else {
                // if it was actually aborted, start a new one
                upload();
            }
        }
    }

    public void upload() {
        if (mFile == null) {
            saveTempFile();
        }
        if (mFile != null) {
            try {
                mUpload = getTransferManager().upload(
                        Constants.BUCKET_NAME.toLowerCase(Locale.US),
                        Util.getPrefix(getContext()) + super.getFileName() + "."
                                + mExtension,
                        mFile);
                mUpload.addProgressListener(mListener);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    }

    private void saveTempFile() {
        ContentResolver resolver = getContext().getContentResolver();
        InputStream in = null;
        FileOutputStream out = null;

        try {
            in = resolver.openInputStream(getUri());
            mFile = File.createTempFile(
                    "s3_demo_file_" + getId(),
                    mExtension,
                    getContext().getCacheDir());
            out = new FileOutputStream(mFile, false);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }
}
