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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSettings;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private Switch smsSwitch;
    private Switch emailSwitch;

    private Map<String, String> settings;
    private CognitoUserSettings newSettings;

    private ProgressDialog waitDialog;
    private AlertDialog userDialog;

    private boolean settingsChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        TextView main_title = (TextView) findViewById(R.id.settings_toolbar_title);
        main_title.setText("Settings");

        init();
    }


    private void init() {
        newSettings = new CognitoUserSettings();
        settingsChanged = false;
        smsSwitch = (Switch) findViewById(R.id.switchSettingsPhone);

        if(smsSwitch != null) {
            smsSwitch.setClickable(true);
            smsSwitch.setChecked(false);
        }

        settings = AppHelper.getUserDetails().getSettings().getSettings();

        if(settings != null) {
            if(settings.containsKey("phone_number")) {
                smsSwitch.setClickable(true);
                if(settings.get("phone_number").contains("sms") || settings.get("phone_number").contains("SMS")) {
                    smsSwitch.setChecked(true);
                    smsSwitch.setText("Enable");
                    smsSwitch.setTextColor(Color.parseColor("#37A51C"));
                }
                else {
                    smsSwitch.setChecked(false);
                    smsSwitch.setText("Disabled");
                    smsSwitch.setTextColor(Color.parseColor("#E94700"));
                }
            }
        }

        smsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleSwitch();
                if (smsSwitch.isChecked()) {
                    updateSetting("phone_number", "SMS");
                } else {
                    updateSetting("phone_number", null);
                }
            }
        });
    }

    private void updateSetting(String attribute, String value) {
        showWaitDialog("Changing SMS MFA setting...");
        newSettings = new CognitoUserSettings();
        newSettings.setSettings(attribute, value);
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).setUserSettingsInBackground(newSettings, updateSettingHandler);
    }

    private void getDetails() {
        settingsChanged = true;
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).getDetailsInBackground(detailsHandler);
    }

    GenericHandler updateSettingHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Success
            getDetails();
        }

        @Override
        public void onFailure(Exception exception) {
            // Failed
            closeWaitDialog();
            smsSwitch.toggle();
            toggleSwitch();
            showDialogMessage("Could not change MFA settings", AppHelper.formatException(exception), false);
        }
    };

    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);
            // showDialogMessage("MFA setting successfully changed","",false);
            Toast.makeText(getApplicationContext(), "MFA settings changed", Toast.LENGTH_LONG);
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            //
        }
    };

    private void toggleSwitch() {
        if(smsSwitch.isChecked()) {
            smsSwitch.setText("Enabled");
            smsSwitch.setTextColor(Color.parseColor("#37A51C"));
        } else {
            smsSwitch.setText("Disabled");
            smsSwitch.setTextColor(Color.parseColor("#E94700"));
        }
    }

    private void showDialogMessage(String title, String body, final boolean exitActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if (exitActivity) {
                        onBackPressed();
                    }
                } catch (Exception e) {
                    onBackPressed();
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra("refresh",settingsChanged);
        setResult(RESULT_OK, intent);
        finish();
    }
}
