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
package com.amazonaws.demo.anonymous.sns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.demo.anonymous.AWSAndroidDemoTVM;
import com.amazonaws.services.sns.*;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;

public class SimpleNotification {

	public static final String TOPIC_ARN = "_Topic_Arn";

	public static AmazonSNSClient getInstance() {
		return AWSAndroidDemoTVM.clientManager.sns();
	}

	public static List<String> getTopicNames() {

		try {
			List<Topic> topics = getInstance().listTopics().getTopics();
			Iterator<Topic> tIter = topics.iterator();
			List<String> topicNames = new ArrayList<String>(topics.size());
			while (tIter.hasNext()) {
				topicNames.add(((Topic) tIter.next()).getTopicArn());
			}
			return topicNames;
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static List<String> getSubscriptionNamesByTopic(String topicARN) {

		try {
			List<Subscription> subscriptions;
			if (topicARN != null) {
				ListSubscriptionsByTopicRequest req = new ListSubscriptionsByTopicRequest(
						topicARN);
				subscriptions = getInstance().listSubscriptionsByTopic(req)
						.getSubscriptions();
			} else {
				subscriptions = getInstance().listSubscriptions()
						.getSubscriptions();
			}
			Iterator<Subscription> sIter = subscriptions.iterator();
			List<String> subscriptionNames = new ArrayList<String>(
					subscriptions.size());
			while (sIter.hasNext()) {
				subscriptionNames.add(((Subscription) sIter.next())
						.getEndpoint());
			}
			return subscriptionNames;
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static List<String> getSubscriptionNames() {
		return getSubscriptionNamesByTopic(null);
	}

	public static CreateTopicResult createTopic(String name) {
		CreateTopicRequest req = new CreateTopicRequest(name);

		try {
			return getInstance().createTopic(req);
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static void deleteTopic(String name) {
		DeleteTopicRequest req = new DeleteTopicRequest(name);

		try {
			getInstance().deleteTopic(req);
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

	public static PublishResult publish(String topicARN, String msg) {
		PublishRequest req = new PublishRequest(topicARN, msg);

		try {
			return getInstance().publish(req);
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static SubscribeResult subscribe(String topicArn, String protocol,
			String endpoint) {
		SubscribeRequest req = new SubscribeRequest(topicArn, protocol,
				endpoint);

		try {
			return getInstance().subscribe(req);
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static String getArnFromTopic(String topicName) {
		List<String> listOfTopicARN = getTopicNames();
		Iterator<String> aIter = listOfTopicARN.iterator();
		while (aIter.hasNext()) {
			String temp = aIter.next();
			if (temp.endsWith(topicName))
				return temp;
		}
		return null;
	}

}
