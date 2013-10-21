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
package com.amazonaws.demo.sqs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.demo.AWSAndroidDemo;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;

public class SimpleQueue {
	
	private static List<Message> lastRecievedMessages = null;
	public static final String QUEUE_URL = "_queue_url"; 
	public static final String MESSAGE_INDEX = "_message_index";
	public static final String MESSAGE_ID = "_message_id";
	
	public static AmazonSQSClient getInstance() {
        return AWSAndroidDemo.clientManager.sqs();
	}
	
	public static CreateQueueResult createQueue(String queueName){
		CreateQueueRequest req = new CreateQueueRequest(queueName);
		return getInstance().createQueue(req);
	}
	
	public static List<String> getQueueUrls(){
		return getInstance().listQueues().getQueueUrls();
	}

	
	public static List<String> recieveMessageBodies(String queueUrl){
		List<String> messageBodies = new ArrayList<String>();
		ReceiveMessageRequest req = new ReceiveMessageRequest(queueUrl);
		lastRecievedMessages = getInstance().receiveMessage(req).getMessages();
		for(Message m : lastRecievedMessages){
			messageBodies.add(m.getBody().toString());
		}
		return messageBodies;
	}
	
	public static List<String> recieveMessageIds(String queueUrl){
		List<String> messageIds = new ArrayList<String>();
		ReceiveMessageRequest req = new ReceiveMessageRequest(queueUrl);
		lastRecievedMessages = getInstance().receiveMessage(req).getMessages();
		for(Message m : lastRecievedMessages){
			messageIds.add(m.getMessageId().toString());
		}
		return messageIds;
	}
	
	
	
	public static String getMessageBody(int messageIndex){
		if(lastRecievedMessages == null) {
			return new String();
		} else {
			return lastRecievedMessages.get(messageIndex).getBody().toString();
		}
	}
	
	public static SendMessageResult sendMessage(String queueUrl, String body){
		SendMessageRequest req = new SendMessageRequest(queueUrl, body);
		return getInstance().sendMessage(req);
	}
	
	public static void deleteMessage(String queueName, String msgHandle){
		DeleteMessageRequest req = new DeleteMessageRequest(createQueue(queueName).getQueueUrl(), msgHandle);
		getInstance().deleteMessage(req);
	}

	public static String getQueueArn(String queueUrl){
		GetQueueAttributesRequest req = new GetQueueAttributesRequest(queueUrl).withAttributeNames("QueueArn");
		return getInstance().getQueueAttributes(req).getAttributes().get("QueueArn").toString();
	}
	
    public static void allowNotifications(String queueUrl, String topicArn){
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("Policy", generateSqsPolicyForTopic(getQueueArn(queueUrl), topicArn));
        getInstance().setQueueAttributes(new SetQueueAttributesRequest(queueUrl, attributes));
    }
	
    private static String generateSqsPolicyForTopic(String queueArn, String topicArn) {
        String policy =
            "{ " +
            "  \"Version\":\"2008-10-17\"," +
            "  \"Id\":\"" + queueArn + "/policyId\"," +
            "  \"Statement\": [" +
            "    {" +
            "        \"Sid\":\"" + queueArn + "/statementId\"," +
            "        \"Effect\":\"Allow\"," +
            "        \"Principal\":{\"AWS\":\"*\"}," +
            "        \"Action\":\"SQS:SendMessage\"," +
            "        \"Resource\": \"" + queueArn + "\"," +
            "        \"Condition\":{" +
            "            \"StringEquals\":{\"aws:SourceArn\":\"" + topicArn + "\"}" +
            "        }" +
            "    }" +
            "  ]" +
            "}";

        return policy;
    }
    


}
