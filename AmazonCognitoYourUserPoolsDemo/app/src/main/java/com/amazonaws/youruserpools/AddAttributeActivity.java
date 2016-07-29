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
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.youruserpools.CognitoYourUserPoolsDemo.R;

import java.util.List;

public class AddAttributeActivity extends AppCompatActivity {
    private Spinner attributeTypes;
    private EditText attributeData;
    private Button addAttributeButton;
    private String attributeSelected;
    private ProgressDialog waitDialog;
    private AlertDialog userDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_attribute);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_attribute);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView main_title = (TextView) findViewById(R.id.add_attr_toolbar_title);
        main_title.setText("Add attribute");

        attributeSelected = null;

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit(true);
            }
        });

        init();
    }

    @Override
    public void onBackPressed() {
        exit(true);
    }

    void init() {
        attributeTypes = (Spinner) findViewById(R.id.spinnerAttributes);
        attributeData = (EditText) findViewById(R.id.editTextAttrData);
        addAttributeButton = (Button) findViewById(R.id.add_attr_button);

        updateDropdownList(AppHelper.getNewAvailableOptions());

        attributeTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                attributeSelected = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addAttributeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAttribute();
            }
        });

        attributeData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewAttrDataLabel);
                    label.setText(attributeData.getHint());
                    attributeData.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = (TextView) findViewById(R.id.textViewAttrDataMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = (TextView) findViewById(R.id.textViewAttrDataLabel);
                    label.setText("");
                }
            }
        });
    }

    // Cognito SDK method to add a new attribute
    private void addAttribute() {
        if(attributeSelected != null) {
            showWaitDialog("Adding "+attributeSelected);
            String attributeValue = attributeData.getText().toString();

            CognitoUserAttributes newAttribute = new CognitoUserAttributes();
            newAttribute.addAttribute(attributeSelected, attributeValue);

            AppHelper.getPool().getUser(AppHelper.getCurrUser()).updateAttributesInBackground(newAttribute, updateHandler);
        }
    }

    // Get user details from CIP service
    private void getDetails() {
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).getDetailsInBackground(detailsHandler);
    }

    // Create a callback handler to get details, this is required to refresh the user details
    // After a successful update.
    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);
            Toast.makeText(getApplicationContext(), "Successfully added",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            // The add was still a success, even through the refresh failed
            Toast.makeText(getApplicationContext(), "Successfully added", Toast.LENGTH_LONG).show();
        }
    };

    // Call back to update user details.
    UpdateAttributesHandler updateHandler = new UpdateAttributesHandler() {
        @Override
        public void onSuccess(List<CognitoUserCodeDeliveryDetails> attributesVerificationList) {
            // Update successful
            AppHelper.addCurrUserattribute(attributeSelected);
            updateDropdownList(AppHelper.getNewAvailableOptions());
            attributeData.setText("");
            getDetails();
        }

        @Override
        public void onFailure(Exception exception) {
            // Update failed
            closeWaitDialog();
            showDialogMessage("Failed to add attribute", AppHelper.formatException(exception));
        }
    };

    private void updateDropdownList(List<String> itemsList) {
        if(itemsList.size() > 0) {
            attributeSelected = itemsList.get(0);
            addAttributeButton.setClickable(true);
            addAttributeButton.setVisibility(View.VISIBLE);
        }
        else {
            itemsList.add("No more attributes to add");
            attributeSelected = null;
            addAttributeButton.setClickable(false);
            addAttributeButton.setVisibility(View.GONE);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        attributeTypes.setAdapter(adapter);
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

    private void exit(boolean refresh) {
        Intent intent = new Intent();
        intent.putExtra("refresh", refresh);
        setResult(RESULT_OK, intent);
        finish();
    }
}
