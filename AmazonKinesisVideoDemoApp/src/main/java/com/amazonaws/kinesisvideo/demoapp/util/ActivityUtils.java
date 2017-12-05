package com.amazonaws.kinesisvideo.demoapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityUtils {
    private static final Bundle NO_EXTRAS = null;

    public static void startActivity(
            final Context context,
            final Class<? extends Activity> activityClass) {

        startActivity(context, activityClass, NO_EXTRAS);
    }

    public static void startActivity(
            final Context context,
            final Class<? extends Activity> activityClass,
            final Bundle extras) {

        final Intent intent = new Intent(context, activityClass);

        if (extras != null) {
            intent.putExtras(extras);
        }

        context.startActivity(intent);
    }

    private ActivityUtils() {
        // no-op
    }
}
