package com.example.selfdefense;

import static com.example.selfdefense.VideoStepsActivity.STORAGE_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddStepsActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private Button btnAddInstruction, btnSaveInstructions;
    private ProgressBar progressBar;
    private ArrayList<InstructionModel> instructionList = new ArrayList<>();
    private InstructionAdapter instructionAdapter;
    private int selectedInstructionPosition = -1, pendingUploads = 0, videoId;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_steps);

        recyclerView = findViewById(R.id.rv_instruction_list);
        btnAddInstruction = findViewById(R.id.btn_add_instruction);
        btnSaveInstructions = findViewById(R.id.btn_save_instructions);
        progressBar = findViewById(R.id.progress_bar);

        videoId = getIntent().getIntExtra("video_id", -1);
        if (videoId == -1) {
            showToast("Invalid video ID. Please try again.");
            finish();
            return;
        }

        instructionAdapter = new InstructionAdapter(this, instructionList, this::selectImageForInstruction);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(instructionAdapter);

        btnAddInstruction.setOnClickListener(v -> addNewInstruction());
        btnSaveInstructions.setOnClickListener(v -> uploadAllImagesThenSave());
    }

    private void addNewInstruction() {
        instructionList.add(new InstructionModel(instructionList.size() + 1, "", "", ""));
        instructionAdapter.notifyItemInserted(instructionList.size() - 1);
    }

    private void selectImageForInstruction(int position) {
        selectedInstructionPosition = position;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            openImagePicker(); // Add this line to actually open the picker
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                showToast("Storage permission is required to select an image.");
            }
        }
    }


    private void openImagePicker() {
        Log.d("AddStepsActivity", "Opening image picker");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_REQUEST_CODE && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            String imagePath = getImagePathFromUri(selectedImageUri);

            if (imagePath != null) {
                instructionList.get(selectedInstructionPosition).setImagePath(imagePath);
                instructionAdapter.notifyItemChanged(selectedInstructionPosition);
            } else {
                showToast("Failed to retrieve image path");
            }
        } else {
            showToast("No image selected");
        }
    }

    private String getImagePathFromUri(Uri uri) {
        if (uri == null) return null;

        // Use ContentResolver to handle the Uri
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Log.e("ImagePicker", "Failed to open InputStream for Uri: " + uri.toString());
                return null;
            }

            // Create a temporary file to store the content
            File tempFile = File.createTempFile("upload_", ".jpg", getCacheDir());
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("ImagePicker", "Error reading Uri: " + e.getMessage());
            return null;
        }
    }



    private void uploadAllImagesThenSave() {
        if (instructionList.isEmpty()) {
            showToast("Please add at least one Step");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        pendingUploads = 0;

        for (InstructionModel instruction : instructionList) {
            if (!instruction.getImagePath().isEmpty() && instruction.getImageUrl().isEmpty()) {
                pendingUploads++;
                uploadImageForInstruction(instruction);
            }
        }

        if (pendingUploads == 0) saveInstructionsToSupabase();
    }

    private void uploadImageForInstruction(InstructionModel instruction) {
        String imagePath = instruction.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) {
            showToast("Image path is empty for step " + instruction.getInstructionNumber());
            if (--pendingUploads == 0) saveInstructionsToSupabase();
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            showToast("File does not exist: " + imagePath);
            if (--pendingUploads == 0) saveInstructionsToSupabase();
            return;
        }

        MediaManager.get().upload(file.getAbsolutePath())
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        instruction.setImageUrl((String) resultData.get("secure_url"));
                        if (--pendingUploads == 0) saveInstructionsToSupabase();
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        showToast("Image upload failed for step " + instruction.getInstructionNumber() + ": " + error.getDescription());
                        if (--pendingUploads == 0) saveInstructionsToSupabase();
                    }

                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                }).dispatch();
    }


    private void saveInstructionsToSupabase() {
        // Remove duplicates and validate instructions
        ArrayList<InstructionModel> validInstructions = new ArrayList<>();
        for (InstructionModel instruction : instructionList) {
            if (!instruction.getDescription().isEmpty() || !instruction.getImageUrl().isEmpty()) {
                validInstructions.add(instruction);
            }
        }

        if (validInstructions.isEmpty()) {
            showToast("No valid instructions to save.");
            return;
        }

        JSONArray instructionsJsonArray = new JSONArray();
        for (InstructionModel instruction : validInstructions) {
            try {
                instructionsJsonArray.put(new JSONObject()
                        .put("video_id", videoId)
                        .put("step_number", instruction.getInstructionNumber())
                        .put("description", instruction.getDescription())
                        .put("image_url", instruction.getImageUrl()));
            } catch (JSONException e) {
                Log.e("Supabase", "JSON Error: " + e.getMessage());
            }
        }

        // Send data to Supabase
        Request request = new Request.Builder()
                .url("https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/steps")
                .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                .addHeader("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), instructionsJsonArray.toString()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to save instructions.");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        showToast("Instructions saved successfully.");
                        finish();
                    } else {
                        showToast("Failed to save instructions.");
                    }
                });
            }
        });
    }


    private File createTempFileFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(File.createTempFile("upload", ".jpg", getCacheDir()))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return new File(getCacheDir(), "upload.jpg");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
