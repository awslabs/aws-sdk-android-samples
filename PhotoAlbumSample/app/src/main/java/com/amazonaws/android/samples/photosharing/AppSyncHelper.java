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

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateAlbumMutation;
import com.amazonaws.amplify.generated.graphql.CreatePhotoMutation;
import com.amazonaws.amplify.generated.graphql.DeleteAlbumMutation;
import com.amazonaws.amplify.generated.graphql.ListAlbumsQuery;
import com.amazonaws.amplify.generated.graphql.UpdateAlbumMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import type.CreateAlbumInput;
import type.CreatePhotoInput;
import type.DeleteAlbumInput;
import type.UpdateAlbumInput;

/**
 * AppSyncHelper maintains utilities for using AppSync.
 */
public class AppSyncHelper {

    private static final String TAG = AppSyncHelper.class.getSimpleName();
    private static Context CURRENT_CONTEXT;
    private AWSAppSyncClient mAWSAppSyncClient;

    /**
     * Initialize an AWSAppSyncClient when create an AppSyncHelper object.
     * @param context
     */
    public AppSyncHelper(Context context) {

        // Initialize AWSAppSyncClient
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(context)
                .awsConfiguration(new AWSConfiguration(context))
                .cognitoUserPoolsAuthProvider(new CognitoUserPoolsAuthProvider() {
                    @Override
                    public String getLatestAuthToken() {
                        try {
                            return AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
                        } catch (Exception e) {
                            Log.e(TAG, e.getLocalizedMessage());
                            return null;
                        }
                    }
                })
            .build();

        CURRENT_CONTEXT = context;

    }

    private GraphQLCall.Callback<CreateAlbumMutation.Data> createAlbumCallback = new GraphQLCall.Callback<CreateAlbumMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateAlbumMutation.Data> response) {
            String albumName = response.data().createAlbum().name();
            Log.i(TAG, "An album with name " + albumName + " is created successfully.");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i(TAG, "Failed to create an album.");
            AlertDialog alertDialog = new AlertDialog.Builder(CURRENT_CONTEXT)
                    .setTitle("Create album failed.")
                    .setPositiveButton("CLOSE", null)
                    .create();
            alertDialog.show();
            e.printStackTrace();
        }
    };

    private GraphQLCall.Callback<DeleteAlbumMutation.Data> deleteAlbumCallback = new GraphQLCall.Callback<DeleteAlbumMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<DeleteAlbumMutation.Data> response) {
            String albumName = response.data().deleteAlbum().name();
            Log.i(TAG, "An album with name " + albumName + " is deleted successfully.");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i(TAG, "Failed to delete an album.");
            AlertDialog alertDialog = new AlertDialog.Builder(CURRENT_CONTEXT)
                    .setTitle("Delete album failed.")
                    .setPositiveButton("CLOSE", null)
                    .create();
            alertDialog.show();
            e.printStackTrace();
        }
    };

    private GraphQLCall.Callback<UpdateAlbumMutation.Data> updateAlbumCallback = new GraphQLCall.Callback<UpdateAlbumMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateAlbumMutation.Data> response) {
            String albumName = response.data().updateAlbum().name();
            Log.i(TAG, "An album with name " + albumName + " is updated successfully.");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i(TAG, "Failed to update an album.");
            AlertDialog alertDialog = new AlertDialog.Builder(CURRENT_CONTEXT)
                    .setTitle("Update album failed.")
                    .setPositiveButton("CLOSE", null)
                    .create();
            alertDialog.show();
            e.printStackTrace();
        }
    };

    /**
     * Do create album mutation in AppSync.
     * @param albumName
     * @param userName
     * @param accessType
     */
    public void createAlbum(final String albumName, final String userName, final String accessType) {

        CreateAlbumInput createAlbumInput = CreateAlbumInput.builder()
                .name(albumName)
                .username(userName)
                .accesstype(accessType)
                .build();

        mAWSAppSyncClient.mutate(CreateAlbumMutation.builder().input(createAlbumInput).build())
                .enqueue(createAlbumCallback);
    }

    /**
     * Do delete album mutation in AppSync.
     * @param albumId
     */
    public void deleteAlbum(final String albumId) {

        DeleteAlbumInput deleteAlbumInput = DeleteAlbumInput.builder()
                .id(albumId)
                .build();

        mAWSAppSyncClient.mutate(DeleteAlbumMutation.builder().input(deleteAlbumInput).build())
                .enqueue(deleteAlbumCallback);
    }

    public void updateAlbum(final String albumName, final String albumId) {
        UpdateAlbumInput updateAlbumInput = UpdateAlbumInput.builder()
                .name(albumName)
                .id(albumId)
                .build();

        mAWSAppSyncClient.mutate(UpdateAlbumMutation.builder().input(updateAlbumInput).build())
                .enqueue(updateAlbumCallback);
    }

    /**
     * Do list query from AppSync to get a list of albums.
     * @return
     */
    public ArrayList<Album> listAlbums() {
        final ArrayList<Album> albumList = new ArrayList<Album>();

        final CountDownLatch listAlbumsLatch = new CountDownLatch(1);
        // limit is set to ensure the all the albums can be queried successfully if the number of albums is less than or equal to 30.
        // The number 30 is chosen for limit because usually the number of albums will not exceed 30.
        // This number can be adjusted by customers.

        // Using NETWORK_ONLY state is set to AppSyncResponseFetcher in case deleted albums still stay in cache.
        mAWSAppSyncClient.query(ListAlbumsQuery.builder().limit(30).build()).responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(new GraphQLCall.Callback<ListAlbumsQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListAlbumsQuery.Data> response) {
                Log.d(TAG, "listAlbums succeeded." + response.data().toString());
                try {
                    List<ListAlbumsQuery.Item> items = response.data().listAlbums().items();
                    for (ListAlbumsQuery.Item item : items) {
                        albumList.add(new Album(item.id(), R.drawable.album1, item.name(), item.accesstype(), new ArrayList<Photo>()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                listAlbumsLatch.countDown();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.d(TAG, "listAlbums failed." + e.getLocalizedMessage());
                AlertDialog alertDialog = new AlertDialog.Builder(CURRENT_CONTEXT)
                        .setTitle("List albums failed.")
                        .setPositiveButton("CLOSE", null)
                        .create();
                alertDialog.show();
                e.printStackTrace();
                listAlbumsLatch.countDown();
            }
        });

        try {
            // wait for query callback
            listAlbumsLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return albumList;
    }

    private GraphQLCall.Callback<CreatePhotoMutation.Data> createPhotoCallback = new GraphQLCall.Callback<CreatePhotoMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreatePhotoMutation.Data> response) {
            String photoName = response.data().createPhoto().name();
            Log.i(TAG, "A photo with name " + photoName + " is created successfully.");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i(TAG, "Failed to create a photo.");
            AlertDialog alertDialog = new AlertDialog.Builder(CURRENT_CONTEXT)
                    .setTitle("Create photo failed.")
                    .setPositiveButton("CLOSE", null)
                    .create();
            alertDialog.show();
            e.printStackTrace();
        }
    };

    /**
     * Do create photo mutation in AppSync.
     * @param albumName
     * @param albumID
     * @param photoS3key
     * @param photoS3Bucket
     */
    public void createPhoto(final String albumName, final String albumID, final String photoS3key, final String photoS3Bucket) {

        CreatePhotoInput createPhotoInput = CreatePhotoInput.builder()
                .photoAlbumId(albumID)
                .name(albumName)
                .key(photoS3key)
                .bucket(photoS3Bucket)
                .build();

        mAWSAppSyncClient.mutate(CreatePhotoMutation.builder().input(createPhotoInput).build())
                .enqueue(createPhotoCallback);
    }

}