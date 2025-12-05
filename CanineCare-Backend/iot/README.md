# ESP32 IoT Setup Guide

## Hardware Requirements

1. **ESP32 DevKit** (Main microcontroller)
2. **DS18B20 Temperature Sensor** (Waterproof version recommended)
3. **MPU6050 Accelerometer/Gyroscope** (6-axis motion sensor)
4. **NEO-6M GPS Module** (Location tracking)
5. **4.7kΩ Resistor** (Pull-up for DS18B20)
6. **Jumper Wires**
7. **Breadboard** (for prototyping)

## Pin Connections

### DS18B20 Temperature Sensor
```
DS18B20 VCC  → ESP32 3.3V
DS18B20 GND  → ESP32 GND
DS18B20 DATA → ESP32 GPIO 4 (with 4.7kΩ pull-up to 3.3V)
```

### MPU6050 Accelerometer (I2C)
```
MPU6050 VCC → ESP32 3.3V
MPU6050 GND → ESP32 GND
MPU6050 SDA → ESP32 GPIO 21
MPU6050 SCL → ESP32 GPIO 22
```

### NEO-6M GPS Module
```
GPS VCC → ESP32 5V (or 3.3V depending on module)
GPS GND → ESP32 GND
GPS TX  → ESP32 GPIO 16 (RX2)
GPS RX  → ESP32 GPIO 17 (TX2)
```

## Arduino IDE Setup

### 1. Install ESP32 Board Support
1. Open Arduino IDE
2. Go to **File → Preferences**
3. Add this URL to "Additional Board Manager URLs":
   ```
   https://dl.espressif.com/dl/package_esp32_index.json
   ```
4. Go to **Tools → Board → Boards Manager**
5. Search for "ESP32" and install "ESP32 by Espressif Systems"

### 2. Install Required Libraries
Go to **Sketch → Include Library → Manage Libraries** and install:

- **OneWire** by Paul Stoffregen
- **DallasTemperature** by Miles Burton
- **TinyGPSPlus** by Mikal Hart

(Wire.h and WiFi.h are built-in)

### 3. Configure the Code

Open `esp32_caninecare.ino` and update:

```cpp
// WiFi credentials
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// ThingSpeak API Key
String thingspeakAPIKey = "YOUR_API_KEY";

// Backend server IP (your computer's local IP)
const char* backendServer = "http://10.203.156.124";

// Dog name
String dogName = "Max";

// Safe zone coordinates (set after first GPS reading)
float safeZoneLat = 12.9716;  // Your home latitude
float safeZoneLon = 77.5946;  // Your home longitude
float safeZoneRadius = 100.0; // meters
```

### 4. Upload to ESP32

1. Connect ESP32 to computer via USB
2. Select **Tools → Board → ESP32 Dev Module**
3. Select correct **Port** (COM port on Windows)
4. Click **Upload** button
5. Open **Serial Monitor** (115200 baud) to see output

## ThingSpeak Setup

1. Create account at [ThingSpeak.com](https://thingspeak.com)
2. Create new channel with 4 fields:
   - Field 1: Temperature (°C)
   - Field 2: Activity (%)
   - Field 3: Latitude
   - Field 4: Longitude
3. Copy the **Write API Key** and paste in Arduino code

## Testing

### 1. Serial Monitor Output
You should see:
```
Connecting to WiFi...
WiFi Connected!
IP Address: 192.168.1.XXX
MPU6050 Initialized
CanineCare+ System Initialized!
Monitoring started...
✓ Data sent to ThingSpeak
Health Check: {"status":"ok"...}
```

### 2. Sensor Verification
- **Temperature**: Should read ~20-30°C (room temp) initially
- **Activity**: Will vary based on movement
- **GPS**: May take 1-2 minutes to get first fix (needs clear sky view)

## Troubleshooting

### WiFi Not Connecting
- Check SSID and password
- Ensure 2.4GHz WiFi (ESP32 doesn't support 5GHz)
- Move closer to router

### Temperature Reading -127°C
- Check DS18B20 connections
- Verify 4.7kΩ pull-up resistor is connected
- Try different GPIO pin

### GPS Not Working
- GPS needs clear view of sky
- Wait 2-5 minutes for first fix
- Check TX/RX connections (they should be crossed)

### MPU6050 Not Responding
- Check I2C connections (SDA/SCL)
- Verify 3.3V power supply
- Try I2C scanner sketch to detect address

## Mounting on Dog Collar

1. Use waterproof enclosure for ESP32 and sensors
2. Attach DS18B20 sensor close to dog's body (under collar)
3. Ensure GPS antenna has clear view upward
4. Use rechargeable battery pack (3.7V LiPo recommended)
5. Secure all connections with hot glue or epoxy

## Power Consumption Tips

- Use deep sleep mode when not transmitting
- Reduce WiFi transmission frequency
- Use lower GPS update rate
- Typical battery life: 6-12 hours with 2000mAh battery

## Safety Notes

⚠️ **Important:**
- Ensure all electronics are waterproofed
- Don't make collar too tight
- Monitor battery temperature
- Test thoroughly before actual use
- Have backup monitoring method
