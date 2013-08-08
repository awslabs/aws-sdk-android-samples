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

public class Response {
    public static final Response SUCCESSFUL = new Response( 200, "OK" );

    private final int responseCode;
    private final String responseMessage;
        
    public Response( final int responseCode, final String responseMessage ) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }
    
    public boolean requestWasSuccessful() {
        return this.getResponseCode() == 200;
    }
    
    public int getResponseCode() {
        return this.responseCode;
    }
    
    public String getResponseMessage() {
        return this.responseMessage;
    }
        
}
