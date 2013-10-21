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
package com.amazonaws.demo.feedback;

import android.util.Log;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

/*
 * Uses Amazon SES http://aws.amazon.com/ses/
 * API: SendEmail http://docs.amazonwebservices.com/ses/latest/APIReference/API_SendEmail.html
 */
public class SESManager {
	private static final String LOG_TAG = "SESManager";
	
	/** Send the feedback message based on data entered */
    public static boolean sendFeedbackEmail(String comments, String name, float rating) {
    	String subjectText = "Feedback from " + name;
    	Content subjectContent = new Content(subjectText);
    	
    	String bodyText = "Rating: " + rating + "\nComments\n" + comments;
    	Body messageBody = new Body(new Content(bodyText));	
    	
    	Message feedbackMessage = new Message(subjectContent,messageBody);
    	
    	String email = PropertyLoader.getInstance().getVerifiedEmail();
    	Destination destination = new Destination().withToAddresses(email);
    	
    	SendEmailRequest request = new SendEmailRequest(email,destination,feedbackMessage);
    	try {
    		FeedbackFormDemoActivity.clientManager.ses().sendEmail(request);
    	}
    	catch (Throwable e) {
    		Log.e( LOG_TAG, "Error sending mail" + e );
    		return false;
    	}
    	
    	return true;
    }
}
