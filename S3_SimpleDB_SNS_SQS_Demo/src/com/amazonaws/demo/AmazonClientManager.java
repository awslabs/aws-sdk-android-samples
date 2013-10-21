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
package com.amazonaws.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.sns.AmazonSNSClient;

import android.util.Log;

/**
* This class is used to get clients to the various AWS services.  Before accessing a client 
* the credentials should be checked to ensure validity.
*/
public class AmazonClientManager {
    private static final String LOG_TAG = "AmazonClientManager";

    private AmazonS3Client s3Client = null;
    private AmazonSQSClient sqsClient = null;
    private AmazonSimpleDBClient sdbClient = null;
    private AmazonSNSClient snsClient = null;
    
    public AmazonClientManager() {
    }
                
    public AmazonS3Client s3() {
        validateCredentials();
        return s3Client;
    }
        
    public AmazonSQSClient sqs() {
        validateCredentials();    
        return sqsClient;
    }

    public AmazonSimpleDBClient sdb() {
        validateCredentials();    
        return sdbClient;
    }

    public AmazonSNSClient sns() {
        validateCredentials();    
        return snsClient;
    }
    
    public boolean hasCredentials() {
        return PropertyLoader.getInstance().hasCredentials();
    }
    
    public void validateCredentials() {
        if ( s3Client == null || sqsClient == null || sdbClient == null || snsClient == null ) {        
            Log.i( LOG_TAG, "Creating New Clients." );
            
            Region region = Region.getRegion(Regions.US_WEST_2); 
        
            AWSCredentials credentials = new BasicAWSCredentials( PropertyLoader.getInstance().getAccessKey(), PropertyLoader.getInstance().getSecretKey() );
		    s3Client = new AmazonS3Client( credentials );
		    s3Client.setRegion(region);
		    
		    sqsClient = new AmazonSQSClient( credentials );
		    sqsClient.setRegion(region);
		    
		    sdbClient = new AmazonSimpleDBClient( credentials );
		    sdbClient.setRegion(region);
		    
		    snsClient = new AmazonSNSClient( credentials );
		    snsClient.setRegion(region);
        }
    }
    
    public void clearClients() {
        s3Client = null;
        sqsClient = null;
        sdbClient = null;
        snsClient = null;    
    }
}
