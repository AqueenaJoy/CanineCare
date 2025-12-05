package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;

public class FertilityResponse {
    
    @SerializedName("dog_name")
    private String dogName;
    
    @SerializedName("breed")
    private String breed;
    
    @SerializedName("age_months")
    private int ageMonths;
    
    @SerializedName("weight_kg")
    private float weightKg;
    
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

    public String getDogName() {
        return dogName;
    }

    public String getBreed() {
        return breed;
    }

    public int getAgeMonths() {
        return ageMonths;
    }

    public float getWeightKg() {
        return weightKg;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public float getPredictionValue() {
        return predictionValue;
    }

    public String getPredictionUnit() {
        return predictionUnit;
    }

    public String getEstimatedDate() {
        return estimatedDate;
    }

    public String getFertilityStatus() {
        return fertilityStatus;
    }

    public String getAlertLevel() {
        return alertLevel;
    }
}
