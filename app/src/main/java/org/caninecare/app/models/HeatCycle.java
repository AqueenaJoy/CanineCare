package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;

public class HeatCycle {
    @SerializedName("id")
    private String id;
    
    @SerializedName("dog_name")
    private String dogName;
    
    @SerializedName("prediction_type")
    private String predictionType;
    
    @SerializedName("prediction_value")
    private float predictionValue;
    
    @SerializedName("prediction_unit")
    private String predictionUnit;
    
    @SerializedName("estimated_date")
    private String estimatedDate;
    
    @SerializedName("fertility_status")
    private String fertilityStatus;
    
    @SerializedName("alert_level")
    private String alertLevel;
    
    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDogName() {
        return dogName;
    }

    public void setDogName(String dogName) {
        this.dogName = dogName;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public void setPredictionType(String predictionType) {
        this.predictionType = predictionType;
    }

    public float getPredictionValue() {
        return predictionValue;
    }

    public void setPredictionValue(float predictionValue) {
        this.predictionValue = predictionValue;
    }

    public String getPredictionUnit() {
        return predictionUnit;
    }

    public void setPredictionUnit(String predictionUnit) {
        this.predictionUnit = predictionUnit;
    }

    public String getEstimatedDate() {
        return estimatedDate;
    }

    public void setEstimatedDate(String estimatedDate) {
        this.estimatedDate = estimatedDate;
    }

    public String getFertilityStatus() {
        return fertilityStatus;
    }

    public void setFertilityStatus(String fertilityStatus) {
        this.fertilityStatus = fertilityStatus;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
