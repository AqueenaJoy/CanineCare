package org.caninecare.app.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.caninecare.app.R;
import org.caninecare.app.api.ApiService;
import org.caninecare.app.api.RetrofitClient;
import org.caninecare.app.models.DogProfile;
import org.caninecare.app.models.DogProfileResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DogProfileActivity extends AppCompatActivity {

    private EditText etDogName, etAge, etWeight;
    private Spinner spinnerBreed;
    private Button btnSaveProfile, btnLoadProfile;
    private ProgressBar progressBar;
    private TextView tvProfileStatus;
    
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "CanineCarePrefs";
    private static final String KEY_DOG_NAME = "dog_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Dog Profile");
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initViews();
        setupBreedSpinner();
        setupListeners();
        loadSavedProfile();
    }

    private void initViews() {
        etDogName = findViewById(R.id.etDogName);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        spinnerBreed = findViewById(R.id.spinnerBreed);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLoadProfile = findViewById(R.id.btnLoadProfile);
        progressBar = findViewById(R.id.progressBar);
        tvProfileStatus = findViewById(R.id.tvProfileStatus);
    }

    private void setupBreedSpinner() {
        // Load breeds from API
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BreedsResponse> call = apiService.getBreeds();
        
        call.enqueue(new Callback<BreedsResponse>() {
            @Override
            public void onResponse(Call<BreedsResponse> call, Response<BreedsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> breeds = response.body().getBreeds();
                    if (breeds != null && !breeds.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            DogProfileActivity.this,
                            android.R.layout.simple_spinner_item,
                            breeds
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerBreed.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<BreedsResponse> call, Throwable t) {
                // Use default breeds
                setupDefaultBreeds();
            }
        });
    }

    private void setupDefaultBreeds() {
        List<String> defaultBreeds = new ArrayList<>();
        defaultBreeds.add("Golden Retriever");
        defaultBreeds.add("Labrador Retriever");
        defaultBreeds.add("German Shepherd");
        defaultBreeds.add("Beagle");
        defaultBreeds.add("Poodle");
        defaultBreeds.add("Bulldog");
        defaultBreeds.add("Mixed Breed");
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            defaultBreeds
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnLoadProfile.setOnClickListener(v -> loadProfileFromServer());
    }

    private void saveProfile() {
        String name = etDogName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String breed = spinnerBreed.getSelectedItem().toString();

        if (name.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int ageMonths = Integer.parseInt(ageStr);
        float weight = Float.parseFloat(weightStr);

        progressBar.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        DogProfile profile = new DogProfile(name, breed, ageMonths, weight);
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<DogProfileResponse> call = apiService.createOrUpdateProfile(profile);

        call.enqueue(new Callback<DogProfileResponse>() {
            @Override
            public void onResponse(Call<DogProfileResponse> call, Response<DogProfileResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    // Save dog name locally
                    prefs.edit().putString(KEY_DOG_NAME, name).apply();
                    
                    tvProfileStatus.setText("✅ Profile saved successfully!");
                    tvProfileStatus.setVisibility(View.VISIBLE);
                    Toast.makeText(DogProfileActivity.this, "Profile saved!", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Failed to save profile");
                }
            }

            @Override
            public void onFailure(Call<DogProfileResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void loadProfileFromServer() {
        String name = etDogName.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter dog name", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLoadProfile.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<DogProfile> call = apiService.getDogProfile(name);

        call.enqueue(new Callback<DogProfile>() {
            @Override
            public void onResponse(Call<DogProfile> call, Response<DogProfile> response) {
                progressBar.setVisibility(View.GONE);
                btnLoadProfile.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    DogProfile profile = response.body();
                    displayProfile(profile);
                    Toast.makeText(DogProfileActivity.this, "Profile loaded!", Toast.LENGTH_SHORT).show();
                } else {
                    showError("Profile not found");
                }
            }

            @Override
            public void onFailure(Call<DogProfile> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLoadProfile.setEnabled(true);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void loadSavedProfile() {
        String savedName = prefs.getString(KEY_DOG_NAME, "");
        if (!savedName.isEmpty()) {
            etDogName.setText(savedName);
        }
    }

    private void displayProfile(DogProfile profile) {
        etDogName.setText(profile.getName());
        etAge.setText(String.valueOf(profile.getAgeMonths()));
        etWeight.setText(String.valueOf(profile.getWeightKg()));
        
        // Set breed in spinner
        ArrayAdapter adapter = (ArrayAdapter) spinnerBreed.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(profile.getBreed());
            if (position >= 0) {
                spinnerBreed.setSelection(position);
            }
        }
        
        tvProfileStatus.setText("✅ Profile loaded from server");
        tvProfileStatus.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvProfileStatus.setText("❌ " + message);
        tvProfileStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Inner class for breeds response
    public static class BreedsResponse {
        private List<String> breeds;

        public List<String> getBreeds() {
            return breeds;
        }

        public void setBreeds(List<String> breeds) {
            this.breeds = breeds;
        }
    }
}
