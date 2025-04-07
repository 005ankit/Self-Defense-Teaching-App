package com.example.selfdefense;

public class Feedback {
    private int feedbackId;
    private int rating;
    private String feedbackText;
    private String createdAt;
    private String username;
    private String avatarUrl;

    public Feedback(int feedbackId, int rating, String feedbackText, String createdAt, String username, String avatarUrl) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.feedbackText = feedbackText;
        this.createdAt = createdAt;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    // Getters and setters
    public int getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
