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

import com.amazonaws.util.HttpUtils;

/**
 * This class is used to construct the Login request for communication with
 * sample Cognito developer authentication.
 */
public class LoginRequest extends Request {

    private final String endpoint;
    private final String uid;
    private final String username;
    private final String password;
    private final String appName;
    private final boolean useSSL;

    private final String decryptionKey;

    public LoginRequest(final String endpoint, final boolean useSSL,
            final String appName, final String uid, final String username,
            final String password) {
        this.endpoint = endpoint;
        this.useSSL = useSSL;
        this.appName = appName;
        this.uid = uid;
        this.username = username;
        this.password = password;

        this.decryptionKey = this.computeDecryptionKey();
    }

    public String getDecryptionKey() {
        return this.decryptionKey;
    }

    /*
     * (non-Javadoc)
     * @see com.amazonaws.cognito.sync.devauth.client.Request#buildRequestUrl()
     */
    @Override
    public String buildRequestUrl() {
        StringBuilder builder = new StringBuilder((this.useSSL ? "https://"
                : "http://"));
        builder.append(this.endpoint);
        builder.append("/");

        String timestamp = Utilities.getTimestamp();
        String signature = Utilities
                .getSignature(timestamp, this.decryptionKey);

        builder.append("login");
        builder.append("?uid=" + HttpUtils.urlEncode(this.uid, false));
        builder.append("&username=" + HttpUtils.urlEncode(this.username, false));
        builder.append("&timestamp=" + HttpUtils.urlEncode(timestamp, false));
        builder.append("&signature=" + HttpUtils.urlEncode(signature, false));

        return builder.toString();
    }

    /**
     * This function computes the decryption key
     */
    protected String computeDecryptionKey() {
        try {
            String salt = this.username + this.appName + this.endpoint;
            return Utilities.getSignature(salt, this.password);
        } catch (Exception exception) {
            return null;
        }
    }
}
