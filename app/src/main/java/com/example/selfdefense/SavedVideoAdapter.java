package com.example.selfdefense;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SavedVideoAdapter extends RecyclerView.Adapter<SavedVideoAdapter.ViewHolder> {

    private List<SavedVideo> savedVideos;
    private OnPlayClickListener onPlayClickListener;
    private OnRemoveClickListener onRemoveClickListener;

    public interface OnPlayClickListener {
        void onPlayClicked(SavedVideo savedVideo);
    }

    public interface OnRemoveClickListener {
        void onRemoveClicked(SavedVideo savedVideo);
    }

    public SavedVideoAdapter(List<SavedVideo> savedVideos, OnPlayClickListener playListener, OnRemoveClickListener removeListener) {
        this.savedVideos = savedVideos;
        this.onPlayClickListener = playListener;
        this.onRemoveClickListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_videos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedVideo savedVideo = savedVideos.get(position);
        holder.bind(savedVideo);
    }

    @Override
    public int getItemCount() {
        return savedVideos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView videoThumbnail;
        private TextView videoTitle;
        private TextView videoDescription;
        private ImageButton btnPlayVideo;
        private ImageButton btnRemoveVideo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            videoThumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoTitle = itemView.findViewById(R.id.video_title);
            videoDescription = itemView.findViewById(R.id.video_description);
            btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
            btnRemoveVideo = itemView.findViewById(R.id.btn_remove_video);
        }

        void bind(SavedVideo savedVideo) {
            // Set video data to views
            videoTitle.setText(savedVideo.getTitle());
            videoDescription.setText(savedVideo.getDescription());

            // Load thumbnail using Glide
            Glide.with(videoThumbnail.getContext())
                    .load(savedVideo.getThumbnail())
                    .placeholder(R.drawable.ic_video_placeholder)
                    .into(videoThumbnail);

            // Set button actions
            btnPlayVideo.setOnClickListener(v -> onPlayClickListener.onPlayClicked(savedVideo));
            btnRemoveVideo.setOnClickListener(v -> onRemoveClickListener.onRemoveClicked(savedVideo));
        }
    }
}
