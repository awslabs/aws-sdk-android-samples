/**
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.cognito.sync.devauth.client;

/**
 * This class is used to parse the response of the Login request of the sample
 * Cognito developer authentication and convert it into LoginResponse object
 */
public class LoginResponseHandler extends ResponseHandler {
    private final String decryptionKey;

    public LoginResponseHandler(final String decryptionKey) {
        this.decryptionKey = decryptionKey;
    }

    public Response handleResponse(int responseCode, String responseBody) {
        if (responseCode == 200) {
            try {
                String json = AESEncryption.unwrap(responseBody,
                        this.decryptionKey.substring(0, 32));
                return new LoginResponse(Utilities.extractElement(json, "key"));
            } catch (Exception exception) {
                return new LoginResponse(500, exception.getMessage());
            }
        } else {
            return new LoginResponse(responseCode, responseBody);
        }
    }
}
