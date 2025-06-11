package com.uocdaily;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ReadNewsActivity extends AppCompatActivity {

    private ImageView imgNewsFull;
    private TextView tvNewsTitle;
    private TextView tvNewsDescription;
    private TextView tvLastUpdated;
    private TextView tvCategory;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_news);

        // Initialize views
        initViews();

        // Get data from Intent
        getIntentData();

        // Set up back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close current activity and go back
            }
        });
    }

    private void initViews() {
        imgNewsFull = findViewById(R.id.img_news_full);
        tvNewsTitle = findViewById(R.id.tv_news_title);
        tvNewsDescription = findViewById(R.id.tv_news_description);
        tvLastUpdated = findViewById(R.id.tv_last_updated);
        tvCategory = findViewById(R.id.tv_category);
        btnBack = findViewById(R.id.btn_back);
    }

    private void getIntentData() {
        // Get data passed from previous activity via Intent
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String newsId = extras.getString("news_id", "");
            String title = extras.getString("news_title", "No Title");
            String description = extras.getString("news_description", "No Description");
            String imageUrl = extras.getString("news_image_url", "");
            long timestamp = extras.getLong("news_timestamp", 0);
            String category = extras.getString("news_category", "NEWS");

            // Set data to views
            tvNewsTitle.setText(title);
            tvNewsDescription.setText(description);
            tvCategory.setText(category.toUpperCase());

            // Calculate and display time ago using the timestamp
            String timeAgo = calculateTimeAgo(timestamp);
            tvLastUpdated.setText("Last updated: " + timeAgo);

            // Set category-specific styling
            setCategoryStyle(category);

            // Load image using Glide
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(getCategoryPlaceholder(category))
                        .error(getCategoryPlaceholder(category))
                        .into(imgNewsFull);
            } else {
                imgNewsFull.setImageResource(getCategoryPlaceholder(category));
            }
        }
    }

    private void setCategoryStyle(String category) {
        // Set different colors for different categories
        int categoryColor;

        switch (category.toLowerCase()) {
            case "sport":
                categoryColor = getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "academic":
                categoryColor = getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "event":
                categoryColor = getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                categoryColor = getResources().getColor(android.R.color.darker_gray);
                break;
        }

        // You can apply the color to the category badge background
        // This requires creating a drawable programmatically or using tint
        tvCategory.setBackgroundColor(categoryColor);
    }

    private int getCategoryPlaceholder(String category) {
        // Return different placeholder images for different categories
        switch (category.toLowerCase()) {
            case "sport":
                return R.drawable.ic_sports; // You need to add this drawable
            case "academic":
                return R.drawable.ic_school; // You already have this
            case "event":
                return R.drawable.ic_event; // You need to add this drawable
            default:
                return R.drawable.ic_event;
        }
    }

    private String calculateTimeAgo(long timestamp) {
        if (timestamp == 0) {
            return "Unknown";
        }

        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;

        long seconds = timeDifference / 1000;
        if (seconds < 60) return "Just now";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");

        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");

        long days = hours / 24;
        if (days < 7) return days + (days == 1 ? " day ago" : " days ago");

        long weeks = days / 7;
        if (weeks < 4) return weeks + (weeks == 1 ? " week ago" : " weeks ago");

        long months = days / 30;
        if (months < 12) return months + (months == 1 ? " month ago" : " months ago");

        long years = days / 365;
        return years + (years == 1 ? " year ago" : " years ago");
    }
}