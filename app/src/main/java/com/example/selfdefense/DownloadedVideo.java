package com.example.selfdefense;

public class DownloadedVideo {
    private String fileName;
    private String filePath;

    public DownloadedVideo(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}
