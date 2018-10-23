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

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.Request;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AbstractAWSSigner;
import com.amazonaws.util.DateUtils;

import org.apache.commons.codec.binary.Hex;

import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for communicating with sample Cognito developer authentication.
 */
public class Utilities {

    public static String getTimestamp() {
        return DateUtils.formatISO8601Date(new Date());
    }

    public static String extractElement(String json, String element) {
        boolean hasElement = (json.indexOf(element) != -1);
        if (hasElement) {
            Log.i("help", json);
            int elementIndex = json.indexOf(element) + element.length() + 1;
            int startIndex = json.indexOf("\"", elementIndex);
            int endIndex = json.indexOf("\"", startIndex + 1);
            Log.i("help", json.substring(startIndex + 1, endIndex));
            return json.substring(startIndex + 1, endIndex);
        }

        return null;
    }

    public static String getSignature(String dataToSign, String key) {
        return new Signer().getSignature(dataToSign, key);
    }

    static class Signer extends AbstractAWSSigner {
        public String getSignature(String dataToSign, String key) {
            try {
                byte[] data = dataToSign.getBytes("UTF8");
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(key.getBytes("UTF8"), "HmacSHA256"));
                char[] signature = Hex.encodeHex(mac.doFinal(data));
                return new String(signature);
            } catch (Exception exception) {
                return null;
            }
        }

        @Override
        public void sign(Request<?> arg0, AWSCredentials arg1)
                throws AmazonClientException {
        }

        @Override
        protected void addSessionCredentials(Request<?> arg0,
                AWSSessionCredentials arg1) {
        }
    }

}
