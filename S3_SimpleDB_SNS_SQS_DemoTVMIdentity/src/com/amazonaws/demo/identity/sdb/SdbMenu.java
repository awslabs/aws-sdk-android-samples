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
package com.amazonaws.demo.identity.sdb;

import com.amazonaws.demo.identity.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SdbMenu extends Activity {

	Button createDomainButton;
	Button createItemButton;
	Button listDomainButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdb_menu);
        wireButtons();
    }
    
    public void wireButtons(){
    	createItemButton = (Button) findViewById(R.id.sdb_main_create_item_button);
    	createDomainButton = (Button) findViewById(R.id.sdb_main_create_domain_button);
    	listDomainButton = (Button) findViewById(R.id.sdb_main_view_domains_button);
    	
    	createItemButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SdbMenu.this, SdbItemCreate.class));
			}
		});
    	
    	createDomainButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SdbMenu.this, SdbDomainCreate.class));
			}
		});
    	
    	listDomainButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SdbMenu.this, SdbDomainList.class));
			}
		});
    }
}
