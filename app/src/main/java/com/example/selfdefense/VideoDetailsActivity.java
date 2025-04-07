package com.example.selfdefense;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoDetailsActivity extends AppCompatActivity {

    private TextView videoTitle, videoDescription;
    private RecyclerView stepsRecyclerView;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private LinearLayout titleContainer, collapsibleSection;
    private ImageView arrowToggle;

    private static final String SUPABASE_URL = "https://murzkwfoygbpejaffnpn.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);

        arrowToggle = findViewById(R.id.arrow_toggle);
        collapsibleSection = findViewById(R.id.collapsible_section);
        titleContainer = findViewById(R.id.title_container);
        videoTitle = findViewById(R.id.video_title);
        videoDescription = findViewById(R.id.video_description);
        stepsRecyclerView = findViewById(R.id.steps_recycler_view);
        playerView = findViewById(R.id.player_view);
        ProgressBar progressBar = findViewById(R.id.download_progress);

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId= sharedPreferences.getString("userId", null);
        // Receive video data from LearnActivity through Intent
        String videoUrl = getIntent().getStringExtra("video_url");
        String title = getIntent().getStringExtra("video_title");
        String description = getIntent().getStringExtra("video_description");
        int videoId = getIntent().getIntExtra("video_id", -1);

        // Toggle on Title Click
        titleContainer.setOnClickListener(v -> {
            if (collapsibleSection.getVisibility() == View.GONE) {
                // Expand
                collapsibleSection.setVisibility(View.VISIBLE);
                arrowToggle.animate().rotation(180f).setDuration(300).start(); // Rotate arrow up
            } else {
                // Collapse
                collapsibleSection.setVisibility(View.GONE);
                arrowToggle.animate().rotation(0f).setDuration(300).start(); // Rotate arrow down
            }
        });

        // Display video details
        videoTitle.setText(title);
        videoDescription.setText(description);

        // Initialize ExoPlayer and play video
        initializePlayer(videoUrl);

        LinearLayout saveButton = findViewById(R.id.btn_save_video);
        saveButton.setOnClickListener(v -> {
            if (userId != null && videoId != -1) {
                Log.d("onCreate: ","Saving Video...");
                saveVideoToSupabase(userId, videoId);
            } else {
                Toast.makeText(this, "Unable to save video. Missing data.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up RecyclerView for steps
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch steps from Supabase and bind to RecyclerView
        fetchStepsFromSupabase(videoId);

        // Handle Download Button Click
        LinearLayout downloadButton = findViewById(R.id.btn_download_video);
        downloadButton.setOnClickListener(v -> {
            if (videoUrl != null && !videoUrl.isEmpty()) {
                new DownloadVideoTask(this, progressBar).execute(videoUrl, title + ".mp4");
            } else {
                Toast.makeText(this, "Video URL is missing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveVideoToSupabase(String userId, int videoId) {
        // Supabase REST API endpoint for saved_videos table
        String url = SUPABASE_URL + "/rest/v1/saved_videos";

        // JSON payload for the save request
        JSONObject payload = new JSONObject();
        try {
            payload.put("user_id", userId);
            payload.put("video_id", videoId);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating save request payload.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Request to Supabase
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(payload.toString(), okhttp3.MediaType.parse("application/json")))
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(VideoDetailsActivity.this, "Failed to save video: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(VideoDetailsActivity.this, "Video saved successfully.", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(VideoDetailsActivity.this, "Video is already saved", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustPlayerView();
    }


    private void initializePlayer(String videoUrl) {
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        // Prepare and play the video
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        // Listen for video completion
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    // Video has finished playing
                    updateProgress();
                }
            }
        });
    }

    private void updateProgress() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Increment progress
        int currentProgress = sharedPreferences.getInt("dailyProgress", 0);
        int newProgress = Math.min(currentProgress + 10, 100); // Increment by 10%, max 100%

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("dailyProgress", newProgress);
        editor.apply();


        // Notify user
        Toast.makeText(this, "Progress updated: " + newProgress + "%", Toast.LENGTH_SHORT).show();
    }
    private void fetchStepsFromSupabase(int videoId) {
        if (videoId == -1) {
            Log.e("VideoDetailsActivity", "Invalid video ID");
            return;
        }

        // Construct the Supabase request URL
        String url = SUPABASE_URL + "/rest/v1/steps?video_id=eq." + videoId;

        // Create the request
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SupabaseError", "Error fetching steps: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    // Deserialize JSON response into Step objects
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Step>>() {}.getType();
                    List<Step> steps = gson.fromJson(responseBody, listType);

                    // Update the RecyclerView on the main thread
                    runOnUiThread(() -> {
                        StepsAdapter stepsAdapter = new StepsAdapter(steps);
                        stepsRecyclerView.setAdapter(stepsAdapter);
                    });
                } else {
                    Log.e("SupabaseError", "Error response code: " + response.code());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class DownloadVideoTask extends AsyncTask<String, Integer, Boolean> {

        private Context context;
        private ProgressBar progressBar;
        private String fileName; // Add fileName as a class-level variable
        private String filePath; // Add filePath as a class-level variable to store the final path

        public DownloadVideoTask(Context context, ProgressBar progressBar) {
            this.context = context;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0); // Reset progress to 0
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String videoUrl = params[0];
            String fileName = params[1];

            try {
                // Define the videos directory
                File videosDir = new File(context.getFilesDir(), "DownloadedVideos");

                // Ensure the directory exists
                if (!videosDir.exists()) {
                    boolean dirCreated = videosDir.mkdir();
                    if (!dirCreated) {
                        Log.e("DownloadVideoTask", "Failed to create directory for videos.");
                        return false;
                    }
                }

                // Save the video file in the videos directory
                File file = new File(videosDir, fileName);
                URL url = new URL(videoUrl);
                InputStream inputStream = url.openStream();

                // Get the file size for progress tracking
                int contentLength = url.openConnection().getContentLength();

                try (OutputStream outputStream = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    int totalBytesRead = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        // Update progress
                        if (contentLength > 0) {
                            publishProgress((int) ((totalBytesRead * 100L) / contentLength));
                        }
                    }

                    return true; // Download completed successfully
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false; // Download failed
            }
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (progressBar != null) {
                progressBar.setProgress(values[0]); // Update progress bar
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            if (success) {
                Toast.makeText(context, "Video downloaded successfully", Toast.LENGTH_SHORT).show();
                // Save video metadata
                if (context instanceof VideoDetailsActivity) {
                    ((VideoDetailsActivity) context).saveVideoMetadata(fileName, filePath);
                }

            } else {
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveVideoMetadata(String fileName, String filePath) {
        SharedPreferences sharedPreferences = getSharedPreferences("DownloadedVideos", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String existingVideos = sharedPreferences.getString("videoList", "[]");
        try {
            JSONArray videoArray = new JSONArray(existingVideos);

            JSONObject videoObject = new JSONObject();
            videoObject.put("fileName", fileName);
            videoObject.put("filePath", filePath);

            videoArray.put(videoObject);

            editor.putString("videoList", videoArray.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Adjust PlayerView on orientation change
    private void adjustPlayerView() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Enter fullscreen mode
            playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            // Hide action bar if available
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Exit fullscreen mode
            playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            // Show action bar if available
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
