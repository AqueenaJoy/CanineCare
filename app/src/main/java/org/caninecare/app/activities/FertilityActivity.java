package org.caninecare.app.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.caninecare.app.R;
import org.caninecare.app.api.ApiService;
import org.caninecare.app.api.RetrofitClient;
import org.caninecare.app.models.FertilityRequest;
import org.caninecare.app.models.FertilityResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FertilityActivity extends AppCompatActivity {

    private EditText etBreed, etAge, etWeight, etLastHeat;
    private Button btnPredict, btnViewHistory;
    private ProgressBar progressBar;
    private TextView tvResults;
    private Calendar lastHeatCalendar;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "CanineCarePrefs";
    private static final String KEY_DOG_NAME = "dog_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertility);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Fertility Prediction");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etBreed = findViewById(R.id.etBreed);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etLastHeat = findViewById(R.id.etLastHeat);
        btnPredict = findViewById(R.id.btnPredict);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        progressBar = findViewById(R.id.progressBar);
        tvResults = findViewById(R.id.tvResults);
        lastHeatCalendar = Calendar.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Make last heat field clickable for date picker
        etLastHeat.setFocusable(false);
        etLastHeat.setClickable(true);
    }

    private void setupListeners() {
        btnPredict.setOnClickListener(v -> predictFertility());
        btnViewHistory.setOnClickListener(v -> viewHeatHistory());
        etLastHeat.setOnClickListener(v -> showDatePicker());
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                lastHeatCalendar.set(selectedYear, selectedMonth, selectedDay);
                
                // Calculate days since last heat
                long diffInMillis = System.currentTimeMillis() - lastHeatCalendar.getTimeInMillis();
                long daysSince = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                
                // Format and display the date
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateStr = sdf.format(lastHeatCalendar.getTime());
                etLastHeat.setText(dateStr + " (" + daysSince + " days ago)");
            },
            year, month, day
        );
        
        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void viewHeatHistory() {
        String dogName = prefs.getString(KEY_DOG_NAME, "");
        if (dogName.isEmpty()) {
            Toast.makeText(this, "Please set up dog profile first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to heat history activity
        android.content.Intent intent = new android.content.Intent(this, HeatHistoryActivity.class);
        intent.putExtra("dog_name", dogName);
        startActivity(intent);
    }

    private void predictFertility() {
        String breed = etBreed.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String lastHeatStr = etLastHeat.getText().toString().trim();

        if (breed.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        float weight = Float.parseFloat(weightStr);
        
        // Calculate days since last heat from calendar
        Integer lastHeat = null;
        if (!lastHeatStr.isEmpty() && lastHeatCalendar != null) {
            long diffInMillis = System.currentTimeMillis() - lastHeatCalendar.getTimeInMillis();
            lastHeat = (int) TimeUnit.MILLISECONDS.toDays(diffInMillis);
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPredict.setEnabled(false);

        // Get dog name from preferences or use default
        String dogName = prefs.getString(KEY_DOG_NAME, "Max");
        FertilityRequest request = new FertilityRequest(dogName, breed, age, weight, lastHeat);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<FertilityResponse> call = apiService.predictFertility(request);

        call.enqueue(new Callback<FertilityResponse>() {
            @Override
            public void onResponse(Call<FertilityResponse> call, Response<FertilityResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnPredict.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    displayResults(response.body());
                } else {
                    showError("Prediction failed");
                }
            }

            @Override
            public void onFailure(Call<FertilityResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPredict.setEnabled(true);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void displayResults(FertilityResponse response) {
        StringBuilder results = new StringBuilder();
        results.append("🔬 Prediction Results\n\n");
        results.append("Prediction Type: ").append(response.getPredictionType()).append("\n\n");
        results.append("Predicted: ").append(response.getPredictionValue())
                .append(" ").append(response.getPredictionUnit()).append("\n\n");
        results.append("Estimated Date: ").append(response.getEstimatedDate()).append("\n\n");
        results.append("Fertility Status: ").append(response.getFertilityStatus()).append("\n\n");
        results.append("Alert Level: ").append(response.getAlertLevel().toUpperCase());

        tvResults.setText(results.toString());
        tvResults.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvResults.setText("Error: " + message);
        tvResults.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
