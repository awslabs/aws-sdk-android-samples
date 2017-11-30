/*
 * Copyright 2013-2017 Amazon.com,
 * Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the
 * License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, express or implied. See the License
 * for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.youruserpools;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.DevicesHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceSettings extends AppCompatActivity {
    private String TAG = "DeviceSettings";

    private String username;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private ListView devicesList;
    private CheckBox currDeviceCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_Devices);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView main_title = (TextView) findViewById(R.id.devices_toolbar_title);
        main_title.setText("Remembered devices");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        init();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    // Initialize devices screen.
    void init() {
        // Get the devices list.
        currDeviceCheckBox = (CheckBox) findViewById(R.id.deviceTrustStateCheckBox);
        currDeviceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeDeviceTrustState(null, isChecked);
            }
        });
        currDeviceCheckBox.setClickable(false);
        getTrustedDevices();
    }

    // Get the list of Trusted devices
    private void getTrustedDevices() {
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).listDevicesInBackground(10, null, devicesHandler);
    }

    // Get Trusted Devices list handler
    DevicesHandler devicesHandler = new DevicesHandler() {
        @Override
        public void onSuccess(List<CognitoDevice> devices) {
            CognitoDevice thisDevice = AppHelper.getPool().getUser(AppHelper.getCurrUser()).thisDevice();
            AppHelper.setThisDevice(thisDevice);
            if (devices == null) {
                // Show no trusted devices are available.
                List<CognitoDevice> emptyDevicesList = new ArrayList<CognitoDevice>();
                AppHelper.setDevicesForDisplay(emptyDevicesList);
            } else {
                // Show the trusted devices on the screen.
                AppHelper.setDevicesForDisplay(devices);
            }
            showTrustedDevices();
        }

        @Override
        public void onFailure(Exception e) {
            showDialogMessage("Devices",AppHelper.formatException(e),false);
        }
    };

    private void showTrustedDevices() {
        TextView currDeviceState = (TextView) findViewById(R.id.currDeviceState);
        CognitoDevice thisDevice = AppHelper.getThisDevice();
        if (AppHelper.getThisDeviceTrustState()) {
            if (thisDevice != null) {
                currDeviceState.setText("This is a remembered device");
                if (!currDeviceCheckBox.isChecked()) {
                    currDeviceCheckBox.toggle();
                }
                currDeviceCheckBox.setClickable(true);
                currDeviceCheckBox.setChecked(true);
                currDeviceState.setTextColor(Color.parseColor("#2A5C91"));
            } else {
                currDeviceCheckBox.setClickable(false);
                currDeviceState.setText(" ");
                currDeviceState.setTextColor(Color.parseColor("#8D9496"));
            }
        } else {
            if (thisDevice != null) {
                currDeviceState.setText("Remember this device?");
                if (currDeviceCheckBox.isChecked()) {
                    currDeviceCheckBox.toggle();
                }
                currDeviceCheckBox.setClickable(true);
                currDeviceState.setTextColor(Color.parseColor("#404142"));
            } else {
                currDeviceCheckBox.setClickable(false);
                currDeviceState.setText(" ");
                currDeviceState.setTextColor(Color.parseColor("#8D9496"));
            }
        }
        final DisplayDevicesAdapter devicesAdapter = new DisplayDevicesAdapter(getApplicationContext());
        final ListView devicesListView;
        devicesListView = (ListView) findViewById(R.id.listViewTrustedDevices);
        devicesListView.setAdapter(devicesAdapter);
        devicesList = devicesListView;
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDeviceDetail(AppHelper.getDeviceDetail(position));
            }
        });
        closeWaitDialog();
    }

    private void showDeviceDetail(CognitoDevice device) {
        if (device != null) {
            showDeviceDetailDialog(device.getDeviceName(), "Since: " + device.getCreateDate(), device);
        }
    }

    private void forgetdevice(CognitoDevice device) {
        showWaitDialog("");
        device.doNotRememberThisDeviceInBackground(deviceStatusChange);
    }

    private void changeDeviceTrustState(CognitoDevice device, boolean trustDevice) {
        if (device == null) {
            if (AppHelper.getThisDevice() != null) {
                if (trustDevice) {
                    AppHelper.getThisDevice().rememberThisDeviceInBackground(deviceStatusChange);
                } else {
                    AppHelper.getThisDevice().doNotRememberThisDeviceInBackground(deviceStatusChange);
                }
            }
        } else {
            if (trustDevice) {
                device.rememberThisDeviceInBackground(deviceStatusChange);
            } else {
                device.doNotRememberThisDeviceInBackground(deviceStatusChange);
            }
        }
    }

    GenericHandler deviceStatusChange = new GenericHandler() {
        @Override
        public void onSuccess() {
            getTrustedDevices();
        }

        @Override
        public void onFailure(Exception exception) {
            showDialogMessage("Devices",AppHelper.formatException(exception),false);
        }
    };

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exit) {
                        exit();
                    }
                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG, " -- Dialog dismiss failed");
                    if(exit) {
                        exit();
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void showDeviceDetailDialog(String title, String body, final CognitoDevice device) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userDialog.dismiss();
            }
        }).setPositiveButton("Forget this device", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userDialog.dismiss();
                forgetdevice(device);
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            // Ignore these errors
        }
    }

    private void exit () {
        finish();
    }
}
