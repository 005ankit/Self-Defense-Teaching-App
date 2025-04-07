package com.example.selfdefense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedbackActivity extends AppCompatActivity {

    private ImageView back;
    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private ProgressBar progressBar;
    private Button submit;
    private RatingBar ratingBar;
    private EditText feedbackDescription;

    private List<Feedback> feedbackList = new ArrayList<>();
    private boolean isLoading = false;
    private int limit = 10; // Number of items per page
    private int offset = 0; // Start offset
    private boolean hasMoreData = true;

    private String userId;
    private final String supabaseUrl = "https://murzkwfoygbpejaffnpn.supabase.co";
    private final String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null);
        // Initialize views
        feedbackRecyclerView = findViewById(R.id.feedback_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        submit = findViewById(R.id.submit_feedback_button);
        ratingBar = findViewById(R.id.rating_bar);
        feedbackDescription = findViewById(R.id.feedback_description);
        back=findViewById(R.id.back_arrow);
        // Set up RecyclerView
        feedbackAdapter = new FeedbackAdapter(this, feedbackList);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackRecyclerView.setAdapter(feedbackAdapter);

        back.setOnClickListener(view -> {
            finish();
        });
        // Add scroll listener for pagination
        feedbackRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMoreData &&
                        layoutManager.findLastCompletelyVisibleItemPosition() == feedbackList.size() - 1) {
                    fetchFeedback(limit, offset);
                }
            }
        });

        // Fetch initial data
        fetchFeedback(limit, offset);

        // Submit feedback button click listener
        submit.setOnClickListener(v -> submitFeedback());
    }

    private void fetchFeedback(int limit, int offset) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient();
        String queryUrl = supabaseUrl + "/rest/v1/feedback?select=feedback_id,rating,feedback_text,created_at,profiles(username,profile_image_url)&limit="
                + limit + "&offset=" + offset + "&order=created_at.desc";

        Request request = new Request.Builder()
                .url(queryUrl)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    Toast.makeText(FeedbackActivity.this, "Failed to fetch feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        if (jsonArray.length() < limit) {
                            hasMoreData = false; // No more data to fetch
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject feedbackObj = jsonArray.getJSONObject(i);

                            int feedbackId = feedbackObj.getInt("feedback_id");
                            int rating = feedbackObj.getInt("rating");
                            String feedbackText = feedbackObj.getString("feedback_text");
                            String createdAt = feedbackObj.getString("created_at");

                            JSONObject profile = feedbackObj.getJSONObject("profiles");
                            String username = profile.getString("username");
                            String avatarUrl = profile.optString("profile_image_url", null);

                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                            Date date = inputFormat.parse(createdAt);
                            // Define the format for just the date
                            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

                            // Get only the date in the desired format
                            String dateOnly = outputFormat.format(date);

                            Feedback feedback = new Feedback(feedbackId, rating, feedbackText, dateOnly, username, avatarUrl);
                            feedbackList.add(feedback);
                        }

                        runOnUiThread(() -> feedbackAdapter.notifyDataSetChanged());
                        FeedbackActivity.this.offset += limit; // Update offset for next fetch
                    } catch (JSONException | ParseException e) {
                        runOnUiThread(() -> Toast.makeText(FeedbackActivity.this, "Error parsing feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(FeedbackActivity.this, "Failed to fetch feedback: " + response.message(), Toast.LENGTH_SHORT).show());
                }

                isLoading = false;
            }
        });
    }

    private void submitFeedback() {
        int rating = (int) ratingBar.getRating();
        String description = feedbackDescription.getText().toString().trim();

        if (rating == 0 || description.isEmpty()) {
            Toast.makeText(this, "Please provide a rating and feedback description.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        submit.setEnabled(false);

        try {
            // Construct JSON payload
            JSONObject feedbackPayload = new JSONObject();
            feedbackPayload.put("rating", rating);
            feedbackPayload.put("feedback_text", description);
            feedbackPayload.put("user_id", userId); // Replace with the actual user ID

            // Send the feedback to Supabase
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, feedbackPayload.toString());
            String url = supabaseUrl + "/rest/v1/feedback";

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("apikey", apiKey)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        submit.setEnabled(true);
                        Toast.makeText(FeedbackActivity.this, "Failed to submit feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        submit.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(FeedbackActivity.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                            feedbackDescription.setText("");
                            ratingBar.setRating(0);
                        } else {
                            Toast.makeText(FeedbackActivity.this, "Failed to submit feedback: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            submit.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
