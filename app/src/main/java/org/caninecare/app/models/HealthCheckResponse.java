package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HealthCheckResponse {
    
    @SerializedName("dog_name")
    private String dogName;
    
    @SerializedName("temperature")
    private float temperature;
    
    @SerializedName("activity_percent")
    private int activityPercent;
    
    @SerializedName("health_status")
    private String healthStatus;
    
    @SerializedName("alerts")
    private List<String> alerts;
    
    @SerializedName("severity")
    private String severity;
    
    @SerializedName("recommendations")
    private List<String> recommendations;
    
    @SerializedName("timestamp")
    private String timestamp;

    public String getDogName() {
        return dogName;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getActivityPercent() {
        return activityPercent;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public String getSeverity() {
        return severity;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
