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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoMfaSettings;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSettings;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.VerifyMfaContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.RegisterMfaHandler;
import com.amazonaws.util.StringUtils;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = "SettingsActivity";
    private Switch smsSwitch;
    private Switch totpSwitch;
    private Button totpButton;

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
        totpSwitch = (Switch) findViewById(R.id.switchTotpPhone);

        if(smsSwitch != null) {
            smsSwitch.setClickable(true);
            smsSwitch.setChecked(false);
        }

        if(totpSwitch != null) {
            totpSwitch.setClickable(true);
            totpSwitch.setChecked(false);
        }

        totpButton = (Button) findViewById(R.id.buttonRegisterTotp);

        settings = AppHelper.getUserDetails().getSettings().getSettings();

        if(settings != null) {
            if(settings.containsKey("phone_number")) {
                smsSwitch.setClickable(true);
                if(settings.get("phone_number").contains("sms") || settings.get("phone_number").contains("SMS")) {
                    smsSwitch.setChecked(true);
                    smsSwitch.setText("Enabled");
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
                CognitoMfaSettings smsMfaSettings = new CognitoMfaSettings(CognitoMfaSettings.SMS_MFA);
                if (smsSwitch.isChecked()) {
                    //updateSetting("phone_number", "SMS");
                    showWaitDialog("Enabling SMS MFA ...");
                    smsMfaSettings.setEnabled(true);
                    smsMfaSettings.setPreferred(false);
                } else {
                    //updateSetting("phone_number", null);
                    showWaitDialog("Disabling SMS MFA ...");
                    smsMfaSettings.setEnabled(false);
                    smsMfaSettings.setPreferred(false);
                }
                List<CognitoMfaSettings> settings = new ArrayList<CognitoMfaSettings>();
                settings.add(smsMfaSettings);
                AppHelper.getPool().getUser(AppHelper.getCurrUser()).setUserMfaSettingsInBackground(settings, updateSettingHandler);
            }
        });

        totpSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTotpSwitch();
                CognitoMfaSettings totpMfaSettings = new CognitoMfaSettings(CognitoMfaSettings.TOTP_MFA);
                if (totpSwitch.isChecked()) {
                    // Enable
                    showWaitDialog("Enabling TOTP ...");
                    totpMfaSettings.setEnabled(true);
                    totpMfaSettings.setPreferred(false);
                } else {
                    // Disable
                    showWaitDialog("Disabling TOTP ...");
                    totpMfaSettings.setEnabled(false);
                    totpMfaSettings.setPreferred(false);
                }
                List<CognitoMfaSettings> settings = new ArrayList<CognitoMfaSettings>();
                settings.add(totpMfaSettings);
                AppHelper.getPool().getUser(AppHelper.getCurrUser()).setUserMfaSettingsInBackground(settings, updatesMFASettingsHandler);
            }
        });

        totpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Register again
                showWaitDialog("Registering TOTP ...");
                AppHelper.getPool().getUser(AppHelper.getCurrUser()).associateSoftwareTokenInBackground(null, registerMFAHandler);
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
            showDialogMessage("Failed to change SMS MFA state", AppHelper.formatException(exception), false);
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

    RegisterMfaHandler registerMFAHandler = new RegisterMfaHandler() {
        @Override
        public void onSuccess(final String sessionToken) {
            // Success
            // Toast.makeText(getApplicationContext(), "TOTP registered", Toast.LENGTH_SHORT);
            closeWaitDialog();
            Log.d(TAG, " -- MFA Register Success");
        }

        @Override
        public void onVerify(VerifyMfaContinuation continuation) {
            closeWaitDialog();
            getTotpVerificationCode(continuation);
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("TOTP MFA registration failed", AppHelper.formatException(exception), false);
        }
    };

    GenericHandler updatesMFASettingsHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            closeWaitDialog();
        }

        @Override
        public void onFailure(Exception exception) {
            totpSwitch.toggle();
            toggleTotpSwitch();
            closeWaitDialog();
            showDialogMessage("Failed to change TOTP MFA state", AppHelper.formatException(exception), false);
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

    private void toggleTotpSwitch() {
        if(totpSwitch.isChecked()) {
            totpSwitch.setText("Enabled");
            totpSwitch.setTextColor(Color.parseColor("#37A51C"));
        } else {
            totpSwitch.setText("Disabled");
            totpSwitch.setTextColor(Color.parseColor("#E94700"));
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

    private void getTotpVerificationCode(final VerifyMfaContinuation continuation) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        builder.setTitle(continuation.getParameters().get("secretKey"));
        Log.d(TAG, " -- Secret Key fot TOTP: "+continuation.getParameters().get("secretKey"));

        final EditText input = new EditText(SettingsActivity.this);
        input.setText("");
        input.setHint("Enter generated TOTP");
        layout.addView(input);

        final EditText friendlyname = new EditText(SettingsActivity.this);
        friendlyname.setText("");
        friendlyname.setHint("Enter friendly name for TOTP");
        layout.addView(friendlyname);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        friendlyname.setLayoutParams(lp);
        input.requestFocus();
        builder.setView(layout);

        builder.setNeutralButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String code = input.getText().toString();
                    String name = friendlyname.getText().toString();
                    if(!StringUtils.isBlank(code)) {
                        if (StringUtils.isBlank(name)) {
                            name = "Yet another TOTP generator";
                        }
                        // Totp
                        continuation.setVerificationResponse(code, name);
                        continuation.continueTask();
                        userDialog.dismiss();
                        showWaitDialog("Completing TOTP registration...");
                    }

                } catch (Exception e) {
                    // Log failure
                }
            }
        }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    totpSwitch.setChecked(false);
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra("refresh",settingsChanged);
        setResult(RESULT_OK, intent);
        finish();
    }
}
