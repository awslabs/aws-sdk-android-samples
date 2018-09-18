package com.amazonaws.demo.s3transferutility;

import android.app.Application;
import android.content.Intent;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;

public class MyApplication extends Application {
    // Overriding this method is totally optional!
    @Override
    public void onCreate() {
        super.onCreate();
        // Required initialization logic here!

        // Network service
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
    }
}
