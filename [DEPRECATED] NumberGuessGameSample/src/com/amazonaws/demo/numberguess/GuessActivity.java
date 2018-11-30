/**
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.demo.numberguess;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.amazonaws.demo.numberguess.manager.CognitoClientManager;
import com.amazonaws.demo.numberguess.manager.CognitoSyncClientManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Dataset.SyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GuessActivity extends ListActivity {

    public static final String TIMES_OF_GUESSES = "times_of_guesses";
    public static final String TIME_TO_COMPLETE = "time_to_complete";
    public static final String BEST_SCORE = "best_score";

    private static final String TAG = "GuessActivity";
    private static List<Integer> allDigits;
    private static int sNumberlength = 4;

    private TextView textForKeyboard;
    private Button btnGuess;
    private Button btnNewGame;
    private Button btnShowAnswer;
    private ImageButton btnCloseKeyboard;
    private LinearLayout keyboardLayout;
    private SimpleAdapter simpleAdapter;
    private Keyboard keyboard;
    private KeyboardView keyboardView;
    private Animation slideDownAnim;
    private Animation slideUpAnim;

    private Dataset dataset;
    private ArrayList<HashMap<String, Object>> guessResults;
    private List<Integer> answerDigits;
    private String mMyBestScore;
    private int mNumberOfGuess;
    private int mSecondsUsed;
    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);
        initData();
        initUI();
    }

    private void initData() {
        /*
         * Initializes CognitoClientManager it must be called before you can use
         * it.
         */
        CognitoClientManager.init(this);
        initCognitoSync();
        mNumberOfGuess = 0;
        mSecondsUsed = 0;
        mStartTime = System.currentTimeMillis();
        guessResults = new ArrayList<HashMap<String, Object>>();
        allDigits = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        getRandomDigits();
    }

    private void initCognitoSync() {
        /* For unauthenticated users, will not sync user data */
        if (!CognitoClientManager.isAuthenticated()) {
            Log.d(TAG, "Unauthenticated user");
            return;
        }

        /*
         * Initializes CognitoSyncClientManager, it must be called before you
         * can use it.
         */
        CognitoSyncClientManager.init(this);
        dataset = CognitoSyncClientManager
                .openOrCreateDataset(Constants.SYNC_DATASET_NAME);
        dataset.synchronize(new SyncCallback() {
            @Override
            public void onSuccess(Dataset ds, List<Record> list) {
                mMyBestScore = ds.get(Constants.SYNC_KEY_BEST);
                Log.d(TAG, "Sync success");
            }

            @Override
            public void onFailure(DataStorageException ex) {
                Log.e(TAG, "Sync fails", ex);
            }

            @Override
            public boolean onDatasetsMerged(Dataset ds, List<String> list) {
                Log.d(TAG, "Datasets merged");
                return false;
            }

            @Override
            public boolean onDatasetDeleted(Dataset ds, String str) {
                Log.d(TAG, "Dataset deleted");
                return false;
            }

            @Override
            public boolean onConflict(Dataset ds, List<SyncConflict> list) {
                Log.d(TAG, "Conflict");
                return false;
            }
        });
    }

    private void getRandomDigits() {
        Collections.shuffle(allDigits);
        answerDigits = allDigits.subList(0, sNumberlength);
    }

    private void initUI() {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout headView = (LinearLayout) inflater.inflate(
                R.layout.head_view, null);
        getListView().addHeaderView(headView);
        keyboardLayout = (LinearLayout) findViewById(R.id.keyboardLayout);
        slideDownAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);
        slideDownAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                keyboardLayout.setVisibility(View.GONE);
            }
        });
        slideUpAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        slideUpAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                keyboardLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
            }
        });
        textForKeyboard = (TextView) findViewById(R.id.textViewForKeyboard);
        btnNewGame = (Button)findViewById(R.id.btnNewGame);
        btnNewGame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mNumberOfGuess = 0;
                mSecondsUsed = 0;
                mStartTime = System.currentTimeMillis();
                guessResults.clear();
                simpleAdapter.notifyDataSetChanged();
                getRandomDigits();
            }
        });
        btnShowAnswer = (Button)findViewById(R.id.btnShowAnswer);
        btnShowAnswer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showAnswerDialog();
            }
        });
        btnGuess = (Button) findViewById(R.id.btnGuess);
        btnGuess.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                openKeyboard();
            }
        });
        simpleAdapter = new SimpleAdapter(this, guessResults,
                R.layout.result_item, new String[] {
                        "answer", "result"
                },
                new int[] {
                        R.id.guessText, R.id.guessResult
                });
        simpleAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                    String textRepresentation) {
                switch (view.getId()) {
                    case R.id.guessText:
                        TextView guessText = (TextView) view;
                        guessText.setText((String) data);
                        return true;
                    case R.id.guessResult:
                        TextView resultText = (TextView) view;
                        resultText.setText((String) data);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);
        getListView().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                if (keyboardLayout.getVisibility() == View.VISIBLE
                        && event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeKeyboard();
                }
                return false;
            }
        });
        initKeyboard();
        btnCloseKeyboard = (ImageButton) findViewById(R.id.btnCloseKeyboard);
        btnCloseKeyboard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                closeKeyboard();
            }
        });
    }

    private void initKeyboard() {
        keyboard = new Keyboard(this, R.xml.keyboard);
        keyboardView = (KeyboardView) findViewById(R.id.keyboardview);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setPreviewEnabled(false);

        OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {
            @Override
            public void onKey(int code, int[] keyCodes) {
                String str = textForKeyboard.getText().toString();
                switch (code) {
                    case Constants.KEY_CODE_BACK:
                        if (!str.isEmpty()) {
                            textForKeyboard.setText(str.substring(0,
                                    str.length() - 1));
                        }
                        break;
                    case Constants.KEY_CODE_ENTER:
                        checkAnswer(str);
                        break;

                    default:
                        if (str.length() < sNumberlength) {
                            textForKeyboard.setText(str + code);
                        }
                }
            }

            @Override
            public void onPress(int arg0) {
            }

            @Override
            public void onRelease(int primaryCode) {
            }

            @Override
            public void onText(CharSequence text) {
            }

            @Override
            public void swipeDown() {
            }

            @Override
            public void swipeLeft() {
            }

            @Override
            public void swipeRight() {
            }

            @Override
            public void swipeUp() {
            }
        };
        keyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        openKeyboard();
    }

    /**
     * Game logic inside, no need to know about it.
     */
    private void checkAnswer(String str) {
        if (str.length() != sNumberlength) {
            return;
        }
        int numberOfSameDigitAndPlace = 0, numberOfSameDigitButDifPlace = 0;
        for (int i = 0; i < sNumberlength; i++) {
            int answer = Integer.parseInt(str.substring(i, i + 1));
            for (int j = 0; j < sNumberlength; j++) {
                if (answerDigits.get(j) == answer) {
                    if (i == j) {
                        numberOfSameDigitAndPlace++;
                    } else {
                        numberOfSameDigitButDifPlace++;
                    }
                    break;
                }
            }
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("answer", str);
        map.put("result", numberOfSameDigitAndPlace + "A "
                + numberOfSameDigitButDifPlace + "B");
        guessResults.add(map);
        simpleAdapter.notifyDataSetChanged();
        textForKeyboard.setText("");
        closeKeyboard();
        mNumberOfGuess++;
        if (numberOfSameDigitAndPlace == sNumberlength) {
            rightAnswerDialog(str);
        }
    }

    private void rightAnswerDialog(String answerStr) {
        mSecondsUsed = (int) (System.currentTimeMillis() - mStartTime) / 1000;
        new AlertDialog.Builder(GuessActivity.this).setCancelable(false)
                .setTitle(getString(R.string.you_got_right_answer))
                .setMessage(getString(R.string.the_number_is) + " " + answerStr)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mMyBestScore == null) {
                            mMyBestScore = "0";
                        }
                        Intent intent = new Intent();
                        intent.setClass(GuessActivity.this,
                                ResultActivity.class);
                        intent.putExtra(TIMES_OF_GUESSES, mNumberOfGuess);
                        intent.putExtra(TIME_TO_COMPLETE, mSecondsUsed);
                        intent.putExtra(BEST_SCORE,
                                Integer.valueOf(mMyBestScore));
                        startActivity(intent);
                        finish();
                    }
                }).show();
    }
    
    private void showAnswerDialog() {
        String str = "";
        for (int i : answerDigits) {
            str += i;
        }
        new AlertDialog.Builder(GuessActivity.this).setCancelable(false)
                .setTitle(getString(R.string.here_is_answer))
                .setMessage(str)
                .setCancelable(true).show();
    }

    public void openKeyboard() {
        keyboardView.setEnabled(true);
        keyboardLayout.startAnimation(slideUpAnim);
    }

    public void closeKeyboard() {
        keyboardView.setEnabled(false);
        keyboardLayout.startAnimation(slideDownAnim);
    }
}
