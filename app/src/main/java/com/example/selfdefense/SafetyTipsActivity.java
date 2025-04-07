package com.example.selfdefense;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SafetyTipsActivity extends AppCompatActivity {

    private Switch notificationSwitch;
    private TextView notificationTime;
    private TextView tipHeader;
    private TextView tipDetails;
    private Button btnFavorite;
    private Button btnViewFavorites;

    private LocalTime targetTime = LocalTime.of(8, 0); // Default time 8:00 AM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_tips);

        notificationSwitch = findViewById(R.id.notification_switch);
        notificationTime = findViewById(R.id.notification_time);
        tipHeader = findViewById(R.id.tip_header);
        tipDetails = findViewById(R.id.tip_details);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnViewFavorites = findViewById(R.id.btn_view_favorites);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Get the safety tip passed by the notification
        String subtitle = getIntent().getStringExtra("subtitle");
        String detailedMessage = getIntent().getStringExtra("detailedMessage");

        if (subtitle != null && detailedMessage != null) {
            tipHeader.setText(subtitle);
            tipDetails.setText(detailedMessage);
        }

        // Restore switch state and time from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("SafetyTipsPrefs", MODE_PRIVATE);
        boolean isSwitchChecked = sharedPreferences.getBoolean("notificationSwitch", false);
        String savedTime = sharedPreferences.getString("notificationTime", "08:00 AM");

        notificationSwitch.setChecked(isSwitchChecked);
        notificationTime.setText(savedTime);

        // Parse the saved time back to LocalTime
        targetTime = parseTime(savedTime);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notificationSwitch", isChecked);
            editor.apply();

            if (isChecked) {
                Toast.makeText(this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
                scheduleDailyNotifications();
            } else {
                WorkManager.getInstance(this).cancelAllWork();
                Toast.makeText(this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        notificationTime.setOnClickListener(view -> showTimePickerDialog());

        btnFavorite.setOnClickListener(view -> {
            // Save favorite as before
            String favoriteSubtitle = tipHeader.getText().toString();
            String favoriteDetails = tipDetails.getText().toString();

           if(!favoriteSubtitle.isEmpty() && !favoriteDetails.isEmpty()) {
               String favoritesJson = sharedPreferences.getString("favoritesList", "[]");
               try {
                   JSONArray favoritesArray = new JSONArray(favoritesJson);
                   JSONObject newFavorite = new JSONObject();
                   newFavorite.put("subtitle", favoriteSubtitle);
                   newFavorite.put("details", favoriteDetails);
                   favoritesArray.put(newFavorite);

                   SharedPreferences.Editor editor = sharedPreferences.edit();
                   editor.putString("favoritesList", favoritesArray.toString());
                   editor.apply();

                   Log.d("Favorites", "Favorites JSON: " + sharedPreferences.getString("favoritesList", "[]"));
                   Toast.makeText(this, "Marked as Favorite!", Toast.LENGTH_SHORT).show();
               } catch (JSONException e) {
                   e.printStackTrace();
                   Toast.makeText(this, "Error saving favorite", Toast.LENGTH_SHORT).show();
               }
           }else{
               Toast.makeText(this, "There is no Tip", Toast.LENGTH_SHORT).show();
           }
        });

        btnViewFavorites.setOnClickListener(view -> {
            Toast.makeText(this, "Viewing Favorites...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SafetyTipsActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

    }

    private void showTimePickerDialog() {
        int hour = targetTime.getHour();
        int minute = targetTime.getMinute();

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    targetTime = LocalTime.of(selectedHour, selectedMinute);
                    String time = String.format(Locale.getDefault(), "%02d:%02d %s",
                            (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12,
                            selectedMinute,
                            (selectedHour < 12 ? "AM" : "PM"));
                    notificationTime.setText(time);

                    // Save time to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("SafetyTipsPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("notificationTime", time);
                    editor.apply();

                    if (notificationSwitch.isChecked()) {
                        WorkManager.getInstance(this).cancelAllWork();
                        scheduleDailyNotifications();
                    }
                },
                hour,
                minute,
                false);
        timePickerDialog.show();
    }

    private LocalTime parseTime(String time) {
        try {
            String[] parts = time.split("[: ]");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String amPm = parts[2];

            if (amPm.equalsIgnoreCase("PM") && hour != 12) {
                hour += 12;
            } else if (amPm.equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }

            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return LocalTime.of(8, 0); // Default to 8:00 AM
        }
    }

    private void scheduleDailyNotifications() {
        long initialDelay = calculateInitialDelay(targetTime);

        PeriodicWorkRequest notificationWork =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueue(notificationWork);
    }

    private long calculateInitialDelay(LocalTime targetTime) {
        long currentTimeMillis = System.currentTimeMillis();
        long targetTimeMillis = targetTime.atDate(java.time.LocalDate.now())
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        if (targetTimeMillis < currentTimeMillis) {
            targetTimeMillis += TimeUnit.DAYS.toMillis(1);
        }
        return targetTimeMillis - currentTimeMillis;
    }
}
