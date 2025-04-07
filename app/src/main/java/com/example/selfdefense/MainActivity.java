package com.example.selfdefense;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find Buttons
        ImageView btnLearn = findViewById(R.id.btn_learn);
        ImageView btnSOS = findViewById(R.id.btn_sos);
        ImageView btnTips = findViewById(R.id.btn_tips);
        ImageButton btnLive = findViewById(R.id.btn_live);
        ImageButton btnProfile = findViewById(R.id.btn_profile);
        ImageButton add = findViewById(R.id.menu);

        // Handle deep links
        Intent intent2 = getIntent();
        if (Intent.ACTION_VIEW.equals(intent2.getAction()) && intent2.getData() != null) {
            Uri deepLink = intent2.getData();

            // Extract the fragment part (everything after #)
            String fragment = deepLink.getFragment();
            if (fragment != null) {
                // Parse the fragment parameters manually
                String[] params = fragment.split("&");
                String token = null;
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("access_token")) {
                        token = keyValue[1];
                        break;
                    }
                }

                Log.d("onCreate: ", token != null ? token : "Token not found");
                if (token != null) {
                    // Redirect to PasswordResetActivity with the token
                    Intent resetIntent = new Intent(this, PasswordResetActivity.class);
                    resetIntent.putExtra("token", token);
                    startActivity(resetIntent);
                    finish();
                    return;
                }
            }

            // Handle invalid deep link
            Toast.makeText(this, "Invalid deep link", Toast.LENGTH_SHORT).show();
        }

        // Manage role-based UI
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", null);
        String name=sharedPreferences.getString("username","");
        if(name.equals("")){
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
            finish();
        }
        if (role != null && !"user".equals(role)) {
            runOnUiThread(() -> add.setVisibility(View.VISIBLE));
        }

        add.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_items, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_add_video) {
                    startActivity(new Intent(MainActivity.this, VideoStepsActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_add_tips) {
                    startActivity(new Intent(MainActivity.this, AddTipsActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_add_live) {
                    startActivity(new Intent(MainActivity.this, AddLiveSessionActivity.class));
                    return true;
                } else {
                    Toast.makeText(this, "Unhandled menu item clicked", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            popupMenu.show();
        });

        // Set up listeners for navigation
        btnLearn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LearnActivity.class)));
        btnSOS.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EmergencyActivity.class)));
        btnTips.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SafetyTipsActivity.class)));

        btnLive.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LiveSessionsActivity.class)));
        btnProfile.setOnClickListener(v -> {
            String username = sharedPreferences.getString("username", null);
            if (username == null) {
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, MeActivity.class));
            }
        });
    }
}
