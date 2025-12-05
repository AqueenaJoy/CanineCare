/*
 * CanineCare+ ESP32 IoT System
 * Complete sensor integration for dog health monitoring
 * 
 * Hardware:
 * - ESP32 DevKit
 * - DS18B20 Temperature Sensor (GPIO 4)
 * - MPU6050 Accelerometer (I2C: SDA=21, SCL=22)
 * - NEO-6M GPS Module (TX=16, RX=17)
 * - 4.7kΩ Pull-up resistor for DS18B20
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <Wire.h>
#include <TinyGPS++.h>
#include <HardwareSerial.h>

// ========== WiFi Configuration ==========
const char* ssid = "Akku";           // Change this
const char* password = "11111111";   // Change this

// ========== ThingSpeak Configuration ==========
const char* thingspeakServer = "http://api.thingspeak.com/update";
String thingspeakAPIKey = "ZG6ON9FDH6VHB0JO";  // Change this

// ========== Backend Server Configuration ==========
const char* backendServer = "http://10.203.156.124:5000";  // Change to your server IP
String dogName = "Max";  // Your dog's name

// ========== Pin Definitions ==========
#define ONE_WIRE_BUS 4        // DS18B20 Temperature sensor
#define GPS_RX 16             // GPS Module RX
#define GPS_TX 17             // GPS Module TX
// MPU6050 uses default I2C pins: SDA=21, SCL=22

// ========== Sensor Objects ==========
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature tempSensor(&oneWire);
TinyGPSPlus gps;
HardwareSerial gpsSerial(2);

// ========== MPU6050 Registers ==========
const int MPU6050_ADDR = 0x68;
const int PWR_MGMT_1 = 0x6B;
const int ACCEL_XOUT_H = 0x3B;

// ========== Global Variables ==========
float temperature = 38.5;
float latitude = 0.0;
float longitude = 0.0;
int activityPercent = 0;
unsigned long lastSendTime = 0;
unsigned long sendInterval = 15000;  // Send data every 15 seconds
unsigned long immobileStartTime = 0;
bool isImmobile = false;

// Activity tracking
int movementCount = 0;
unsigned long activityWindow = 10000;  // 10 second window
unsigned long lastActivityCheck = 0;

// Safe zone configuration (Thuruthiparambu area for testing)
float safeZoneLat = 10.302500;    // Thuruthiparambu, Kerala
float safeZoneLon = 76.332800;
float safeZoneRadius = 100.0;  // meters

void setup() {
  Serial.begin(115200);
  
  // Initialize I2C for MPU6050
  Wire.begin();
  
  // Initialize GPS
  gpsSerial.begin(9600, SERIAL_8N1, GPS_RX, GPS_TX);
  
  // Initialize Temperature Sensor
  tempSensor.begin();
  
  // Initialize MPU6050
  initMPU6050();
  
  // Connect to WiFi
  connectWiFi();
  
  Serial.println("CanineCare+ System Initialized!");
  Serial.println("Monitoring started...");
}

void loop() {
  // Read all sensors
  readTemperature();
  readGPS();
  readActivity();
  
  // Send data to cloud
  if (millis() - lastSendTime >= sendInterval) {
    sendToThingSpeak();
    sendToBackend();
    lastSendTime = millis();
  }
  
  // Check for emergencies
  checkEmergencies();
  
  delay(100);
}

// ========== WiFi Connection ==========
void connectWiFi() {
  Serial.print("Connecting to WiFi");
  WiFi.begin(ssid, password);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi Connected!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nWiFi Connection Failed!");
  }
}

// ========== MPU6050 Initialization ==========
void initMPU6050() {
  Wire.beginTransmission(MPU6050_ADDR);
  Wire.write(PWR_MGMT_1);
  Wire.write(0);  // Wake up MPU6050
  Wire.endTransmission(true);
  Serial.println("MPU6050 Initialized");
}

// ========== Read Temperature ==========
void readTemperature() {
  tempSensor.requestTemperatures();
  float tempC = tempSensor.getTempCByIndex(0);
  
  if (tempC != DEVICE_DISCONNECTED_C && tempC > 0) {
    temperature = tempC;
  }
}

// ========== Read GPS ==========
void readGPS() {
  // DUMMY GPS DATA FOR TESTING (Remove when GPS module is working)
  // Coordinates: Thuruthiparambu, Our Lady of Grace Church, Kerala
  
  // OPTION 1: Inside Safe Zone (use this for normal monitoring)
  latitude = 10.302500;
  longitude = 76.332800;
  
  // OPTION 2: Outside Safe Zone - Uncomment to test geofence breach alert
  // latitude = 10.305000;  // ~300m away - triggers geofence alert
  // longitude = 76.335000;
  
 
  while (gpsSerial.available() > 0) {
    if (gps.encode(gpsSerial.read())) {
      if (gps.location.isValid()) {
        latitude = gps.location.lat();
        longitude = gps.location.lng();
      }
    }
  }
  
}

// ========== Read Activity from MPU6050 ==========
void readActivity() {
  Wire.beginTransmission(MPU6050_ADDR);
  Wire.write(ACCEL_XOUT_H);
  Wire.endTransmission(false);
  Wire.requestFrom(MPU6050_ADDR, 6, true);
  
  int16_t accelX = Wire.read() << 8 | Wire.read();
  int16_t accelY = Wire.read() << 8 | Wire.read();
  int16_t accelZ = Wire.read() << 8 | Wire.read();
  
  // Calculate total acceleration magnitude
  float accelMagnitude = sqrt(accelX*accelX + accelY*accelY + accelZ*accelZ);
  
  // Detect movement (threshold-based)
  if (accelMagnitude > 20000) {  // Adjust threshold as needed
    movementCount++;
    if (isImmobile) {
      isImmobile = false;
      Serial.println("Movement detected - dog is active");
    }
  }
  
  // Calculate activity percentage every 10 seconds
  if (millis() - lastActivityCheck >= activityWindow) {
    activityPercent = map(movementCount, 0, 100, 0, 100);
    activityPercent = constrain(activityPercent, 0, 100);
    
    // Check for immobility
    if (movementCount < 5) {  // Very low movement
      if (!isImmobile) {
        immobileStartTime = millis();
        isImmobile = true;
      }
    }
    
    movementCount = 0;
    lastActivityCheck = millis();
  }
}

// ========== Send Data to ThingSpeak ==========
void sendToThingSpeak() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    
    String url = String(thingspeakServer) + "?api_key=" + thingspeakAPIKey;
    url += "&field1=" + String(temperature, 2);
    url += "&field2=" + String(activityPercent);
    url += "&field3=" + String(latitude, 6);
    url += "&field4=" + String(longitude, 6);
    
    http.begin(url);
    int httpCode = http.GET();
    
    if (httpCode > 0) {
      Serial.println("✓ Data sent to ThingSpeak");
    } else {
      Serial.println("✗ ThingSpeak send failed");
    }
    
    http.end();
  }
}

// ========== Send Data to Backend ==========
void sendToBackend() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    
    // Health Check
    String healthUrl = String(backendServer) + "/api/health-check";
    http.begin(healthUrl);
    http.addHeader("Content-Type", "application/json");
    
    String healthPayload = "{";
    healthPayload += "\"dog_name\":\"" + dogName + "\",";
    healthPayload += "\"temperature\":" + String(temperature, 2) + ",";
    healthPayload += "\"activity_percent\":" + String(activityPercent) + ",";
    healthPayload += "\"latitude\":" + String(latitude, 6) + ",";
    healthPayload += "\"longitude\":" + String(longitude, 6);
    healthPayload += "}";
    
    int healthCode = http.POST(healthPayload);
    if (healthCode > 0) {
      String response = http.getString();
      Serial.println("Health Check: " + response);
    }
    http.end();
    
    // Location Update
    if (latitude != 0.0 && longitude != 0.0) {
      String locUrl = String(backendServer) + "/api/location-update";
      http.begin(locUrl);
      http.addHeader("Content-Type", "application/json");
      
      String locPayload = "{";
      locPayload += "\"dog_name\":\"" + dogName + "\",";
      locPayload += "\"latitude\":" + String(latitude, 6) + ",";
      locPayload += "\"longitude\":" + String(longitude, 6) + ",";
      locPayload += "\"safe_zone_lat\":" + String(safeZoneLat, 6) + ",";
      locPayload += "\"safe_zone_lon\":" + String(safeZoneLon, 6) + ",";
      locPayload += "\"safe_zone_radius\":" + String(safeZoneRadius);
      locPayload += "}";
      
      int locCode = http.POST(locPayload);
      if (locCode > 0) {
        String response = http.getString();
        Serial.println("Location: " + response);
      }
      http.end();
    }
  }
}

// ========== Check for Emergencies ==========
void checkEmergencies() {
  bool emergency = false;
  String emergencyMsg = "";
  
  // Critical temperature
  if (temperature >= 40.0) {
    emergency = true;
    emergencyMsg = "CRITICAL FEVER: " + String(temperature) + "°C";
  } else if (temperature <= 37.0) {
    emergency = true;
    emergencyMsg = "CRITICAL HYPOTHERMIA: " + String(temperature) + "°C";
  }
  
  // Prolonged immobility (1 minute)
  if (isImmobile && (millis() - immobileStartTime) >= 60000) {
    emergency = true;
    emergencyMsg += " | IMMOBILE FOR 1 MINUTE";
  }
  
  // Geofence breach
  if (latitude != 0.0 && safeZoneLat != 0.0) {
    float distance = calculateDistance(latitude, longitude, safeZoneLat, safeZoneLon);
    if (distance > safeZoneRadius) {
      emergency = true;
      emergencyMsg += " | GEOFENCE BREACH: " + String(distance) + "m";
    }
  }
  
  if (emergency) {
    Serial.println("🚨 EMERGENCY: " + emergencyMsg);
    sendEmergencyAlert();
  }
}

// ========== Send Emergency Alert ==========
void sendEmergencyAlert() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    
    String url = String(backendServer) + "/api/emergency-check";
    http.begin(url);
    http.addHeader("Content-Type", "application/json");
    
    unsigned long immobileDuration = isImmobile ? (millis() - immobileStartTime) / 1000 : 0;
    
    String payload = "{";
    payload += "\"dog_name\":\"" + dogName + "\",";
    payload += "\"temperature\":" + String(temperature, 2) + ",";
    payload += "\"activity_percent\":" + String(activityPercent) + ",";
    payload += "\"immobile_duration\":" + String(immobileDuration);
    payload += "}";
    
    int httpCode = http.POST(payload);
    if (httpCode > 0) {
      Serial.println("Emergency alert sent!");
    }
    
    http.end();
  }
}

// ========== Calculate Distance (Haversine Formula) ==========
float calculateDistance(float lat1, float lon1, float lat2, float lon2) {
  float R = 6371000; // Earth radius in meters
  float dLat = (lat2 - lat1) * PI / 180.0;
  float dLon = (lon2 - lon1) * PI / 180.0;
  
  float a = sin(dLat/2) * sin(dLat/2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
            sin(dLon/2) * sin(dLon/2);
  
  float c = 2 * atan2(sqrt(a), sqrt(1-a));
  return R * c;
}

// ========== Print Sensor Data (Debug) ==========
void printSensorData() {
  Serial.println("\n========== Sensor Data ==========");
  Serial.print("Temperature: "); Serial.print(temperature); Serial.println(" °C");
  Serial.print("Activity: "); Serial.print(activityPercent); Serial.println(" %");
  Serial.print("GPS: "); Serial.print(latitude, 6); Serial.print(", "); Serial.println(longitude, 6);
  Serial.println("================================\n");
}
