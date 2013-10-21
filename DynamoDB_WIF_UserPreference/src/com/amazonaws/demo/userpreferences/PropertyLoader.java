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

package com.amazonaws.demo.userpreferences;

import java.util.Properties;
import android.util.Log;

public class PropertyLoader {
	
	private boolean hasCredentials = false;
	private String testTableName = null;
	private String accessKeyID = null;
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
			
			this.testTableName = properties.getProperty( "testTableName" );
			this.accessKeyID = properties.getProperty( "ACCESS_KEY_ID" );
			this.secretKey = properties.getProperty( "SECRET_KEY" );
			
			if (this.accessKeyID.equals("CHANGE_ME") || this.secretKey.equals("CHANGE_ME") || this.testTableName.equals( "" ) ) {
				
				this.hasCredentials = false;
				this.testTableName = null;
				this.accessKeyID = null;
				this.secretKey = null;
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

	
	public String getTestTableName() {
		return this.testTableName;
	}
	
	public String getAccessKeyID() {
		return this.accessKeyID;
	}
	
	public String getSecretKey() {
		return this.secretKey;
	}
	
}
