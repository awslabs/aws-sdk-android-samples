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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScoresAdapter extends ArrayAdapter<HighScore> {

	protected List<HighScore> scores = new ArrayList<HighScore>(100);

	public ScoresAdapter(Context context, int resourceId) {
		super(context, resourceId);
	}

	public int getCount() {
		return scores.size();
	}

	public HighScore getItem(int pos) {
//		if (scores.isEmpty()) {
//			scores.addAll(this.highScores.getHighScores());
//		} else if (scores.size() <= pos) {
//			scores.addAll(this.highScores.getNextPageOfScores());
//		}

		return scores.get(pos);
	}

	public void removeItemAt(int pos) {
		this.scores.remove(pos);
	}

	public View getView(int pos, View convertView, ViewGroup parent) {
		final HighScore score = this.getItem(pos);

		LinearLayout layout = new LinearLayout(parent.getContext());

		TextView playerText = new TextView(parent.getContext());
		playerText.setText(score.getPlayer());
		playerText.setGravity(Gravity.LEFT);
		playerText.setPadding(10, 10, 10, 10);
		playerText.setTextSize(16);

		TextView scoreText = new TextView(parent.getContext());
		scoreText.setText(String.valueOf(score.getScore()));
		scoreText.setGravity(Gravity.RIGHT);
		scoreText.setPadding(10, 10, 10, 10);
		scoreText.setTextSize(16);

		layout.addView(playerText);
		layout.addView(scoreText);

		return layout;
	}

	public List<HighScore> getScores() {
		return scores;
	}
}