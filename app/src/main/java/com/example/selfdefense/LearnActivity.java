package com.example.selfdefense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LearnActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private OkHttpClient client;
    private List<String> categories;
    private ArrayAdapter<String> spinnerAdapter;
    private List<Video> videoList;
    private VideoAdapter videoAdapter;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        categorySpinner = findViewById(R.id.category_spinner);
        client = new OkHttpClient();
        categories = new ArrayList<>();
        videoList = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        recyclerView=findViewById(R.id.video_recycler_view);
        setupCategorySpinner();
        videoAdapter = new VideoAdapter(videoList, video -> {
            Intent intent = new Intent(LearnActivity.this, VideoDetailsActivity.class);
            intent.putExtra("video_url", video.getVideoUrl());
            intent.putExtra("video_title", video.getTitle());
            intent.putExtra("video_description", video.getDescription());
            intent.putExtra("video_id",video.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set a LinearLayoutManager
        recyclerView.setAdapter(videoAdapter); // Attach the adapter


    }

    private void setupCategorySpinner() {
        Map<String, Integer> categoryMap = new HashMap<>();
        categories.add("All"); // Add "All" at the top
        spinnerAdapter.notifyDataSetChanged();

        String url = "https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/categories";

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                .header("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LearnActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    Log.e("Supabase", "Request failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : null;
                Log.d("Supabase", "Categories Response: " + responseBody);
                if (response.isSuccessful()) {
                    try {
                        JSONArray responseArray = new JSONArray(responseBody);
                        categories.clear();
                        categories.add("All");

                        for (int i = 0; i < responseArray.length(); i++) {
                            JSONObject category = responseArray.getJSONObject(i);
                            String categoryName = category.getString("name");
                            int categoryId = category.getInt("category_id"); // Adjust field name if needed
                            categoryMap.put(categoryName, categoryId);
                            categories.add(categoryName);
                        }

                        runOnUiThread(() -> spinnerAdapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e("Supabase", "Error parsing categories: " + e.getMessage());
                    }
                } else {
                    Log.e("Supabase", "Categories Request failed: " + response.message());
                }
            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories.get(position);
                if (selectedCategory.equals("All")) {
                    loadVideosByCategory(-1);
                } else {
                    Integer categoryId = categoryMap.get(selectedCategory);
                    if (categoryId != null) {
                        loadVideosByCategory(categoryId);
                    } else {
                        Toast.makeText(LearnActivity.this, "Invalid category", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadVideosByCategory(int categoryId) {
        videoList.clear();

        String url = (categoryId == -1) ?
                "https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/videos" :
                "https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/videos?category_id=eq." + categoryId;

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                .header("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LearnActivity.this, "Failed to load videos", Toast.LENGTH_SHORT).show();
                    Log.e("Supabase", "Request failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : null;
                Log.d("Supabase", "Videos Response: " + responseBody);
                if (response.isSuccessful()) {
                    try {
                        JSONArray responseArray = new JSONArray(responseBody);

                        for (int i = 0; i < responseArray.length(); i++) {
                            JSONObject video = responseArray.getJSONObject(i);
                            int id=video.getInt("video_id");
                            String title = video.getString("title");
                            String description = video.getString("description");
                            String videoUrl = video.getString("video_url");
                            String thumbnailUrl = video.getString("thumbnail");

                            videoList.add(new Video(id,title, description, videoUrl, thumbnailUrl));
                        }

                        runOnUiThread(() -> {
                            videoAdapter.notifyDataSetChanged();
                            if (videoList.isEmpty()) {
                                Toast.makeText(LearnActivity.this, "No videos available", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("Supabase", "Error parsing videos: " + e.getMessage());
                    }
                } else {
                    Log.e("Supabase", "Videos Request failed: " + response.message());
                }
            }
        });
    }
}
