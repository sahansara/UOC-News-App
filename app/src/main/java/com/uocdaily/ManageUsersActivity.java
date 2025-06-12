package com.uocdaily;


import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageUsersActivity extends AppCompatActivity {

    // Views
    private ImageButton btnBack, btnRefresh;
    private TextView tvTitle, tvUsersCount;
    private TextInputEditText etSearchUsers;
    private RecyclerView recyclerViewUsers;
    private LinearLayout emptyStateLayout, loadingLayout;


    // Firebase
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;

    // Data
    private List<User> usersList;
    private List<User> filteredUsersList;
    private UserAdapter userAdapter;

    // String resources
    private static final String MANAGE_USERS_TITLE = "Manage Users";
    private static final String BACK_BUTTON = "Back";
    private static final String REFRESH_DATA = "Refresh Data";
    private static final String LOADING_DATA = "Loading users...";
    private static final String NO_USERS_FOUND = "No users found";
    private static final String REMOVE_USER = "Remove User";
    private static final String DELETE_USER_TITLE = "Delete User";
    private static final String DELETE_USER_MESSAGE = "Are you sure you want to delete this user? This action cannot be undone.";
    private static final String DELETE_BUTTON = "Delete";
    private static final String CANCEL_BUTTON = "Cancel";
    private static final String USER_DELETED_SUCCESS = "User deleted successfully";
    private static final String DELETE_ERROR = "Error deleting user";
    private static final String LOAD_ERROR = "Error loading users";
    private static final String SEARCH_HINT = "🔍 Search users...";
    private static final String USERS_COUNT_FORMAT = "%d Users found";
    private static final String ACTIVE_STATUS = "Active since registration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        initializeViews();
        setupFirebase();
        setupRecyclerView();
        setupSearchFunctionality();
        setupClickListeners();
        loadUsers();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvTitle = findViewById(R.id.tvTitle);
        tvUsersCount = findViewById(R.id.tffvUsersCount);
        etSearchUsers = findViewById(R.id.etSearchUsers);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingLayout = findViewById(R.id.loadingLayout);

        // Set title
        tvTitle.setText(MANAGE_USERS_TITLE);
        tvUsersCount.setText(LOADING_DATA);
    }

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
    }

    private void setupRecyclerView() {
        usersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();
        userAdapter = new UserAdapter(filteredUsersList);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
        recyclerViewUsers.setHasFixedSize(true);
    }

    private void setupSearchFunctionality() {
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnRefresh.setOnClickListener(v -> {
            loadUsers();
            etSearchUsers.setText("");
        });
    }

    private void loadUsers() {
        showLoading(true);

        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }

        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        User user = new User();
                        user.setUserId(snapshot.getKey());
                        user.setUsername(snapshot.child("username").getValue(String.class));
                        user.setEmail(snapshot.child("email").getValue(String.class));
                        user.setRole(snapshot.child("role").getValue(String.class));

                        Long createdAtLong = snapshot.child("createdAt").getValue(Long.class);
                        if (createdAtLong != null) {
                            user.setCreatedAt(createdAtLong);
                        }

                        usersList.add(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                filteredUsersList.clear();
                filteredUsersList.addAll(usersList);
                userAdapter.notifyDataSetChanged();

                updateUsersCount();
                showLoading(false);

                if (usersList.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showLoading(false);
                Toast.makeText(ManageUsersActivity.this, LOAD_ERROR + ": " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        usersRef.addValueEventListener(usersListener);
    }
//for search users
    private void filterUsers(String query) {
        filteredUsersList.clear();

        if (query.isEmpty()) {
            filteredUsersList.addAll(usersList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (User user : usersList) {
                if ((user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerCaseQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseQuery))) {
                    filteredUsersList.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged();
        updateUsersCount();

        if (filteredUsersList.isEmpty() && !query.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }

    private void updateUsersCount() {
        tvUsersCount.setText(String.format(Locale.getDefault(), USERS_COUNT_FORMAT, filteredUsersList.size()));
    }

    private void showLoading(boolean show) {
        loadingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewUsers.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewUsers.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle(DELETE_USER_TITLE)
                .setMessage(DELETE_USER_MESSAGE)
                .setPositiveButton(DELETE_BUTTON, (dialog, which) -> {
                    usersRef.child(user.getUserId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ManageUsersActivity.this, USER_DELETED_SUCCESS,
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ManageUsersActivity.this, DELETE_ERROR + ": " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(CANCEL_BUTTON, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersListener != null && usersRef != null) {
            usersRef.removeEventListener(usersListener);
        }
    }

    // User Model Class
    public static class User {
        private String userId;
        private String username;
        private String email;
        private String role;
        private long createdAt;

        public User() {}

        public User(String userId, String username, String email, String role, long createdAt) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
            this.createdAt = createdAt;
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

        public String getFormattedJoinDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            return "Joined: " + sdf.format(new Date(createdAt));
        }
    }

    // RecyclerView Adapter
    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;

        public UserAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_card, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
            private CardView cardView;
            private ImageView ivUserAvatar;
            private TextView tvUsername, tvUserEmail, tvUserRole, tvJoinDate;
            private TextView tvUserStats, tvUserId;
            private ImageButton btnDeleteUser;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);

                cardView = itemView.findViewById(R.id.cardView);
                ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
                tvUserRole = itemView.findViewById(R.id.tvUserRole);
                tvJoinDate = itemView.findViewById(R.id.tvJoinDate);
                tvUserStats = itemView.findViewById(R.id.tvUserStats);
                tvUserId = itemView.findViewById(R.id.tvUserId);
                btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
            }

            public void bind(User user) {
                // Set user data
                tvUsername.setText(user.getUsername() != null ? user.getUsername() : "Unknown");
                tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
                tvUserRole.setText(user.getRole() != null ? user.getRole() : "user");
                tvJoinDate.setText(user.getFormattedJoinDate());
                tvUserStats.setText(ACTIVE_STATUS);
                tvUserId.setText("ID: #" + (user.getUserId() != null ? user.getUserId().substring(0, 8) : "Unknown"));

                // Set role badge color based on role
                if ("admin".equals(user.getRole())) {
                    tvUserRole.setBackgroundResource(R.drawable.admin_role_badge_background);
                } else {
                    tvUserRole.setBackgroundResource(R.drawable.role_badge_background);
                }

                // Set click listeners
                btnDeleteUser.setOnClickListener(v -> deleteUser(user));

                cardView.setOnClickListener(v -> {
                    // Handle card click if needed
                    Toast.makeText(itemView.getContext(),
                            "User: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}