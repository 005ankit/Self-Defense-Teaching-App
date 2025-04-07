package com.example.selfdefense;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationWorker extends Worker {

    private static final String SUPABASE_URL = "https://murzkwfoygbpejaffnpn.supabase.co";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";
    private static final String CHANNEL_ID = "safety_tips_channel";
    private static final int NOTIFICATION_ID = 1;

    private final OkHttpClient client = new OkHttpClient();

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        fetchSafetyTip();
        return Result.success();
    }

    private void fetchSafetyTip() {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/rpc/get_random_safety_tip")
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("NotificationWorker", "Error fetching tip: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        if (jsonArray.length() > 0) {
                            JSONObject firstTip = jsonArray.getJSONObject(0);
                            String subtitle = firstTip.getString("subtitle");
                            String detailedMessage = firstTip.getString("detailed_message");

                            showNotification(subtitle, detailedMessage);
                        } else {
                            Log.e("NotificationWorker", "No safety tips found.");
                        }
                    } catch (JSONException e) {
                        Log.e("NotificationWorker", "Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("NotificationWorker", "Error response code: " + response.code());
                }
            }
        });
    }

    private void showNotification(String subtitle, String detailedMessage) {
        Context context = getApplicationContext();

        // Create the intent to open SafetyTipsActivity
        Intent intent = new Intent(context, SafetyTipsActivity.class);
        intent.putExtra("subtitle", subtitle);
        intent.putExtra("detailedMessage", detailedMessage);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create NotificationChannel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Safety Tips",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo) // Replace with your app's notification icon
                .setContentTitle("Daily Safety Tip")
                .setContentText(subtitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(subtitle))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
