/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazonaws.demo.personalfilestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Login extends AlertActivity {

    protected Button fbButton;
    protected Button amznButton;
    protected Button googleButton;
    protected TextView introText;

    private static final int FB_LOGIN = 1;
    private static final int GOOGLE_LOGIN = 2;
    private static final int AMZN_LOGIN = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_menu);

        introText = (TextView) findViewById(R.id.login_intro_text);
        introText.setText( "Login" );

        fbButton = (Button) findViewById(R.id.fb_login_button);
        amznButton = (Button) findViewById(R.id.lwa_login_button);
        googleButton = (Button) findViewById(R.id.sign_in_button);

        wireButtons();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Login.this.finish();
        }
    }



    public void wireButtons() {
        /* FB_LOGIN BEGIN 
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startActivityForResult(new Intent(Login.this, FacebookLogin.class), FB_LOGIN); }
        });
         FB_LOGIN END */

        /* AMZN_LOGIN BEGIN
    	amznButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startActivityForResult(new Intent(Login.this, AmazonLogin.class), AMZN_LOGIN); }
        });
        /* AMZN_LOGIN END */

        /* GOOGLE_LOGIN BEGIN
	    googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startActivityForResult(new Intent(Login.this, GoogleLogin.class), GOOGLE_LOGIN); }
        });
        /* GOOGLE_LOGIN END */
    }  
}
