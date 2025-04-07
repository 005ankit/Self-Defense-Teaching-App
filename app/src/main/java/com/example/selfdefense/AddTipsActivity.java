package com.example.selfdefense;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddTipsActivity extends AppCompatActivity {

    private EditText subtitleEditText, detailedMessageEditText;
    private Button submitButton;

    // Supabase credentials
    private final String SUPABASE_URL = "https://murzkwfoygbpejaffnpn.supabase.co";
    private final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tips);

        // Bind UI components
        subtitleEditText = findViewById(R.id.subtitleEditText);
        detailedMessageEditText = findViewById(R.id.detailedMessageEditText);
        submitButton = findViewById(R.id.submitButton);

        // Set Submit Button Listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSafetyTip();
            }
        });
    }

    private void submitSafetyTip() {
        String subtitle = subtitleEditText.getText().toString().trim();
        String detailedMessage = detailedMessageEditText.getText().toString().trim();

        if (TextUtils.isEmpty(subtitle) || TextUtils.isEmpty(detailedMessage)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare JSON data
        JSONObject safetyTipData = new JSONObject();
        try {
            safetyTipData.put("subtitle", subtitle);
            safetyTipData.put("detailed_message", detailedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send data to Supabase
        sendToSupabase(safetyTipData.toString());
    }

    private void sendToSupabase(String jsonData) {
        String url = SUPABASE_URL + "/rest/v1/safety_tips";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(jsonData, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AddTipsActivity.this, "Submission failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("onResponse: ",response.body().string());
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddTipsActivity.this, "Safety Tip Submitted Successfully!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AddTipsActivity.this, "Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void clearForm() {
        subtitleEditText.setText("");
        detailedMessageEditText.setText("");
    }
}
