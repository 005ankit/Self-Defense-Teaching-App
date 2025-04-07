package com.example.selfdefense;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class PasswordResetActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private OkHttpClient httpClient;

    private static final String SUPABASE_URL = "https://murzkwfoygbpejaffnpn.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";
    private static final String RESET_PASSWORD_ENDPOINT = SUPABASE_URL + "/auth/v1/user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        // Initialize UI elements
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        httpClient = new OkHttpClient();

        // Extract the reset token from the intent
        String token = getIntent().getStringExtra("token");
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Invalid or missing token", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Decode the token to extract the user ID
        String userId = decodeUserIdFromToken(token);
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "Failed to extract user ID from token", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnResetPassword.setOnClickListener(view -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send the reset password request
            resetPassword(userId, newPassword,token);
        });
    }

    private String decodeUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("email").asString(); // The 'sub' field contains the user ID
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("JWT Decode Error", "Failed to decode JWT: " + e.getMessage());
            return null;
        }
    }

    private void resetPassword(String userId, String newPassword, String token) {
        // Create JSON payload
        JSONObject passwordResetData = new JSONObject();
        try {
            passwordResetData.put("email", userId); // Use the extracted user ID
            passwordResetData.put("password", newPassword);
            passwordResetData.put("data", new JSONObject().put("hello", "world"));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build HTTP request
        RequestBody body = RequestBody.create(passwordResetData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(RESET_PASSWORD_ENDPOINT)
                .header("apikey", SUPABASE_KEY) // Service role key here
                .header("Authorization", "Bearer " + token) // Service role key here
                .put(body)
                .build();

        // Send HTTP request
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(PasswordResetActivity.this, "Password reset failed. Please check your connection.", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int responseCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "null";

                Log.d("Response Code", String.valueOf(responseCode));
                Log.d("Response Body", responseBody);

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PasswordResetActivity.this, "Password reset successful", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(PasswordResetActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Log.e("Error Response", "Code: " + responseCode + ", Body: " + responseBody);
                    runOnUiThread(() ->
                            Toast.makeText(PasswordResetActivity.this, "Error: " + responseBody, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
