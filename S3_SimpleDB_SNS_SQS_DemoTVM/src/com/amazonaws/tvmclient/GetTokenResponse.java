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
package com.amazonaws.tvmclient;

public class GetTokenResponse extends Response {
    private final String accessKey;
    private final String secretKey;
    private final String securityToken;
    private final String expirationDate;
    
    public GetTokenResponse( final int responseCode, final String responseMessage ) {
        super( responseCode, responseMessage );
        this.accessKey = null;
        this.secretKey = null;
        this.securityToken = null;
        this.expirationDate = null;
    }
    
    public GetTokenResponse( final String accessKey, final String secretKey, final String securityToken, final String expirationDate ) {
        super( 200, null );
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.securityToken = securityToken;
        this.expirationDate = expirationDate;
    }
    
    public String getAccessKey() {
        return this.accessKey;
    }
    
    public String getSecretKey() {
        return this.secretKey;
    }
    
    public String getSecurityToken() {
        return this.securityToken;
    }
    
    public String getExpirationDate() {
        return this.expirationDate;
    }
}
