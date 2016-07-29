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
import android.content.pm.ActivityInfo;
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

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currPassword;
    private EditText newPassword;
    private Button changeButton;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarChangePass);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView main_title = (TextView) findViewById(R.id.change_password_toolbar_title);
        main_title.setText("Change password");

        init();

    }

    private void init() {
        currPassword = (EditText) findViewById(R.id.editTextChangePassCurrPass);
        currPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText(currPassword.getHint());
                    currPassword.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewChangePassCurrPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText("");
                }
            }
        });


        newPassword = (EditText) findViewById(R.id.editTextChangePassNewPass);
        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText(newPassword.getHint());
                    newPassword.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewChangePassNewPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText("");
                }
            }
        });

        changeButton = (Button) findViewById(R.id.change_pass_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        String cPass = currPassword.getText().toString();

        if(cPass == null || cPass.length() < 1) {
            TextView label = (TextView) findViewById(R.id.textViewChangePassCurrPassMessage);
            label.setText(currPassword.getHint()+" cannot be empty");
            currPassword.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        String nPass = newPassword.getText().toString();

        if(nPass == null || nPass.length() < 1) {
            TextView label = (TextView) findViewById(R.id.textViewChangePassNewPassMessage);
            label.setText(newPassword.getHint()+" cannot be empty");
            newPassword.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }
        showWaitDialog("Changing password...");
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).changePasswordInBackground(cPass, nPass, callback);
    }

    GenericHandler callback = new GenericHandler() {
        @Override
        public void onSuccess() {
            closeWaitDialog();
            //showDialogMessage("Success!","Password has been changed",true);
            Toast.makeText(getApplicationContext(),"Your password was changed",Toast.LENGTH_LONG).show();
            clearInput();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            newPassword.setBackground(getDrawable(R.drawable.text_border_error));
            currPassword.setBackground(getDrawable(R.drawable.text_border_error));
            showDialogMessage("Password change failed", AppHelper.formatException(exception), false);
        }
    };

    private  void clearInput() {
        currPassword.setText("");
        newPassword.setText("");
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
            // Wait dialog is already closed or does not exist
        }
    }
}
