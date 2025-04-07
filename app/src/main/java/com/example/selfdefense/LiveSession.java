package com.example.selfdefense;

import java.io.Serializable;

public class LiveSession implements Serializable {
    private String id;
    private String title;
    private String hostName;
    private String description;
    private String dateTime;
    private String sessionType;
    private String joinLink;
    private String address;
    private double latitude;
    private double longitude;

    // Constructor
    public LiveSession(String id, String title, String hostName, String description, String dateTime,
                       String sessionType, String joinLink, String address, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.hostName = hostName;
        this.description = description;
        this.dateTime = dateTime;
        this.sessionType = sessionType;
        this.joinLink = joinLink;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getHostName() {
        return hostName;
    }

    public String getDescription() {
        return description;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getJoinLink() {
        return joinLink;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
