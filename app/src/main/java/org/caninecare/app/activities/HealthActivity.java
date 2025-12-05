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
import org.caninecare.app.models.HealthCheckRequest;
import org.caninecare.app.models.HealthCheckResponse;
import org.caninecare.app.models.SensorDataResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthActivity extends AppCompatActivity {

    private TextView tvCurrentTemp, tvCurrentActivity, tvHealthStatus, tvSeverity, tvRecommendations;
    private Button btnFetchData, btnAnalyze;
    private ProgressBar progressBar;
    
    private float currentTemp = 38.5f;
    private int currentActivity = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Health Monitor");
        }

        initViews();
        setupListeners();
        fetchSensorData();
    }

    private void initViews() {
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvCurrentActivity = findViewById(R.id.tvCurrentActivity);
        tvHealthStatus = findViewById(R.id.tvHealthStatus);
        tvSeverity = findViewById(R.id.tvSeverity);
        tvRecommendations = findViewById(R.id.tvRecommendations);
        btnFetchData = findViewById(R.id.btnFetchData);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnFetchData.setOnClickListener(v -> fetchSensorData());
        btnAnalyze.setOnClickListener(v -> analyzeHealth());
    }

    private void fetchSensorData() {
        progressBar.setVisibility(View.VISIBLE);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<SensorDataResponse> call = apiService.getSensorData(1);

        call.enqueue(new Callback<SensorDataResponse>() {
            @Override
            public void onResponse(Call<SensorDataResponse> call, Response<SensorDataResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    SensorDataResponse data = response.body();
                    if (data.getData() != null && !data.getData().isEmpty()) {
                        SensorDataResponse.SensorData sensorData = data.getData().get(0);
                        currentTemp = sensorData.getTemperature();
                        currentActivity = sensorData.getActivityPercent();
                        
                        tvCurrentTemp.setText(String.format("%.1f°C", currentTemp));
                        tvCurrentActivity.setText(currentActivity + "%");
                        
                        Toast.makeText(HealthActivity.this, "Data refreshed", Toast.LENGTH_SHORT).show();
                    } else {
                        showError("No sensor data available");
                    }
                } else {
                    showError("Failed to fetch data");
                }
            }

            @Override
            public void onFailure(Call<SensorDataResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void analyzeHealth() {
        progressBar.setVisibility(View.VISIBLE);
        
        HealthCheckRequest request = new HealthCheckRequest("Max", currentTemp, currentActivity);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<HealthCheckResponse> call = apiService.checkHealth(request);

        call.enqueue(new Callback<HealthCheckResponse>() {
            @Override
            public void onResponse(Call<HealthCheckResponse> call, Response<HealthCheckResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    displayHealthResults(response.body());
                } else {
                    showError("Analysis failed");
                }
            }

            @Override
            public void onFailure(Call<HealthCheckResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void displayHealthResults(HealthCheckResponse response) {
        tvHealthStatus.setText("Status: " + response.getHealthStatus());
        tvSeverity.setText("Severity: " + response.getSeverity().toUpperCase());
        
        // Color code severity
        switch (response.getSeverity().toLowerCase()) {
            case "critical":
                tvSeverity.setTextColor(getResources().getColor(R.color.severityCritical));
                break;
            case "high":
                tvSeverity.setTextColor(getResources().getColor(R.color.severityHigh));
                break;
            case "medium":
                tvSeverity.setTextColor(getResources().getColor(R.color.severityMedium));
                break;
            default:
                tvSeverity.setTextColor(getResources().getColor(R.color.severityLow));
        }
        
        // Display recommendations
        StringBuilder recommendations = new StringBuilder("Recommendations:\n\n");
        if (response.getRecommendations() != null) {
            for (String rec : response.getRecommendations()) {
                recommendations.append("• ").append(rec).append("\n");
            }
        }
        tvRecommendations.setText(recommendations.toString());
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvHealthStatus.setText("Error: " + message);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
