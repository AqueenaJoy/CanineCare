package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model for sensor data response from API
 */
public class SensorDataResponse {
    
    @SerializedName("data")
    private List<SensorData> data;
    
    @SerializedName("count")
    private int count;

    public List<SensorData> getData() {
        return data;
    }

    public int getCount() {
        return count;
    }

    public static class SensorData {
        @SerializedName("dog_name")
        private String dogName;
        
        @SerializedName("temperature")
        private float temperature;
        
        @SerializedName("activity_percent")
        private int activityPercent;
        
        @SerializedName("health_status")
        private String healthStatus;
        
        @SerializedName("timestamp")
        private String timestamp;
        
        @SerializedName("latitude")
        private double latitude;
        
        @SerializedName("longitude")
        private double longitude;

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

        public String getTimestamp() {
            return timestamp;
        }
        
        public double getLatitude() {
            return latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
    }
}
