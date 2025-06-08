package com.uocdaily;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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
            // User is already signed in, navigate to main activity
            navigateToMainActivity();
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
     * Handle user login
     */
    private void loginUser() {
        try {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validate input
            if (!validateInput(email, password)) {
                return;
            }

            // Show loading state
            setLoadingState(true);

            // Authenticate with Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Log.d(TAG, "Login successful for user: " + email);
                        showSuccessToast("Login Successful");
                        navigateToMainActivity();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Login failed: " + e.getMessage(), e);
                        showErrorToast("Login Failed: " + e.getMessage());
                        setLoadingState(false);
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
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Navigate to main activity after successful login
     */
    private void navigateToMainActivity() {
        try {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to main activity: " + e.getMessage(), e);
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
                Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
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
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing success toast: " + e.getMessage(), e);
        }
    }
}