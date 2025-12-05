package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HeatCyclesResponse {
    @SerializedName("cycles")
    private List<HeatCycle> cycles;
    
    @SerializedName("count")
    private int count;
    
    @SerializedName("error")
    private String error;

    public List<HeatCycle> getCycles() {
        return cycles;
    }

    public void setCycles(List<HeatCycle> cycles) {
        this.cycles = cycles;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
