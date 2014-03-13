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

import com.amazonaws.demo.highscores.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddScoreActivity extends Activity {

	protected Button addButton;
	protected Button cancelButton;

	protected EditText player;
	protected EditText score;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_score);

		player = (EditText) findViewById(R.id.player);
		score = (EditText) findViewById(R.id.score);

		addButton = (Button) findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String player = AddScoreActivity.this.player.getText()
						.toString();
				int score = Integer.parseInt(AddScoreActivity.this.score
						.getText().toString());

				HighScore newScore = new HighScore(player, score);
				new AddHighScoreTask().execute(newScore);
			}
		});

		cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddScoreActivity.this.finish();
			}
		});
	}

	private class AddHighScoreTask extends AsyncTask<HighScore, Void, Void> {

		protected Void doInBackground(HighScore... highScores) {

			HighScoreList hs = new HighScoreList();
			hs.createHighScoresDomain();
			hs.addHighScore(highScores[0]);

			return null;
		}

		protected void onPostExecute(Void result) {

			AddScoreActivity.this.finish();
		}
	}
}
