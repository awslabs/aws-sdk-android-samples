package com.example.mynotificationapplication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import android.content.ContentValues.TAG
import android.provider.Settings.Global.getString
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener

import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        getPinpointManager(applicationContext)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object{
        private var pinpointManager: PinpointManager? = null
        fun getPinpointManager(applicationContext: Context?): PinpointManager? {
            if (pinpointManager == null) {
                val awsConfig = AWSConfiguration(applicationContext)
                AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, AwsClientCallback())
                val pinpointConfig = PinpointConfiguration(
                        applicationContext,
                        AWSMobileClient.getInstance(),
                        awsConfig)
                pinpointManager = PinpointManager(pinpointConfig)
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log and toast
                    val msg =  token?.let { Log.d(TAG, it) }
                    msg?.let {
                        //Toast.makeText(applicationContext, it,Toast.LENGTH_LONG).show()
                    }
                }) }
            return pinpointManager
    }
}

class AwsClientCallback():Callback<UserStateDetails?> {

    override fun onError(e: Exception?) {
        Log.e("INIT", "Initialization error.", e)
    }

    override fun onResult(userStateDetails: UserStateDetails?) {
        Log.i("INIT", userStateDetails?.userState.toString())
    }
}}

