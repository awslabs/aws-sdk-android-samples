/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.userpreferencesom;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * This class is used to get clients to the various AWS services. Before
 * accessing a client the credentials should be checked to ensure validity.
 */
public class AmazonClientManager {

    private static final String LOG_TAG = "AmazonClientManager";

    private AmazonDynamoDBClient ddb = null;
    private Context context;

    public AmazonClientManager(Context context) {
        this.context = context;
    }

    public AmazonDynamoDBClient ddb() {
        validateCredentials();
        return ddb;
    }

    public boolean hasCredentials() {
        return (!(Constants.IDENTITY_POOL_ID.equalsIgnoreCase("CHANGE_ME")
                || Constants.TEST_TABLE_NAME.equalsIgnoreCase("CHANGE_ME")));
    }

    public void validateCredentials() {

        if (ddb == null) {
            initClients();
        }
    }

    private void initClients() {
        CognitoCachingCredentialsProvider credentials = new CognitoCachingCredentialsProvider(
                context,
                Constants.IDENTITY_POOL_ID,
                Constants.COGNITO_REGION);

        ddb = new AmazonDynamoDBClient(credentials);
        ddb.setRegion(Region.getRegion(Constants.DYNAMODB_REGION));
    }

    public boolean wipeCredentialsOnAuthError(AmazonServiceException ex) {
        Log.e(LOG_TAG, "Error, wipeCredentialsOnAuthError called" + ex);
        if (
        // STS
        // http://docs.amazonwebservices.com/STS/latest/APIReference/CommonErrors.html
        ex.getErrorCode().equals("IncompleteSignature")
                || ex.getErrorCode().equals("InternalFailure")
                || ex.getErrorCode().equals("InvalidClientTokenId")
                || ex.getErrorCode().equals("OptInRequired")
                || ex.getErrorCode().equals("RequestExpired")
                || ex.getErrorCode().equals("ServiceUnavailable")

                // DynamoDB
                // http://docs.amazonwebservices.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIErrorTypes
                || ex.getErrorCode().equals("AccessDeniedException")
                || ex.getErrorCode().equals("IncompleteSignatureException")
                || ex.getErrorCode().equals(
                        "MissingAuthenticationTokenException")
                || ex.getErrorCode().equals("ValidationException")
                || ex.getErrorCode().equals("InternalFailure")
                || ex.getErrorCode().equals("InternalServerError")) {

            return true;
        }

        return false;
    }
}
