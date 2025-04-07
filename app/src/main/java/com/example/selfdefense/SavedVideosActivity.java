package com.example.selfdefense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SavedVideosActivity extends AppCompatActivity {

    private ImageView back;
    private RecyclerView savedVideosRecyclerView;
    private TextView emptyStateMessage;
    private List<SavedVideo> savedVideos;
    private SavedVideoAdapter savedVideoAdapter;

    private String userId;
    // Supabase API URL and Key
    private final String supabaseUrl = "https://murzkwfoygbpejaffnpn.supabase.co";
    private final String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_videos);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);

        back=findViewById(R.id.back_arrow);
        // Initialize views
        savedVideosRecyclerView = findViewById(R.id.saved_videos_recycler_view);
        emptyStateMessage = findViewById(R.id.empty_state_message);

        // Initialize list and adapter
        savedVideos = new ArrayList<>();
        savedVideoAdapter = new SavedVideoAdapter(savedVideos, this::onPlayClicked, this::onRemoveClicked);
        savedVideosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedVideosRecyclerView.setAdapter(savedVideoAdapter);

        back.setOnClickListener(view -> {
            finish();
        });
        // Fetch saved videos
        fetchSavedVideos();
    }

    private void onRemoveClicked(SavedVideo savedVideo) {
        removeVideoFromSaved(savedVideo);
    }

    private void onPlayClicked(SavedVideo video) {
        Intent intent=new Intent(SavedVideosActivity.this,VideoDetailsActivity.class);
        intent.putExtra("video_url", video.getVideoUrl());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("video_description", video.getDescription());
        intent.putExtra("video_id",video.getVideoId());
        startActivity(intent);
    }

    private void fetchSavedVideos() {
        OkHttpClient client = new OkHttpClient();
        String queryUrl = supabaseUrl + "/rest/v1/saved_videos?select=videos(video_id,title,description,video_url,thumbnail)&user_id=eq."+userId;

        Request request = new Request.Builder()
                .url(queryUrl)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SavedVideosActivity.this, "Failed to fetch videos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<SavedVideo> fetchedVideos = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject videoObj = jsonArray.getJSONObject(i).getJSONObject("videos");

                            int videoId = videoObj.getInt("video_id");
                            String title = videoObj.getString("title");
                            String description = videoObj.optString("description", "");
                            String videoUrl = videoObj.getString("video_url");
                            String thumbnail = videoObj.optString("thumbnail", "");

                            fetchedVideos.add(new SavedVideo(videoId, title, description, videoUrl, thumbnail));
                        }

                        runOnUiThread(() -> {
                            savedVideos.clear();
                            savedVideos.addAll(fetchedVideos);
                            savedVideoAdapter.notifyDataSetChanged();
                            updateEmptyState();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(SavedVideosActivity.this, "Error parsing videos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(SavedVideosActivity.this, "Failed to fetch videos: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateEmptyState() {
        if (savedVideos.isEmpty()) {
            emptyStateMessage.setVisibility(View.VISIBLE);
            savedVideosRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateMessage.setVisibility(View.GONE);
            savedVideosRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void removeVideoFromSaved(SavedVideo video) {
        OkHttpClient client = new OkHttpClient();
        String queryUrl = supabaseUrl + "/rest/v1/saved_videos?video_id=eq." + video.getVideoId() + "&user_id=eq."+userId;

        Request request = new Request.Builder()
                .url(queryUrl)
                .delete()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(SavedVideosActivity.this, "Failed to remove video: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("onResponse: ",response.body().string());
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        savedVideos.remove(video);
                        savedVideoAdapter.notifyDataSetChanged();
                        updateEmptyState();
                        Toast.makeText(SavedVideosActivity.this, video.getTitle() + " removed.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(SavedVideosActivity.this, "Failed to remove video: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
