package com.example.selfdefense;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Calendar;

public class AddLiveSessionActivity extends AppCompatActivity {

    private EditText etTitle, etHostName, etDescription, etJoinLink, etAddress;
    private Button btnDateTime, btnExpiresAt, btnSubmit;
    private RadioGroup radioGroupSessionType;
    private EditText layoutJoinLink, layoutAddress;
    private String selectedDateTime, expiryDateTime;
    private TextView txt1,txt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_live_session);

        // Initialize Views
        etTitle = findViewById(R.id.et_title);
        etHostName = findViewById(R.id.et_host_name);
        etDescription = findViewById(R.id.et_description);
        btnDateTime = findViewById(R.id.btn_date_time);
        btnExpiresAt = findViewById(R.id.btn_expires_at);
        etJoinLink = findViewById(R.id.et_join_link);
        etAddress = findViewById(R.id.et_address);
        radioGroupSessionType = findViewById(R.id.radio_group_session_type);
        btnSubmit = findViewById(R.id.btn_submit);
        layoutJoinLink = findViewById(R.id.et_join_link);
        layoutAddress = findViewById(R.id.et_address);
        txt1=findViewById(R.id.txt1);
        txt2=findViewById(R.id.txt2);

        // Handle Session Type Change
        radioGroupSessionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_online) {
                layoutJoinLink.setVisibility(View.VISIBLE);
                txt1.setVisibility(View.VISIBLE);
                layoutAddress.setVisibility(View.GONE);
                txt2.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_offline) {
                layoutJoinLink.setVisibility(View.GONE);
                txt1.setVisibility(View.GONE);
                layoutAddress.setVisibility(View.VISIBLE);
                txt2.setVisibility(View.VISIBLE);
            }
        });

        // Date & Time Pickers
        btnDateTime.setOnClickListener(v -> showDateTimePicker(true));
        btnExpiresAt.setOnClickListener(v -> showDateTimePicker(false));

        // Submit Button Click
        btnSubmit.setOnClickListener(v -> submitLiveSession());
    }

    private void showDateTimePicker(boolean isSessionTime) {
        Calendar calendar = Calendar.getInstance(); // Current date and time
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                // Format the selected date and time
                                String formattedDateTime = String.format("%04d-%02d-%02d %02d:%02d:00",
                                        year, month + 1, dayOfMonth, hourOfDay, minute);

                                if (isSessionTime) {
                                    // Set the session date and time
                                    selectedDateTime = formattedDateTime;
                                    btnDateTime.setText(formattedDateTime);
                                } else {
                                    // Validate expiry date against session date
                                    if (selectedDateTime != null && !selectedDateTime.isEmpty()) {
                                        try {
                                            Calendar sessionCalendar = Calendar.getInstance();
                                            sessionCalendar.setTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(selectedDateTime));
                                            if (calendar.before(sessionCalendar)) {
                                                Toast.makeText(this, "Expiry date must be after the session date", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    // Set the expiry date and time
                                    expiryDateTime = formattedDateTime;
                                    btnExpiresAt.setText(formattedDateTime);
                                }
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Set the minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Prevent selecting past dates

        datePickerDialog.show();
    }


    private void submitLiveSession() {

        String title = etTitle.getText().toString().trim();
        String hostName = etHostName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Check if a session type is selected
        int selectedSessionTypeId = radioGroupSessionType.getCheckedRadioButtonId();
        if (selectedSessionTypeId == -1) {
            Toast.makeText(this, "Please select a session type", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRadioButton = findViewById(selectedSessionTypeId);
        String sessionType = selectedRadioButton.getText().toString();

        String joinLink = etJoinLink.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate Title
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        // Validate Host Name
        if (hostName.isEmpty()) {
            etHostName.setError("Host name is required");
            etHostName.requestFocus();
            return;
        }

        // Validate Description
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        // Validate Date and Time
        if (selectedDateTime == null || selectedDateTime.isEmpty()) {
            Toast.makeText(this, "Please select the session date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Expiry Date and Time
        if (expiryDateTime == null || expiryDateTime.isEmpty()) {
            Toast.makeText(this, "Please select the expiry date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Join Link for Online Sessions
        if (sessionType.equals("Online") && joinLink.isEmpty()) {
            etJoinLink.setError("Join link is required for online sessions");
            etJoinLink.requestFocus();
            return;
        }

        // Validate Address for Offline Sessions
        if (sessionType.equals("Offline") && address.isEmpty()) {
            etAddress.setError("Address is required for offline sessions");
            etAddress.requestFocus();
            return;
        }



        try {
            // Create JSON Object for POST Request
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("host_name", hostName);
            jsonObject.put("description", description);
            jsonObject.put("date_time", selectedDateTime);
            jsonObject.put("expires_at", expiryDateTime);
            jsonObject.put("session_type", sessionType);
            jsonObject.put("join_link", sessionType.equals("Online") ? joinLink : null);
            jsonObject.put("address", sessionType.equals("Offline") ? address : null);

            // Send POST Request using OkHttp
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            Request request = new Request.Builder()
                    .url("https://murzkwfoygbpejaffnpn.supabase.co/rest/v1/live_sessions")
                    .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                    .addHeader("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im11cnprd2ZveWdicGVqYWZmbnBuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQwMjA2MzYsImV4cCI6MjA0OTU5NjYzNn0.H0HmrFTfY9wPNuXVZGd7CQhuBdGgjSk5Zr5QtLAj_XA")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(AddLiveSessionActivity.this, "Failed to submit session", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(AddLiveSessionActivity.this, "Session added successfully", Toast.LENGTH_SHORT).show());
                        finish();
                    } else {
                        runOnUiThread(() -> Toast.makeText(AddLiveSessionActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
