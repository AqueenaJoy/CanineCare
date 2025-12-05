package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;

public class HealthCheckRequest {
    
    @SerializedName("dog_name")
    private String dogName;
    
    @SerializedName("temperature")
    private float temperature;
    
    @SerializedName("activity_percent")
    private int activityPercent;

    public HealthCheckRequest(String dogName, float temperature, int activityPercent) {
        this.dogName = dogName;
        this.temperature = temperature;
        this.activityPercent = activityPercent;
    }

    public String getDogName() {
        return dogName;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getActivityPercent() {
        return activityPercent;
    }
}
