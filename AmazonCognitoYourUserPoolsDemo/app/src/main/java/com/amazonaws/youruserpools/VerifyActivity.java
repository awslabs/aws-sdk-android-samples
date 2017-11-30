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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

public class VerifyActivity extends AppCompatActivity {
    private Button reqPhoneVerf;
    private Button reqEmailVerf;
    private Button sendVerfCode;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;

    private EditText verifCode;

    private CognitoUserAttributes userAttributes;

    private String attrReqCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_verify);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit(true);
            }
        });

        TextView main_title = (TextView) findViewById(R.id.verify_toolbar_title);
        main_title.setText("Verify attribute");

        init();

    }

    private void init() {
        verifCode = (EditText) findViewById(R.id.editTextVerifyCode);
        reqPhoneVerf = (Button) findViewById(R.id.buttonVerifyPhone);
        reqEmailVerf = (Button) findViewById(R.id.buttonVerifyEmail);
        sendVerfCode = (Button) findViewById(R.id.buttonSendVerifyCode);

        if(AppHelper.isEmailAvailable()) {
            if(AppHelper.isEmailVerified()) {
                reqEmailVerf.setClickable(false);
                reqEmailVerf.setBackground(getDrawable(R.drawable.button_success));
                reqEmailVerf.setText("Email verified");
                reqEmailVerf.setTextColor(Color.parseColor("#37A51C"));
            }
            else {
                reqEmailVerf.setText("Send code");
                reqEmailVerf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reqEmailCode();
                    }
                });
            }
        }
        else {
            reqEmailVerf.setClickable(false);
        }

        if(AppHelper.isPhoneAvailable()) {
            if(AppHelper.isPhoneVerified()) {
                reqPhoneVerf.setClickable(false);
                reqPhoneVerf.setBackground(getDrawable(R.drawable.button_success));
                reqPhoneVerf.setText("Phone number verified");
                reqPhoneVerf.setTextColor(Color.parseColor("#37A51C"));
            }
            else {
                reqPhoneVerf.setText("Send code");
                reqPhoneVerf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reqPhoneCode();
                    }
                });
            }
        }
        else {
            reqPhoneVerf.setClickable(false);
        }
        hideCodeTX();
    }

    private void reqEmailCode() {
        attrReqCode = "email";
        reqEmailVerf.setBackground(getDrawable(R.drawable.button_selected));
        reqEmailVerf.setText("Resend code");
        reqEmailVerf.setTextColor(Color.parseColor("#2A5C91"));
        reqVerfCode();
    }

    private void reqPhoneCode() {
        attrReqCode = "phone_number";
        reqPhoneVerf.setBackground(getDrawable(R.drawable.button_selected));
        reqPhoneVerf.setText("Resend code");
        reqPhoneVerf.setTextColor(Color.parseColor("#2A5C91"));
        reqVerfCode();
    }

    private void reqVerfCode() {
        showWaitDialog("Requesting verification code...");
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).getAttributeVerificationCodeInBackground(attrReqCode, verReqHandler);
    }

    private void sendVerfCode() {
        String code = verifCode.getText().toString();

        if(code == null) {
            TextView label = (TextView) findViewById(R.id.textViewVerifyCodeMessage);
            label.setText(verifCode.getHint()+" cannot be empty");
            verifCode.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        if(code.length() < 1) {
            TextView label = (TextView) findViewById(R.id.textViewVerifyCodeMessage);
            label.setText(verifCode.getHint()+" cannot be empty");
            verifCode.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }
        showWaitDialog("Verifying...");
        hideCodeTX();
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).verifyAttributeInBackground(attrReqCode, code, verHandler);
    }

    private void getDetails() {
        //showWaitDialog("Refreshing...");
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).getDetailsInBackground(detailsHandler);
    }

    VerificationHandler verReqHandler = new VerificationHandler() {
        @Override
        public void onSuccess(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            // Show message
            closeWaitDialog();
            showCodeTX();
            verifCode.requestFocus();
            showDialogMessage("Verification code sent",
                    "Code was sent to "+cognitoUserCodeDeliveryDetails.getDestination()+" via "+cognitoUserCodeDeliveryDetails.getDeliveryMedium(),
                    false);
        }

        @Override
        public void onFailure(Exception exception) {
            // Show error
            closeWaitDialog();
            showDialogMessage("Verfication code request failed!",exception.toString(),false);
        }
    };

    GenericHandler verHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Refresh the screen
            getDetails();
        }

        @Override
        public void onFailure(Exception exception) {
            // Show error
            closeWaitDialog();
            showDialogMessage("Verification failed",AppHelper.formatException(exception),false);
        }
    };

    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);

            if(attrReqCode.equals("phone_number")) {
                reqPhoneVerf.setBackground(getDrawable(R.drawable.button_success));
                reqPhoneVerf.setText("Phone number verified");
                reqPhoneVerf.setTextColor(Color.parseColor("#37A51C"));
                reqPhoneVerf.setClickable(false);
                Toast.makeText(getApplicationContext(), "Phone number verified", Toast.LENGTH_LONG).show();
            }
            else if(attrReqCode.equals("email")) {
                reqEmailVerf.setBackground(getDrawable(R.drawable.button_success));
                reqEmailVerf.setText("Email verified");
                reqEmailVerf.setTextColor(Color.parseColor("#37A51C"));
                reqEmailVerf.setClickable(false);
                Toast.makeText(getApplicationContext(), "Email verified", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();

            // Attributes were verified but user detals read was not successful
            if(attrReqCode.equals("phone_number")) {
                reqPhoneVerf.setBackground(getDrawable(R.drawable.button_success));
                reqPhoneVerf.setText("Phone number verified");
                reqPhoneVerf.setTextColor(Color.parseColor("#37A51C"));
                reqPhoneVerf.setClickable(false);
                Toast.makeText(getApplicationContext(), "Phone number verified", Toast.LENGTH_LONG).show();
            }
            else if(attrReqCode.equals("email")) {
                reqEmailVerf.setBackground(getDrawable(R.drawable.button_success));
                reqEmailVerf.setText("Email verified");
                reqEmailVerf.setTextColor(Color.parseColor("#37A51C"));
                reqEmailVerf.setClickable(false);
                Toast.makeText(getApplicationContext(), "Email verified", Toast.LENGTH_LONG).show();
            }
        }
    };

    private void hideCodeTX() {
        verifCode.setText("");
        verifCode.setVisibility(View.INVISIBLE);
        sendVerfCode.setClickable(false);
        sendVerfCode.setVisibility(View.INVISIBLE);
    }

    private void showCodeTX() {
        verifCode.setVisibility(View.VISIBLE);
        sendVerfCode.setClickable(true);
        sendVerfCode.setVisibility(View.VISIBLE);
        sendVerfCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerfCode();
            }
        });

        verifCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewVerifyCodeLabel);
                    label.setText(verifCode.getHint());
                    verifCode.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewVerifyCodeMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewVerifyCodeLabel);
                    label.setText("");
                }
            }
        });
    }

    private void showDialogMessage(String title, String body, final boolean exitActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if (exitActivity) {
                        exit(true);
                    }
                } catch (Exception e) {
                    exit(true);
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

    private void exit(boolean refresh) {
        Intent intent = new Intent();
        intent.putExtra("refresh",refresh);
        setResult(RESULT_OK, intent);
        finish();
    }
}
