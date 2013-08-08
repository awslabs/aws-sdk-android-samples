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

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.amazonaws.demo.anonymous.CustomListActivity;


public class SdbDomainList extends CustomListActivity {

	protected List<String> domainNameList;
	
	private static final String SUCCESS = "Domain List";
	public static final int NUM_DOMAINS = 5;
	
	private final Runnable postResults = new Runnable() {
		@Override
		public void run(){
			updateUi(domainNameList, SUCCESS);
		}
	};
	
	private final Runnable postMore = new Runnable() {
		@Override
		public void run(){
			updateList(domainNameList);
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enablePagination();
        startPopulateList();
    }
    
    @Override
    protected void obtainListItems(){
		domainNameList = SimpleDB.getDomainNames(NUM_DOMAINS);
        getHandler().post(postResults);
    }
    
    @Override
    protected void obtainMoreItems(){
		Log.e("DOMAIN", "buttonPressed");
    	domainNameList = SimpleDB.getMoreDomainNames();
    	for(String domain: domainNameList)
    		Log.e("DOMAIN", domain);
    	getHandler().post(postMore);
    }
    
   	@Override	
	protected void wireOnListClick(){
		getItemList().setOnItemClickListener(new OnItemClickListener() {
		    @Override
		    public void onItemClick(AdapterView<?> list, View view, int position, long id) {
			    	final String domainName = ((TextView)view).getText().toString();
					Intent bucketViewIntent = new Intent(SdbDomainList.this, SdbItemList.class);
					bucketViewIntent.putExtra( SimpleDB.DOMAIN_NAME, domainName );
					startActivity(bucketViewIntent);
		    }
		 });
	}
}
