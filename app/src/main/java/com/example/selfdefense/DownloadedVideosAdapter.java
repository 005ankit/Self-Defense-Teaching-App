package com.example.selfdefense;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class DownloadedVideosAdapter extends RecyclerView.Adapter<DownloadedVideosAdapter.ViewHolder> {

    private List<File> videoFiles;
    private Context context;

    public DownloadedVideosAdapter(List<File> videoFiles, Context context) {
        this.videoFiles = videoFiles;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_downloaded_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File videoFile = videoFiles.get(position);
        holder.tvVideoTitle.setText(videoFile.getName());

        // Play button click listener
        holder.play.setOnClickListener(v -> playVideo(videoFile));

        // Delete button click listener
        holder.delete.setOnClickListener(v -> confirmDelete(videoFile, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return videoFiles.size();
    }

    /**
     * Plays a video using an external video player.
     */
    private void playVideo(File videoFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the content URI using FileProvider
        Uri videoUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                videoFile
        );

        intent.setDataAndType(videoUri, "video/mp4");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant temporary read permission

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the video file after user confirmation.
     */
    private void confirmDelete(File videoFile, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Video")
                .setMessage("Are you sure you want to delete this video?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (videoFile.delete()) {
                        videoFiles.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Video deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete video", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvVideoTitle;
        ImageButton play, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVideoTitle = itemView.findViewById(R.id.video_file_name);
            play = itemView.findViewById(R.id.btn_play_video);
            delete = itemView.findViewById(R.id.btn_delete_video);
        }
    }
}
