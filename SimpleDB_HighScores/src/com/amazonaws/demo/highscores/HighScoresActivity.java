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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class HighScoresActivity extends Activity {
	
	protected Button populateButton;
	protected Button addScoreButton;
	protected Button viewButton;
	protected Button clearButton;
	
	protected ToggleButton sortByPlayer;
	protected ToggleButton sortByScore;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sortByPlayer = (ToggleButton)findViewById(R.id.sortByPlayer);
        sortByPlayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HighScoresActivity.this.sortByScore.setChecked( false );
			}
		});
               
        sortByScore = (ToggleButton)findViewById(R.id.sortByScore);
        sortByScore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				HighScoresActivity.this.sortByPlayer.setChecked( false );
			}
		});
        

        populateButton = (Button)findViewById(R.id.populateButton); 
        populateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new PopulateHighScoresTask().execute();
			}
		});
        
        addScoreButton = (Button)findViewById(R.id.addScoreButton); 
        addScoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
    			startActivity(new Intent(HighScoresActivity.this, AddScoreActivity.class));
			}
		});
        
        viewButton = (Button)findViewById(R.id.viewButton); 
        viewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HighScoresActivity.this, ViewScoresActivity.class);
				if ( HighScoresActivity.this.sortByPlayer.isChecked() )	{
					intent.putExtra( "SortMethod", HighScoreList.PLAYER_SORT );
				}
				else if ( HighScoresActivity.this.sortByScore.isChecked() ) {
					intent.putExtra( "SortMethod", HighScoreList.SCORE_SORT );
				}
				
    			startActivity( intent );
			}
		});
        
        clearButton = (Button)findViewById(R.id.clearButton); 
        clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new ClearHighScoresTask().execute();
			}
		});
    }
    
    private class PopulateHighScoresTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			HighScoreList list = new HighScoreList();
            list.createHighScoresDomain();
            
			for (int i = 1; i <= 10; i++) {
                String playerName = Constants.getRandomPlayerName();
                int score = Constants.getRandomScore();
				HighScore hs = new HighScore( playerName, score );
				
				list.addHighScore(hs);
			}

			return null;
		}

		protected void onPostExecute(Void result) {

		}
	}
    
    private class ClearHighScoresTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			new HighScoreList().clearHighScores();

			return null;
		}

		protected void onPostExecute(Void result) {

		}
	}
}