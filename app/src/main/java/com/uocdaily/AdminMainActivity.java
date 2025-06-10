package com.uocdaily;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminMainActivity extends AppCompatActivity {

    private static final String TAG = "AdminMainActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    // UI Components
    private LinearLayout btnSignOut;
    private CardView cardManageUsers;
    private CardView cardManageNews;
    private TextView tvTotalUsers;
    private TextView tvTotalNews;

    // Data counters
    private int totalUsers = 0;
    private int totalNews = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        initializeFirebase();

        // Initialize Views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load dashboard data
        loadDashboardData();
    }

    private void initializeFirebase() {
        try {
            mAuth = FirebaseAuth.getInstance();
            databaseRef = FirebaseDatabase.getInstance("https://uocdaily-default-rtdb.firebaseio.com/").getReference();
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            showErrorMessage(getString(R.string.error_network));
        }
    }

    private void initializeViews() {
        try {
            btnSignOut = findViewById(R.id.btnSignOut);
            cardManageUsers = findViewById(R.id.cardManageUsers);
            cardManageNews = findViewById(R.id.cardManageNews);
            tvTotalUsers = findViewById(R.id.tvTotalUsers);
            tvTotalNews = findViewById(R.id.tvTotalNews);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            showErrorMessage("Error setting up interface");
        }
    }

    private void setupClickListeners() {
        // Sign Out Button
        btnSignOut.setOnClickListener(v -> showSignOutConfirmation());

        // Manage Users Card
        cardManageUsers.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(AdminMainActivity.this, ManageUsersActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to ManageUsersActivity: " + e.getMessage());
                showErrorMessage("Error opening user management");
            }
        });

        // Manage News Card
        cardManageNews.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(AdminMainActivity.this, ManageNewsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to ManageNewsActivity: " + e.getMessage());
                showErrorMessage("Error opening news management");
            }
        });
    }

    private void loadDashboardData() {
        showLoadingMessage();
        loadUsersCount();
        loadNewsCount();
    }

    private void loadUsersCount() {
        try {
            databaseRef.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        totalUsers = (int) dataSnapshot.getChildrenCount();
                        updateUsersDisplay();
                        Log.d(TAG, "Users count loaded: " + totalUsers);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing users data: " + e.getMessage());
                        showErrorMessage(getString(R.string.error_loading_users));
                        updateUsersDisplay(); // Show 0 or previous value
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error loading users count: " + databaseError.getMessage());
                    showErrorMessage(getString(R.string.error_loading_users));
                    updateUsersDisplay(); // Show 0 or previous value
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up users listener: " + e.getMessage());
            showErrorMessage(getString(R.string.error_loading_users));
        }
    }

    private void loadNewsCount() {
        try {
            databaseRef.child("news").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        totalNews = 0;

                        // Count news from all categories
                        DataSnapshot academicNews = dataSnapshot.child("academic");
                        DataSnapshot eventNews = dataSnapshot.child("events");
                        DataSnapshot sportsNews = dataSnapshot.child("sports");

                        totalNews += (int) academicNews.getChildrenCount();
                        totalNews += (int) eventNews.getChildrenCount();
                        totalNews += (int) sportsNews.getChildrenCount();

                        updateNewsDisplay();
                        Log.d(TAG, "News count loaded: " + totalNews);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing news data: " + e.getMessage());
                        showErrorMessage(getString(R.string.ad_error_loading_news));
                        updateNewsDisplay(); // Show 0 or previous value
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error loading news count: " + databaseError.getMessage());
                    showErrorMessage(getString(R.string.ad_error_loading_news));
                    updateNewsDisplay(); // Show 0 or previous value
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up news listener: " + e.getMessage());
            showErrorMessage(getString(R.string.ad_error_loading_news));
        }
    }

    private void updateUsersDisplay() {
        try {
            if (tvTotalUsers != null) {
                tvTotalUsers.setText(String.valueOf(totalUsers));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating users display: " + e.getMessage());
        }
    }

    private void updateNewsDisplay() {
        try {
            if (tvTotalNews != null) {
                tvTotalNews.setText(String.valueOf(totalNews));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating news display: " + e.getMessage());
        }
    }

    private void showSignOutConfirmation() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.logout_admin))
                    .setMessage(getString(R.string.confirm_logout_admin))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> performSignOut())
                    .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing sign out confirmation: " + e.getMessage());
            // If dialog fails, still allow sign out
            performSignOut();
        }
    }

    private void performSignOut() {
        try {
            if (mAuth != null) {
                mAuth.signOut();
                Log.d(TAG, "User signed out successfully");

                // Navigate to login activity
                Intent intent = new Intent(AdminMainActivity.this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                throw new Exception("FirebaseAuth instance is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during sign out: " + e.getMessage());
            showErrorMessage("Error signing out. Please try again.");

            // Try to navigate anyway in case of auth error
            try {
                Intent intent = new Intent(AdminMainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception navError) {
                Log.e(TAG, "Error navigating to login: " + navError.getMessage());
            }
        }
    }

    private void showLoadingMessage() {
        try {
            Toast.makeText(this, getString(R.string.loading_data), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading message: " + e.getMessage());
        }
    }

    private void showErrorMessage(String message) {
        try {
            if (message == null || message.isEmpty()) {
                message = getString(R.string.operation_failed);
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error message shown: " + message);
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message: " + e.getMessage());
        }
    }

    private void showSuccessMessage(String message) {
        try {
            if (message == null || message.isEmpty()) {
                message = getString(R.string.operation_successful);
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing success message: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Refresh data when returning to dashboard
            loadDashboardData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Clean up any listeners if needed
            Log.d(TAG, "AdminMainActivity destroyed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage());
        }
    }

    // Method to refresh dashboard data manually
    public void refreshDashboard() {
        try {
            showLoadingMessage();
            loadDashboardData();
            showSuccessMessage(getString(R.string.data_refreshed));
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing dashboard: " + e.getMessage());
            showErrorMessage("Error refreshing data");
        }
    }

    // Method to check if user is authenticated
    private boolean isUserAuthenticated() {
        try {
            return mAuth != null && mAuth.getCurrentUser() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking authentication: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            // Check if user is still authenticated
            if (!isUserAuthenticated()) {
                Log.w(TAG, "User not authenticated, redirecting to login");
                Intent intent = new Intent(this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart: " + e.getMessage());
        }
    }
}