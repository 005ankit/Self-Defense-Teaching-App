package com.example.selfdefense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String SUPABASE_AUTH_URL = "https://murzkwfoygbpejaffnpn.supabase.co/auth/v1/signup";
    private static final String SUPABASE_DATABASE_URL = "https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/profiles";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";

    private ImageView profileImageView;
    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText, emergencyContactEditText;
    private Button signupButton, uploadImageButton;
    private ProgressBar progressBar;
    private TextView tvAlreadyAccount;

    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI elements
        profileImageView = findViewById(R.id.appLogo);
        usernameEditText = findViewById(R.id.etUsername);
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword);
        emergencyContactEditText = findViewById(R.id.etEmergencyContact);
        signupButton = findViewById(R.id.btnSignUp);
        uploadImageButton = findViewById(R.id.btnUploadImage);
        progressBar = findViewById(R.id.progressBar);
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);

        progressBar.setVisibility(View.GONE);

        tvAlreadyAccount.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        uploadImageButton.setOnClickListener(view -> openImagePicker());

        signupButton.setOnClickListener(view -> handleSignup());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
            uploadImageToCloudinary();
        }
    }

    private void uploadImageToCloudinary() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        MediaManager.get().upload(selectedImageUri)
                .option("folder", "self_defense")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(SignupActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        progressBar.setProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressBar.setVisibility(View.GONE);
                        uploadedImageUrl = resultData.get("secure_url").toString();
                        Glide.with(SignupActivity.this).load(uploadedImageUrl).circleCrop().into(profileImageView);
                        Toast.makeText(SignupActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignupActivity.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignupActivity.this, "Image upload rescheduled.", Toast.LENGTH_SHORT).show();
                    }
                }).dispatch();
    }

    private void handleSignup() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String emergencyContact = emergencyContactEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(emergencyContact)) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uploadedImageUrl == null) {
            Toast.makeText(this, "Please upload a profile image.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Step 1: Sign up user with Supabase Auth
        JSONObject authData = new JSONObject();
        try {
            authData.put("email", email);
            authData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody authBody = RequestBody.create(authData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request authRequest = new Request.Builder()
                .url(SUPABASE_AUTH_URL)
                .header("apikey", SUPABASE_KEY)
                .post(authBody)
                .build();

        client.newCall(authRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d("ResponseBody", responseBody);  // Log the response body for inspection
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String userId = jsonResponse.getString("id");

                        // Step 2: Add user profile to Supabase Database
                        saveUserProfile(userId, username, emergencyContact, uploadedImageUrl,email);

                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Error parsing response.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignupActivity.this, "Signup failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void saveUserProfile(String userId, String username, String emergencyContact, String profileImageUrl,String email) {
        JSONObject profileData = new JSONObject();
        try {
            profileData.put("id", userId);
            profileData.put("username", username);
            profileData.put("emergency_contact", emergencyContact);
            profileData.put("profile_image_url", profileImageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody profileBody = RequestBody.create(profileData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request profileRequest = new Request.Builder()
                .url(SUPABASE_DATABASE_URL)
                .header("apikey", SUPABASE_KEY)
                .header("Prefer", "return=representation")
                .post(profileBody)
                .build();

        client.newCall(profileRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignupActivity.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));

                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        redirectToProfile(userId,username,emergencyContact,profileImageUrl,email);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Failed to save profile: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void redirectToProfile(String user_Id,String username,String emergencyContact,String profileImageUrl,String email) {
        Intent intent = new Intent(SignupActivity.this, ProfileActivity.class);
        intent.putExtra("userId",user_Id);
        intent.putExtra("username",username);
        intent.putExtra("emergency_contact",emergencyContact);
        intent.putExtra("profile_image_url",profileImageUrl);
        intent.putExtra("email",email);
        startActivity(intent);
        finish();
    }
}
