package com.uocdaily;
import java.util.ArrayList;
import java.util.List;

import com.uocdaily.News;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventNewsActivity extends AppCompatActivity {

    private static final String TAG = "EventNewsActivity";
    private static final String EVENT_CATEGORY = "Event";

    // Firebase
    private DatabaseReference newsRef;

    // UI Components
    private ImageButton btnBack;
    private EditText etSearch;
    private LinearLayout newsContainer;

    // Data
    private List<News> EventNewsList;
    private List<News> filteredNewsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_news);

        initializeFirebase();
        initializeViews();
        setupListeners();
        loadEventNews();
    }

    private void initializeFirebase() {
        newsRef = FirebaseDatabase.getInstance().getReference("news");
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        etSearch = findViewById(R.id.et_search);

        // Get the ScrollView's LinearLayout container
        newsContainer = findViewById(R.id.news_container);
        if (newsContainer == null) {
            // If news_container doesn't exist, we'll create news cards dynamically
            // This will be handled in the displayNews method
        }

        // Initialize lists
        EventNewsList = new ArrayList<>();
        filteredNewsList = new ArrayList<>();
    }

    private void setupListeners() {
        // Back button click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNews(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void loadEventNews() {
        // Show loading state (you can add a progress bar here)

        // Query to get only Event news
        Query EventQuery = newsRef.orderByChild("category").equalTo(EVENT_CATEGORY);

        EventQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventNewsList.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot newsSnapshot : dataSnapshot.getChildren()) {
                        try {
                            News news = newsSnapshot.getValue(News.class);
                            if (news != null) {
                                // Set the key as ID if ID is not set
                                if (news.getId() == null || news.getId().isEmpty()) {
                                    news.setId(newsSnapshot.getKey());
                                }
                                EventNewsList.add(news);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing news data: " + e.getMessage());
                        }
                    }

                    // Sort news by timestamp (latest first)
                    Collections.sort(EventNewsList, new Comparator<News>() {
                        @Override
                        public int compare(News n1, News n2) {
                            return Long.compare(n2.getTimestamp(), n1.getTimestamp());
                        }
                    });

                    // Display the news
                    filteredNewsList.clear();
                    filteredNewsList.addAll(EventNewsList);
                    displayNews();

                    Log.d(TAG, "Loaded " + EventNewsList.size() + " Event news items");
                } else {
                    // No Event news found
                    showNoNewsMessage();
                    Log.d(TAG, "No Event news found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load Event news: " + databaseError.getMessage());
                Toast.makeText(EventNewsActivity.this,
                        "Failed to load news. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNews() {
        if (newsContainer != null) {
            // Clear existing views
            newsContainer.removeAllViews();

            // Add news cards dynamically
            for (News news : filteredNewsList) {
                View newsCard = createNewsCard(news);
                newsContainer.addView(newsCard);
            }
        } else {
            // If using static layout, update the existing views
            updateStaticNewsViews();
        }
    }

    private View createNewsCard(News news) {
        // Inflate a news card layout
        View cardView = getLayoutInflater().inflate(R.layout.news_card_item_layout, null);

        // Add margin between cards
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Convert dp to pixels for proper spacing
        int marginInDp = 16; // Change this value for more/less spacing
        int marginInPx = (int) (marginInDp * getResources().getDisplayMetrics().density);
        params.setMargins(0, 0, 0, marginInPx); // left, top, right, bottom
        cardView.setLayoutParams(params);

        // Find views in the card
        ImageView ivNewsImage = cardView.findViewById(R.id.iv_news_image);
        TextView tvNewsTitle = cardView.findViewById(R.id.tv_news_title);
        TextView tvNewsDescription = cardView.findViewById(R.id.tv_news_description);
        TextView tvNewsTime = cardView.findViewById(R.id.tv_news_time);
        CardView cardContainer = cardView.findViewById(R.id.card_news_container);

        // Set data
        tvNewsTitle.setText(news.getTitle());
        tvNewsDescription.setText(news.getDescription());
        tvNewsTime.setText(news.getTimeAgo());

        // Load image using Glide
        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(news.getImageUrl())
                    .placeholder(R.drawable.ic_event)
                    .error(R.drawable.ic_event)
                    .into(ivNewsImage);
        } else {
            ivNewsImage.setImageResource(R.drawable.ic_event);
        }

        // Set click listener for the card
        cardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewsDetail(news);
            }
        });

        return cardView;
    }

    private void updateStaticNewsViews() {
        // This method updates the static news cards in your XML layout
        // Update based on available news items



        // Featured news (first item)
        if (filteredNewsList.size() > 0) {
            updateNewsCard(R.id.card_featured_news, R.id.iv_featured_news_image,
                    R.id.tv_featured_news_title, R.id.tv_featured_news_description,
                    0, filteredNewsList.get(0)); // 0 means no time view for featured
        }
    }

    private void updateNewsCard(int cardId, int imageId, int titleId, int descId, int timeId, News news) {
        CardView card = findViewById(cardId);
        ImageView image = findViewById(imageId);
        TextView title = findViewById(titleId);
        TextView description = findViewById(descId);
        TextView time = timeId != 0 ? findViewById(timeId) : null;

        if (card != null && image != null && title != null && description != null) {
            card.setVisibility(View.VISIBLE);
            title.setText(news.getTitle());
            description.setText(news.getDescription());

            if (time != null) {
                time.setText(news.getTimeAgo());
            }

            // Load image
            if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(news.getImageUrl())
                        .placeholder(R.drawable.ic_event)
                        .error(R.drawable.ic_event)
                        .into(image);
            } else {
                image.setImageResource(R.drawable.ic_event);
            }

            // Set click listener
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNewsDetail(news);
                }
            });
        }
    }

    private void filterNews(String searchText) {
        filteredNewsList.clear();

        if (searchText.isEmpty()) {
            filteredNewsList.addAll(EventNewsList);
        } else {
            String searchLower = searchText.toLowerCase().trim();
            for (News news : EventNewsList) {
                if (news.getTitle().toLowerCase().contains(searchLower) ||
                        news.getDescription().toLowerCase().contains(searchLower)) {
                    filteredNewsList.add(news);
                }
            }
        }

        displayNews();
    }

    private void openNewsDetail(News news) {
        // Create intent to open news detail activity
        Intent intent = new Intent(this, ReadNewsActivity.class);
        intent.putExtra("news_id", news.getId());
        intent.putExtra("news_title", news.getTitle());
        intent.putExtra("news_description", news.getDescription());
        intent.putExtra("news_image_url", news.getImageUrl());
        intent.putExtra("news_timestamp", news.getTimestamp());
        intent.putExtra("news_category", news.getCategory());
        startActivity(intent);
    }

    private void showNoNewsMessage() {
        // Show a message when no news is available
        Toast.makeText(this, "No Event news available at the moment", Toast.LENGTH_SHORT).show();

        // You can also add a TextView to show "No news available" message
        // in your layout and make it visible here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }
}