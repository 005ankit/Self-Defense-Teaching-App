package com.example.selfdefense;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.InstructionViewHolder> {

    private final Context context;
    private final ArrayList<InstructionModel> instructions;
    private final OnImageUploadListener imageUploadListener;

    public interface OnImageUploadListener {
        void onImageUpload(int position);
    }

    public InstructionAdapter(Context context, ArrayList<InstructionModel> instructions, OnImageUploadListener imageUploadListener) {
        this.context = context;
        this.instructions = instructions;
        this.imageUploadListener = imageUploadListener;
    }

    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.instruction_card, parent, false);
        return new InstructionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {
        InstructionModel instruction = instructions.get(position);

        // Set instruction number
        holder.tvInstructionNumber.setText(String.format("Step %d", instruction.getInstructionNumber()));

        // Remove any existing TextWatcher to avoid duplicate listeners
        if (holder.textWatcher != null) {
            holder.etInstructionDescription.removeTextChangedListener(holder.textWatcher);
        }

        // Set description
        holder.etInstructionDescription.setText(instruction.getDescription());

        // Add a new TextWatcher for this EditText
        holder.textWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                instruction.setDescription(s.toString()); // Update the corresponding model
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };
        holder.etInstructionDescription.addTextChangedListener(holder.textWatcher);

        // Load image if URL or local path is available
        if (instruction.getImagePath() != null && !instruction.getImagePath().isEmpty()) {
            // Load from local path
            holder.ivImagePreview.setVisibility(View.VISIBLE);
            String imagePath = instruction.getImagePath();
            if (!imagePath.startsWith("content://") && !imagePath.startsWith("file://")) {
                imagePath = "file://" + imagePath; // Ensure proper prefix for file path
            }
            Glide.with(context)
                    .load(imagePath)
                    .placeholder(R.drawable.logo)    // Placeholder for local images
                    .error(R.drawable.logo)   // Fallback if path fails
                    .into(holder.ivImagePreview);
        } else {
            // Hide the ImageView if no image available
            holder.ivImagePreview.setVisibility(View.GONE);
        }


        // Button to upload an image
        holder.btnSelectImage.setOnClickListener(v -> {
            if (imageUploadListener != null) {
                imageUploadListener.onImageUpload(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }
    public static class InstructionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInstructionNumber;
        private final EditText etInstructionDescription;
        private final Button btnSelectImage;
        private final ImageView ivImagePreview;
        public android.text.TextWatcher textWatcher; // Store the TextWatcher for this ViewHolder

        public InstructionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstructionNumber = itemView.findViewById(R.id.tv_instruction_number);
            etInstructionDescription = itemView.findViewById(R.id.et_instruction_description);
            btnSelectImage = itemView.findViewById(R.id.btn_select_image);
            ivImagePreview = itemView.findViewById(R.id.iv_image_preview);
        }
    }
}
