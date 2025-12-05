package org.caninecare.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.caninecare.app.R;
import org.caninecare.app.api.ApiService;
import org.caninecare.app.api.RetrofitClient;
import org.caninecare.app.models.AlertsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertsActivity extends AppCompatActivity {

    private TextView tvAlertsList, tvNoAlerts;
    private Button btnRefresh;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_alerts);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Alerts");
            }

            initViews();
            setupListeners();
            loadAlerts();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading alerts: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        tvAlertsList = findViewById(R.id.tvAlertsList);
        tvNoAlerts = findViewById(R.id.tvNoAlerts);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadAlerts());
    }

    private void loadAlerts() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            btnRefresh.setEnabled(false);
            
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<AlertsResponse> call = apiService.getAlerts(10);

            call.enqueue(new Callback<AlertsResponse>() {
                @Override
                public void onResponse(Call<AlertsResponse> call, Response<AlertsResponse> response) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        btnRefresh.setEnabled(true);
                        
                        if (response.isSuccessful() && response.body() != null) {
                            displayAlerts(response.body());
                        } else {
                            showError("Failed to load alerts (Code: " + response.code() + ")");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Error processing alerts: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<AlertsResponse> call, Throwable t) {
                    try {
                        progressBar.setVisibility(View.GONE);
                        btnRefresh.setEnabled(true);
                        showError("Connection error: " + t.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            btnRefresh.setEnabled(true);
            showError("Error loading alerts: " + e.getMessage());
        }
    }

    private void displayAlerts(AlertsResponse response) {
        if (response == null || response.getAlerts() == null || response.getAlerts().isEmpty()) {
            tvNoAlerts.setVisibility(View.VISIBLE);
            tvAlertsList.setVisibility(View.GONE);
            tvNoAlerts.setText("No alerts available");
            return;
        }

        tvNoAlerts.setVisibility(View.GONE);
        tvAlertsList.setVisibility(View.VISIBLE);

        StringBuilder alertsText = new StringBuilder();
        int count = 1;
        
        for (AlertsResponse.Alert alert : response.getAlerts()) {
            if (alert == null) continue;
            
            // Determine severity - use emergency_level if available, otherwise severity
            String severity = alert.getEmergencyLevel() != null ? 
                            alert.getEmergencyLevel().toLowerCase() : 
                            (alert.getSeverity() != null ? alert.getSeverity().toLowerCase() : "low");
            String emoji = getSeverityEmoji(severity);
            +m
            // Health Status or Emergency Type
            String title = alert.getHealthStatus() != null ? alert.getHealthStatus() : "Alert";
            alertsText.append(emoji).append(" ").append(count++).append(". ").append(title).append("\n");
            
            // Severity/Emergency Level
            String severityDisplay = severity.toUpperCase();
            alertsText.append("   Level: ").append(severityDisplay).append("\n");
            
            // Temperature & Activity (if available)
            if (alert.getTemperature() > 0) {
                alertsText.append("   Temperature: ").append(String.format("%.1f°C", alert.getTemperature())).append("\n");
            }
            if (alert.getActivityPercent() > 0) {
                alertsText.append("   Activity: ").append(String.format("%.0f%%", alert.getActivityPercent())).append("\n");
            }
            
            // Immobile Duration (if available)
            if (alert.getImmobileDuration() > 0) {
                int minutes = alert.getImmobileDuration() / 60;
                alertsText.append("   Immobile: ").append(minutes).append(" minutes\n");
            }
            
            // Emergency Details (structured emergencies)
            if (alert.getEmergencies() != null && !alert.getEmergencies().isEmpty()) {
                alertsText.append("   \n   🚨 EMERGENCIES:\n");
                for (AlertsResponse.Emergency emergency : alert.getEmergencies()) {
                    alertsText.append("   ━━━━━━━━━━━━━━━━\n");
                    alertsText.append("   Type: ").append(emergency.getType()).append("\n");
                    alertsText.append("   ⚠️ ").append(emergency.getMessage()).append("\n");
                    alertsText.append("   ➤ ").append(emergency.getAction()).append("\n");
                }
            }
            
            // Alert Messages (simple alerts)
            if (alert.getAlertMessages() != null && !alert.getAlertMessages().isEmpty()) {
                alertsText.append("   \n   Details:\n");
                for (String message : alert.getAlertMessages()) {
                    alertsText.append("   • ").append(message).append("\n");
                }
            }
            
            // Timestamp - format it nicely
            if (alert.getTimestamp() != null) {
                String formattedTime = formatTimestamp(alert.getTimestamp());
                alertsText.append("   Time: ").append(formattedTime).append("\n");
            }
            
            alertsText.append("\n");
        }

        tvAlertsList.setText(alertsText.toString());
        Toast.makeText(this, response.getCount() + " alerts loaded", Toast.LENGTH_SHORT).show();
    }
    
    private String getSeverityEmoji(String severity) {
        switch (severity.toLowerCase()) {
            case "critical":
                return "🚨";
            case "high":
                return "⚠️";
            case "medium":
                return "⚡";
            case "low":
            default:
                return "ℹ️";
        }
    }
    
    private String formatTimestamp(String timestamp) {
        try {
            // Backend sends ISO format: 2025-10-21T18:30:15.123456
            // Display as: 21 Oct 2025, 18:30
            if (timestamp.contains("T")) {
                String[] parts = timestamp.split("T");
                String date = parts[0];
                String time = parts[1].substring(0, 5); // Get HH:MM
                
                String[] dateParts = date.split("-");
                String year = dateParts[0];
                String month = getMonthName(dateParts[1]);
                String day = dateParts[2];
                
                return day + " " + month + " " + year + ", " + time;
            }
            return timestamp;
        } catch (Exception e) {
            return timestamp;
        }
    }
    
    private String getMonthName(String month) {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        try {
            int m = Integer.parseInt(month);
            if (m >= 1 && m <= 12) {
                return months[m];
            }
        } catch (Exception e) {
            // ignore
        }
        return month;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvNoAlerts.setText("Error: " + message);
        tvNoAlerts.setVisibility(View.VISIBLE);
        tvAlertsList.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
