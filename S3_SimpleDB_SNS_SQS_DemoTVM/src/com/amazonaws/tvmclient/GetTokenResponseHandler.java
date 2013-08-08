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

public class GetTokenResponseHandler extends ResponseHandler {

    private final String key;

    public GetTokenResponseHandler( final String key ) {
        this.key = key;
    }    

    public Response handleResponse( int responseCode, String responseBody ) {
        if ( responseCode == 200 ) {   
            try { 
                String json = AESEncryption.unwrap( responseBody, this.key );
                String accessKey = Utilities.extractElement( json, "accessKey" );
                String secretKey = Utilities.extractElement( json, "secretKey" );
                String securityToken = Utilities.extractElement( json, "securityToken" );
                String expirationDate = Utilities.extractElement( json, "expirationDate" );

                return new GetTokenResponse( accessKey, secretKey, securityToken, expirationDate );
            }
            catch ( Exception exception ) {
                return new GetTokenResponse( 500, exception.getMessage() );                
            }
        }
        else {
            return new GetTokenResponse( responseCode, responseBody );
        }
    } 
    
}
