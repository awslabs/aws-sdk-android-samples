/*
 * Copyright 2013-2016 Amazon.com,
 * Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the
 * License. A copy of the License is located at
 *
 *     http://aws.amazon.com/asl/
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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.NavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

public class MainActivity extends AppCompatActivity {
    private final String TAG="MainActivity";

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;

    // Screen fields
    private EditText inUsername;
    private EditText inPassword;

    //Continuations
    private MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation;
    private ForgotPasswordContinuation forgotPasswordContinuation;

    // User Details
    private String username;
    private String password;

    // Mandatory overrides first
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("Sign in");
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        nDrawer = (NavigationView) findViewById(R.id.nav_view);
        setNavDrawer();

        // Initialize application
        AppHelper.init(getApplicationContext());
        initApp();
        findCurrent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Open/Close the navigation drawer when menu icon is selected
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                // Register user
                if(resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    if (!name.isEmpty()) {
                        inUsername.setText(name);
                        inPassword.setText("");
                        inPassword.requestFocus();
                    }
                }
                break;
            case 2:
                // Confirm register user
                if(resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    if (!name.isEmpty()) {
                        inUsername.setText(name);
                        inPassword.setText("");
                        inPassword.requestFocus();
                    }
                }
                break;
            case 3:
                // Forgot password
                if(resultCode == RESULT_OK) {
                    String newPass = data.getStringExtra("newPass");
                    String code = data.getStringExtra("code");
                    if (newPass != null && code != null) {
                        if (!newPass.isEmpty() && !code.isEmpty()) {
                            showWaitDialog("Setting new password...");
                            forgotPasswordContinuation.setPassword(newPass);
                            forgotPasswordContinuation.setVerificationCode(code);
                            forgotPasswordContinuation.continueTask();
                        }
                    }
                }
                break;
            case 4:
                // User
                if(resultCode == RESULT_OK) {
                    clearInput();
                    String name = data.getStringExtra("TODO");
                    if(name != null) {
                        if (!name.isEmpty()) {
                            name.equals("exit");
                            onBackPressed();
                        }
                    }
                }
                break;
            case 5:
                //MFA
                closeWaitDialog();
                if(resultCode == RESULT_OK) {
                    String code = data.getStringExtra("mfacode");
                    if(code != null) {
                        if (code.length() > 0) {
                            showWaitDialog("Signing in...");
                            multiFactorAuthenticationContinuation.setMfaCode(code);
                            multiFactorAuthenticationContinuation.continueTask();
                        } else {
                            inPassword.setText("");
                            inPassword.requestFocus();
                        }
                    }
                }
                break;
        }
    }

    // App methods
    // Register user - start process
    public void signUp(View view) {
        signUpNewUser();
    }

    // Login if a user is already present
    public void logIn(View view) {
        signInUser();
    }

    // Forgot password processing
    public void forgotPassword(View view) {
        forgotpasswordUser();
    }


    // Private methods
    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                performAction(item);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();

        // Find which item was selected
        switch(item.getItemId()) {
            case R.id.nav_sign_up:
                // Start sign-up
                signUpNewUser();
                break;
            case R.id.nav_sign_up_confirm:
                // Confirm new user
                confirmUser();
                break;
            case R.id.nav_sign_in_forgot_password:
                // User has forgotten the password, start the process to set a new password
                forgotpasswordUser();
                break;
            case R.id.nav_about:
                // For the inquisitive
                Intent aboutAppActivity = new Intent(this, AboutApp.class);
                startActivity(aboutAppActivity);
                break;

        }
    }

    private void signUpNewUser() {
        Intent registerActivity = new Intent(this, RegisterUser.class);
        startActivityForResult(registerActivity, 1);
    }

    private void signInUser() {
        username = inUsername.getText().toString();
        if(username == null || username.length() < 1) {
            TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
            label.setText(inUsername.getHint()+" cannot be empty");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        AppHelper.setUser(username);

        password = inPassword.getText().toString();
        if(password == null || password.length() < 1) {
            TextView label = (TextView) findViewById(R.id.textViewUserPasswordMessage);
            label.setText(inPassword.getHint()+" cannot be empty");
            inPassword.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        showWaitDialog("Signing in...");
        AppHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
    }

    private void forgotpasswordUser() {
        username = inUsername.getText().toString();
        if(username == null) {
            TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
            label.setText(inUsername.getHint()+" cannot be empty");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        if(username.length() < 1) {
            TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
            label.setText(inUsername.getHint()+" cannot be empty");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        showWaitDialog("");
        AppHelper.getPool().getUser(username).forgotPasswordInBackground(forgotPasswordHandler);
    }

    private void getForgotPasswordCode(ForgotPasswordContinuation forgotPasswordContinuation) {
        this.forgotPasswordContinuation = forgotPasswordContinuation;
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        intent.putExtra("destination",forgotPasswordContinuation.getParameters().getDestination());
        intent.putExtra("deliveryMed",forgotPasswordContinuation.getParameters().getDeliveryMedium());
        startActivityForResult(intent, 3);
    }

    private void mfaAuth(MultiFactorAuthenticationContinuation continuation) {
        multiFactorAuthenticationContinuation = continuation;
        Intent mfaActivity = new Intent(this, MFAActivity.class);
        mfaActivity.putExtra("mode", multiFactorAuthenticationContinuation.getParameters().getDeliveryMedium());

        startActivityForResult(mfaActivity, 5);
    }

    private void confirmUser() {
        Intent confirmActivity = new Intent(this, SignUpConfirm.class);
        confirmActivity.putExtra("source","main");
        startActivityForResult(confirmActivity, 2);

    }

    private void launchUser() {
        Intent userActivity = new Intent(this, UserActivity.class);
        userActivity.putExtra("name", username);
        startActivityForResult(userActivity, 4);
    }

    private void findCurrent() {
        CognitoUser user = AppHelper.getPool().getCurrentUser();
        username = user.getUserId();
        if(username != null) {
            AppHelper.setUser(username);
            inUsername.setText(user.getUserId());
            user.getSessionInBackground(authenticationHandler);
        }
    }

    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if(username != null) {
            this.username = username;
            AppHelper.setUser(username);
        }
        if(this.password == null) {
            inUsername.setText(username);
            password = inPassword.getText().toString();
            if(password == null) {
                TextView label = (TextView) findViewById(R.id.textViewUserPasswordMessage);
                label.setText(inPassword.getHint()+" enter password");
                inPassword.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            if(password.length() < 1) {
                TextView label = (TextView) findViewById(R.id.textViewUserPasswordMessage);
                label.setText(inPassword.getHint()+" enter password");
                inPassword.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }
        }
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(this.username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }

    // initialize app
    private void initApp() {
        inUsername = (EditText) findViewById(R.id.editTextUserId);
        inUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewUserIdLabel);
                    label.setText(R.string.Username);
                    inUsername.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewUserIdLabel);
                    label.setText("");
                }
            }
        });

        inPassword = (EditText) findViewById(R.id.editTextUserPassword);
        inPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewUserPasswordLabel);
                    label.setText(R.string.Password);
                    inPassword.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewUserPasswordMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewUserPasswordLabel);
                    label.setText("");
                }
            }
        });
    }


    // Callbacks
    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {
            closeWaitDialog();
            showDialogMessage("Password successfully changed!","");
            inPassword.setText("");
            inPassword.requestFocus();
        }

        @Override
        public void getResetCode(ForgotPasswordContinuation forgotPasswordContinuation) {
            closeWaitDialog();
            getForgotPasswordCode(forgotPasswordContinuation);
        }

        @Override
        public void onFailure(Exception e) {
            closeWaitDialog();
            showDialogMessage("Forgot password failed",AppHelper.formatException(e));
        }
    };

    //
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession) {
            AppHelper.setCurrSession(cognitoUserSession);
            closeWaitDialog();
            launchUser();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            closeWaitDialog();
            getUserAuthentication(authenticationContinuation, username);
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            closeWaitDialog();
            mfaAuth(multiFactorAuthenticationContinuation);
        }

        @Override
        public void onFailure(Exception e) {
            closeWaitDialog();
            TextView label = (TextView) findViewById(R.id.textViewUserIdMessage);
            label.setText("Sign-in failed");
            inPassword.setBackground(getDrawable(R.drawable.text_border_error));

            label = (TextView) findViewById(R.id.textViewUserIdMessage);
            label.setText("Sign-in failed");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));

            showDialogMessage("Sign-in failed", AppHelper.formatException(e));
        }
    };

    private void clearInput() {
        if(inUsername == null) {
            inUsername = (EditText) findViewById(R.id.editTextUserId);
        }

        if(inPassword == null) {
            inPassword = (EditText) findViewById(R.id.editTextUserPassword);
        }

        inUsername.setText("");
        inUsername.requestFocus();
        inUsername.setBackground(getDrawable(R.drawable.text_border_selector));
        inPassword.setText("");
        inPassword.setBackground(getDrawable(R.drawable.text_border_selector));
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void showDialogMessage(String title, String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    //
                }
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
            //
        }
    }
}
