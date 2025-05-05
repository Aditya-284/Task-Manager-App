package com.example.projectapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;

public class SplashActivity extends Activity {
    private static final int SPLASH_TIMEOUT = 2000; // 2 seconds
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Splash screen created");
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "Splash layout set");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Starting MainActivity after " + SPLASH_TIMEOUT + "ms");
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                Log.d(TAG, "MainActivity started");
                finish();
                Log.d(TAG, "SplashActivity finished");
            }
        }, SPLASH_TIMEOUT);
    }
} 