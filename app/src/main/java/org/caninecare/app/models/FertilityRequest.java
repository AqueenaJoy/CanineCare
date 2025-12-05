package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;

public class FertilityRequest {
    
    @SerializedName("dog_name")
    private String dogName;
    
    @SerializedName("breed")
    private String breed;
    
    @SerializedName("age_months")
    private int ageMonths;
    
    @SerializedName("weight_kg")
    private float weightKg;
    
    @SerializedName("last_heat_days")
    private Integer lastHeatDays;

    public FertilityRequest(String dogName, String breed, int ageMonths, float weightKg, Integer lastHeatDays) {
        this.dogName = dogName;
        this.breed = breed;
        this.ageMonths = ageMonths;
        this.weightKg = weightKg;
        this.lastHeatDays = lastHeatDays;
    }
}
