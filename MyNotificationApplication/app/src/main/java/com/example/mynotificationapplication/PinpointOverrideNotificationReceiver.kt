package com.example.mynotificationapplication
import android.content.Context
import android.content.Intent
import android.util.Log

import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.PinpointNotificationReceiver

class PinpointOverrideNotificationReceiver: PinpointNotificationReceiver() {

    init {
        Log.d("PinpointOverride", "PinpointOverrideNotificationReceiver:NotificationOpenedReceiver constructor called")
    }
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("PinpointOverride", "OnReceive before super")
           super.onReceive(context, intent)
            Log.d("PinpointOverride", "OnReceive after super")
        }
    }
