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
package com.amazonaws.demo.personalfilestore.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.demo.personalfilestore.S3PersonalFileStore;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3 {

    public static final String BUCKET_NAME = "_bucket_name";
    public static final String OBJECT_NAME = "_object_name";
    public static final String PREFIX = "prefix";

    public static AmazonS3 getInstance() {
        return S3PersonalFileStore.clientManager.s3();
    }

    public static List<String> getObjectNamesForBucket( String bucketName, String prefix, int numItems) {
        ListObjectsRequest req= new ListObjectsRequest();
        req.setMaxKeys(numItems);
        req.setBucketName(bucketName);
        req.setPrefix(prefix + "/");
        ObjectListing objects = getInstance().listObjects( req );
        List<String> objectNames = new ArrayList<String>( objects.getObjectSummaries().size());
        Iterator<S3ObjectSummary> oIter = objects.getObjectSummaries().iterator();
        while(oIter.hasNext()){
            objectNames.add( oIter.next().getKey().substring( prefix.length() + 1 ));
        }

        return objectNames;		
    }	

    public static void createObjectForBucket( String bucketName, String objectName, String data ) {
            ByteArrayInputStream bais = new ByteArrayInputStream( data.getBytes() );
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength( data.getBytes().length );
            getInstance().putObject(bucketName, objectName, bais, metadata );
    }

    public static void deleteObject( String bucketName, String objectName ) {
        getInstance().deleteObject( bucketName, objectName );
    }

    public static String getDataForObject( String bucketName, String objectName ) throws IOException{
        return read( getInstance().getObject( bucketName, objectName ).getObjectContent() );
    }

    protected static String read(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8196);
        byte[] buffer = new byte[32768];
        int length = 0;
        while ((length = stream.read(buffer)) > 0) {
            baos.write(buffer, 0, length);
        }

        return baos.toString();
    }
}
