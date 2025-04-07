package com.example.selfdefense;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import okhttp3.*;

public class VideoStepsActivity extends AppCompatActivity {

    private static final int VIDEO_REQUEST_CODE = 1;
    private static final int THUMBNAIL_REQUEST_CODE = 2;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    private Spinner spinnerCategory;
    private EditText etVideoTitle, etVideoDescription;
    private ImageView ivThumbnailPreview;
    private VideoView videoPreview;
    private ProgressBar progressBar;
    private Uri videoUri, thumbnailUri;

    private List<String> categoryList = new ArrayList<>();
    private List<Integer> categoryIds = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_steps);
        initViews();
        fetchCategories();
        checkPermissions();
    }

    private void initViews() {
        spinnerCategory = findViewById(R.id.spinner_category);
        etVideoTitle = findViewById(R.id.et_video_title);
        etVideoDescription = findViewById(R.id.et_video_description);
        ivThumbnailPreview = findViewById(R.id.iv_thumbnail_preview);
        videoPreview = findViewById(R.id.vv_video_preview);
        progressBar = findViewById(R.id.progress_bar);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        spinnerCategory.setAdapter(spinnerAdapter);

        findViewById(R.id.btn_upload_video).setOnClickListener(v -> openPicker("video/*", VIDEO_REQUEST_CODE));
        findViewById(R.id.btn_upload_thumbnail).setOnClickListener(v -> openPicker("image/*", THUMBNAIL_REQUEST_CODE));
        findViewById(R.id.btn_save_video).setOnClickListener(v -> saveVideoDetails());
    }

    private void fetchCategories() {
        String url = "https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/categories";
        Request request = new Request.Builder()
                .url(url)
                .header("apikey","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showToast("Failed to load categories");
                Log.e("Supabase", "Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        categoryList.clear();
                        categoryIds.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject category = jsonArray.getJSONObject(i);
                            categoryList.add(category.getString("name"));
                            categoryIds.add(category.getInt("category_id"));
                        }
                        runOnUiThread(() -> spinnerAdapter.notifyDataSetChanged());
                    } catch (JSONException e) {
                        Log.e("Supabase", "JSON Parsing Error: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void openPicker(String type, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(type);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (requestCode == VIDEO_REQUEST_CODE) {
                videoUri = selectedUri;
                videoPreview.setVisibility(View.VISIBLE);
                videoPreview.setVideoURI(videoUri);
                videoPreview.start();
            } else if (requestCode == THUMBNAIL_REQUEST_CODE) {
                thumbnailUri = selectedUri;
                ivThumbnailPreview.setVisibility(View.VISIBLE);
                ivThumbnailPreview.setImageURI(thumbnailUri);
            }
        }
    }

    private void saveVideoDetails() {
        if (fieldsAreEmpty()) return;
        progressBar.setVisibility(View.VISIBLE);

        uploadToCloudinary(thumbnailUri, "image", thumbnailUrl -> {
            uploadToCloudinary(videoUri, "video", videoUrl -> {
                uploadVideoDetails(etVideoTitle.getText().toString(), etVideoDescription.getText().toString(), videoUrl, thumbnailUrl);
            });
        });
    }

    private boolean fieldsAreEmpty() {
        if (etVideoTitle.getText().toString().isEmpty() || etVideoDescription.getText().toString().isEmpty() ||
                videoUri == null || thumbnailUri == null || spinnerCategory.getSelectedItemPosition() == -1) {
            showToast("Fill in all fields and upload media");
            return true;
        }
        return false;
    }

    private void uploadToCloudinary(Uri uri, String resourceType, CloudinaryCallback callback) {
        String path = getRealPathFromURI(uri);
        MediaManager.get().upload(path)
                .option("resource_type", resourceType)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {

                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                    }

                    @Override public void onSuccess(String requestId, Map resultData) {
                        callback.onSuccess((String) resultData.get("secure_url"));
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {

                    }

                    @Override public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        showToast("Error uploading " + resourceType);
                    }
                }).dispatch();
    }

    private void uploadVideoDetails(String title, String description, String videoUrl, String thumbnailUrl) {
        try {
            JSONObject videoDetails = new JSONObject();
            videoDetails.put("title", title);
            videoDetails.put("description", description);
            videoDetails.put("category_id", categoryIds.get(spinnerCategory.getSelectedItemPosition()));
            videoDetails.put("video_url", videoUrl);
            videoDetails.put("thumbnail", thumbnailUrl);

            RequestBody body = RequestBody.create(videoDetails.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url("https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/videos")
                    .header("apikey","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                    .post(body)
                    .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                    .addHeader("Prefer", "return=representation")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to save video details");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    String responseBody = response.body() != null ? response.body().string() : null;
                    Log.d("onResponse: ",responseBody);
                    if (response.isSuccessful()) {
                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            if (jsonArray.length() > 0) {
                                JSONObject videoObject = jsonArray.getJSONObject(0);
                                int videoId = videoObject.getInt("video_id"); // Assuming the response contains video_id

                                // Navigate to AddStepsActivity
                                Intent intent = new Intent(VideoStepsActivity.this, AddStepsActivity.class);
                                intent.putExtra("video_id", videoId);
                                runOnUiThread(() -> {
                                    Toast.makeText(VideoStepsActivity.this, "Video details saved successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                });
                            }
                        } catch (JSONException e) {
                            Log.e("Supabase", "JSON Parsing Error: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (JSONException e) {
            Log.e("Supabase", "JSON Error: " + e.getMessage());
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:" + getPackageName())));
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showToast("Permissions granted");
        } else {
            showToast("Permissions denied");
        }
    }

    private String getRealPathFromURI(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                File file = new File(getCacheDir(), fileName);
                try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                    java.nio.file.Files.copy(inputStream, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    return file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            Log.e("getRealPathFromURI", "Error: " + e.getMessage());
        }
        return null;
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(VideoStepsActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    interface CloudinaryCallback {
        void onSuccess(String url);
    }
}
