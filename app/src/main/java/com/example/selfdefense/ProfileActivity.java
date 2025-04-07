package com.example.selfdefense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;

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

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView,back;
    private EditText usernameEditText, emergencyContactEditText;
    private TextView emailText;
    private Button uploadImageButton, saveProfileButton;
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    private String uploadedImageUrl,userId;


    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI elements
        profileImageView = findViewById(R.id.profileImageView);
        usernameEditText = findViewById(R.id.editTextUsername);
        emergencyContactEditText = findViewById(R.id.editTextEmergencyContact);
        emailText = findViewById(R.id.textViewEmail);
        uploadImageButton = findViewById(R.id.btnUploadImage);
        saveProfileButton = findViewById(R.id.btnSaveProfile);
        back=findViewById(R.id.backIcon);
        progressBar = findViewById(R.id.progressBar);

        // Load user data from token
        loadUserData();

        back.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this,MeActivity.class));
            finish();
        });
        // Set onClick listener for uploading a new image
        uploadImageButton.setOnClickListener(view -> openImagePicker());

        // Set onClick listener for saving the profile
        saveProfileButton.setOnClickListener(view -> saveUserData());
    }

    private void loadUserData() {

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Check if user details exist in SharedPreferences
        if (sharedPreferences.contains("username")) {
            // Load saved profile information
            String username = sharedPreferences.getString("username", "");
            String emergencyContact = sharedPreferences.getString("emergencyContact", "");
            String email = sharedPreferences.getString("email", "");
            String uploadedImageUrl = sharedPreferences.getString("profileImageUrl", "");
            userId = sharedPreferences.getString("userId","");
            role=sharedPreferences.getString("role","");

            // Set the data to UI elements
            usernameEditText.setText(username);
            emergencyContactEditText.setText(emergencyContact);
            emailText.setText(email);

            // Load profile image if available
            if (!uploadedImageUrl.isEmpty()) {
                Glide.with(this).load(uploadedImageUrl).circleCrop().into(profileImageView);
            }

        } else {
            // Load data from Intent (Signup flow)
            Intent intent = getIntent();
            userId = intent.getStringExtra("userId");
            String username = intent.getStringExtra("username");
            String email = intent.getStringExtra("email");
            String emergencyContact = intent.getStringExtra("emergency_contact");
            String uploadedImageUrl = intent.getStringExtra("profile_image_url");
            role=intent.getStringExtra("role");
            // Set the data to UI elements
            usernameEditText.setText(username);
            emergencyContactEditText.setText(emergencyContact);
            emailText.setText(email);

            if (!TextUtils.isEmpty(uploadedImageUrl)) {
                Glide.with(this).load(uploadedImageUrl).circleCrop().into(profileImageView);
            }
            // Save user data from signup to SharedPreferences
            saveUserDataToSharedPreferences(userId,username, email, emergencyContact, uploadedImageUrl,role);

        }
    }

    private void saveUserDataToSharedPreferences(String userId,String username, String email, String emergencyContact, String profileImageUrl,String role) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId",userId);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("emergencyContact", emergencyContact);
        editor.putString("profileImageUrl", profileImageUrl);
        editor.putString("role",role);
        editor.apply();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);

            // Automatically upload the image to Cloudinary
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
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Show upload progress (if needed)
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Optional: Show upload progress
                    }

                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        progressBar.setVisibility(View.GONE); // Hide loading indicator
                        uploadedImageUrl = resultData.get("secure_url").toString();
                        Glide.with(ProfileActivity.this).load(uploadedImageUrl).circleCrop().into(profileImageView);
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        progressBar.setVisibility(View.GONE); // Hide loading indicator
                        Toast.makeText(ProfileActivity.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        // Handle reschedule if needed
                    }
                }).dispatch();
    }

    private void updateSupabaseProfile(String userId, String username, String emergencyContact, String profileImageUrl) {
        String supabaseUrl = "https://murzkwfoygbpejaffnpn.supabase.co";
        String supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA";

        OkHttpClient client = new OkHttpClient();

        JSONObject profileData = new JSONObject();
        try {
            profileData.put("id", userId); // Primary key of the user in Supabase
            profileData.put("username", username);
            profileData.put("emergency_contact", emergencyContact);
            profileData.put("profile_image_url", profileImageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON data", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(
                profileData.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/profiles?id=eq." + userId)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .method("PATCH", body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Failed to update profile in Supabase", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("onResponse: ",response.body().string());
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Profile updated in Supabase!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Error updating profile: " + response.message(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }


    private void saveUserData() {
        String username = usernameEditText.getText().toString().trim();
        String emergencyContact = emergencyContactEditText.getText().toString().trim();
        String email = emailText.getText().toString().trim();

        if (username.isEmpty() || emergencyContact.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emergencyContact.length() != 10) {
            Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the previously saved image URL if no new image is uploaded
        if (TextUtils.isEmpty(uploadedImageUrl)) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            uploadedImageUrl = sharedPreferences.getString("profileImageUrl", "");
        }
        updateSupabaseProfile(userId, username, emergencyContact, uploadedImageUrl);

        // Save updated profile to SharedPreferences
        saveUserDataToSharedPreferences(userId, username, email, emergencyContact, uploadedImageUrl, role);

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }


}
