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
package com.amazonaws.demo.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.amazonaws.demo.AWSAndroidDemo;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3 {

	private static ObjectListing objListing = null;
	public static final String BUCKET_NAME = "_bucket_name";
	public static final String OBJECT_NAME = "_object_name";
			
	public static AmazonS3Client getInstance() {
        return AWSAndroidDemo.clientManager.s3();
	}

	public static List<String> getBucketNames() {
		List<Bucket> buckets = getInstance().listBuckets();
		
		List<String> bucketNames = new ArrayList<String>( buckets.size() );
		Iterator<Bucket> bIter = buckets.iterator();
		while(bIter.hasNext()){
			bucketNames.add((bIter.next().getName()));
		}
		return bucketNames;
	}

	public static List<String> getObjectNamesForBucket( String bucketName ) {
		ObjectListing objects = getInstance().listObjects( bucketName );
		objListing = objects;
		List<String> objectNames = new ArrayList<String>( objects.getObjectSummaries().size() );
		Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();
		while(oIter.hasNext()){
			objectNames.add(oIter.next().getKey());
		}
		// By default list objects will only return 1000 keys
		// This code will make multiple calls to fetch all keys in a bucket
		// NOTE: This could potentially cause an out of memory error
		while (objects.isTruncated()) {
			objects = getInstance().listNextBatchOfObjects(objects);
			oIter = objects.getObjectSummaries().iterator();
			while(oIter.hasNext()){
				objectNames.add(oIter.next().getKey());
			}
		}
		return objectNames;		
	}	
	
	public static List<String> getObjectNamesForBucket( String bucketName , int numItems) {
		ListObjectsRequest req= new ListObjectsRequest();
		req.setMaxKeys(Integer.valueOf(numItems));
		req.setBucketName(bucketName);
		ObjectListing objects = getInstance().listObjects( req );
		objListing = objects;
		List<String> objectNames = new ArrayList<String>( objects.getObjectSummaries().size());
		Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();
		while(oIter.hasNext()){
			objectNames.add(oIter.next().getKey());
		}

		return objectNames;		
	}	
	
	public static List<String> getMoreObjectNamesForBucket() {
		try{
			ObjectListing objects = getInstance().listNextBatchOfObjects(objListing);
			objListing = objects;
			List<String> objectNames = new ArrayList<String>( objects.getObjectSummaries().size());
			Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();
			while(oIter.hasNext()){
				objectNames.add(oIter.next().getKey());
			}
			return objectNames;
		} catch (NullPointerException e){
			return new ArrayList<String>();
		}

	}	
	public static void createBucket( String bucketName ) {
		getInstance().createBucket( bucketName );
	}		
	
	public static void deleteBucket( String bucketName ) {
		getInstance().deleteBucket(  bucketName );
	}

	public static void createObjectForBucket( String bucketName, String objectName, String data ) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream( data.getBytes() );
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength( data.getBytes().length );
			getInstance().putObject(bucketName, objectName, bais, metadata );
		}
		catch ( Exception exception ) {
			Log.e( "TODO", "createObjectForBucket" );
		}
	}
	
	public static void deleteObject( String bucketName, String objectName ) {
		getInstance().deleteObject( bucketName, objectName );
	}

	public static String getDataForObject( String bucketName, String objectName ) {
		return read( getInstance().getObject( bucketName, objectName ).getObjectContent() );
	}
	
	protected static String read( InputStream stream ) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream( 8196 );
			byte[] buffer = new byte[1024];
			int length = 0;
			while ( ( length = stream.read( buffer ) ) > 0 ) {
				baos.write( buffer, 0, length );
			}
			
			return baos.toString();
		}
		catch ( Exception exception ) {
			return exception.getMessage();
		}
	}
}
