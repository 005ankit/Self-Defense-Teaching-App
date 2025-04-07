package com.example.selfdefense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProcessItemAdapter extends RecyclerView.Adapter<ProcessItemAdapter.ProcessViewHolder> {
    private Context context;
    private List<ProcessItem> processList;
    private OnProcessInteractionListener listener;

    public interface OnProcessInteractionListener {
        void onImageUploadClick(int position);
        void onDescriptionChange(int position, String newDescription);
    }

    public ProcessItemAdapter(Context context, List<ProcessItem> processList, OnProcessInteractionListener listener) {
        this.context = context;
        this.processList = processList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProcessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.process_item_layout, parent, false);
        return new ProcessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProcessViewHolder holder, int position) {
        ProcessItem processItem = processList.get(position);

        holder.etDescription.setText(processItem.getDescription());

        if (processItem.getImageUrl() != null && !processItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(processItem.getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.ivPreview);
            holder.ivPreview.setVisibility(View.VISIBLE);
        } else {
            holder.ivPreview.setVisibility(View.GONE);
        }

        holder.btnUploadImage.setOnClickListener(v -> listener.onImageUploadClick(position));
        holder.etDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                listener.onDescriptionChange(position, holder.etDescription.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return processList.size();
    }

    static class ProcessViewHolder extends RecyclerView.ViewHolder {
        EditText etDescription;
        Button btnUploadImage;
        ImageView ivPreview;

        public ProcessViewHolder(@NonNull View itemView) {
            super(itemView);
            etDescription = itemView.findViewById(R.id.et_process_description);
            btnUploadImage = itemView.findViewById(R.id.btn_process_upload_image);
            ivPreview = itemView.findViewById(R.id.iv_process_preview);
        }
    }
}
