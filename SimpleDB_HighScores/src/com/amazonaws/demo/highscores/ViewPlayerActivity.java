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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewPlayerActivity extends Activity {
	
	protected Button okButton;

	protected TextView playerText;
	protected TextView scoreText;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_player);

        Intent intent = this.getIntent();
        HighScore player = (HighScore)intent.getSerializableExtra( "player" );
                
        playerText = (TextView)findViewById(R.id.playerNameText);
        playerText.setText( player.getPlayer() );
        
        playerText = (TextView)findViewById(R.id.scoreText);
        playerText.setText( String.valueOf( player.getScore() ) );
                
        okButton = (Button)findViewById(R.id.okButton); 
        okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewPlayerActivity.this.finish();				
			}
		});
    }
}
