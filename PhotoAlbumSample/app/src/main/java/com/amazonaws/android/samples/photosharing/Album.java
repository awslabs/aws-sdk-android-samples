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

import java.util.ArrayList;

/**
 * Defines Album class.
 */
public class Album {
    // album id in GraphQL
    private String albumId;
    // Resource id defined in XML resource file
    private int resId;
    // name of album
    private String albumName;
    // accessType refers to public, private or protected
    private String accessType;
    // a list of photos that the album has
    private ArrayList<Photo> photos;

    /**
     * Constructor of an Album object.
     * @param albumId
     * @param resId
     * @param name
     * @param accessType
     * @param photos
     */
    public Album(String albumId, int resId, String name, String accessType, ArrayList<Photo> photos) {
        this.albumId = albumId;
        this.resId = resId;
        this.albumName = name;
        this.accessType = accessType;
        this.photos = photos;
    }

    public String getId() {
        return this.albumId;
    }

    public int getResId() {
        return this.resId;
    }

    public String getAlbumName() {
        return this.albumName;
    }

    public ArrayList<Photo> getPhotos() {
        return this.photos;
    }

}
