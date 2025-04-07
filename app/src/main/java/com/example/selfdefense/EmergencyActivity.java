package com.example.selfdefense;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class EmergencyActivity extends AppCompatActivity {
    Button btnSafetyGuidelines;
    Button call;
    private FusedLocationProviderClient fusedLocationClient;
    String emergencyContact;
    private static final int REQUEST_CALL_PHONE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        btnSafetyGuidelines = findViewById(R.id.btn_safety_guidelines);
        btnSafetyGuidelines.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EmergencyActivity.this);
            builder.setTitle("Safety Guidelines")
                    .setMessage("1. Be aware of your surroundings.\n"
                            + "2. Trust your instincts.\n"
                            + "3. Keep your phone charged.\n"
                            + "4. Avoid poorly lit areas.\n"
                            + "5. Have emergency contacts ready.\n"
                            + "6. Carry a self-defense tool if possible.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        call= findViewById(R.id.btn_emergency_sos);
        call.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            if (sharedPreferences.contains("username")){
                emergencyContact = sharedPreferences.getString("emergencyContact", "");
                makePhoneCall(emergencyContact);
            }else{
                Toast.makeText(this, "You did not registered emergency contact", Toast.LENGTH_SHORT).show();
            }

        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button shareLocation = findViewById(R.id.btn_share_location);
        shareLocation.setOnClickListener(v -> shareLiveLocation());

    }

    private void shareLiveLocation() {
        // Check permissions
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                String locationUrl = "https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                shareLocationMessage(locationUrl);
            } else {
                Toast.makeText(EmergencyActivity.this, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareLocationMessage(String locationUrl) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String message = "This is my live location. Please help: " + locationUrl;
        intent.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(intent, "Share Live Location"));
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 100) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                shareLiveLocation();
//            } else {
//                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        } else {
            // Permission already granted, make the call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 100:
                    // Handle location permission
                    shareLiveLocation();
                    break;

                case REQUEST_CALL_PHONE:
                    // Handle call phone permission
                    makePhoneCall(emergencyContact); // Replace with the actual phone number
                    break;

                default:
                    // Handle other permissions if needed
                    break;
            }
        } else {
            switch (requestCode) {
                case 100:
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
                    break;

                case REQUEST_CALL_PHONE:
                    Toast.makeText(this, "Permission denied to make calls", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    // Handle other denied permissions if needed
                    break;
            }
        }
    }




}
