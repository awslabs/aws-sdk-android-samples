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

package com.amazonaws.demo;

public class Constants {

    /**
     * Identity Pool Id associated with the app see the readme for details on
     * what to fill this field in with.
     */
    public static final String IDENTITY_POOL_ID = "CHANGE ME!";

    /**
     * Mobile Analytics App Id associated with the app see the readme for
     * details on what to fill this field in with.
     */
    public static final String MOBILE_ANALYTICS_APP_ID = "CHANGE ME!";

    /**
     * The following 4 fields specifies the names used for Cognito Identity and
     * DynamoDB, which are associated with the settings of AWS services.
     */
    public static final String SYNC_DATASET_NAME = "SampleDataSet";
    public static final String SYNC_KEY_BEST = "MyBestScore";
    public static final String DB_TABLE_NAME = "NumberGuessHighScore";
    public static final String DB_INDEX_NAME = "gameIdIndex";

    public static final int GAME_ID = 100001;

    public static final int KEY_CODE_1 = 1;
    public static final int KEY_CODE_2 = 2;
    public static final int KEY_CODE_3 = 3;
    public static final int KEY_CODE_4 = 4;
    public static final int KEY_CODE_5 = 5;
    public static final int KEY_CODE_6 = 6;
    public static final int KEY_CODE_7 = 7;
    public static final int KEY_CODE_8 = 8;
    public static final int KEY_CODE_9 = 9;
    public static final int KEY_CODE_0 = 0;
    public static final int KEY_CODE_ENTER = 100;
    public static final int KEY_CODE_BACK = -1;

}
