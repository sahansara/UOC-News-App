package com.uocdaily;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    // UI Elements
    private EditText etUsername, etPassword, etConfirmPassword, etEmail;
    private Button btnSignUp;
    private TextView tvAlreadyHaveAccount;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_sign_up);
            Log.d(TAG, "Layout set successfully");

            // Initialize Firebase Authincations  and Database
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "Firebase initialized successfully");

            // Initialize UI elements
            initializeViews();

            // Set click listeners
            setClickListeners();


            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            etEmail = findViewById(R.id.etEmail);
            btnSignUp = findViewById(R.id.btnSignUp);
            tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);

            // Check if all views are found
            if (etUsername == null || etPassword == null || etConfirmPassword == null ||
                    etEmail == null || btnSignUp == null || tvAlreadyHaveAccount == null) {
                throw new RuntimeException("One or more views not found in layout");
            }

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            throw e;
        }
    }

    private void setClickListeners() {
        try {
            // Sign Up Button Click then Using lambda expression
            btnSignUp.setOnClickListener(v -> {
                Log.d(TAG, "Sign up button clicked");
                signUpUser();
            });

            // Already have account text click - Using lambda expression
            tvAlreadyHaveAccount.setOnClickListener(v -> {
                Log.d(TAG, "Already have account clicked");
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            });

            Log.d(TAG, "Click listeners set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting click listeners: ", e);
            throw e;
        }
    }

    private void signUpUser() {
        try {
            // for only degub purpos
            Log.d(TAG, "Starting user sign up process");

            // Get input values
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(username)) {
                etUsername.setError("Username is required");
                etUsername.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            // Show loading state
            btnSignUp.setText("Creating Account...");
            btnSignUp.setEnabled(false);

            Log.d(TAG, "Validation passed, creating user with email: " + email);

            // Create user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User creation successful");
                            // Sign up success, save user data to Realtime Database
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToDatabase(user.getUid(), username, email);
                            }
                        } else {
                            Log.e(TAG, "User creation failed", task.getException());
                            // Sign up failed
                            btnSignUp.setText("Sign Up");
                            btnSignUp.setEnabled(true);
                            String errorMessage = "Registration failed";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in signUpUser: ", e);
            btnSignUp.setText("Sign Up");
            btnSignUp.setEnabled(true);
            Toast.makeText(this, "Error during sign up: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    // data insert part in db
    private void saveUserToDatabase(String userId, String username, String email) {
        try {
            //for degus only
            Log.d(TAG, "Saving user to database: " + userId);

            // Create user data use  HashMap
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("role", "user");
            userData.put("createdAt", System.currentTimeMillis());

            // Save to Firebase Realtime Database
            mDatabase.child("users").child(userId).setValue(userData)
                    .addOnCompleteListener(task -> {
                        btnSignUp.setText("Sign Up");
                        btnSignUp.setEnabled(true);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "User data saved successfully");
                            Toast.makeText(SignUpActivity.this,
                                    "Account created successfully!", Toast.LENGTH_SHORT).show();

                            // Navigate to Sign In Activity instead of NewsActivity to avoid crash
                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Failed to save user data", task.getException());
                            String errorMessage = "Failed to save user data";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in saveUserToDatabase: ", e);
            btnSignUp.setText("Sign Up");
            btnSignUp.setEnabled(true);
            Toast.makeText(this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}