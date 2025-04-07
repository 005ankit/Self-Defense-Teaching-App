package com.example.selfdefense;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Configure Cloudinary
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "diig6qhjl");
        config.put("api_key", "627236377285327");
        config.put("api_secret", "3AQYhR4QoGYttQNZ2WoucgPUAx4");
        MediaManager.init(this, config);


    }
}
