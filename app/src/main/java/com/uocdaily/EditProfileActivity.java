package com.uocdaily;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.auth.AuthResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // UI Components
    private ImageButton btnBack;
    private CircleImageView ivProfile;
    private ImageView ivCamera;
    private EditText etUsername, etEmail;
    private Button btnCancel, btnSave;

    // Progress Dialog
    private ProgressDialog progressDialog;

    // User Data
    private String originalUsername;
    private String originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeFirebase();
        initializeViews();
        setupProgressDialog();
        setupClickListeners();
        loadUserData();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to login
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        ivProfile = findViewById(R.id.iv_profile);
        ivCamera = findViewById(R.id.iv_camera);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Camera button (for future profile image functionality)
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement profile image change functionality
                Toast.makeText(EditProfileActivity.this, "Profile image change coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveUserData();
            }
        });
    }

    private void loadUserData() {
        if (currentUser == null) return;

        // Get current user data from Firebase Auth
        originalUsername = currentUser.getDisplayName();
        originalEmail = currentUser.getEmail();

        // Auto-fill the input fields
        if (originalUsername != null && !originalUsername.isEmpty()) {
            etUsername.setText(originalUsername);
        } else {
            // If display name is null, use email prefix as username
            if (originalEmail != null && !originalEmail.isEmpty()) {
                String emailPrefix = originalEmail.split("@")[0];
                etUsername.setText(emailPrefix);
                originalUsername = emailPrefix;
            }
        }

        if (originalEmail != null && !originalEmail.isEmpty()) {
            etEmail.setText(originalEmail);
        }

        // Set cursor to end of text
        if (etUsername.getText().length() > 0) {
            etUsername.setSelection(etUsername.getText().length());
        }
        if (etEmail.getText().length() > 0) {
            etEmail.setSelection(etEmail.getText().length());
        }
    }

    private void validateAndSaveUserData() {
        String newUsername = etUsername.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        // Validate username
        if (TextUtils.isEmpty(newUsername)) {
            etUsername.setError(getString(R.string.error_username_empty));
            etUsername.requestFocus();
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(newEmail)) {
            etEmail.setError(getString(R.string.error_email_empty));
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }

        // Check if data has changed
        if (newUsername.equals(originalUsername) && newEmail.equals(originalEmail)) {
            Toast.makeText(this, "No changes made", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the updated data
        saveUserData(newUsername, newEmail);
    }

    private void saveUserData(String username, String email) {
        progressDialog.setMessage(getString(R.string.saving_changes));
        progressDialog.show();

        // Update display name first
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Display name updated, now update email if changed
                            if (!email.equals(originalEmail)) {
                                updateUserEmail(email, username);
                            } else {
                                // Only username changed
                                progressDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this,
                                        getString(R.string.profile_updated_success),
                                        Toast.LENGTH_SHORT).show();

                                originalUsername = username;
                                setResult(RESULT_OK);
                                finish();
                            }
                        } else {
                            progressDialog.dismiss();
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error";
                            Toast.makeText(EditProfileActivity.this,
                                    getString(R.string.profile_update_failed) + ": " + errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserEmail(String newEmail, String username) {
        currentUser.updateEmail(newEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            // Send verification email
                            sendEmailVerification();

                            Toast.makeText(EditProfileActivity.this,
                                    "Profile updated! Please verify your new email address.",
                                    Toast.LENGTH_LONG).show();

                            originalUsername = username;
                            originalEmail = newEmail;
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this,
                                    "Email update failed. Please re-login and try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Move this outside the above method
    private void sendEmailVerification() {
        currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    currentUser.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditProfileActivity.this,
                                                "Verification email sent.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        String currentUsername = etUsername.getText().toString().trim();
        String currentEmail = etEmail.getText().toString().trim();

        // Check if user made changes
        if (!currentUsername.equals(originalUsername) || !currentEmail.equals(originalEmail)) {
            Toast.makeText(this, "Changes not saved", Toast.LENGTH_SHORT).show();
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}