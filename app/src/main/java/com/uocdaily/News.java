package com.uocdaily;

public class News {
    private String id;
    private String category;
    private String description;
    private String imageUrl;
    private long timestamp;
    private String title;

    // Default constructor required for Firebase
    public News() {
    }

    // Constructor with parameters
    public News(String id, String category, String description, String imageUrl, long timestamp, String title) {
        this.id = id;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.title = title;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // Method to get formatted time difference
    public String getTimeAgo() {
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
