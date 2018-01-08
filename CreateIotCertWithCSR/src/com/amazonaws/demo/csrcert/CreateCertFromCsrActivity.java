/**
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.demo.csrcert;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.CreateCertificateFromCsrRequest;
import com.amazonaws.services.iot.model.CreateCertificateFromCsrResult;

import java.security.KeyPair;

public class CreateCertFromCsrActivity extends Activity {

    private static final String LOG_TAG = CreateCertFromCsrActivity.class.getCanonicalName();

    // --- Constants to modify per your configuration ---

    /** Cognito pool ID. For this app, pool needs to be unauthenticated pool with AWS IoT permissions. */
    private static final String COGNITO_POOL_ID = "CHANGE_ME";

    /** Region of AWS IoT */
    private static final Regions MY_REGION = Regions.US_EAST_1;

    /** Filename of KeyStore file on the filesystem */
    private static final String KEYSTORE_NAME = "iot_keystore";

    /** Password for the private key in the KeyStore */
    private static final String KEYSTORE_PASSWORD = "password";

    /** Certificate and key aliases in the KeyStore */
    private static final String CERTIFICATE_ID = "default";

    /** Set below to app's file directory */
    private String keystorePath;

    /** The credentials provider which fetches the identity id to connect to AWS services */
    CognitoCachingCredentialsProvider credentialsProvider;

    /** High level client to communicate with AWS IoT */
    AWSIotClient iotClient;

    /** Button - onClick will create a certificate. */
    Button btnCreateCertificate;

    /** Displays the status of the certificate. */
    TextView tvCertificateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keystorePath = getFilesDir().getPath();

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        iotClient = new AWSIotClient(credentialsProvider);
        iotClient.setRegion(Region.getRegion(MY_REGION));

        btnCreateCertificate = (Button) findViewById(R.id.btnCreateCertificate);
        btnCreateCertificate.setEnabled(false);
        tvCertificateStatus = (TextView) findViewById(R.id.tvCertificateStatus);

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, KEYSTORE_NAME)) {
                Log.i(LOG_TAG, "Keystore " + KEYSTORE_NAME + " found in " + keystorePath + ".");
                if (AWSIotKeystoreHelper.keystoreContainsAlias(CERTIFICATE_ID, keystorePath,
                        KEYSTORE_NAME, KEYSTORE_PASSWORD)) {
                    Log.i(LOG_TAG, "Certificate and key found in keystore.");
                    tvCertificateStatus.setText("Certificate and key found in keystore.");
                    btnCreateCertificate.setEnabled(false);
                } else {
                    Log.i(LOG_TAG, "Certificate and key NOT found in keystore.");
                    tvCertificateStatus
                            .setText("Certificate and key NOT found in keystore.  Click to create one with a CSR.");
                    btnCreateCertificate.setEnabled(true);
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + KEYSTORE_NAME + " NOT found in " + keystorePath + ".");
                tvCertificateStatus.setText("Keystore " + KEYSTORE_NAME + " NOT found in "
                        + keystorePath
                        + ".  Click to create a certificate with a CSR and store in keystore.");
                btnCreateCertificate.setEnabled(true);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred accessing the keystore and retrieving cert/key.", e);
        }
    }

    /**
     * Create a IoT certificate
     * @param view the view
     */
    public void createCertificate(View view) {
        Log.d(LOG_TAG, "Creating CSR.");
        CreateCertificateTask createCertificateTask = new CreateCertificateTask();
        createCertificateTask.execute();
    }

    private class CreateCertificateTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {

        @Override
        protected AsyncTaskResult<String> doInBackground(Void... voids) {
            try {
                // first generate a Keypair with Private and Public keys
                KeyPair keyPair = AWSIotKeystoreHelper.generatePrivateAndPublicKeys();

                // then create the CSR (uses SpongyCastle (BouncyCastle))
                String csrPemString = CsrHelper.generateCsrPemString(keyPair);

                // now create the create certificate request using that CSR
                CreateCertificateFromCsrRequest request = new CreateCertificateFromCsrRequest();
                request.setSetAsActive(true);
                request.setCertificateSigningRequest(csrPemString);

                // submit the request
                CreateCertificateFromCsrResult result = iotClient.createCertificateFromCsr(request);

                Log.i(LOG_TAG, "Certificate created.");
                Log.i(LOG_TAG, "Certificate  ID: " + result.getCertificateId());
                Log.i(LOG_TAG, "Certificate ARN: " + result.getCertificateArn());
                Log.i(LOG_TAG, "Certificate PEM: " + result.getCertificatePem());

                StringBuilder sb = new StringBuilder();
                sb.append("Cert ID:\n");
                sb.append(result.getCertificateId());
                sb.append("\n\n");
                sb.append("Cert ARN:\n");
                sb.append(result.getCertificateArn());
                sb.append("\n\n");
                sb.append("Certificate:\n");
                sb.append(result.getCertificatePem().substring(0, 100));
                sb.append(".....");

                // now save the key and certificate in the keystore - the next
                // time the app starts it will use the stored one
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(CERTIFICATE_ID,
                        result.getCertificatePem(), keyPair.getPrivate(), keystorePath,
                        KEYSTORE_NAME, KEYSTORE_PASSWORD);

                return new AsyncTaskResult<String>(sb.toString());
            } catch (Exception e) {
                Log.e(LOG_TAG,
                        "An error occurred while creating the CSR and calling create certificate API.",
                        e);
                return new AsyncTaskResult<String>(e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<String> result) {
            if (result.getError() == null) {
                tvCertificateStatus.setText(result.getResult());
                btnCreateCertificate.setEnabled(false);
            } else {
                tvCertificateStatus.setText("An error occurred.  Check the log for the errors.");
                Log.e(LOG_TAG, "Create certificate error.", result.getError());
            }
        }
    }
}
