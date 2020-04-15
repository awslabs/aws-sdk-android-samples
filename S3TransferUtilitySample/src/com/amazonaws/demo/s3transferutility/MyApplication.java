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

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;

import java.util.UUID;

public class MyApplication extends Application {
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();

        Intent tsIntent = new Intent(this, TransferService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = UUID.randomUUID().toString();
            String name = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, tsIntent, 0);

            // Notification manager to listen to a channel
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // Valid notification object required
            Notification notification = new Notification.Builder(this, id)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent)
                    .build();

            tsIntent.putExtra(TransferService.INTENT_KEY_NOTIFICATION, notification);
            tsIntent.putExtra(TransferService.INTENT_KEY_NOTIFICATION_ID, 15);
            tsIntent.putExtra(TransferService.INTENT_KEY_REMOVE_NOTIFICATION, true);

            // Foreground service required starting from Android Oreo
            startForegroundService(tsIntent);
        } else {
            startService(tsIntent);
        }
    }
}
