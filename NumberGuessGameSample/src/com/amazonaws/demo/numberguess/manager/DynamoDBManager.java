/**
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.numberguess.manager;

import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.demo.numberguess.Constants;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

import java.util.ArrayList;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";
    private static AmazonDynamoDBClient ddb;

    /**
     * Initializes the AmazonDynamoDBClient. This must be called before calling
     * any methods in this class.
     */
    public static void init() {
        if (ddb == null) {
            ddb = new AmazonDynamoDBClient(
                    CognitoClientManager.getCredentials());
        }
    }

    /**
     * Saves the object given into DynamoDB, using the default configuration.
     * 
     * @param userId Cognito Identity Id
     * @param score game score
     * @param userName a name to display
     */
    public static void saveRecord(String userId, int score, String userName) {
        Log.d(TAG, "Insert record called");
        checkDynamoDBClientAvailability();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        try {
            GameRecord r = new GameRecord();
            r.setUserId(userId);
            r.setScore(score);
            r.setUserName(userName);
            r.setGameId(Constants.GAME_ID);

            Log.d(TAG, "Inserting record");
            mapper.save(r);
            Log.d(TAG, "record inserted");
        } catch (AmazonServiceException ex) {
            throw new IllegalStateException("Error inserting record", ex);
        }
    }

    /**
     * Queries an Amazon DynamoDB table and returns the matching results, using
     * the default configuration.
     * 
     * @return a list of game records sorted by scores
     */
    public static ArrayList<GameRecord> getGameRecords() {
        Log.d(TAG, "Get record rank called");
        checkDynamoDBClientAvailability();

        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        GameRecord r = new GameRecord();
        r.setGameId(Constants.GAME_ID);

        Condition rangeKeyCondition = new Condition();
        rangeKeyCondition.withComparisonOperator(ComparisonOperator.GT)
                .withAttributeValueList(new AttributeValue().withN("0"));

        DynamoDBQueryExpression<GameRecord> queryExpression = new DynamoDBQueryExpression<GameRecord>()
                .withHashKeyValues(r)
                .withRangeKeyCondition("score", rangeKeyCondition)
                .withConsistentRead(false).withScanIndexForward(false);

        try {
            Log.d(TAG, "Querying game records");
            PaginatedQueryList<GameRecord> result = mapper.query(
                    GameRecord.class, queryExpression);
            Log.d(TAG, "Query result successfully received");
            ArrayList<GameRecord> resultList = new ArrayList<GameRecord>();
            if (result == null) {
                return resultList;
            }
            for (GameRecord up : result) {
                resultList.add(up);
            }
            return resultList;
        } catch (AmazonServiceException ex) {
            throw new IllegalStateException("Error querying", ex);
        }
    }

    /**
     * Gets a specific record according to the given userId.
     * 
     * @param userId Cognito Identity Id of the user
     * @return an instance of GameRecord
     */
    public static GameRecord getRecord(int userId) {
        Log.d(TAG, "Get record called");
        checkDynamoDBClientAvailability();

        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        try {
            Log.d(TAG, "Loading game record");
            GameRecord n = mapper.load(GameRecord.class, userId);
            Log.d(TAG, "Loaded successfully");
            return n;
        } catch (AmazonServiceException ex) {
            throw new IllegalStateException("Error getting record", ex);
        }
    }

    /**
     * Updates one attribute/value pair for the specified record.
     * 
     * @param r a record to be updated
     */
    public static void updateRecord(GameRecord r) {
        Log.d(TAG, "Update record called");
        checkDynamoDBClientAvailability();

        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        try {
            Log.d(TAG, "Updating record");
            mapper.save(r);
            Log.d(TAG, "Updated successfully");
        } catch (AmazonServiceException ex) {
            throw new IllegalStateException("Error updating record", ex);
        }
    }

    /**
     * Deletes the specified record and all of its attribute/value pairs.
     * 
     * @param r a record to be deleted
     */
    public static void deleteRecord(GameRecord r) {
        Log.d(TAG, "Delete record called");
        checkDynamoDBClientAvailability();

        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        try {
            Log.d(TAG, "Deleting record");
            mapper.delete(r);
            Log.d(TAG, "Deleted successfully");
        } catch (AmazonServiceException ex) {
            throw new IllegalStateException("Error deleting record", ex);
        }
    }

    private static void checkDynamoDBClientAvailability() {
        if (ddb == null) {
            throw new IllegalStateException(
                    "DynamoDB client not initialized yet");
        }
    }

    @DynamoDBTable(tableName = Constants.DB_TABLE_NAME)
    public static class GameRecord {
        private String userId;
        private String userName;
        private int gameId;
        private int score;

        @DynamoDBHashKey(attributeName = "userId")
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @DynamoDBAttribute(attributeName = "userName")
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        @DynamoDBIndexHashKey(attributeName = "gameId", globalSecondaryIndexName = Constants.DB_INDEX_NAME)
        public int getGameId() {
            return gameId;
        }

        public void setGameId(int gameId) {
            this.gameId = gameId;
        }

        @DynamoDBIndexRangeKey(attributeName = "score", globalSecondaryIndexName = Constants.DB_INDEX_NAME)
        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }
}
