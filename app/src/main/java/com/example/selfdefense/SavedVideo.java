package com.example.selfdefense;

public class SavedVideo {
    private int videoId;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnail;

    public SavedVideo(int videoId, String title, String description, String videoUrl, String thumbnail) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnail = thumbnail;
    }

    public int getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
