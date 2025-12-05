package org.caninecare.app.api;

import org.caninecare.app.models.SensorDataResponse;
import org.caninecare.app.models.HealthCheckRequest;
import org.caninecare.app.models.HealthCheckResponse;
import org.caninecare.app.models.FertilityRequest;
import org.caninecare.app.models.FertilityResponse;
import org.caninecare.app.models.AlertsResponse;
import org.caninecare.app.models.DogProfile;
import org.caninecare.app.models.DogProfileResponse;
import org.caninecare.app.models.HeatCyclesResponse;
import org.caninecare.app.activities.DogProfileActivity;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API Service interface for Retrofit
 */
public interface ApiService {
    
    @GET("sensor-data")
    Call<SensorDataResponse> getSensorData(@Query("limit") int limit);
    
    @POST("health-check")
    Call<HealthCheckResponse> checkHealth(@Body HealthCheckRequest request);
    
    @POST("predict-fertility")
    Call<FertilityResponse> predictFertility(@Body FertilityRequest request);
    
    @GET("alerts")
    Call<AlertsResponse> getAlerts(@Query("limit") int limit);
    
    @GET("health")
    Call<ApiHealthResponse> checkApiHealth();
    
    @POST("dog-profile")
    Call<DogProfileResponse> createOrUpdateProfile(@Body DogProfile profile);
    
    @GET("dog-profile/{name}")
    Call<DogProfile> getDogProfile(@Path("name") String name);
    
    @GET("heat-cycles/{name}")
    Call<HeatCyclesResponse> getHeatCycles(@Path("name") String name, @Query("limit") int limit);
    
    @GET("breeds")
    Call<DogProfileActivity.BreedsResponse> getBreeds();
}

class ApiHealthResponse {
    private String status;
    private boolean models_loaded;
    
    public String getStatus() {
        return status;
    }
    
    public boolean isModelsLoaded() {
        return models_loaded;
    }
}
