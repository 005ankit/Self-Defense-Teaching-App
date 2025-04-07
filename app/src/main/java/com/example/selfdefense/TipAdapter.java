package com.example.selfdefense;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipViewHolder> {

    private Context context;
    private List<Tip> tipList;

    public TipAdapter(Context context, List<Tip> tipList) {
        this.context = context;
        this.tipList = tipList;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_card, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        Tip tip = tipList.get(position);

        // Bind data
        holder.tipTitle.setText(tip.getTitle());
        holder.tipDetails.setText(tip.getDetails());

        // Handle button actions
        holder.btnShare.setOnClickListener(v -> {
            // Create an implicit intent to share the tip's title and details
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            // Prepare the text to share
            String shareText = "Safety Tip: " + tip.getTitle() + "\n" + tip.getDetails();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            // Start the sharing activity
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Tip")
                    .setMessage("Are you sure you want to delete this tip?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        removeFromSharedPreferences(tip);
                        tipList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, tipList.size());
                        Toast.makeText(context, "Deleted: " + tip.getTitle(), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();

//            tipList.remove(position);
//            notifyItemRemoved(position);
//            notifyItemRangeChanged(position, tipList.size());
//            Toast.makeText(context, "Deleted: " + tip.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    // Method to remove the tip from SharedPreferences
    private void removeFromSharedPreferences(Tip tip) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SafetyTipsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Load existing favorites
        String favoritesJson = sharedPreferences.getString("favoritesList", "[]");
        try {
            JSONArray favoritesArray = new JSONArray(favoritesJson);
            for (int i = 0; i < favoritesArray.length(); i++) {
                JSONObject favoriteObject = favoritesArray.getJSONObject(i);
                String title = favoriteObject.getString("subtitle");

                // If title matches, remove the object
                if (title.equals(tip.getTitle())) {
                    favoritesArray.remove(i);
                    break;
                }
            }

            // Save updated list back to SharedPreferences
            editor.putString("favoritesList", favoritesArray.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    public static class TipViewHolder extends RecyclerView.ViewHolder {
        TextView tipTitle, tipDetails;
        ImageView btnShare, btnDelete;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tipTitle = itemView.findViewById(R.id.tip_title);
            tipDetails = itemView.findViewById(R.id.tip_details);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
