package com.example.selfdefense;

public class Video {
    private int id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;

    public Video(int id,String title, String description, String videoUrl, String thumbnailUrl) {
        this.id=id;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getId(){
        return id;
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
