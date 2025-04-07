package com.example.selfdefense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private final Context context;
    private final List<Feedback> feedbackList;

    public FeedbackAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);

        // Bind data to views
        holder.usernameTextView.setText(feedback.getUsername());
        holder.dateTextView.setText(feedback.getCreatedAt());
        holder.feedbackTextView.setText(feedback.getFeedbackText());
        holder.ratingBar.setRating(feedback.getRating());

        // Load avatar using Glide
        Glide.with(context)
                .load(feedback.getAvatarUrl()).circleCrop()
                .placeholder(R.drawable.ic_avatar_placeholder) // Add a placeholder drawable in your resources
                .into(holder.avatarImageView);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatarImageView;
        private final TextView usernameTextView;
        private final TextView dateTextView;
        private final TextView feedbackTextView;
        private final RatingBar ratingBar;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatar_image);
            usernameTextView = itemView.findViewById(R.id.username_text);
            dateTextView = itemView.findViewById(R.id.timestamp_text);
            feedbackTextView = itemView.findViewById(R.id.feedback_text);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
