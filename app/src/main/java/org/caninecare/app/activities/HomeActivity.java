package org.caninecare.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.caninecare.app.R;
import org.caninecare.app.api.ApiService;
import org.caninecare.app.api.RetrofitClient;
import org.caninecare.app.models.SensorDataResponse;
import org.caninecare.app.utils.ApiConfig;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Home Activity - Real-time monitoring dashboard
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvTemperature, tvActivity, tvLocation, tvStatus;
    private SwipeRefreshLayout swipeRefresh;
    private Handler handler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Home Dashboard");
        }

        initViews();
        setupSwipeRefresh();
        startAutoRefresh();
        loadSensorData();
    }

    private void initViews() {
        tvTemperature = findViewById(R.id.tvTemperature);
        tvActivity = findViewById(R.id.tvActivity);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        swipeRefresh = findViewById(R.id.swipeRefresh);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadSensorData();
        });
    }

    private void startAutoRefresh() {
        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadSensorData();
                handler.postDelayed(this, ApiConfig.HOME_REFRESH_INTERVAL);
            }
        };
        handler.post(refreshRunnable);
    }

    private void loadSensorData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<SensorDataResponse> call = apiService.getSensorData(1);

        call.enqueue(new Callback<SensorDataResponse>() {
            @Override
            public void onResponse(Call<SensorDataResponse> call, Response<SensorDataResponse> response) {
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    SensorDataResponse data = response.body();
                    if (data.getData() != null && !data.getData().isEmpty()) {
                        updateUI(data.getData().get(0));
                    } else {
                        showNoDataMessage();
                    }
                } else {
                    showError("Failed to load data");
                }
            }

            @Override
            public void onFailure(Call<SensorDataResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void updateUI(SensorDataResponse.SensorData data) {
        // Update temperature
        float temp = data.getTemperature();
        tvTemperature.setText(String.format("%.1f°C", temp));
        
        // Color code temperature
        if (temp >= ApiConfig.TEMP_FEVER) {
            tvTemperature.setTextColor(getResources().getColor(R.color.tempFever));
        } else if (temp <= ApiConfig.TEMP_HYPOTHERMIA) {
            tvTemperature.setTextColor(getResources().getColor(R.color.tempHypothermia));
        } else {
            tvTemperature.setTextColor(getResources().getColor(R.color.tempNormal));
        }

        // Update activity
        int activity = data.getActivityPercent();
        tvActivity.setText(activity + "%");

        // Update location
        double lat = data.getLatitude();
        double lon = data.getLongitude();
        if (lat != 0.0 && lon != 0.0) {
            tvLocation.setText(String.format("GPS: %.4f, %.4f", lat, lon));
        } else {
            tvLocation.setText("GPS: Waiting for signal...");
        }

        // Update status
        tvStatus.setText("Status: Monitoring...");
        tvStatus.setTextColor(getResources().getColor(R.color.healthNormal));
    }

    private void showNoDataMessage() {
        tvTemperature.setText("--°C");
        tvActivity.setText("--%");
        tvLocation.setText("No data");
        tvStatus.setText("No sensor data available");
        tvStatus.setTextColor(getResources().getColor(R.color.healthWarning));
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvStatus.setText("Error: " + message);
        tvStatus.setTextColor(getResources().getColor(R.color.healthDanger));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
