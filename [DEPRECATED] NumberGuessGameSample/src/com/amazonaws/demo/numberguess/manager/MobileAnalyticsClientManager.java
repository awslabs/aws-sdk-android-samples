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

package com.amazonaws.demo.numberguess.manager;

import android.content.Context;
import android.util.Log;

import com.amazonaws.demo.numberguess.Constants;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsConfig;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.AnalyticsEvent;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;
import com.amazonaws.regions.Regions;

public class MobileAnalyticsClientManager {
    private static final String TAG = "MobileAnalyticsClientManager";

    /*
     * Define keys for custom events of Mobile Analytics.
     */
    private static final String GAME_RESULT_EVENT_NAME = "gameResultEvent";
    private static final String GAME_RESULT_METRIC_TIME_TO_COMPLETE = "GameTime";
    private static final String GAME_RESULT_METRIC_TIMES_OF_GUESSES = "TimesOfGuesses";
    private static final String GAME_RESULT_METRIC_SCORE = "Score";

    private static MobileAnalyticsManager analytics;

    /**
     * Initializes the MobileAnalyticsManager. This must be called before
     * calling any methods in this class.
     * 
     * @param context a context of the app
     */
    public static void init(Context context) {
        if (analytics == null) {
            AnalyticsConfig options = new AnalyticsConfig();
            options.withAllowsWANDelivery(true);
            analytics = MobileAnalyticsManager.getOrCreateInstance(context,
                    Constants.MOBILE_ANALYTICS_APP_ID, Regions.US_EAST_1,
                    CognitoClientManager.getCredentials(), options);
        }
    }

    /**
     * Notify MobileAnalyticsManager that a session pause happened and submit
     * the events recorded. You may call this method in activity's onPause().
     */
    public static void pauseAndSubmit() {
        Log.d(TAG, "Pause and submit called");
        checkAnalyticsAvailability();
        analytics.getSessionClient().pauseSession();
        /*
         * Attempt to send any events that have been recorded to the Mobile
         * Analytics service.
         */
        analytics.getEventClient().submitEvents();
    }

    /**
     * Notify MobileAnalyticsManager that a session resume happened. You may
     * call this method in activity's onResume().
     */
    public static void resumeSession() {
        checkAnalyticsAvailability();
        analytics.getSessionClient().resumeSession();
    }

    /**
     * Record a custom event to the local filestore.
     */
    public static void recordGameResult(int timeToComplete, int timesOfGuesses,
            int score) {
        checkAnalyticsAvailability();
        AnalyticsEvent gameResultEvent = analytics
                .getEventClient()
                .createEvent(GAME_RESULT_EVENT_NAME)
                .withMetric(GAME_RESULT_METRIC_TIME_TO_COMPLETE,
                        (double) timeToComplete)
                .withMetric(GAME_RESULT_METRIC_TIMES_OF_GUESSES,
                        (double) timesOfGuesses)
                .withMetric(GAME_RESULT_METRIC_SCORE, (double) score);
        analytics.getEventClient().recordEvent(gameResultEvent);
    }

    private static void checkAnalyticsAvailability() {
        if (analytics == null) {
            throw new IllegalStateException(
                    "Mobile Analytics client not initialized yet");
        }
    }

}
