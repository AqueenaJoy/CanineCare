package org.caninecare.app.utils;

/**
 * API Configuration
 * Update BASE_URL with your backend server IP address
 */
public class ApiConfig {
    
    // Backend server URL - CHANGE THIS TO YOUR COMPUTER'S IP
    // Get IP: Run 'ipconfig' in Windows Command Prompt
    // Example: http://192.168.1.100:5000/api/
    public static final String BASE_URL = "http://10.203.156.124:5000/api/";
    
    // API Endpoints
    public static final String HEALTH_CHECK = "health-check";
    public static final String PREDICT_FERTILITY = "predict-fertility";
    public static final String BEHAVIOR_ANALYSIS = "behavior-analysis";
    public static final String LOCATION_UPDATE = "location-update";
    public static final String EMERGENCY_CHECK = "emergency-check";
    public static final String SENSOR_DATA = "sensor-data";
    public static final String ALERTS = "alerts";
    public static final String DOG_PROFILE = "dog-profile/{dog_name}";
    public static final String BREEDS = "breeds";
    public static final String STATISTICS = "statistics";
    public static final String API_HEALTH = "health";
    
    // Request timeout (seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
    
    // Refresh intervals (milliseconds)
    public static final long HOME_REFRESH_INTERVAL = 10000; // 10 seconds
    public static final long SENSOR_REFRESH_INTERVAL = 5000; // 5 seconds
    
    // Health thresholds
    public static final float TEMP_NORMAL_MIN = 38.0f;
    public static final float TEMP_NORMAL_MAX = 39.2f;
    public static final float TEMP_FEVER = 39.5f;
    public static final float TEMP_HYPOTHERMIA = 37.5f;
    public static final float TEMP_CRITICAL_HIGH = 40.0f;
    public static final float TEMP_CRITICAL_LOW = 37.0f;
    
    public static final int ACTIVITY_LOW_THRESHOLD = 20;
    public static final int ACTIVITY_HIGH_THRESHOLD = 80;
}
