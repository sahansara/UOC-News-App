package com.uocdaily;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

public class ProfileActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private ImageView ivProfileImage;
    private TextView tvUsername;
    private TextView tvEmail;
    private Button btnEditInfo;
    private Button btnSignOut;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // User data
    private String username;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Load user data
        loadUserData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        btnEditInfo = findViewById(R.id.btn_edit_info);
        btnSignOut = findViewById(R.id.btn_sign_out);
    }

    private void setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });

        // Edit info button click listener
        btnEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to edit profile activity
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        // Sign out button click listener
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignOutDialog();
            }
        });

        // Profile image click listener (optional - for future profile image change)
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Profile image feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // data get form firbase
    private void loadUserData() {
        if (currentUser != null) {
            // Get email from Firebase Auth
            email = currentUser.getEmail();

            // Get username from Firebase Auth display name
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().trim().isEmpty()) {
                username = currentUser.getDisplayName();
            } else if (email != null) {
                // Extract username from email (part before @)
                username = email.substring(0, email.indexOf("@"));
            } else {
                username = "User";
            }

            // Update UI immediately
            updateUI();

        } else {
            redirectToLogin();
        }
    }

    private void updateUI() {
        // Set username
        if (username != null && !username.trim().isEmpty()) {
            tvUsername.setText(username);
        } else {
            tvUsername.setText("User");
        }

        // Set email
        if (email != null && !email.trim().isEmpty()) {
            tvEmail.setText(email);
        } else {
            tvEmail.setText("No email");
        }
    }

    private void useCurrentUserData() {
        // Fallback to Firebase Auth user data
        if (currentUser != null) {
            email = currentUser.getEmail();
            username = currentUser.getDisplayName();

            if (username == null || username.isEmpty()) {
                username = "User";
            }

            updateUI();
        }
    }

    private void showSignOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    signOutUser();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void signOutUser() {
        // Show loading or disable button to prevent multiple clicks
        btnSignOut.setEnabled(false);
        btnSignOut.setText("Signing out...");

        // Sign out from Firebase auth firbase
        mAuth.signOut();

        // Show success message
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();

        // Redirect to login activity
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning to this activity
        if (currentUser != null) {
            loadUserData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}