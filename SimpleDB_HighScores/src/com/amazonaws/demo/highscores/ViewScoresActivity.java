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
package com.amazonaws.demo.highscores;

import java.util.List;

import com.amazonaws.demo.highscores.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ViewScoresActivity extends Activity {
	protected static final String[] MENU_ITEMS = new String[] { "View",
			"Delete", "Cancel" };
	protected ScoresAdapter scoresAdapter;

	protected Button backButton;
	protected ListView scores;
	protected int sortMethod;
	protected int totalCount = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = this.getIntent();
		sortMethod = intent.getIntExtra("SortMethod", HighScoreList.NO_SORT);

		setContentView(R.layout.high_score_table);

		backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewScoresActivity.this.finish();
			}
		});

		scores = (ListView) findViewById(R.id.listView);
		registerForContextMenu(scores);

		scores.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				System.out.println("onScroll called. firstVisibleItem: "
						+ firstVisibleItem + ", visibleItemCount: "
						+ visibleItemCount);
				if (scoresAdapter != null
						&& firstVisibleItem + visibleItemCount == scoresAdapter
								.getScores().size()
						&& scoresAdapter.getScores().size() != totalCount) {
					System.out.println("Load more!!!");
					new GetHighScoresTask().execute();
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
		});

		new GetHighScoresTask().execute();
		new GetTotalCountTask().execute();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(this.scoresAdapter.getItem(info.position)
					.getPlayer());
			for (int i = 0; i < MENU_ITEMS.length; i++) {
				menu.add(Menu.NONE, i, i, MENU_ITEMS[i]);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String menuItemName = MENU_ITEMS[menuItemIndex];

		if (menuItemName.equals("Delete")) {
			new RemoveUserTask().execute(info.position);
		} else if (menuItemName.equals("View")) {
			new GetUserInfoTask().execute(info.position);
		}

		return true;
	}

	private class GetHighScoresTask extends AsyncTask<Void, Void, Boolean> {

		protected Boolean doInBackground(Void... voids) {

			boolean shouldRefresh = false;

			HighScoreList hs = new HighScoreList(sortMethod);
			scoresAdapter = new ScoresAdapter(ViewScoresActivity.this,
					R.layout.list_item);
			List<HighScore> list = null;
			if (scoresAdapter.getScores().size() == 0) {
				list = hs.getHighScores();
				scoresAdapter.getScores().addAll(list);
				shouldRefresh = true;
			} else {
				list = hs.getNextPageOfScores();
				scoresAdapter.getScores().addAll(list);
				shouldRefresh = true;
			}

			return shouldRefresh;
		}

		protected void onPostExecute(Boolean shouldRefresh) {
			if (shouldRefresh) {
				if (scores.getAdapter() != null) {
					scoresAdapter.notifyDataSetChanged();
				} else {
					scores.setAdapter(scoresAdapter);
				}
			}
		}
	}

	private class RemoveUserTask extends AsyncTask<Integer, Void, Void> {

		protected Void doInBackground(Integer... inputs) {

			scoresAdapter.removeItemAt(inputs[0]);
			scoresAdapter.getScores().remove(inputs[0]);

			return null;
		}

		protected void onPostExecute(Void result) {
			scoresAdapter.notifyDataSetChanged();
		}
	}

	private class GetUserInfoTask extends AsyncTask<Integer, Void, HighScore> {

		protected HighScore doInBackground(Integer... inputs) {

			return new HighScoreList().getPlayer(scoresAdapter.getItem(
					inputs[0]).getPlayer());
		}

		protected void onPostExecute(HighScore player) {
			Intent intent = new Intent(ViewScoresActivity.this,
					ViewPlayerActivity.class);
			intent.putExtra("player", player);
			startActivity(intent);
		}
	}

	private class GetTotalCountTask extends AsyncTask<Void, Void, Integer> {

		protected Integer doInBackground(Void... voids) {

			return new HighScoreList().getCount();
		}

		protected void onPostExecute(Integer result) {
			TextView title = (TextView) findViewById(R.id.high_score_table_title);
			totalCount = result;
			title.setText("High Score List (" + totalCount + ")");
		}
	}
}