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
 * This class is used to store the response of the Login call of sample Cognito
 * developer authentication.
 */
public class LoginResponse extends Response {
    private final String key;

    public LoginResponse(final int responseCode, final String responseMessage) {
        super(responseCode, responseMessage);
        this.key = null;
    }

    public LoginResponse(final String key) {
        super(200, null);
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
