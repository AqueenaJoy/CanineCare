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
import org.caninecare.app.models.SensorDataResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {

    private TextView tvLatitude, tvLongitude, tvStatus;
    private Button btnRefresh;
    private ProgressBar progressBar;
    private SensorDataResponse.SensorData currentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Location Tracking");
        }

        initViews();
        setupListeners();
        loadLocationData();
    }

    private void initViews() {
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvStatus = findViewById(R.id.tvStatus);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> loadLocationData());
    }

    private void loadLocationData() {
        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setEnabled(false);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<SensorDataResponse> call = apiService.getSensorData(1);

        call.enqueue(new Callback<SensorDataResponse>() {
            @Override
            public void onResponse(Call<SensorDataResponse> call, Response<SensorDataResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnRefresh.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    SensorDataResponse data = response.body();
                    if (data.getData() != null && !data.getData().isEmpty()) {
                        currentData = data.getData().get(0);
                        displayLocation();
                    } else {
                        showNoData();
                    }
                } else {
                    showError("Failed to load location");
                }
            }

            @Override
            public void onFailure(Call<SensorDataResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRefresh.setEnabled(true);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void displayLocation() {
        if (currentData != null) {
            double lat = currentData.getLatitude();
            double lon = currentData.getLongitude();
            
            if (lat != 0.0 && lon != 0.0) {
                tvLatitude.setText(String.format("Latitude: %.6f", lat));
                tvLongitude.setText(String.format("Longitude: %.6f", lon));
                tvStatus.setText("✅ GPS Active - Within Safe Zone");
                tvStatus.setTextColor(getResources().getColor(R.color.healthNormal));
            } else {
                tvLatitude.setText("Latitude: Waiting for GPS...");
                tvLongitude.setText("Longitude: Waiting for GPS...");
                tvStatus.setText("⚠️ GPS signal not available");
                tvStatus.setTextColor(getResources().getColor(R.color.healthWarning));
            }
        } else {
            showNoData();
        }
        Toast.makeText(this, "Location refreshed", Toast.LENGTH_SHORT).show();
    }

    private void showNoData() {
        tvLatitude.setText("Latitude: --");
        tvLongitude.setText("Longitude: --");
        tvStatus.setText("No GPS data available");
        tvStatus.setTextColor(getResources().getColor(R.color.healthWarning));
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvStatus.setText("Error: " + message);
        tvStatus.setTextColor(getResources().getColor(R.color.healthDanger));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
