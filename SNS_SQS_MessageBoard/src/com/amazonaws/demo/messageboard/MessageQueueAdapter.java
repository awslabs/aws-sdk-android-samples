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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.amazonaws.services.sqs.model.Message;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageQueueAdapter extends ArrayAdapter<Message> {

	protected List<Message> messages = new ArrayList<Message>(1000);

	public MessageQueueAdapter(Context context, int resourceId) {
		super(context, resourceId);
	}

	public int getCount() {
		return this.messages.size();
	}

	public Message getItem(int pos) {
		return this.messages.get(pos);
	}

	public String getMessageText(int pos) {
		String messageText = "";

		Message message = this.getItem(pos);
		if (message != null && message.getBody() != null) {
			messageText = this.extractMessageFromJsonObject(message.getBody());
		}

		return messageText;
	}

	public void removeItemAt(int pos) {
		this.messages.remove(pos);
	}

	public View getView(int pos, View convertView, ViewGroup parent) {
		TextView messageText = new TextView(parent.getContext());
		messageText.setText(this.getMessageText(pos));
		messageText.setGravity(Gravity.LEFT);
		messageText.setPadding(10, 10, 10, 10);
		messageText.setMaxWidth(200);
		messageText.setMinWidth(200);
		messageText.setTextSize(16);

		return messageText;
	}

	private String extractMessageFromJsonObject(String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> jsonDic = mapper.readValue(json,
					new TypeReference<Map<String, Object>>() {
					});

			return (String) jsonDic.get("Message");
		} catch (Exception exception) {
			return null;
		}
	}

	public List<Message> getMessages() {
		return messages;
	}
}
