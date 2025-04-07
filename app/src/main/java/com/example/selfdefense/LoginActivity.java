package com.example.selfdefense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
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

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;

    private static final String SUPABASE_URL = "https://murzkwfoygbpejaffnpn.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA"; // Keep your key secure
    private static final String AUTH_ENDPOINT = SUPABASE_URL + "/auth/v1/token";
    private static final String PASSWORD_RESET_ENDPOINT = SUPABASE_URL + "/auth/v1/recover";

    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Initialize OkHttp client
        httpClient = new OkHttpClient();

        // Set up login button
        btnLogin.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Fill credentials", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        // Redirect to signup activity
        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle forgot password
        tvForgotPassword.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(email);
            }
        });
    }

    private void loginUser(String email, String password) {
        JSONObject loginData = new JSONObject();
        try {
            loginData.put("email", email);
            loginData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(loginData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .header("apikey", SUPABASE_KEY)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed. Please check your connection.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful() && !responseBody.isEmpty()) {
                    try {
                        JSONObject responseObject = new JSONObject(responseBody);
                        JSONObject user = responseObject.getJSONObject("user");
                        String userId = user.getString("id");
                        String userToken = responseObject.getString("access_token");

                        // Fetch profile data after successful login
                        fetchUserProfile(userId, userToken);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        try {
                            JSONObject errorObj = new JSONObject(responseBody);
                            String errorMessage = errorObj.optString("message", "Login failed. Try again.");
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "Unexpected error occurred.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    private void resetPassword(String email) {
        // JSON for password reset request
        JSONObject resetData = new JSONObject();
        try {
            resetData.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(resetData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(PASSWORD_RESET_ENDPOINT)
                .header("apikey", SUPABASE_KEY)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void fetchUserProfile(String userId, String userToken) {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/profiles?id=eq." + userId)
                .header("Authorization", "Bearer " + userToken)
                .header("apikey", SUPABASE_KEY)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : null;
                Log.d("onResponse: ", responseBody);
                if (response.isSuccessful()) {
                    try {
                        JSONArray responseArray = new JSONArray(responseBody); // Assuming response is an array
                        JSONObject profile = responseArray.getJSONObject(0); // First object in the array

                        String username = profile.getString("username");
                        String emergencyContact = profile.getString("emergency_contact");
                        String profileImageUrl = profile.getString("profile_image_url");
                        String role = profile.getString("role");
                        String userId=profile.getString("id");

                        String email = editTextEmail.getText().toString().trim();
                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                        intent.putExtra("userId",userId);
                        intent.putExtra("email", email);
                        intent.putExtra("username", username);
                        intent.putExtra("emergency_contact", emergencyContact);
                        intent.putExtra("profile_image_url", profileImageUrl);
                        intent.putExtra("role", role);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
