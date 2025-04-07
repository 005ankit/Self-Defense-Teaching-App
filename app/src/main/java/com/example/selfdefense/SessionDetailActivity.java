package com.example.selfdefense;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SessionDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvHost, tvDateTime, tvDescription;
    private Button btnJoinOnline, btnNavigateOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        // Initialize views
        tvTitle = findViewById(R.id.tv_detail_title);
        tvHost = findViewById(R.id.tv_detail_host);
        tvDateTime = findViewById(R.id.tv_detail_datetime);
        tvDescription = findViewById(R.id.tv_detail_description);
        btnJoinOnline = findViewById(R.id.btn_join_online);
        btnNavigateOffline = findViewById(R.id.btn_navigate_offline);

        // Get session data from the intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String hostName = intent.getStringExtra("hostName");
        String dateTime = intent.getStringExtra("dateTime");
        String description = intent.getStringExtra("description");
        String sessionType = intent.getStringExtra("sessionType");
        String joinLink = intent.getStringExtra("joinLink");
        String address = intent.getStringExtra("address");

        // Set data to the views
        tvTitle.setText(title);
        tvHost.setText("Hosted by: " + hostName);
        tvDateTime.setText("Date: " + dateTime);
        tvDescription.setText("Description: " + description);

        // Show/hide buttons based on session type
        if ("online".equalsIgnoreCase(sessionType)) {
            btnJoinOnline.setVisibility(View.VISIBLE);
            btnNavigateOffline.setVisibility(View.GONE);

            // Set button click listener for online sessions
            btnJoinOnline.setOnClickListener(v -> {
                if (joinLink != null && !joinLink.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(joinLink));
                    startActivity(browserIntent);
                }
            });
        } else if ("offline".equalsIgnoreCase(sessionType)) {
            btnJoinOnline.setVisibility(View.GONE);
            btnNavigateOffline.setVisibility(View.VISIBLE);

            // Set button click listener for offline sessions
            btnNavigateOffline.setOnClickListener(v -> {
                    // Open Google Maps with the provided latitude and longitude
                    String geoUri = "geo:0,0?q=" + Uri.encode(address);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                    mapIntent.setPackage("com.google.android.apps.maps"); // Open specifically in Google Maps
                    startActivity(mapIntent);
                
            });
        }
    }
}
