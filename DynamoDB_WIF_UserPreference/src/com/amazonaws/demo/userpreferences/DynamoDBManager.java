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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

public class DynamoDBManager {

	/*
	 * Creates a table with the following attributes:
	 * 
	 * Table name: testTableName Hash key: userNo type N Read Capacity Units: 10
	 * Write Capacity Units: 5
	 */
	public static void createTable() {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();
		KeySchemaElement kse = new KeySchemaElement().withAttributeName(
				"userNo").withKeyType(KeyType.HASH);
		AttributeDefinition ad = new AttributeDefinition().withAttributeName(
				"userNo").withAttributeType(ScalarAttributeType.N);
		ProvisionedThroughput pt = new ProvisionedThroughput()
				.withReadCapacityUnits(10l).withWriteCapacityUnits(5l);

		CreateTableRequest request = new CreateTableRequest()
				.withTableName(PropertyLoader.getInstance().getTestTableName())
				.withKeySchema(kse).withAttributeDefinitions(ad)
				.withProvisionedThroughput(pt);

		try {
			ddb.createTable(request);
		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}
	}

	/*
	 * Retrieves the table description and returns the table status as a string.
	 */
	public static String getTestTableStatus() {

		try {
			AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
					.ddb();

			DescribeTableRequest request = new DescribeTableRequest()
					.withTableName(PropertyLoader.getInstance()
							.getTestTableName());
			DescribeTableResult result = ddb.describeTable(request);

			String status = result.getTable().getTableStatus();
			return status == null ? "" : status;

		} catch (ResourceNotFoundException e) {
			return "";
		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}

		return "";
	}

	/*
	 * Inserts ten users with userNo from 1 to 10 and random names.
	 */
	public static void insertUsers() {
		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();

		try {
			for (int i = 1; i <= 10; i++) {

				HashMap<String, AttributeValue> item = new HashMap<String, AttributeValue>();

				AttributeValue userNo = new AttributeValue().withN(String
						.valueOf(i));
				item.put("userNo", userNo);

				AttributeValue firstName = new AttributeValue().withS(Constants
						.getRandomName());
				item.put("firstName", firstName);

				AttributeValue lastName = new AttributeValue().withS(Constants
						.getRandomName());
				item.put("lastName", lastName);

				PutItemRequest request = new PutItemRequest().withTableName(
						PropertyLoader.getInstance().getTestTableName())
						.withItem(item);

				ddb.putItem(request);
			}
		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}
	}

	/*
	 * Scans the table and returns the list of users.
	 */
	public static List<Map<String, AttributeValue>> getUserList() {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();

		ScanRequest request = new ScanRequest();
		request.setTableName(PropertyLoader.getInstance().getTestTableName());

		try {
			ScanResult result = ddb.scan(request);
			return result.getItems();

		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	/*
	 * Retrieves all of the attribute/value pairs for the specified user.
	 */
	public static Map<String, AttributeValue> getUserInfo(int userNo) {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();

		AttributeValue userNoAttr = new AttributeValue().withN(String
				.valueOf(userNo));
		HashMap<String, AttributeValue> keyMap = new HashMap<String, AttributeValue>();
		keyMap.put("userNo", userNoAttr);
		GetItemRequest request = new GetItemRequest().withTableName(
				PropertyLoader.getInstance().getTestTableName()).withKey(
				keyMap);

		try {
			GetItemResult result = ddb.getItem(request);
			return result.getItem();

		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	/*
	 * Updates one attribute/value pair for the specified user.
	 */
	public static void updateAttributeStringValue(String value, String key,
			AttributeValue targetValue) {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();

		AttributeValue av = new AttributeValue().withS(value);
		AttributeValueUpdate avu = new AttributeValueUpdate().withValue(av)
				.withAction(AttributeAction.PUT);
		HashMap<String, AttributeValue> keyMap = new HashMap<String, AttributeValue>();
		keyMap.put("userNo", targetValue);
		HashMap<String, AttributeValueUpdate> updates = new HashMap<String, AttributeValueUpdate>();
		updates.put(key, avu);

		UpdateItemRequest request = new UpdateItemRequest()
				.withTableName(PropertyLoader.getInstance().getTestTableName())
				.withKey(keyMap).withAttributeUpdates(updates);

		try {
			ddb.updateItem(request);
		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}
	}

	/*
	 * Deletes the specified user and all of its attribute/value pairs.
	 */
	public static void deleteUser(AttributeValue targetValue) {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();

		HashMap<String, AttributeValue> keyMap = new HashMap<String, AttributeValue>();
		keyMap.put("userNo", targetValue);
		DeleteItemRequest request = new DeleteItemRequest().withTableName(
				PropertyLoader.getInstance().getTestTableName()).withKey(
				keyMap);
		try {
			ddb.deleteItem(request);
		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}
	}

	/*
	 * Deletes the test table and all of its users and their attribute/value
	 * pairs.
	 */
	public static void cleanUp() {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();

		DeleteTableRequest request = new DeleteTableRequest()
				.withTableName(PropertyLoader.getInstance().getTestTableName());
		try {
			ddb.deleteTable(request);

		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}
	}
}
