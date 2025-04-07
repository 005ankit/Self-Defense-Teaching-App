package com.example.selfdefense;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadedVideosActivity extends AppCompatActivity {

    private ImageView back;
    private RecyclerView recyclerView;
    private TextView tvNoVideos;
    private DownloadedVideosAdapter adapter;
    private List<File> downloadedVideos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded_videos);

        recyclerView = findViewById(R.id.rv_downloaded_videos);
        tvNoVideos = findViewById(R.id.tv_no_videos);
        back = findViewById(R.id.iv_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        back.setOnClickListener(view -> {
            finish();
        });

        // Load downloaded videos
        downloadedVideos = getDownloadedVideos(this);

        if (downloadedVideos.isEmpty()) {
            tvNoVideos.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoVideos.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Set up the adapter with the downloaded videos
            adapter = new DownloadedVideosAdapter(downloadedVideos, this);
            recyclerView.setAdapter(adapter);
        }
    }


    /**
     * Retrieves all downloaded videos stored in the app's private directory.
     */
    private List<File> getDownloadedVideos(Context context) {
        List<File> videoFiles = new ArrayList<>();
        File directory = context.getFilesDir(); // App's private directory
        File videosDir = new File(directory, "DownloadedVideos");

        if (videosDir.exists() && videosDir.isDirectory()) {
            File[] files = videosDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".mp4")) {
                        videoFiles.add(file);
                    }
                }
            }
        } else {
            Log.e("DownloadedVideos", "Videos directory does not exist.");
        }

        return videoFiles;
    }

    /**
     * Plays a video using an external video player.
     */
    private void playVideo(File videoFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the content URI using FileProvider
        Uri videoUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                videoFile
        );

        intent.setDataAndType(videoUri, "video/mp4");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant temporary read permission

        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DownloadedVideosActivity", "Error playing video: " + e.getMessage());
        }
    }

}
