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

import java.util.Properties;
import android.util.Log;

public class PropertyLoader {
	   
    private boolean hasCredentials = false;       
    private String accessKey = null;  
    private String secretKey = null;  
       
    private static PropertyLoader instance = null;
    
    public static PropertyLoader getInstance() {
        if ( instance == null ) {
            instance = new PropertyLoader();
        }
        
        return instance;
    }       
              
    public PropertyLoader() {
        try {
    	    Properties properties = new Properties();
    	    properties.load( this.getClass().getResourceAsStream( "AwsCredentials.properties" ) );

    	    this.accessKey = properties.getProperty( "ACCESS_KEY_ID" );
    	    this.secretKey = properties.getProperty( "SECRET_KEY" );

    	    if ( this.accessKey == null || this.accessKey.equals( "" ) || this.accessKey.equals( "CHANGEME" ) ||
                 this.secretKey == null || this.secretKey.equals( "" ) || this.secretKey.equals( "CHANGEME" ) ) {
                this.hasCredentials = false;
            }
            else {
                this.hasCredentials = true;
            }
        }
        catch ( Exception exception ) {
            Log.e( "PropertyLoader", "Unable to read property file." );
        }
    }
    
    public boolean hasCredentials() {
        return this.hasCredentials;
    }
    
    public String getAccessKey() {
        return this.accessKey;
    }
        
    public String getSecretKey() {
        return this.secretKey;
    }
}
