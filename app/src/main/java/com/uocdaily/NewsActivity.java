package com.uocdaily;


import android.os.Bundle;
import android.util.Log;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewsActivity extends AppCompatActivity {

    // Constants for logging and error handling
    private static final String TAG = "MainActivity";
    private static final String FIREBASE_NEWS_PATH = "news";

    // UI Components
    private ImageView profileIcon;
    private ImageView appLogo;

    // Navigation components
    private LinearLayout navHome, navAcademic, navEvents, navSports, navDevInfo;

    // Firebase Database reference
    private DatabaseReference databaseReference;
    private ValueEventListener newsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main_news);

            // Initialize Firebase
            initializeFirebase();

            // Initialize UI components
            initializeViews();

            // Set up click listeners
            setupClickListeners();

            // Load news data
            loadNewsData();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorToast("Failed to initialize app");
            finish(); // Close activity if critical error
        }
    }

    /**
     * Initialize Firebase Database reference
     */
    private void initializeFirebase() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference(FIREBASE_NEWS_PATH);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.getMessage(), e);
            showErrorToast("Database connection failed");
        }
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        try {
            // Top bar components
            profileIcon = findViewById(R.id.profileIcon);
            appLogo = findViewById(R.id.appLogo);

            // Navigation components
            navHome = findViewById(R.id.navHome);
            navAcademic = findViewById(R.id.navAcademic);
            navEvents = findViewById(R.id.navEvents);
            navSports = findViewById(R.id.navSports);
            navDevInfo = findViewById(R.id.navDevInfo);

            Log.d(TAG, "Views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            showErrorToast("Failed to load interface");
        }
    }

    /**
     * Set up click listeners for all interactive components
     */
    private void setupClickListeners() {
        try {
            // Profile icon click listener
            if (profileIcon != null) {
                profileIcon.setOnClickListener(v -> navigateToProfile());
            }

            // App logo click listener (optional - could refresh or go to home)
            if (appLogo != null) {
                appLogo.setOnClickListener(v -> refreshNewsData());
            }

            // Bottom navigation click listeners
            setupNavigationListeners();

            Log.d(TAG, "Click listeners set up successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Set up navigation bottom bar click listeners
     */
    private void setupNavigationListeners() {
        try {
            if (navHome != null) {
                navHome.setOnClickListener(v -> navigateToHome());
            }

            if (navAcademic != null) {
                navAcademic.setOnClickListener(v -> navigateToAcademic());
            }

            if (navEvents != null) {
                navEvents.setOnClickListener(v -> navigateToEvents());
            }

            if (navSports != null) {
                navSports.setOnClickListener(v -> navigateToSports());
            }

            if (navDevInfo != null) {
                navDevInfo.setOnClickListener(v -> navigateToDevInfo());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Load news data from Firebase Realtime Database
     */
    private void loadNewsData() {
        if (databaseReference == null) {
            Log.e(TAG, "Database reference is null");
            showErrorToast("Database not available");
            return;
        }

        try {
            showLoadingState(true);

            newsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            processNewsData(dataSnapshot);
                            Log.d(TAG, "News data loaded successfully");
                        } else {
                            Log.w(TAG, "No news data found");
                            showErrorToast("No news available");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing news data: " + e.getMessage(), e);
                        showErrorToast("Failed to process news data");
                    } finally {
                        showLoadingState(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Database error: " + databaseError.getMessage());
                    showErrorToast("Failed to load news: " + databaseError.getMessage());
                    showLoadingState(false);
                }
            };

            databaseReference.addValueEventListener(newsListener);

        } catch (Exception e) {
            Log.e(TAG, "Error loading news data: " + e.getMessage(), e);
            showErrorToast("Failed to load news");
            showLoadingState(false);
        }
    }

    /**
     * Process news data from Firebase
     */
    private void processNewsData(DataSnapshot dataSnapshot) {
        try {
            // Process Academic News
            DataSnapshot academicNews = dataSnapshot.child("academic");
            if (academicNews.exists()) {
                updateAcademicCard(academicNews);
            }

            // Process Faculty Events
            DataSnapshot eventsNews = dataSnapshot.child("events");
            if (eventsNews.exists()) {
                updateEventsCard(eventsNews);
            }

            // Process Sports News
            DataSnapshot sportsNews = dataSnapshot.child("sports");
            if (sportsNews.exists()) {
                updateSportsCard(sportsNews);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error processing news data: " + e.getMessage(), e);
            throw e; // Re-throw to be handled by caller
        }
    }

    /**
     * Update Academic news card with Firebase data
     */
    private void updateAcademicCard(DataSnapshot academicData) {
        try {
            String title = academicData.child("title").getValue(String.class);
            String description = academicData.child("description").getValue(String.class);
            String imageUrl = academicData.child("imageUrl").getValue(String.class);
            String lastUpdate = academicData.child("lastUpdate").getValue(String.class);

            // TODO: Implement UI update logic when card views are added to layout
            updateCardContent("academic", title, description, imageUrl, lastUpdate);

            Log.d(TAG, "Academic card updated: " + title);

        } catch (Exception e) {
            Log.e(TAG, "Error updating academic card: " + e.getMessage(), e);
        }
    }

    /**
     * Update Events news card with Firebase data
     */
    private void updateEventsCard(DataSnapshot eventsData) {
        try {
            String title = eventsData.child("title").getValue(String.class);
            String description = eventsData.child("description").getValue(String.class);
            String imageUrl = eventsData.child("imageUrl").getValue(String.class);
            String lastUpdate = eventsData.child("lastUpdate").getValue(String.class);

            // TODO: Implement UI update logic when card views are added to layout
            updateCardContent("events", title, description, imageUrl, lastUpdate);

            Log.d(TAG, "Events card updated: " + title);

        } catch (Exception e) {
            Log.e(TAG, "Error updating events card: " + e.getMessage(), e);
        }
    }

    /**
     * Update Sports news card with Firebase data
     */
    private void updateSportsCard(DataSnapshot sportsData) {
        try {
            String title = sportsData.child("title").getValue(String.class);
            String description = sportsData.child("description").getValue(String.class);
            String imageUrl = sportsData.child("imageUrl").getValue(String.class);
            String lastUpdate = sportsData.child("lastUpdate").getValue(String.class);

            // TODO: Implement UI update logic when card views are added to layout
            updateCardContent("sports", title, description, imageUrl, lastUpdate);

            Log.d(TAG, "Sports card updated: " + title);

        } catch (Exception e) {
            Log.e(TAG, "Error updating sports card: " + e.getMessage(), e);
        }
    }

    /**
     * Generic method to update card content
     * TODO: Implement this method when UI cards are added to the layout
     */
    private void updateCardContent(String cardType, String title, String description, String imageUrl, String lastUpdate) {
        // This method will be implemented when you add the actual card views to your layout
        // For now, just log the information
        Log.d(TAG, String.format("Updating %s card - Title: %s, Description: %s", cardType, title, description));
    }

    /**
     * Show or hide loading state
     */
    private void showLoadingState(boolean isLoading) {
        try {
            if (isLoading) {
                Log.d(TAG, "Loading news data...");
                // TODO: Show progress bar when added to layout
            } else {
                Log.d(TAG, "Loading complete");
                // TODO: Hide progress bar when added to layout
            }
        } catch (Exception e) {
            Log.e(TAG, "Error managing loading state: " + e.getMessage(), e);
        }
    }

    /**
     * Refresh news data
     */
    private void refreshNewsData() {
        try {
            Log.d(TAG, "Refreshing news data");
            loadNewsData();
            showSuccessToast("News refreshed");
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing news: " + e.getMessage(), e);
            showErrorToast("Failed to refresh news");
        }
    }

    // Navigation Methods
    private void navigateToProfile() {
        try {
            Log.d(TAG, "Navigating to profile");
            // TODO: Uncomment when ProfileActivity is created
            // Intent intent = new Intent(this, ProfileActivity.class);
            // startActivity(intent);
            showInfoToast("Profile screen - To be implemented");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to profile: " + e.getMessage(), e);
            showErrorToast("Cannot open profile");
        }
    }

    private void navigateToHome() {
        try {
            Log.d(TAG, "Already on home screen");
            showInfoToast("You are already on the home screen");
        } catch (Exception e) {
            Log.e(TAG, "Error in home navigation: " + e.getMessage(), e);
        }
    }

    private void navigateToAcademic() {
        try {
            Log.d(TAG, "Navigating to academic");
            // TODO: Uncomment when AcademicActivity is created
            // Intent intent = new Intent(this, AcademicActivity.class);
            // startActivity(intent);
            showInfoToast("Academic screen - To be implemented");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to academic: " + e.getMessage(), e);
            showErrorToast("Cannot open academic section");
        }
    }

    private void navigateToEvents() {
        try {
            Log.d(TAG, "Navigating to events");
            // TODO: Uncomment when EventsActivity is created
            //Intent intent = new Intent(this, EventsActivity.class);
            // startActivity(intent);
            showInfoToast("Events screen - To be implemented");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to events: " + e.getMessage(), e);
            showErrorToast("Cannot open events section");
        }
    }

    private void navigateToSports() {
        try {
            Log.d(TAG, "Navigating to sports");
            // TODO: Uncomment when SportsActivity is created
            // Intent intent = new Intent(this, SportsActivity.class);
            // startActivity(intent);
            showInfoToast("Sports screen - To be implemented");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to sports: " + e.getMessage(), e);
            showErrorToast("Cannot open sports section");
        }
    }

    private void navigateToDevInfo() {
        try {
            Log.d(TAG, "Navigating to dev info");
            // TODO: Uncomment when DeveloperInfoActivity is created
            // Intent intent = new Intent(this, DeveloperInfoActivity.class);
            // startActivity(intent);
            showInfoToast("Developer info screen - To be implemented");
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to dev info: " + e.getMessage(), e);
            showErrorToast("Cannot open developer info");
        }
    }

    // Utility Methods for Toast Messages
    private void showErrorToast(String message) {
        try {
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing error toast: " + e.getMessage(), e);
        }
    }

    private void showSuccessToast(String message) {
        try {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing success toast: " + e.getMessage(), e);
        }
    }

    private void showInfoToast(String message) {
        try {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing info toast: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            // Remove Firebase listener to prevent memory leaks
            if (databaseReference != null && newsListener != null) {
                databaseReference.removeEventListener(newsListener);
                Log.d(TAG, "Firebase listener removed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            Log.d(TAG, "Activity resumed");
            // Optionally refresh data when activity resumes
            // refreshNewsData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            Log.d(TAG, "Activity paused");
        } catch (Exception e) {
            Log.e(TAG, "Error in onPause: " + e.getMessage(), e);
        }
    }
}