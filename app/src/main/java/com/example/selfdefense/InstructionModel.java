package com.example.selfdefense;

public class InstructionModel {
    private int instructionNumber;
    private String description;
    private String imagePath;
    private String imageUrl; // If required for Cloudinary URLs

    // Constructor
    public InstructionModel(int instructionNumber, String description, String imagePath, String imageUrl) {
        this.instructionNumber = instructionNumber;
        this.description = description;
        this.imagePath = imagePath;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getInstructionNumber() {
        return instructionNumber;
    }

    public void setInstructionNumber(int instructionNumber) {
        this.instructionNumber = instructionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

