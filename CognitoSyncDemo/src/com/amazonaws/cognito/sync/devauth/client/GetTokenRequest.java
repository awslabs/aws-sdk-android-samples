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

import java.net.URL;
import java.util.Map;

/**
 * A class used to construct the GetToken request to the sample Cognito
 * developer authentication application.
 */
public class GetTokenRequest extends Request {

    private final URL endpoint;
    private final String uid;
    private final String key;
    private final Map<String, String> logins;
    private final String identityId;


    public GetTokenRequest(final URL endpoint, final String uid, final String key,
                           Map<String, String> logins, String identityId) {
        this.endpoint = endpoint;
        this.uid = uid;
        this.key = key;
        this.logins = logins;
        this.identityId = identityId;
    }

    /*
     * (non-Javadoc)
     * @see com.amazonaws.cognito.sync.devauth.client.Request#buildRequestUrl()
     * Constructs the request url for GetToken call to sample Cognito developer
     * authentication. The signature is a calculated on the concatenation of
     * timestamp and the contents of the logins mao.
     */
    @Override
    public String buildRequestUrl() {
        String url = this.endpoint.toString();

        StringBuilder builder = new StringBuilder(url);
        if (!url.endsWith("/")) {
            builder.append("/");
        }

        String timestamp = Utilities.getTimestamp();

        builder.append("gettoken");
        builder.append("?uid=" + HttpUtils.urlEncode(this.uid, false));
        builder.append("&timestamp=" + HttpUtils.urlEncode(timestamp, false));

        int counter = 1;

        StringBuilder loginString = new StringBuilder();
        for (Map.Entry<String, String> entry : logins.entrySet()) {
            loginString.append(entry.getKey() + entry.getValue());
            builder.append("&provider");
            builder.append(counter);
            builder.append("=");
            builder.append(HttpUtils.urlEncode(entry.getKey(), false));
            builder.append("&token");
            builder.append(counter);
            builder.append("=");
            builder.append(HttpUtils.urlEncode(entry.getValue(), false));
            counter++;
        }

        builder.append("&identityId=" + HttpUtils.urlEncode(identityId, false));

        String signature = null;
        if (identityId != null) {
            signature = Utilities.getSignature(timestamp + loginString + identityId, key);
        } else {
            signature = Utilities.getSignature(timestamp + loginString, key);
        }
        builder.append("&signature=" + HttpUtils.urlEncode(signature, false));
        return builder.toString();
    }

}
