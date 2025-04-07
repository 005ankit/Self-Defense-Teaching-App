package com.example.selfdefense;

public class Step {
    private int step_id;
    private int video_id;
    private int step_number;
    private String description;
    private String image_url;

    // Getters and setters for all fields
    public int getStepId() {
        return step_id;
    }

    public void setStepId(int step_id) {
        this.step_id = step_id;
    }

    public int getVideoId() {
        return video_id;
    }

    public void setVideoId(int video_id) {
        this.video_id = video_id;
    }

    public int getStepNumber() {
        return step_number;
    }

    public void setStepNumber(int step_number) {
        this.step_number = step_number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }
}
