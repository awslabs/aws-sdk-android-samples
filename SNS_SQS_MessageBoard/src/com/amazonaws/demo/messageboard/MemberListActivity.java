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
import java.util.HashMap;
import java.util.List;

import com.amazonaws.demo.messageboard.R;
import com.amazonaws.services.sns.model.Subscription;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

// Activity used to display the subscribers for the topic.
public class MemberListActivity extends Activity {
	protected static final String[] MENU_ITEMS = new String[] { "Delete",
			"Cancel" };

	protected Button backButton;
	protected ListView memberList;
	protected SimpleAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.member_list);

		this.backButton = (Button) findViewById(R.id.backButton);
		this.backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MemberListActivity.this.finish();
			}
		});

		this.memberList = (ListView) findViewById(R.id.listView);
		registerForContextMenu(this.memberList);

		this.loadData();
	}

	protected void loadData() {
		new ListSubscribersTask().execute();
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			@SuppressWarnings("unchecked")
			HashMap<String, String> data = (HashMap<String, String>) this.adapter
					.getItem(info.position);
			menu.setHeaderTitle(data.get("endpoint"));
			for (int i = 0; i < MENU_ITEMS.length; i++) {
				menu.add(Menu.NONE, i, i, MENU_ITEMS[i]);
			}
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String menuItemName = MENU_ITEMS[menuItemIndex];

		if (menuItemName.equals("Delete")) {
			@SuppressWarnings("unchecked")
			HashMap<String, String> data = (HashMap<String, String>) this.adapter
					.getItem(info.position);
			
			new RemoveSubscriberTask().execute(data.get("arn"));
		}

		return true;
	}

	class Subscribers extends ArrayList<HashMap<String, String>> {
		private static final long serialVersionUID = 1L;

		public Subscribers(List<Subscription> subscribers) {
			super(subscribers.size());

			for (Subscription sub : subscribers) {
				HashMap<String, String> data = new HashMap<String, String>(2);
				data.put("endpoint", sub.getEndpoint());
				data.put("protocol", sub.getProtocol());
				data.put("arn", sub.getSubscriptionArn());

				super.add(data);
			}
		}
	}

	private class ListSubscribersTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			Subscribers subscribers = new Subscribers(MessageBoard.instance()
					.listSubscribers());
			adapter = new SimpleAdapter(MemberListActivity.this, subscribers,
					R.layout.listitem, new String[] { "endpoint", "protocol" },
					new int[] { R.id.endpoint, R.id.protocol });

			return null;
		}

		protected void onPostExecute(Void result) {

			memberList.setAdapter(adapter);
		}
	}
	
	private class RemoveSubscriberTask extends AsyncTask<String, Void, Void> {

		protected Void doInBackground(String... inputs) {

			MessageBoard.instance().removeSubscriber(inputs[0]);
			
			return null;
		}

		protected void onPostExecute(Void result) {

			loadData();
			memberList.invalidateViews();
		}
	}
}
