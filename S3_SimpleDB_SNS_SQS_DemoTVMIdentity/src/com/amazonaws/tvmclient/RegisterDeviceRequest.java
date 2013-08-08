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

import com.amazonaws.util.HttpUtils;

public class RegisterDeviceRequest extends Request {

    private final String endpoint;
    private final String uid;
    private final String key;
    private final boolean useSSL;
    
    public RegisterDeviceRequest( final String endpoint, final boolean useSSL, final String uid, final String key ) {
        this.endpoint = endpoint;
        this.useSSL = useSSL;
        this.uid = uid;
        this.key = key;
    }
    
    public String buildRequestUrl() {
        StringBuilder builder = new StringBuilder( ( this.useSSL ? "https://" : "http://" ) );
        builder.append( this.endpoint );
        builder.append( "/" );
        builder.append( "registerdevice" );
        builder.append( "?uid=" + HttpUtils.urlEncode( this.uid, false ) );
        builder.append( "&key=" + HttpUtils.urlEncode( this.key, false ) );
        
        return builder.toString();
    }
    
}
