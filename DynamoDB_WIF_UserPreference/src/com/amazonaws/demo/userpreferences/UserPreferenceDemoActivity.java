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

package com.amazonaws.demo.userpreferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UserPreferenceDemoActivity extends Activity {

	private static final String TAG = "UserPreferenceDemoActivity";
	private static final String success = "Welcome to the User Preferences Demo!";
    private static final String fail = "Load Failed. Please Try Restarting the Application.";
    
    protected Button createTableButton;
    protected Button insertUsersButton;
    protected Button listUsersButton;
    protected Button deleteTableButton;
    protected Button loginButton;
    protected Button logoutButton;
    protected TextView welcomeText;
    
	public static AmazonClientManager clientManager = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			clientManager = new AmazonClientManager( getSharedPreferences( "com.amazon.aws.demo.AWSDemo", Context.MODE_PRIVATE ), bundle);
		} catch (NameNotFoundException e) {
			displayErrorAndExit("Unable to load application bundle: " + e.getMessage());
		}

		
        welcomeText = (TextView) findViewById(R.id.main_into_text);   
		createTableButton = (Button) findViewById(R.id.create_table_bttn);
		insertUsersButton = (Button) findViewById(R.id.insert_users_bttn);
		listUsersButton = (Button) findViewById(R.id.list_users_bttn);
		deleteTableButton = (Button) findViewById(R.id.delete_table_bttn);
		loginButton = (Button) findViewById(R.id.main_login_button);
		logoutButton = (Button) findViewById(R.id.main_logout_button);
		
		if ( !clientManager.hasCredentials() ) {
            this.displayCredentialsIssueAndExit();
            welcomeText.setText(fail);
        }
        else if ( !clientManager.isLoggedIn() ) {
            welcomeText.setText(success);
            loginButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        }
        else {
            welcomeText.setText(success);
            createTableButton.setVisibility(View.VISIBLE);
            insertUsersButton.setVisibility(View.VISIBLE);
            listUsersButton.setVisibility(View.VISIBLE);
            deleteTableButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        } 
        
	}
	
	protected void onResume() {
        super.onResume();
        this.updateUI();

    }
	
	private void updateUI(){
		if (PropertyLoader.getInstance().hasCredentials()){
			welcomeText.setText(success);
            createTableButton.setVisibility(View.VISIBLE);
            insertUsersButton.setVisibility(View.VISIBLE);
            listUsersButton.setVisibility(View.VISIBLE);
            deleteTableButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            logoutButton.setVisibility(View.INVISIBLE);
            this.wireButtons();
		}
		else if ( !UserPreferenceDemoActivity.clientManager.isLoggedIn() ) {
            welcomeText.setText(success);
            createTableButton.setVisibility(View.INVISIBLE);
            insertUsersButton.setVisibility(View.INVISIBLE);
            listUsersButton.setVisibility(View.INVISIBLE);
            deleteTableButton.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        }
        else {
            loginButton.setVisibility(View.INVISIBLE);

            welcomeText.setText(success);
            createTableButton.setVisibility(View.VISIBLE);
            insertUsersButton.setVisibility(View.VISIBLE);
            listUsersButton.setVisibility(View.VISIBLE);
            deleteTableButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            this.wireButtons();
        } 
	}

	
	private void wireButtons(){
		createTableButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.i(TAG, "createTableBttn clicked.");
				new DynamoDBManagerTask()
				.execute(DynamoDBManagerType.CREATE_TABLE);
			}
		});
		insertUsersButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.i(TAG, "insertUsersBttn clicked.");

				new DynamoDBManagerTask()
				.execute(DynamoDBManagerType.INSERT_USER);
			}
		});

		listUsersButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.i(TAG, "listUsersBttn clicked.");

				new DynamoDBManagerTask()
						.execute(DynamoDBManagerType.LIST_USERS);
			}
		});

		deleteTableButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Log.i(TAG, "deleteTableBttn clicked.");

				new DynamoDBManagerTask()
						.execute(DynamoDBManagerType.CLEAN_UP);
			}
		});
		
		loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserPreferenceDemoActivity.this, Login.class));                
            }
        });
		
		logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientManager.clearCredentials();  
                clientManager.wipe();              
                displayLogoutSuccess();

                createTableButton.setVisibility(View.INVISIBLE);
                insertUsersButton.setVisibility(View.INVISIBLE);
                listUsersButton.setVisibility(View.INVISIBLE);
                deleteTableButton.setVisibility(View.INVISIBLE);
                logoutButton.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.VISIBLE);                
            }
        });
    }

	private class DynamoDBManagerTask
			extends
			AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {
		
		protected DynamoDBManagerTaskResult doInBackground(
				DynamoDBManagerType... types) {

			String tableStatus = DynamoDBManager.getTestTableStatus();

			DynamoDBManagerTaskResult result = new DynamoDBManagerTaskResult();
			result.setTableStatus(tableStatus);
			result.setTaskType(types[0]);

			if (types[0] == DynamoDBManagerType.CREATE_TABLE) {
				if (tableStatus.length() == 0) {
					DynamoDBManager.createTable();
				}
			} else if (types[0] == DynamoDBManagerType.INSERT_USER) {
				if (tableStatus.equalsIgnoreCase("ACTIVE")) {
					DynamoDBManager.insertUsers();
				}
			} else if (types[0] == DynamoDBManagerType.LIST_USERS) {
				if (tableStatus.equalsIgnoreCase("ACTIVE")) {
					DynamoDBManager.getUserList();
				}
			} else if (types[0] == DynamoDBManagerType.CLEAN_UP) {
				if (tableStatus.equalsIgnoreCase("ACTIVE")) {
					DynamoDBManager.cleanUp();
				}
			}

			return result;
		}

		protected void onPostExecute(DynamoDBManagerTaskResult result) {

			if (result.getTaskType() == DynamoDBManagerType.CREATE_TABLE) {
				
				if(result.getTableStatus().length() != 0)
				{
				Toast.makeText(
						UserPreferenceDemoActivity.this,
						"The test table already exists.\nTable Status: "
								+ result.getTableStatus(), Toast.LENGTH_LONG)
						.show();
				}

			} else if (result.getTaskType() == DynamoDBManagerType.LIST_USERS
					&& result.getTableStatus().equalsIgnoreCase("ACTIVE")) {
				
				startActivity(new Intent(UserPreferenceDemoActivity.this,
						UserListActivity.class));
				
			} else if (!result.getTableStatus().equalsIgnoreCase("ACTIVE")) {
				
				Toast.makeText(
						UserPreferenceDemoActivity.this,
						"The test table is not ready yet.\nTable Status: "
								+ result.getTableStatus(), Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private enum DynamoDBManagerType {
		GET_TABLE_STATUS, CREATE_TABLE, INSERT_USER, LIST_USERS, CLEAN_UP
	}

	private class DynamoDBManagerTaskResult {
		private DynamoDBManagerType taskType;
		private String tableStatus;

		public DynamoDBManagerType getTaskType() {
			return taskType;
		}

		public void setTaskType(DynamoDBManagerType taskType) {
			this.taskType = taskType;
		}

		public String getTableStatus() {
			return tableStatus;
		}

		public void setTableStatus(String tableStatus) {
			this.tableStatus = tableStatus;
		}
	}
	
	protected void displayCredentialsIssueAndExit() {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle("Credential Problem!");
        confirm.setMessage( "AWS Credentials not configured correctly.  Please review the README file." );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                UserPreferenceDemoActivity.this.finish();
            }
        } );
        confirm.show();                
    }
	protected void displayErrorAndExit( String msg ) {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        if ( msg == null ) { 
            confirm.setTitle("Error Code Unknown" );
            confirm.setMessage( "Please review the README file." );
        } 
        else {
            confirm.setTitle( "Error" );
            confirm.setMessage( msg + "\nPlease review the README file."  );
        }

        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
            	UserPreferenceDemoActivity.this.finish();
            }
        } );
        confirm.show();                
    }
	protected void displayLogoutSuccess() {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle("Logout");
        confirm.setMessage( "You have successfully logged out." );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
            }
        } );
        confirm.show();                
    }
}
