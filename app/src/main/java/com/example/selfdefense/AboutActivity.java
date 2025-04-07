package com.example.selfdefense;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AboutActivity extends AppCompatActivity {

    private ImageView back,facebookIcon,twitterIcon,instagramIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        facebookIcon = findViewById(R.id.social_facebook);
        twitterIcon = findViewById(R.id.social_twitter);
        instagramIcon = findViewById(R.id.social_instagram);
        back=findViewById(R.id.back_arrow);
        back.setOnClickListener(view -> {
            finish();
        });
        facebookIcon.setOnClickListener(view -> openSocialMedia("https://www.facebook.com/YourPageName"));
        twitterIcon.setOnClickListener(view -> openSocialMedia("https://www.twitter.com/YourPageName"));
        instagramIcon.setOnClickListener(view -> openSocialMedia("https://www.instagram.com/YourPageName"));

    }
    private void openSocialMedia(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}