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

package com.amazonaws.demo.messageboard;

public class Constants {

	public static final String ACCESS_KEY_ID = "CHANGE ME";
	public static final String SECRET_KEY = "CHANGE ME";
	public static final String TOPIC_NAME = "MessageBoard";
	public static final String QUEUE_NAME = "message-board-queue";
	
	public static final String CONFIRM_SUBSCRIPTION_MESSAGE = "A confirmation must be accepted before messages are received.";
	public static final String QUEUE_NOTICE                  = "It may take a few minutes before the queue starts receiving messages.";
	
}
