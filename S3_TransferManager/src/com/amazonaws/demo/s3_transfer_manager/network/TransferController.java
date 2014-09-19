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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.amazonaws.demo.s3_transfer_manager.models.TransferModel;

/*
 * This class is a bridge to the NetworkService, making it easy to have the service
 * do operations for us. This just makes it so that we don't have to worry about
 * missing parameters in the Intent and that kind of thing
 */
public class TransferController {
    public static void abort(Context context, TransferModel model) {
        Intent intent = makeIdIntent(context, model.getId());
        intent.setAction(NetworkService.ACTION_ABORT);
        context.startService(intent);
    }

    public static void upload(Context context, Uri uri) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(Intent.ACTION_SEND);
        intent.setData(uri);
        context.startService(intent);
    }

    public static void download(Context context, String[] keys) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(NetworkService.S3_KEYS_EXTRA, keys);
        context.startService(intent);
    }

    public static void pause(Context context, TransferModel model) {
        Intent intent = makeIdIntent(context, model.getId());
        intent.setAction(NetworkService.ACTION_PAUSE);
        context.startService(intent);
    }

    public static void resume(Context context, TransferModel model) {
        Intent intent = makeIdIntent(context, model.getId());
        intent.setAction(NetworkService.ACTION_RESUME);
        context.startService(intent);
    }

    private static Intent makeIdIntent(Context context, int id) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(NetworkService.NOTIF_ID_EXTRA, id);
        return intent;
    }
}
