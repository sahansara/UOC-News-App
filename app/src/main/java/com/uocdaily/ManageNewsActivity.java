package com.uocdaily;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ManageNewsActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack, imgPreview;
    private Spinner spinnerCategory;
    private EditText etTitle, etDescription;
    private Button btnUploadImage, btnSubmitNews;
    private TextView tvValidationMessage;
    private LinearLayout layoutAcademicNews, layoutSportNews, layoutEventNews;
    private TextView tvNoAcademicNews, tvNoSportNews, tvNoEventNews;

    // Firebase
    private DatabaseReference newsDatabase;
    private StorageReference storageReference;

    // Variables
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;

    // Categories
    private String[] categories = {"Select Category", "Academic", "Sport", "Event"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_news);

        // Initialize Firebase
        initFirebase();

        // Initialize UI
        initViews();

        // Setup listeners
        setupListeners();

        // Load existing news
        loadAllNews();

        // Check permissions
        checkPermissions();
    }

    private void initFirebase() {
        newsDatabase = FirebaseDatabase.getInstance().getReference("news");
        storageReference = FirebaseStorage.getInstance().getReference("news_images");
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btn_back);

        // Add News Section
        spinnerCategory = findViewById(R.id.spinner_category);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        imgPreview = findViewById(R.id.img_preview);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        tvValidationMessage = findViewById(R.id.tv_validation_message);
        btnSubmitNews = findViewById(R.id.btn_submit_news);

        // News Display Section
        layoutAcademicNews = findViewById(R.id.layout_academic_news);
        layoutSportNews = findViewById(R.id.layout_sport_news);
        layoutEventNews = findViewById(R.id.layout_event_news);
        tvNoAcademicNews = findViewById(R.id.tv_no_academic_news);
        tvNoSportNews = findViewById(R.id.tv_no_sport_news);
        tvNoEventNews = findViewById(R.id.tv_no_event_news);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Upload image button
        btnUploadImage.setOnClickListener(v -> pickImageFromDevice());

        // Submit news button
        btnSubmitNews.setOnClickListener(v -> validateAndSubmitNews());
    }

    private void checkPermissions() {
        String permission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }

    private void pickImageFromDevice() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            showToast("Error opening gallery: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Add this validation
                try {
                    getContentResolver().openInputStream(selectedImageUri).close();
                    imgPreview.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(imgPreview);
                    btnUploadImage.setText("Image Selected ✓");
                    hideValidationMessage();
                } catch (Exception e) {
                    selectedImageUri = null;
                    showError("Invalid image selected: " + e.getMessage());
                }
            }
        }
    }

    private void validateAndSubmitNews() {
        String category = spinnerCategory.getSelectedItem().toString();
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation (image is now optional)
        if (category.equals("Select Category")) {
            showValidationMessage("Please select a category");
            return;
        }
        if (title.isEmpty()) {
            showValidationMessage("Please enter news title");
            etTitle.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            showValidationMessage("Please enter news description");
            etDescription.requestFocus();
            return;
        }

        // All validation passed
        hideValidationMessage();

        // Check if image is selected
        if (selectedImageUri != null) {
            uploadNewsWithImage(category, title, description);
        } else {
            // Save news without image
            saveNewsToDatabase(newsDatabase.push().getKey(), category, title, description, "");
        }
    }
    private void resetSubmitButton() {
        btnSubmitNews.setText("Add News");
        btnSubmitNews.setEnabled(true);
    }
    private void uploadNewsWithImage(String category, String title, String description) {
        btnSubmitNews.setText("Uploading...");
        btnSubmitNews.setEnabled(false);

        try {
            String newsId = newsDatabase.push().getKey();
            if (newsId == null) {
                showError("Failed to generate news ID");
                return;
            }

            // Verify image URI is valid
            try {
                getContentResolver().openInputStream(selectedImageUri).close();
            } catch (Exception e) {
                // If image is invalid, save without image
                showToast("Image invalid, saving without image");
                saveNewsToDatabase(newsId, category, title, description, "");
                return;
            }

            String imageFileName = "news_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageReference.child(imageFileName);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            saveNewsToDatabase(newsId, category, title, description, downloadUri.toString());
                        }).addOnFailureListener(e -> {
                            // If getting URL fails, save without image
                            showToast("Image upload failed, saving without image");
                            saveNewsToDatabase(newsId, category, title, description, "");
                        });
                    })
                    .addOnFailureListener(e -> {
                        // If upload fails, save without image
                        showToast("Image upload failed, saving without image");
                        saveNewsToDatabase(newsId, category, title, description, "");
                    });

        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            resetSubmitButton();
        }
    }

    private void saveNewsToDatabase(String newsId, String category, String title, String description, String imageUrl) {
        try {
            // Create news object
            Map<String, Object> newsData = new HashMap<>();
            newsData.put("id", newsId);
            newsData.put("category", category);
            newsData.put("title", title);
            newsData.put("description", description);
            newsData.put("imageUrl", imageUrl);
            newsData.put("timestamp", System.currentTimeMillis());

            // Save to Firebase Realtime Database
            newsDatabase.child(newsId).setValue(newsData)
                    .addOnSuccessListener(aVoid -> {
                        showToast("News added successfully!");
                        clearForm();
                        loadAllNews(); // Refresh news list
                    })
                    .addOnFailureListener(e -> {
                        showError("Failed to save news: " + e.getMessage());
                    });

        } catch (Exception e) {
            showError("Error saving news: " + e.getMessage());
        }
    }

    private void loadAllNews() {
        try {
            newsDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Clear existing news
                    clearAllNewsLayouts();

                    if (!snapshot.exists()) {
                        showEmptyMessages();
                        return;
                    }

                    // Process each news item
                    for (DataSnapshot newsSnapshot : snapshot.getChildren()) {
                        try {
                            String newsId = newsSnapshot.child("id").getValue(String.class);
                            String category = newsSnapshot.child("category").getValue(String.class);
                            String title = newsSnapshot.child("title").getValue(String.class);
                            String description = newsSnapshot.child("description").getValue(String.class);
                            String imageUrl = newsSnapshot.child("imageUrl").getValue(String.class);

                            if (newsId != null && category != null && title != null) {
                                addNewsToLayout(newsId, category, title, description, imageUrl);
                            }
                        } catch (Exception e) {
                            showToast("Error loading news item: " + e.getMessage());
                        }
                    }

                    updateEmptyMessages();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showError("Failed to load news: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Error setting up news listener: " + e.getMessage());
        }
    }

    private void addNewsToLayout(String newsId, String category, String title, String description, String imageUrl) {
        try {
            // Inflate news item layout
            View newsItemView = LayoutInflater.from(this).inflate(R.layout.news_item_card, null);

            // Get views from news item
            ImageView imgThumbnail = newsItemView.findViewById(R.id.img_news_thumbnail);
            TextView tvTitle = newsItemView.findViewById(R.id.tv_news_title);
            TextView tvDescription = newsItemView.findViewById(R.id.tv_news_description);
            TextView tvCategory = newsItemView.findViewById(R.id.tv_news_category);
            ImageView btnDelete = newsItemView.findViewById(R.id.btn_delete_news);

            // Set data
            tvTitle.setText(title);
            tvDescription.setText(description);
            tvCategory.setText(category);

            // Load image
            // In addNewsToLayout method, update the image loading part:
// Load image (handle empty imageUrl)
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(imgThumbnail);
            } else {
                // Set default image or hide thumbnail
                imgThumbnail.setImageResource(R.drawable.ic_event); // Add a placeholder image
                // OR hide the image: imgThumbnail.setVisibility(View.GONE);
            }

            // Set category badge color
            setCategoryBadgeColor(tvCategory, category);

            // Delete button listener
            btnDelete.setOnClickListener(v -> showDeleteConfirmation(newsId, title));

            // Add to appropriate category layout
            LinearLayout targetLayout = getCategoryLayout(category);
            if (targetLayout != null) {
                targetLayout.addView(newsItemView);
            }

        } catch (Exception e) {
            showToast("Error adding news to layout: " + e.getMessage());
        }
    }

    private LinearLayout getCategoryLayout(String category) {
        switch (category) {
            case "Academic":
                return layoutAcademicNews;
            case "Sport":
                return layoutSportNews;
            case "Event":
                return layoutEventNews;
            default:
                return null;
        }
    }

    private void setCategoryBadgeColor(TextView tvCategory, String category) {
        switch (category) {
            case "Academic":
                tvCategory.setBackgroundResource(R.drawable.academic_badge);
                break;
            case "Sport":
                tvCategory.setBackgroundResource(R.drawable.sport_badge);
                break;
            case "Event":
                tvCategory.setBackgroundResource(R.drawable.event_badge);
                break;
            default:
                tvCategory.setBackgroundResource(R.drawable.category_badge_background);
                break;
        }
    }

    private void showDeleteConfirmation(String newsId, String title) {
        new AlertDialog.Builder(this)
                .setTitle("Delete News")
                .setMessage("Are you sure you want to delete \"" + title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNews(newsId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNews(String newsId) {
        try {
            newsDatabase.child(newsId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        showToast("News deleted successfully!");
                        // News list will auto-refresh due to ValueEventListener
                    })
                    .addOnFailureListener(e -> {
                        showError("Failed to delete news: " + e.getMessage());
                    });
        } catch (Exception e) {
            showError("Error deleting news: " + e.getMessage());
        }
    }

    private void clearAllNewsLayouts() {
        layoutAcademicNews.removeAllViews();
        layoutSportNews.removeAllViews();
        layoutEventNews.removeAllViews();
    }

    private void showEmptyMessages() {
        tvNoAcademicNews.setVisibility(View.VISIBLE);
        tvNoSportNews.setVisibility(View.VISIBLE);
        tvNoEventNews.setVisibility(View.VISIBLE);
    }

    private void updateEmptyMessages() {
        tvNoAcademicNews.setVisibility(layoutAcademicNews.getChildCount() == 0 ? View.VISIBLE : View.GONE);
        tvNoSportNews.setVisibility(layoutSportNews.getChildCount() == 0 ? View.VISIBLE : View.GONE);
        tvNoEventNews.setVisibility(layoutEventNews.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void clearForm() {
        // Reset form
        spinnerCategory.setSelection(0);
        etTitle.setText("");
        etDescription.setText("");
        selectedImageUri = null;
        imgPreview.setVisibility(View.GONE);
        btnUploadImage.setText("Upload Image");
        btnSubmitNews.setText("Add News");
        btnSubmitNews.setEnabled(true);
        hideValidationMessage();
    }

    private void showValidationMessage(String message) {
        tvValidationMessage.setText(message);
        tvValidationMessage.setVisibility(View.VISIBLE);
    }

    private void hideValidationMessage() {
        tvValidationMessage.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
        // Reset submit button
        btnSubmitNews.setText("Add News");
        btnSubmitNews.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permission granted!");
            } else {
                showToast("Permission required to select images");
            }
        }
    }



}
