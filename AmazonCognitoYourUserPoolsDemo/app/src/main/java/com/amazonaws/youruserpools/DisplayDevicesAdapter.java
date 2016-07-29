/*
 *  Copyright 2013-2016 Amazon.com,
 *  Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Amazon Software License (the "License").
 *  You may not use this file except in compliance with the
 *  License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 *  or in the "license" file accompanying this file. This file is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, express or implied. See the License
 *  for the specific language governing permissions and
 *  limitations under the License.
 */

package com.amazonaws.youruserpools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

/**
 * Adapter to show device details on the screen.
 */
public class DisplayDevicesAdapter extends BaseAdapter {
    private String TAG = "DisplayDevicesAdapter";
    private Context context;
    private int count;
    private static LayoutInflater layoutInflater;

    public DisplayDevicesAdapter(Context context) {
        this.context = context;

        count = AppHelper.getDevicesCount();

        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.fields_generic, null);
            holder = new Holder();
            holder.label = (TextView) convertView.findViewById(R.id.textViewUserDetailLabel);
            holder.data = (TextView) convertView.findViewById(R.id.editTextUserDetailInput);
            holder.message = (TextView) convertView.findViewById(R.id.textViewUserDetailMessage);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        ItemToDisplay item = AppHelper.getDeviceForDisplay(position);
        holder.label.setText(item.getLabelText());
        holder.label.setTextColor(item.getLabelColor());
        holder.data.setHint(item.getLabelText());
        holder.data.setText(item.getDataText());
        holder.data.setTextColor(item.getDataColor());
        int resID = 0;
        if(item.getDataDrawable() != null) {
            if(item.getDataDrawable().equals("checked")) {
                resID = R.drawable.checked;
            }
            else if(item.getDataDrawable().equals("not_checked")) {
                resID = R.drawable.not_checked;
            }
        }
        holder.data.setCompoundDrawablesWithIntrinsicBounds(0, 0, resID, 0);
        holder.message.setText(item.getMessageText());
        holder.message.setTextColor(item.getMessageColor());

        return convertView;

    }

    // Helper class to recycle View's
    static class Holder {
        TextView label;
        TextView data;
        TextView message;
    }
}
