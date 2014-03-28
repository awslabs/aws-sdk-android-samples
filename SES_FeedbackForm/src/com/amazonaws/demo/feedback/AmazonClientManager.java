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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
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
    	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    	// This sample App is for demonstration purposes only.
    	// It is not secure to embed your credentials into source code.
    	// DO NOT EMBED YOUR CREDENTIALS IN PRODUCTION APPS.
    	// We offer two solutions for getting credentials to your mobile App.
    	// Please read the following article to learn about Token Vending Machine:
    	// * http://aws.amazon.com/articles/Mobile/4611615499399490
    	// Or consider using web identity federation:
    	// * http://aws.amazon.com/articles/Mobile/4617974389850313
    	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    	
    	if ( sesClient == null ) {        
            Log.i( LOG_TAG, "Creating New Clients." );
        
            AWSCredentials credentials = new BasicAWSCredentials( PropertyLoader.getInstance().getAccessKey(), PropertyLoader.getInstance().getSecretKey() );
		    sesClient = new AmazonSimpleEmailServiceClient( credentials );
		    //Set the region to US_WEST_2 as the default region
		    sesClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        }
    }
    
    public void clearClients() {
        sesClient = null;    
    }
}
