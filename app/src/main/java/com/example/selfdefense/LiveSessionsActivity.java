package com.example.selfdefense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LiveSessionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LiveSessionAdapter liveSessionAdapter;
    private List<LiveSession> liveSessionList;
    private OkHttpClient httpClient;

    private static final String SUPABASE_URL = "https://murzkwfoygbpejaffnpn.supabase.co"; // Replace with your Supabase project URL
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA"; // Replace with your Supabase API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_sessions);

        recyclerView = findViewById(R.id.recycler_live_sessions);
        liveSessionList = new ArrayList<>();
        httpClient = new OkHttpClient();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        liveSessionAdapter = new LiveSessionAdapter(this, liveSessionList, this::onSessionClick);
        recyclerView.setAdapter(liveSessionAdapter);

        // Fetch data from Supabase
        fetchLiveSessions();
    }

    private void fetchLiveSessions() {
        String url = SUPABASE_URL + "/rest/v1/live_sessions?select=*"; // Replace "live_sessions" with your table name
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("LiveSessionsActivity", "Failed to fetch sessions: " + e.getMessage());
                showToast("Failed to fetch sessions.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONArray sessions = new JSONArray(responseBody);

                        for (int i = 0; i < sessions.length(); i++) {
                            JSONObject sessionObject = sessions.getJSONObject(i);

                            // Retrieve all fields
                            String id = sessionObject.getString("id");
                            String title = sessionObject.getString("title");
                            String hostName = sessionObject.getString("host_name");
                            String description = sessionObject.optString("description", "");
                            String dateTime = sessionObject.getString("date_time");
                            String sessionType = sessionObject.getString("session_type");
                            String joinLink = sessionObject.optString("join_link", "");
                            String address = sessionObject.optString("address", "");
                            double latitude = sessionObject.optDouble("latitude", 0.0);
                            double longitude = sessionObject.optDouble("longitude", 0.0);

                            // Add to the session list
                            liveSessionList.add(new LiveSession(id, title, hostName, description, dateTime, sessionType,
                                    joinLink, address, latitude, longitude));
                        }

                        runOnUiThread(() -> liveSessionAdapter.notifyDataSetChanged());
                    } catch (Exception e) {
                        Log.e("LiveSessionsActivity", "Error parsing JSON: " + e.getMessage());
                        showToast("Error parsing session data.");
                    }
                } else {
                    Log.e("LiveSessionsActivity", "Server error: " + response.message());
                    showToast("Failed to fetch sessions.");
                }
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void onSessionClick(LiveSession session) {
        Intent intent = new Intent(this, SessionDetailActivity.class);
        intent.putExtra("id", session.getId());
        intent.putExtra("title", session.getTitle());
        intent.putExtra("hostName", session.getHostName());
        intent.putExtra("description", session.getDescription());
        intent.putExtra("dateTime", session.getDateTime());
        intent.putExtra("sessionType", session.getSessionType());
        intent.putExtra("joinLink", session.getJoinLink());
        intent.putExtra("address", session.getAddress());
        intent.putExtra("latitude", session.getLatitude());
        intent.putExtra("longitude", session.getLongitude());
        startActivity(intent);
    }
}
