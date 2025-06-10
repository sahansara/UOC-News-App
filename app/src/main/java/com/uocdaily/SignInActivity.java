package com.uocdaily;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import com.google.firebase.database.FirebaseDatabase;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth with debug logging
        mAuth = FirebaseAuth.getInstance();

        // Debug: Check Firebase connection
        Log.d(TAG, "Firebase Auth instance: " + (mAuth != null ? "OK" : "NULL"));
        Log.d(TAG, "Current user: " + (mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "None"));

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in: " + currentUser.getEmail());
            // User is already signed in, navigate to main activity
//            navigateNewsActivity();
        } else {
            Log.d(TAG, "No user currently signed in");
        }
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        try {
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            buttonLogin = findViewById(R.id.buttonLogin);
            textSignUp = findViewById(R.id.textSignUp);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            showErrorToast("Failed to load interface");
        }
    }

    /**
     * Set up click listeners for interactive components
     */
    private void setupClickListeners() {
        try {
            // Login button click listener
            if (buttonLogin != null) {
                buttonLogin.setOnClickListener(v -> loginUser());
            }

            // Sign up text click listener
            if (textSignUp != null) {
                textSignUp.setOnClickListener(v -> navigateToSignUp());
            }

            Log.d(TAG, "Click listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Handle user login with enhanced debugging
     */
    private void loginUser() {
        try {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Debug logging
            Log.d(TAG, "Attempting login for email: " + email);
            Log.d(TAG, "Password length: " + password.length());

            // Validate input
            if (!validateInput(email, password)) {
                return;
            }

            // Show loading state
            setLoadingState(true);

            // Authenticate with Firebase
            Log.d(TAG, "Starting Firebase authentication...");
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Log.d(TAG, "Login successful for user: " + email);
                        Log.d(TAG, "Auth result: " + authResult.getUser().getUid());
                        showSuccessToast("Login Successful");
                        setLoadingState(false);
                        navigateNewsActivity();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Login failed: " + e.getMessage(), e);

                        // Enhanced error handling with specific error codes
                        String errorMessage = "Login failed";

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Invalid email or password";
                            Log.e(TAG, "Invalid credentials error");
                        } else if (e instanceof FirebaseAuthInvalidUserException) {
                            FirebaseAuthInvalidUserException invalidUserException = (FirebaseAuthInvalidUserException) e;
                            String errorCode = invalidUserException.getErrorCode();
                            Log.e(TAG, "Invalid user error code: " + errorCode);

                            switch (errorCode) {
                                case "ERROR_USER_NOT_FOUND":
                                    errorMessage = "No account found with this email";
                                    break;
                                case "ERROR_USER_DISABLED":
                                    errorMessage = "This account has been disabled";
                                    break;
                                default:
                                    errorMessage = "User account error: " + errorCode;
                            }
                        } else if (e instanceof FirebaseAuthException) {
                            FirebaseAuthException authException = (FirebaseAuthException) e;
                            String errorCode = authException.getErrorCode();
                            Log.e(TAG, "Firebase auth error code: " + errorCode);
                            errorMessage = "Authentication error: " + errorCode;
                        }

                        showErrorToast(errorMessage);
                        setLoadingState(false);
                    })
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "Authentication task completed. Success: " + task.isSuccessful());
                        if (!task.isSuccessful() && task.getException() != null) {
                            Log.e(TAG, "Task exception: " + task.getException().getClass().getSimpleName());
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error during login: " + e.getMessage(), e);
            showErrorToast("Login error occurred");
            setLoadingState(false);
        }
    }

    /**
     * Validate user input
     */
    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            Log.d(TAG, "Validation failed: Email is empty");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            Log.d(TAG, "Validation failed: Invalid email format");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            Log.d(TAG, "Validation failed: Password is empty");
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            Log.d(TAG, "Validation failed: Password too short");
            return false;
        }

        Log.d(TAG, "Input validation passed");
        return true;
    }

    /**
     * Navigate to main activity after successful login
     */
    // OLD METHOD - REPLACE COMPLETELY:
    private void navigateNewsActivity() {
        try {
            // Get current user
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                showErrorToast("User authentication failed");
                return;
            }

            String userId = currentUser.getUid();

            // Check user role from database
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(userId)
                    .child("role")
                    .get()
                    .addOnSuccessListener(dataSnapshot -> {
                        String userRole = dataSnapshot.getValue(String.class);

                        Intent intent;
                        if ("admin".equals(userRole)) {
                            // Navigate to Admin Dashboard
                            intent = new Intent(SignInActivity.this, AdminMainActivity.class);
                        } else {
                            // Navigate to Normal User Dashboard
                            intent = new Intent(SignInActivity.this, NewsActivity.class);
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error checking user role: " + e.getMessage());
                        // Default to normal user if role check fails
                        Intent intent = new Intent(SignInActivity.this, NewsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error navigating after login: " + e.getMessage(), e);
            showErrorToast("Navigation error occurred");
        }
    }

    /**
     * Navigate to sign up activity
     */
    private void navigateToSignUp() {
        try {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to sign up: " + e.getMessage(), e);
            showErrorToast("Cannot open sign up screen");
        }
    }

    /**
     * Set loading state for UI components
     */
    private void setLoadingState(boolean isLoading) {
        try {
            if (buttonLogin != null) {
                buttonLogin.setEnabled(!isLoading);
                buttonLogin.setText(isLoading ? "Signing In..." : "Sign In");
            }

            if (editTextEmail != null) {
                editTextEmail.setEnabled(!isLoading);
            }

            if (editTextPassword != null) {
                editTextPassword.setEnabled(!isLoading);
            }

            if (textSignUp != null) {
                textSignUp.setEnabled(!isLoading);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting loading state: " + e.getMessage(), e);
        }
    }

    /**
     * Show error toast message
     */
    private void showErrorToast(String message) {
        try {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Error toast shown: " + message);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing error toast: " + e.getMessage(), e);
        }
    }

    /**
     * Show success toast message
     */
    private void showSuccessToast(String message) {
        try {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Success toast shown: " + message);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing success toast: " + e.getMessage(), e);
        }
    }
}