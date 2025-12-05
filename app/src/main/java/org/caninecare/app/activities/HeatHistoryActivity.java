package org.caninecare.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.caninecare.app.R;
import org.caninecare.app.api.ApiService;
import org.caninecare.app.api.RetrofitClient;
import org.caninecare.app.models.HeatCycle;
import org.caninecare.app.models.HeatCyclesResponse;
import org.caninecare.app.adapters.HeatCycleAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HeatHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private HeatCycleAdapter adapter;
    private String dogName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Heat Cycle History");
        }

        dogName = getIntent().getStringExtra("dog_name");
        if (dogName == null || dogName.isEmpty()) {
            Toast.makeText(this, "Dog name not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadHeatHistory();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewHeatHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HeatCycleAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void loadHeatHistory() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<HeatCyclesResponse> call = apiService.getHeatCycles(dogName, 20);

        call.enqueue(new Callback<HeatCyclesResponse>() {
            @Override
            public void onResponse(Call<HeatCyclesResponse> call, Response<HeatCyclesResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<HeatCycle> cycles = response.body().getCycles();
                    if (cycles != null && !cycles.isEmpty()) {
                        adapter.updateData(cycles);
                        recyclerView.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showError("Failed to load history");
                }
            }

            @Override
            public void onFailure(Call<HeatCyclesResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Connection error: " + t.getMessage());
            }
        });
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("No heat cycle history found.\nMake predictions to build history.");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("Error: " + message);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
