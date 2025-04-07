package com.example.selfdefense;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TipAdapter adapter;
    private List<Tip> favoriteTips;
    private Button clearAllButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites); // Ensure this XML exists and is properly configured

        // Initialize SharedPreferences (ensure file name matches SafetyTipsActivity)
        sharedPreferences = getSharedPreferences("SafetyTipsPrefs", Context.MODE_PRIVATE);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.favorites_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteTips = new ArrayList<>();

        // Set Adapter
        adapter = new TipAdapter(this, favoriteTips);
        recyclerView.setAdapter(adapter);

        // Load data from SharedPreferences
        loadFavorites();



        // Initialize Clear All Button
        clearAllButton = findViewById(R.id.btn_clear_all);
        clearAllButton.setOnClickListener(v -> clearAllFavorites());
    }

    // Method to load data from SharedPreferences
    private void loadFavorites() {
        favoriteTips.clear(); // Clear existing list to avoid duplicates

        String favoritesJson = sharedPreferences.getString("favoritesList", "[]"); // Default to empty JSON array
        Log.d("FavoritesActivity", "Loaded Favorites JSON: " + favoritesJson);

        try {
            JSONArray favoritesArray = new JSONArray(favoritesJson);

            for (int i = 0; i < favoritesArray.length(); i++) {
                JSONObject favoriteObject = favoritesArray.getJSONObject(i);
                String title = favoriteObject.getString("subtitle");
                String details = favoriteObject.getString("details");

                // Add the tip to the list
                if(!title.isEmpty() && !details.isEmpty()){
                    favoriteTips.add(new Tip(title, details));
                }

            }

            // Notify adapter about the changes
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e("FavoritesActivity", "Error parsing favorites JSON", e);
            Toast.makeText(this, "Failed to load favorites!", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to clear all favorites
    private void clearAllFavorites() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("favoritesList", "[]"); // Save an empty JSON array
        editor.apply();

        // Clear the local list
        favoriteTips.clear();

        // Notify the adapter to update RecyclerView
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "All favorites cleared!", Toast.LENGTH_SHORT).show();
    }
}
