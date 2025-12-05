package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;

public class DogProfileResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("profile")
    private DogProfile profile;
    
    @SerializedName("error")
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DogProfile getProfile() {
        return profile;
    }

    public void setProfile(DogProfile profile) {
        this.profile = profile;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
