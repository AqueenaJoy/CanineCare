package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;

public class DogProfile {
    @SerializedName("name")
    private String name;
    
    @SerializedName("breed")
    private String breed;
    
    @SerializedName("age_months")
    private int ageMonths;
    
    @SerializedName("weight_kg")
    private float weightKg;
    
    @SerializedName("last_updated")
    private String lastUpdated;

    public DogProfile() {
    }

    public DogProfile(String name, String breed, int ageMonths, float weightKg) {
        this.name = name;
        this.breed = breed;
        this.ageMonths = ageMonths;
        this.weightKg = weightKg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(int ageMonths) {
        this.ageMonths = ageMonths;
    }

    public float getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(float weightKg) {
        this.weightKg = weightKg;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
