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
package com.amazonaws.demo.s3_transfer_manager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.amazonaws.demo.s3_transfer_manager.models.TransferModel;
import com.amazonaws.demo.s3_transfer_manager.network.TransferController;

import java.util.Timer;
import java.util.TimerTask;

/* 
 * Activity where the user can see the history of transfers, go to the downloads
 * page, or upload images/videos.
 *
 * The reason we separate image and videos is for compatibility with Android versions
 * that don't support multiple MIME types. We only allow videos and images because
 * they are nice for demonstration
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int REFRESH_DELAY = 500;

    private Timer mTimer;
    private LinearLayout mLayout;
    private TransferModel[] mModels = new TransferModel[0];

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);

        mLayout = (LinearLayout)findViewById(R.id.transfers_layout);

        findViewById(R.id.download).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                    MainActivity.this, DownloadActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.upload_image).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });

        findViewById(R.id.upload_video).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, 0);
            }
        });

        //make timer that will refresh all the transfer views
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncModels();
                        for(int i = 0; i < mLayout.getChildCount(); i++) {
                            ((TransferView)mLayout.getChildAt(i)).refresh();
                        }
                    }
                });
            }
        };
        mTimer.schedule(task, 0, REFRESH_DELAY);
    }

    /* When we get a Uri back from the gallery, upload the associated image/video */
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if(uri != null) {
                TransferController.upload(this, uri);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncModels();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.purge();
    }

    /* makes sure that we are up to date on the transfers */
    private void syncModels() {
        TransferModel[] models = TransferModel.getAllTransfers();
        if(mModels.length != models.length) {
            //add the transfers we haven't seen yet
            for(int i = mModels.length; i < models.length; i++) {
                mLayout.addView(new TransferView(this, models[i]), 0);
            }
            mModels = models; 
        }
    }
}
