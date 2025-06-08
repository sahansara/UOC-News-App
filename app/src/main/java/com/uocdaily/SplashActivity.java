package com.uocdaily;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.uocdaily.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // Move to Sign In screen
            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
