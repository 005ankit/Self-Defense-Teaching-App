package com.example.selfdefense;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepViewHolder> {

    private List<Step> steps;
    private Context context;

    // Constructor
    public StepsAdapter(List<Step> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = steps.get(position);

        // Set step description
        holder.stepText.setText("Step " + step.getStepNumber() + ": " + step.getDescription());

        Log.d("StepsAdapter", "Image URL: " + step.getImageUrl());

        // Load step image using Glide
        Glide.with(context)
                .load(step.getImageUrl())
                .placeholder(R.drawable.logo) // Replace with your placeholder image
                .error(R.drawable.img)             // Replace with your error image
                .into(holder.stepImage);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    // ViewHolder class
    static class StepViewHolder extends RecyclerView.ViewHolder {
        ImageView stepImage;
        TextView stepText;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepImage = itemView.findViewById(R.id.step_image);
            stepText = itemView.findViewById(R.id.step_text);
        }
    }
}
