package org.caninecare.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.caninecare.app.activities.AlertsActivity;
import org.caninecare.app.activities.DogProfileActivity;
import org.caninecare.app.activities.FertilityActivity;
import org.caninecare.app.activities.HealthActivity;
import org.caninecare.app.activities.HomeActivity;
import org.caninecare.app.activities.LocationActivity;

/**
 * Main Activity - Dashboard with navigation cards
 */
public class MainActivity extends AppCompatActivity {

    private CardView cardHome, cardHealth, cardFertility, cardLocation, cardAlerts, cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initViews();

        // Set click listeners
        setupClickListeners();

        // Show welcome message
        Toast.makeText(this, "Welcome to CanineCare+", Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        cardHome = findViewById(R.id.cardHome);
        cardHealth = findViewById(R.id.cardHealth);
        cardFertility = findViewById(R.id.cardFertility);
        cardLocation = findViewById(R.id.cardLocation);
        cardAlerts = findViewById(R.id.cardAlerts);
        cardProfile = findViewById(R.id.cardProfile);
    }

    private void setupClickListeners() {
        cardHome.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        cardHealth.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HealthActivity.class);
            startActivity(intent);
        });

        cardFertility.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FertilityActivity.class);
            startActivity(intent);
        });

        cardLocation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        cardAlerts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlertsActivity.class);
            startActivity(intent);
        });

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DogProfileActivity.class);
            startActivity(intent);
        });
    }
}
