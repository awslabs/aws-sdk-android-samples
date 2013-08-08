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
package com.amazonaws.demo.identity;

import java.util.List;

import com.amazonaws.demo.identity.R;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public abstract class CustomListActivity extends AlertActivity {
	
	protected Handler mHandler;
	protected ListView itemList;
	protected TextView loadingText;
	protected Button moreButton;
	protected ArrayAdapter<String> itemListAdapter;
		
	public static final int LEFT = 0;
	public static final int CENTER = 1;
		
	private final Runnable postWaitingForMore = new Runnable() {
		@Override
		public void run(){
			moreButton.setOnClickListener(null);
		}
	};
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_list);
        mHandler = new Handler();
        itemList = (ListView) findViewById(R.id.item_list_view);
        loadingText = (TextView) findViewById(R.id.item_list_loading_text);
        moreButton = (Button) findViewById(R.id.item_list_more_button);
    }
    	
	protected void updateUi(String[] list, String successMessage){
		updateUi(list, successMessage, CENTER);
    }
    
	protected void updateUi(List<String> list, String successMessage){
		updateUi(list, successMessage, CENTER);
    }
	
	protected void updateUi(List<String> list, String successMessage, int justify){
    	loadingText.setText(successMessage);
    	loadingText.setTextSize(16);
    	if(justify == LEFT){
    		itemListAdapter = new ArrayAdapter<String>(this, R.layout.row_left, list);
    	} else if(justify == CENTER){
    		itemListAdapter = new ArrayAdapter<String>(this, R.layout.row, list);
    	}
        itemList.setAdapter(itemListAdapter);
    	itemListAdapter.notifyDataSetChanged();
		wireOnListClick();
    }
	
	protected void updateUi(String[] list, String successMessage, int justify){
    	loadingText.setText(successMessage);
    	loadingText.setTextSize(16);
    	if(justify == LEFT){
    		itemListAdapter = new ArrayAdapter<String>(this, R.layout.row_left);
    	} else if(justify == CENTER){
    		itemListAdapter = new ArrayAdapter<String>(this, R.layout.row);
    	}
        itemList.setAdapter(itemListAdapter);
        for(String item : list){
        	itemListAdapter.add(item);
        }
    	itemListAdapter.notifyDataSetChanged();
		wireOnListClick();
    }
	
    protected void updateList(String[] itemNameList){
    	for(String item: itemNameList){
    		itemListAdapter.add(item);
    	}
    	itemListAdapter.notifyDataSetChanged();
		wireOnListClick();
    }
    
    protected void updateList(List<String> itemNameList){
    	if(itemNameList.size() == 0){
    		disablePagination();
    	} else {
    		enablePagination();
	    	for(String item: itemNameList){
	    		itemListAdapter.add(item);
	    	}
	    	itemListAdapter.notifyDataSetChanged();
			wireOnListClick();
    	}
    }
    
    
    protected void startPopulateList(){
    	Thread t = new Thread() {
    		@Override
    		public void run(){
    			try{
    				obtainListItems();
    			} catch (AmazonS3Exception e) {
    				if("ExpiredToken".equals(e.getErrorCode())){
    					putRefreshError();
    				}
    			} catch (AmazonServiceException e) {
    				String errorCode = e.getErrorCode();
    				if("InvalidClientTokenId".equals(errorCode) || "InvalidAccessKeyId".equals(errorCode) || "ExpiredToken".equals(errorCode)){
    					putRefreshError();
    				}
    			} catch(Throwable e){
    				setStackAndPost(e);
    			}
    		}
    	};
    	t.start();
    }
    
    protected void getMoreItems(){
    	Thread t = new Thread() {
    		@Override
    		public void run(){
    			try{
    				mHandler.post(postWaitingForMore);
    				obtainMoreItems();
    			} catch (AmazonS3Exception e) {
    				if("ExpiredToken".equals(e.getErrorCode())){
    					putRefreshError();
    				}
    			} catch (AmazonServiceException e) {
    				String errorCode = e.getErrorCode();
    				if("InvalidClientTokenId".equals(errorCode) || "InvalidAccessKeyId".equals(errorCode) || "ExpiredToken".equals(errorCode)){
    					putRefreshError();
    				}
    			} catch(Throwable e){
    				setStackAndPost(e);
    			}
    	}
    };
    	t.start();
    }
    
    protected abstract void obtainListItems();
    
    protected void obtainMoreItems(){
    	return;
    }
	
    public Handler getHandler(){
    	return mHandler;
    }
    
	public ListView getItemList(){
		return itemList;
	}
	
	public void enablePagination(){
		moreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMoreItems();
			}
		});
		moreButton.setVisibility(View.VISIBLE);
	}
	
	public void disablePagination(){
		moreButton.setOnClickListener(null);
		moreButton.setVisibility(View.INVISIBLE);
	}
    
    protected void wireOnListClick(){
    	return;
    }
    
    
}
