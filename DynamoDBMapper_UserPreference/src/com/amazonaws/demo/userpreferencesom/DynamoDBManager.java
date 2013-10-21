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

package com.amazonaws.demo.userpreferencesom;

import java.util.ArrayList;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

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
		DynamoDBMapper mapper = new DynamoDBMapper(ddb);

		try {
			for (int i = 1; i <= 10; i++) {
				UserPreference userPreference = new UserPreference();
				userPreference.setUserNo(i);
				userPreference.setFirstName(Constants.getRandomName());
				userPreference.setLastName(Constants.getRandomName());

				mapper.save(userPreference);
			}
		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}
	}

	/*
	 * Scans the table and returns the list of users.
	 */
	public static ArrayList<UserPreference> getUserList() {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();
		DynamoDBMapper mapper = new DynamoDBMapper(ddb);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		try {
			PaginatedScanList<UserPreference> result = mapper.scan(
					UserPreference.class, scanExpression);

			ArrayList<UserPreference> resultList = new ArrayList<UserPreference>();
			for (UserPreference up : result) {
				resultList.add(up);
			}

			return resultList;

		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	/*
	 * Retrieves all of the attribute/value pairs for the specified user.
	 */
	public static UserPreference getUserPreference(int userNo) {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();
		DynamoDBMapper mapper = new DynamoDBMapper(ddb);

		try {
			UserPreference userPreference = mapper.load(UserPreference.class,
					userNo);

			return userPreference;

		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	/*
	 * Updates one attribute/value pair for the specified user.
	 */
	public static void updateUserPreference(UserPreference updateUserPreference) {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();
		DynamoDBMapper mapper = new DynamoDBMapper(ddb);

		try {
			mapper.save(updateUserPreference);

		} catch (AmazonServiceException ex) {
			UserPreferenceDemoActivity.clientManager
					.wipeCredentialsOnAuthError(ex);
		}
	}

	/*
	 * Deletes the specified user and all of its attribute/value pairs.
	 */
	public static void deleteUser(UserPreference deleteUserPreference) {

		AmazonDynamoDBClient ddb = UserPreferenceDemoActivity.clientManager
				.ddb();
		DynamoDBMapper mapper = new DynamoDBMapper(ddb);

		try {
			mapper.delete(deleteUserPreference);

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

	@DynamoDBTable(tableName = "TestUserPreference")
	public static class UserPreference {
		private int userNo;
		private String firstName;
		private String lastName;
		private Boolean autoLogin;
		private Boolean vibrate;
		private Boolean silent;
		private String colorTheme;

		@DynamoDBHashKey(attributeName = "userNo")
		public int getUserNo() {
			return userNo;
		}

		public void setUserNo(int userNo) {
			this.userNo = userNo;
		}

		@DynamoDBAttribute(attributeName = "firstName")
		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		@DynamoDBAttribute(attributeName = "lastName")
		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		@DynamoDBAttribute(attributeName = "autoLogin")
		public Boolean isAutoLogin() {
			return autoLogin;
		}

		public void setAutoLogin(Boolean autoLogin) {
			this.autoLogin = autoLogin;
		}

		@DynamoDBAttribute(attributeName = "vibrate")
		public Boolean isVibrate() {
			return vibrate;
		}

		public void setVibrate(Boolean vibrate) {
			this.vibrate = vibrate;
		}

		@DynamoDBAttribute(attributeName = "silent")
		public Boolean isSilent() {
			return silent;
		}

		public void setSilent(Boolean silent) {
			this.silent = silent;
		}

		@DynamoDBAttribute(attributeName = "colorTheme")
		public String getColorTheme() {
			return colorTheme;
		}

		public void setColorTheme(String colorTheme) {
			this.colorTheme = colorTheme;
		}
	}
}
