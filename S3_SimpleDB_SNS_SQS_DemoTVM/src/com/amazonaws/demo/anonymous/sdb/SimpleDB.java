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
package com.amazonaws.demo.anonymous.sdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.demo.anonymous.AWSAndroidDemoTVM;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;

public class SimpleDB {

	private static String nextToken = null;
	private static int prevNumDomains = 0;
	public static final String DOMAIN_NAME = "_domain_name";

	public static AmazonSimpleDBClient getInstance() {
		return AWSAndroidDemoTVM.clientManager.sdb();
	}

	public static List<String> getDomainNames() {
		try {
			return getInstance().listDomains().getDomainNames();
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static List<String> getDomainNames(int numDomains) {
		prevNumDomains = numDomains;
		return getDomainNames(numDomains, null);
	}

	private static List<String> getDomainNames(int numDomains, String nextToken) {
		ListDomainsRequest req = new ListDomainsRequest();
		req.setMaxNumberOfDomains(numDomains);
		if (nextToken != null)
			req.setNextToken(nextToken);

		try {
			ListDomainsResult result = getInstance().listDomains(req);
			List<String> domains = result.getDomainNames();
			SimpleDB.nextToken = result.getNextToken();
			return domains;
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static List<String> getMoreDomainNames() {
		if (nextToken == null) {
			return new ArrayList<String>();
		} else {
			return getDomainNames(prevNumDomains, nextToken);
		}

	}

	public static void createDomain(String domainName) {
		try {
			getInstance().createDomain(new CreateDomainRequest(domainName));
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

	public static void deleteDomain(String domainName) {
		try {
			getInstance().deleteDomain(new DeleteDomainRequest(domainName));
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

	public static void createItem(String domainName, String itemName) {
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>(
				1);
		attributes.add(new ReplaceableAttribute().withName("Name").withValue(
				"Value"));

		try {
			getInstance().putAttributes(
					new PutAttributesRequest(domainName, itemName, attributes));
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

	public static void createAttributeForItem(String domainName,
			String itemName, String attributeName, String attributeValue) {
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>(
				1);
		attributes.add(new ReplaceableAttribute().withName(attributeName)
				.withValue(attributeValue).withReplace(true));

		try {
			getInstance().putAttributes(
					new PutAttributesRequest(domainName, itemName, attributes));
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

	public static String[] getItemNamesForDomain(String domainName) {
		SelectRequest selectRequest = new SelectRequest(
				"select itemName() from `" + domainName + "`")
				.withConsistentRead(true);

		try {
			List<Item> items = getInstance().select(selectRequest).getItems();

			String[] itemNames = new String[items.size()];
			for (int i = 0; i < items.size(); i++) {
				itemNames[i] = ((Item) items.get(i)).getName();
			}

			return itemNames;
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static HashMap<String, String> getAttributesForItem(
			String domainName, String itemName) {

		try {
			GetAttributesRequest getRequest = new GetAttributesRequest(
					domainName, itemName).withConsistentRead(true);
			GetAttributesResult getResult = getInstance().getAttributes(
					getRequest);

			HashMap<String, String> attributes = new HashMap<String, String>(30);
			for (Object attribute : getResult.getAttributes()) {
				String name = ((Attribute) attribute).getName();
				String value = ((Attribute) attribute).getValue();

				attributes.put(name, value);
			}

			return attributes;
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}

		return null;
	}

	public static void updateAttributesForItem(String domainName,
			String itemName, HashMap<String, String> attributes) {
		List<ReplaceableAttribute> replaceableAttributes = new ArrayList<ReplaceableAttribute>(
				attributes.size());

		for (String attributeName : attributes.keySet()) {
			replaceableAttributes
					.add(new ReplaceableAttribute().withName(attributeName)
							.withValue(attributes.get(attributeName))
							.withReplace(true));
		}

		try {
			getInstance().putAttributes(
					new PutAttributesRequest(domainName, itemName,
							replaceableAttributes));
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

	public static void deleteItem(String domainName, String itemName) {
		try {
			getInstance().deleteAttributes(
					new DeleteAttributesRequest(domainName, itemName));
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

	public static void deleteItemAttribute(String domainName, String itemName,
			String attributeName) {
		try {
			getInstance().deleteAttributes(
					new DeleteAttributesRequest(domainName, itemName)
							.withAttributes(new Attribute[] { new Attribute()
									.withName(attributeName) }));
		} catch (AmazonServiceException ex) {
			AWSAndroidDemoTVM.clientManager.wipeCredentialsOnAuthError(ex);
		}
	}

}
