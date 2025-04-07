package com.example.selfdefense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MeActivity extends AppCompatActivity {

    private ImageView profile, proimage,back;
    private Button logout;
    private TextView name, emailtv, progressText;
    private LinearLayout about, download, saved, feedback;

    private ProgressBar progress_bar;
    private static final String SHARED_PREFS = "UserSession";
    private static final String PROGRESS_KEY = "dailyProgress";
    private static final String LAST_RESET_TIME_KEY = "lastResetTime";
    private static final int MAX_PROGRESS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_me);

        back=findViewById(R.id.back_icon);
        saved = findViewById(R.id.savedvideo);
        download = findViewById(R.id.download);
        proimage = findViewById(R.id.profile_image);
        name = findViewById(R.id.profile_name);
        emailtv = findViewById(R.id.profile_email);
        profile = findViewById(R.id.profile_edit_icon);
        logout = findViewById(R.id.logout_button);
        about = findViewById(R.id.about);
        feedback = findViewById(R.id.feedback);
        progress_bar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // Reset progress after 24 hours
        resetProgressIfNeeded(sharedPreferences);

        back.setOnClickListener(view -> {
            startActivity(new Intent(MeActivity.this,MainActivity.class));
            finish();
        });
        // Load progress from SharedPreferences
        int progress = sharedPreferences.getInt(PROGRESS_KEY, 0);
        updateProgressUI(progress);

        // Check if user details exist in SharedPreferences
        if (sharedPreferences.contains("username")) {
            // Load saved profile information
            String username = sharedPreferences.getString("username", "");
            String email = sharedPreferences.getString("email", "");
            String profileImageUrl = sharedPreferences.getString("profileImageUrl", "");

            // Set the data to UI elements
            name.setText(username);
            emailtv.setText(email);

            // Load profile image if available
            if (!profileImageUrl.isEmpty()) {
                Glide.with(this).load(profileImageUrl).circleCrop().into(proimage);
            }
        }

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(MeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        logout.setOnClickListener(view -> logoutUser());

        feedback.setOnClickListener(view -> startActivity(new Intent(MeActivity.this, FeedbackActivity.class)));

        saved.setOnClickListener(view -> startActivity(new Intent(MeActivity.this, SavedVideosActivity.class)));

        about.setOnClickListener(view -> {
            Intent intent = new Intent(MeActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        download.setOnClickListener(view -> startActivity(new Intent(MeActivity.this, DownloadedVideosActivity.class)));

    }

    private void updateProgressUI(int progress) {
        progress_bar.setProgress(progress);
        progressText.setText(progress + "%");
    }

    private void resetProgressIfNeeded(SharedPreferences sharedPreferences) {
        long lastResetTime = sharedPreferences.getLong(LAST_RESET_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();

        // Check if 24 hours have passed since the last reset
        if (currentTime - lastResetTime >= 24 * 60 * 60 * 1000) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PROGRESS_KEY, 0); // Reset progress
            editor.putLong(LAST_RESET_TIME_KEY, currentTime); // Update reset time
            editor.apply();

            updateProgressUI(0);
        }
    }

    private void incrementProgress() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int progress = sharedPreferences.getInt(PROGRESS_KEY, 0);

        if (progress < MAX_PROGRESS) {
            progress += 10; // Increment progress by 10%
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PROGRESS_KEY, progress);
            editor.apply();

            updateProgressUI(progress);
        }
    }



    private void logoutUser() {
        // Clear user session data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
