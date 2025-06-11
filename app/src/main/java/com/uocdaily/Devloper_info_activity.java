package com.uocdaily;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Devloper_info_activity extends AppCompatActivity {

    private static final String TAG = "Devloper_info_activity";

    // UI Components
    private ImageButton btnBack;
    private CardView btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developer_info_activity);

        // Initialize Views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        Log.d(TAG, "Devloper_info_activity created successfully");
    }

    private void initializeViews() {
        try {
            
            btnExit = findViewById(R.id.btnExit);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        

        // Exit Button Click
        btnExit.setOnClickListener(v -> {
            try {
                goToPreviousPage();
            } catch (Exception e) {
                Log.e(TAG, "Error handling exit button click: " + e.getMessage());
            }
        });
    }

    private void goToPreviousPage() {
        try {
            finish(); // Simply go back to previous activity
            Log.d(TAG, "Navigating to previous page");
        } catch (Exception e) {
            Log.e(TAG, "Error going to previous page: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
            Log.d(TAG, "Back pressed - returning to previous activity");
        } catch (Exception e) {
            Log.e(TAG, "Error handling back press: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Log.d(TAG, "Devloper_info_activity destroyed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage());
        }
    }
}