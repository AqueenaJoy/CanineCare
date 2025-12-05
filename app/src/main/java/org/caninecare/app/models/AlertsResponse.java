package org.caninecare.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AlertsResponse {
    
    @SerializedName("alerts")
    private List<Alert> alerts;
    
    @SerializedName("count")
    private int count;

    public List<Alert> getAlerts() {
        return alerts;
    }

    public int getCount() {
        return count;
    }

    public static class Alert {
        @SerializedName("dog_name")
        private String dogName;
        
        @SerializedName("health_status")
        private String healthStatus;
        
        @SerializedName("severity")
        private String severity;
        
        @SerializedName("alerts")
        private List<String> alertMessages;
        
        @SerializedName("timestamp")
        private String timestamp;
        
        // Emergency fields
        @SerializedName("emergency_level")
        private String emergencyLevel;
        
        @SerializedName("emergencies")
        private List<Emergency> emergencies;
        
        @SerializedName("temperature")
        private float temperature;
        
        @SerializedName("activity_percent")
        private float activityPercent;
        
        @SerializedName("immobile_duration")
        private int immobileDuration;

        public String getDogName() {
            return dogName;
        }

        public String getHealthStatus() {
            return healthStatus;
        }

        public String getSeverity() {
            return severity;
        }

        public List<String> getAlertMessages() {
            return alertMessages;
        }

        public String getTimestamp() {
            return timestamp;
        }
        
        public String getEmergencyLevel() {
            return emergencyLevel;
        }
        
        public List<Emergency> getEmergencies() {
            return emergencies;
        }
        
        public float getTemperature() {
            return temperature;
        }
        
        public float getActivityPercent() {
            return activityPercent;
        }
        
        public int getImmobileDuration() {
            return immobileDuration;
        }
    }
    
    public static class Emergency {
        @SerializedName("type")
        private String type;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("action")
        private String action;
        
        public String getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getAction() {
            return action;
        }
    }
}
