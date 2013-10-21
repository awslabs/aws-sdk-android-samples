/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazonaws.demo.feedback;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;

import android.util.Log;

/**
* This class is used to get clients to the various AWS services.  Before accessing a client 
* the credentials should be checked to ensure validity.
*/
public class AmazonClientManager {
    private static final String LOG_TAG = "AmazonClientManager";

    private AmazonSimpleEmailServiceClient sesClient = null;
    
    public AmazonClientManager() {
    }
                
    public AmazonSimpleEmailServiceClient ses() {
        validateCredentials();    
        return sesClient;
    }
    
    public boolean hasCredentials() {
        return PropertyLoader.getInstance().hasCredentials();
    }
    
    public void validateCredentials() {
        if ( sesClient == null ) {        
            Log.i( LOG_TAG, "Creating New Clients." );
        
            AWSCredentials credentials = new BasicAWSCredentials( PropertyLoader.getInstance().getAccessKey(), PropertyLoader.getInstance().getSecretKey() );
		    sesClient = new AmazonSimpleEmailServiceClient( credentials );
        }
    }
    
    public void clearClients() {
        sesClient = null;    
    }
}
