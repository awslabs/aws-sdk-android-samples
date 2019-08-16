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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * PhotoAdapter is a customized adapter to adapt photos to gridView.
 */
public class PhotoAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<Photo> photoList;

    public PhotoAdapter(Activity activity, ArrayList<Photo> photoList) {
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
        this.photoList = photoList;
    }

    @Override
    public int getCount() {
        return photoList != null ? photoList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return photoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {
        ViewHolder holder = null;
        if (null == view) {
            view = inflater.inflate(R.layout.grid_photo_item, null);
            holder = new ViewHolder();
            holder.photo_image =  view.findViewById(R.id.photo_image);
            holder.photo_image.setImageBitmap(photoList.get(position).getPhoto());
            holder.photo_file_name =  view.findViewById(R.id.photo_file_name);
            holder.photo_file_name.setText(photoList.get(position).getName());
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        return view;
    }

    /**
     * ViewHolder defines each cell in the gridView.
     */
    protected class ViewHolder {
        // ImageView to show the image of a photo
        protected ImageView photo_image;
        // TextView to show image file name of a photo
        protected TextView photo_file_name;
    }
}
