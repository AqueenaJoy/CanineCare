"""
CanineCare+ Backend API
Main Flask Application
"""

from flask import Flask, request, jsonify, render_template
from flask_cors import CORS
import pandas as pd
import joblib
import os
from datetime import datetime, timedelta
import numpy as np
from collections import deque
import requests
from config import config
import firebase_admin
from firebase_admin import credentials, firestore

# Initialize Flask app
app = Flask(__name__, 
            template_folder='../frontend/templates', 
            static_folder='../frontend/static')

# Load configuration
app.config.from_object(config['development'])
CORS(app, origins=app.config['CORS_ORIGINS'])

# ============== Load ML Models ==============
try:
    first_heat_model = joblib.load(app.config['FIRST_HEAT_MODEL'])
    next_heat_model = joblib.load(app.config['NEXT_HEAT_MODEL'])
    breed_mapping = joblib.load(app.config['BREED_MAPPING'])
    print("✅ ML Models loaded successfully")
except Exception as e:
    print(f"⚠️ Error loading models: {e}")
    first_heat_model = None
    next_heat_model = None
    breed_mapping = {}

# ============== Firebase Initialization ==============
try:
    # Initialize Firebase (use your own credentials)
    # For now, we'll use a fallback if credentials don't exist
    firebase_cred_path = os.path.join(os.path.dirname(__file__), 'firebase_credentials.json')
    if os.path.exists(firebase_cred_path):
        cred = credentials.Certificate(firebase_cred_path)
        firebase_admin.initialize_app(cred)
        db = firestore.client()
        firebase_enabled = True
        print("✅ Firebase initialized successfully")
    else:
        db = None
        firebase_enabled = False
        print("⚠️ Firebase credentials not found. Using local storage.")
except Exception as e:
    print(f"⚠️ Firebase initialization error: {e}")
    db = None
    firebase_enabled = False

# ============== Data Storage ==============
sensor_data_buffer = deque(maxlen=app.config['MAX_SENSOR_BUFFER'])
alert_history = deque(maxlen=app.config['MAX_ALERT_HISTORY'])
dog_profiles = {}  # Local fallback storage

# ============== Helper Functions ==============

def calculate_distance(lat1, lon1, lat2, lon2):
    """Calculate distance between two GPS coordinates in meters"""
    from math import radians, sin, cos, sqrt, atan2
    
    R = 6371000  # Earth's radius in meters
    
    lat1_rad = radians(lat1)
    lat2_rad = radians(lat2)
    delta_lat = radians(lat2 - lat1)
    delta_lon = radians(lon2 - lon1)
    
    a = sin(delta_lat/2)**2 + cos(lat1_rad) * cos(lat2_rad) * sin(delta_lon/2)**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    
    distance = R * c
    return distance

def get_health_recommendations(temperature, activity):
    """Generate health recommendations"""
    recommendations = []
    
    if temperature >= app.config['TEMP_CRITICAL_HIGH']:
        recommendations.append("🚨 EMERGENCY: Contact veterinarian immediately")
        recommendations.append("Keep dog hydrated and cool")
        recommendations.append("Monitor temperature every 30 minutes")
    elif temperature >= app.config['TEMP_FEVER']:
        recommendations.append("Contact veterinarian soon")
        recommendations.append("Keep dog hydrated")
        recommendations.append("Monitor temperature every 2 hours")
    elif temperature <= app.config['TEMP_CRITICAL_LOW']:
        recommendations.append("🚨 EMERGENCY: Contact veterinarian immediately")
        recommendations.append("Warm the dog gradually with blankets")
        recommendations.append("Avoid direct heat sources")
    elif temperature <= app.config['TEMP_HYPOTHERMIA']:
        recommendations.append("Warm the dog gradually")
        recommendations.append("Contact veterinarian")
    
    if activity < app.config['ACTIVITY_LOW_THRESHOLD']:
        recommendations.append("Monitor for signs of pain or discomfort")
        recommendations.append("Check for appetite changes")
        recommendations.append("Ensure adequate rest")
    elif activity > app.config['ACTIVITY_HIGH_THRESHOLD']:
        recommendations.append("Check for stress triggers")
        recommendations.append("Provide calming environment")
        recommendations.append("Monitor for anxiety symptoms")
    
    if not recommendations:
        recommendations.append("✅ Continue regular monitoring")
        recommendations.append("Maintain healthy diet and exercise")
    
    return recommendations

def calculate_stress_level(activity_percent):
    """Calculate stress level based on activity"""
    if activity_percent > 80:
        return "High"
    elif activity_percent > 60:
        return "Moderate"
    elif activity_percent < 20:
        return "Low (Possible Depression)"
    else:
        return "Normal"

# ============== Routes ==============

@app.route('/')
def index():
    """Home page"""
    return render_template('index.html')

@app.route('/dashboard')
def dashboard():
    """Dashboard page"""
    return render_template('dashboard.html')

@app.route('/mobile')
def mobile():
    """Mobile app page"""
    return render_template('mobile.html')

# ============== API Endpoints ==============

@app.route('/api/predict-fertility', methods=['POST'])
def predict_fertility():
    """Predict fertility cycle (first heat or next heat)"""
    try:
        data = request.get_json()
        
        if not first_heat_model or not next_heat_model:
            return jsonify({"error": "ML models not loaded"}), 500
        
        dog_name = data.get("dog_name", "Unknown")
        breed = data.get("breed", "")
        age_months = int(data.get("age_months", 0))
        weight_kg = float(data.get("weight_kg", 0))
        last_heat_days = data.get("last_heat_days", None)
        
        # Encode breed
        breed_enc = breed_mapping.get(breed, -1)
        
        # Create advanced features
        weight_category = 0 if weight_kg < 10 else (1 if weight_kg < 25 else 2)
        age_category = 0 if age_months < 12 else (1 if age_months < 36 else 2)
        weight_age_ratio = weight_kg / (age_months + 1)
        
        # Prepare features
        if last_heat_days is None or last_heat_days == "":
            # First heat prediction
            input_df = pd.DataFrame([{
                "Age_Months": age_months,
                "Weight_kg": weight_kg,
                "Breed_enc": breed_enc,
                "Days_Since_Last_Heat": 0,
                "Weight_Category": weight_category,
                "Age_Category": age_category,
                "Weight_Age_Ratio": weight_age_ratio
            }])
            prediction_value = first_heat_model.predict(input_df)[0]
            prediction_type = "First Heat"
            prediction_unit = "months"
            
            # Calculate estimated date
            estimated_date = datetime.now() + timedelta(days=int(prediction_value * 30))
            
        else:
            # Next heat prediction
            days_since_last = int(last_heat_days)
            input_df = pd.DataFrame([{
                "Age_Months": age_months,
                "Weight_kg": weight_kg,
                "Breed_enc": breed_enc,
                "Days_Since_Last_Heat": days_since_last,
                "Weight_Category": weight_category,
                "Age_Category": age_category,
                "Weight_Age_Ratio": weight_age_ratio
            }])
            prediction_value = next_heat_model.predict(input_df)[0]
            prediction_type = "Next Heat"
            prediction_unit = "days"
            
            # Calculate estimated date
            estimated_date = datetime.now() + timedelta(days=int(prediction_value))
        
        # Determine fertility status
        if prediction_type == "Next Heat":
            if prediction_value <= 7:
                fertility_status = "Imminent Heat Cycle"
                alert_level = "high"
            elif prediction_value <= 30:
                fertility_status = "Approaching Heat Cycle"
                alert_level = "medium"
            else:
                fertility_status = "Normal Cycle"
                alert_level = "low"
        else:
            fertility_status = "Puppy - Awaiting First Heat"
            alert_level = "low"
        
        result = {
            "dog_name": dog_name,
            "breed": breed,
            "age_months": age_months,
            "weight_kg": weight_kg,
            "prediction_type": prediction_type,
            "prediction_value": round(prediction_value, 1),
            "prediction_unit": prediction_unit,
            "estimated_date": estimated_date.strftime("%Y-%m-%d"),
            "fertility_status": fertility_status,
            "alert_level": alert_level,
            "timestamp": datetime.now().isoformat()
        }
        
        # Store in dog profile (local)
        dog_profiles[dog_name] = result
        
        # Store in Firebase if enabled
        if firebase_enabled and db:
            try:
                # Store dog profile
                profile_ref = db.collection('dog_profiles').document(dog_name)
                profile_ref.set({
                    'name': dog_name,
                    'breed': breed,
                    'age_months': age_months,
                    'weight_kg': weight_kg,
                    'last_updated': firestore.SERVER_TIMESTAMP
                }, merge=True)
                
                # Store heat cycle history
                heat_cycle_ref = db.collection('heat_cycles').document()
                heat_cycle_ref.set({
                    'dog_name': dog_name,
                    'prediction_type': prediction_type,
                    'prediction_value': round(prediction_value, 1),
                    'prediction_unit': prediction_unit,
                    'estimated_date': estimated_date.strftime("%Y-%m-%d"),
                    'fertility_status': fertility_status,
                    'alert_level': alert_level,
                    'timestamp': firestore.SERVER_TIMESTAMP,
                    'created_at': datetime.now().isoformat()
                })
            except Exception as e:
                print(f"Firebase storage error: {e}")
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/health-check', methods=['POST'])
def health_check():
    """Analyze health based on temperature and activity"""
    try:
        data = request.get_json()
        
        temperature = float(data.get("temperature", 38.5))
        activity_percent = float(data.get("activity_percent", 50))
        dog_name = data.get("dog_name", "Unknown")
        latitude = float(data.get("latitude", 0.0))
        longitude = float(data.get("longitude", 0.0))
        
        health_status = "Normal"
        alerts = []
        severity = "low"
        
        # Temperature analysis
        if temperature >= app.config['TEMP_CRITICAL_HIGH']:
            health_status = "Critical Fever"
            alerts.append(f"CRITICAL: Temperature {temperature}°C (Normal: 38-39.2°C)")
            severity = "critical"
        elif temperature >= app.config['TEMP_FEVER']:
            health_status = "Fever Detected"
            alerts.append(f"High temperature: {temperature}°C (Normal: 38-39.2°C)")
            severity = "high"
        elif temperature <= app.config['TEMP_CRITICAL_LOW']:
            health_status = "Critical Hypothermia"
            alerts.append(f"CRITICAL: Temperature {temperature}°C (Normal: 38-39.2°C)")
            severity = "critical"
        elif temperature <= app.config['TEMP_HYPOTHERMIA']:
            health_status = "Hypothermia Risk"
            alerts.append(f"Low temperature: {temperature}°C (Normal: 38-39.2°C)")
            severity = "high"
        elif temperature < app.config['TEMP_NORMAL_MIN'] or temperature > app.config['TEMP_NORMAL_MAX']:
            health_status = "Temperature Abnormal"
            alerts.append(f"Temperature slightly abnormal: {temperature}°C")
            severity = "medium"
        
        # Activity analysis
        if activity_percent < app.config['ACTIVITY_LOW_THRESHOLD']:
            if health_status == "Normal":
                health_status = "Low Activity"
            alerts.append(f"Low activity: {activity_percent}% (Possible lethargy)")
            if severity == "low":
                severity = "medium"
        elif activity_percent > app.config['ACTIVITY_HIGH_THRESHOLD']:
            if health_status == "Normal":
                health_status = "High Activity"
            alerts.append(f"High activity: {activity_percent}% (Possible stress/anxiety)")
            if severity == "low":
                severity = "medium"
        
        # Combined analysis
        if temperature >= app.config['TEMP_FEVER'] and activity_percent < app.config['ACTIVITY_LOW_THRESHOLD']:
            health_status = "Critical: Fever + Lethargy"
            severity = "critical"
        
        result = {
            "dog_name": dog_name,
            "temperature": temperature,
            "activity_percent": activity_percent,
            "latitude": latitude,
            "longitude": longitude,
            "health_status": health_status,
            "alerts": alerts,
            "severity": severity,
            "timestamp": datetime.now().isoformat(),
            "recommendations": get_health_recommendations(temperature, activity_percent)
        }
        
        # Store alert if severity is medium or higher
        if severity in ["medium", "high", "critical"]:
            alert_history.append(result)
        
        # Store in buffer
        sensor_data_buffer.append(result)
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/behavior-analysis', methods=['POST'])
def behavior_analysis():
    """Analyze behavior patterns from accelerometer data"""
    try:
        data = request.get_json()
        
        activity_percent = float(data.get("activity_percent", 50))
        duration_minutes = int(data.get("duration_minutes", 30))
        dog_name = data.get("dog_name", "Unknown")
        
        behavior_type = "Normal"
        behavior_alerts = []
        
        # Behavior classification
        if activity_percent < 10:
            behavior_type = "Sleeping/Resting"
        elif activity_percent < 30:
            behavior_type = "Low Activity"
            behavior_alerts.append("Possible lethargy or fatigue")
        elif activity_percent < 60:
            behavior_type = "Normal Activity"
        elif activity_percent < 80:
            behavior_type = "High Activity"
        else:
            behavior_type = "Hyperactive"
            behavior_alerts.append("Possible stress, anxiety, or excitement")
        
        # Detect abnormal patterns
        if len(sensor_data_buffer) >= 3:
            recent_activities = [d.get("activity_percent", 50) for d in list(sensor_data_buffer)[-3:]]
            avg_activity = np.mean(recent_activities)
            std_activity = np.std(recent_activities)
            
            if std_activity > 30:
                behavior_alerts.append("Erratic behavior pattern detected")
        
        result = {
            "dog_name": dog_name,
            "behavior_type": behavior_type,
            "activity_percent": activity_percent,
            "duration_minutes": duration_minutes,
            "alerts": behavior_alerts,
            "timestamp": datetime.now().isoformat(),
            "stress_level": calculate_stress_level(activity_percent)
        }
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/location-update', methods=['POST'])
def location_update():
    """Update and track dog location"""
    try:
        data = request.get_json()
        
        latitude = float(data.get("latitude", 0))
        longitude = float(data.get("longitude", 0))
        dog_name = data.get("dog_name", "Unknown")
        safe_zone_lat = float(data.get("safe_zone_lat", 0))
        safe_zone_lon = float(data.get("safe_zone_lon", 0))
        safe_zone_radius = float(data.get("safe_zone_radius", app.config['DEFAULT_SAFE_ZONE_RADIUS']))
        
        # Calculate distance from safe zone
        distance = calculate_distance(latitude, longitude, safe_zone_lat, safe_zone_lon)
        
        is_safe = distance <= safe_zone_radius
        alert_type = "none"
        
        if not is_safe:
            alert_type = "geofence_breach"
            alert_message = f"Dog has left safe zone! Distance: {distance:.0f}m"
        else:
            alert_message = "Dog is within safe zone"
        
        result = {
            "dog_name": dog_name,
            "latitude": latitude,
            "longitude": longitude,
            "distance_from_safe_zone": round(distance, 2),
            "is_safe": is_safe,
            "alert_type": alert_type,
            "alert_message": alert_message,
            "timestamp": datetime.now().isoformat()
        }
        
        if not is_safe:
            alert_history.append(result)
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/emergency-check', methods=['POST'])
def emergency_check():
    """Check for emergency conditions"""
    try:
        data = request.get_json()
        
        temperature = float(data.get("temperature", 38.5))
        activity_percent = float(data.get("activity_percent", 50))
        immobile_duration = int(data.get("immobile_duration", 0))
        dog_name = data.get("dog_name", "Unknown")
        
        emergencies = []
        emergency_level = "none"
        
        # Critical temperature
        if temperature >= app.config['TEMP_CRITICAL_HIGH']:
            emergencies.append({
                "type": "Critical Fever",
                "message": f"Temperature critically high: {temperature}°C",
                "action": "IMMEDIATE VETERINARY CARE REQUIRED"
            })
            emergency_level = "critical"
        
        if temperature <= app.config['TEMP_CRITICAL_LOW']:
            emergencies.append({
                "type": "Severe Hypothermia",
                "message": f"Temperature critically low: {temperature}°C",
                "action": "IMMEDIATE VETERINARY CARE REQUIRED"
            })
            emergency_level = "critical"
        
        # Prolonged immobility
        if immobile_duration >= app.config['IMMOBILITY_DURATION']:
            emergencies.append({
                "type": "Prolonged Immobility",
                "message": f"No movement for {immobile_duration/60:.0f} minutes",
                "action": "Check dog immediately - possible injury or illness"
            })
            if emergency_level != "critical":
                emergency_level = "high"
        
        # Combined conditions
        if temperature >= app.config['TEMP_FEVER'] and activity_percent < 15:
            emergencies.append({
                "type": "Fever + Lethargy",
                "message": "Dog has fever and is extremely lethargic",
                "action": "Contact veterinarian urgently"
            })
            if emergency_level == "none":
                emergency_level = "high"
        
        result = {
            "dog_name": dog_name,
            "emergency_level": emergency_level,
            "emergencies": emergencies,
            "temperature": temperature,
            "activity_percent": activity_percent,
            "immobile_duration": immobile_duration,
            "timestamp": datetime.now().isoformat()
        }
        
        if emergencies:
            alert_history.append(result)
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/sensor-data', methods=['GET'])
def get_sensor_data():
    """Get recent sensor data"""
    limit = int(request.args.get('limit', 20))
    data = list(sensor_data_buffer)[-limit:]
    return jsonify({"data": data, "count": len(data)})

@app.route('/api/alerts', methods=['GET'])
def get_alerts():
    """Get alert history"""
    limit = int(request.args.get('limit', 50))
    alerts = list(alert_history)[-limit:]
    return jsonify({"alerts": alerts, "count": len(alerts)})

@app.route('/api/dog-profile/<dog_name>', methods=['GET'])
def get_dog_profile(dog_name):
    """Get dog profile and predictions"""
    # Try Firebase first
    if firebase_enabled and db:
        try:
            profile_ref = db.collection('dog_profiles').document(dog_name)
            profile_doc = profile_ref.get()
            if profile_doc.exists:
                return jsonify(profile_doc.to_dict())
        except Exception as e:
            print(f"Firebase read error: {e}")
    
    # Fallback to local storage
    if dog_name in dog_profiles:
        return jsonify(dog_profiles[dog_name])
    else:
        return jsonify({"error": "Dog profile not found"}), 404

@app.route('/api/dog-profile', methods=['POST'])
def create_or_update_dog_profile():
    """Create or update dog profile"""
    try:
        data = request.get_json()
        
        dog_name = data.get("name", "")
        breed = data.get("breed", "")
        age_months = int(data.get("age_months", 0))
        weight_kg = float(data.get("weight_kg", 0))
        
        if not dog_name:
            return jsonify({"error": "Dog name is required"}), 400
        
        profile_data = {
            "name": dog_name,
            "breed": breed,
            "age_months": age_months,
            "weight_kg": weight_kg,
            "last_updated": datetime.now().isoformat()
        }
        
        # Store locally
        dog_profiles[dog_name] = profile_data
        
        # Store in Firebase if enabled
        if firebase_enabled and db:
            try:
                profile_ref = db.collection('dog_profiles').document(dog_name)
                firebase_data = profile_data.copy()
                firebase_data['last_updated'] = firestore.SERVER_TIMESTAMP
                profile_ref.set(firebase_data, merge=True)
            except Exception as e:
                print(f"Firebase storage error: {e}")
        
        return jsonify({"success": True, "profile": profile_data})
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/heat-cycles/<dog_name>', methods=['GET'])
def get_heat_cycles(dog_name):
    """Get heat cycle history for a dog (last 20 cycles)"""
    try:
        limit = int(request.args.get('limit', 20))
        
        if firebase_enabled and db:
            try:
                # Query Firebase for heat cycles
                cycles_ref = db.collection('heat_cycles')\
                    .where('dog_name', '==', dog_name)\
                    .order_by('created_at', direction=firestore.Query.DESCENDING)\
                    .limit(limit)
                
                cycles = []
                for doc in cycles_ref.stream():
                    cycle_data = doc.to_dict()
                    cycle_data['id'] = doc.id
                    cycles.append(cycle_data)
                
                return jsonify({"cycles": cycles, "count": len(cycles)})
            except Exception as e:
                print(f"Firebase read error: {e}")
                return jsonify({"cycles": [], "count": 0, "error": str(e)})
        else:
            return jsonify({"cycles": [], "count": 0, "message": "Firebase not enabled"})
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/all-profiles', methods=['GET'])
def get_all_profiles():
    """Get all dog profiles"""
    try:
        if firebase_enabled and db:
            try:
                profiles_ref = db.collection('dog_profiles')
                profiles = []
                for doc in profiles_ref.stream():
                    profile_data = doc.to_dict()
                    profile_data['id'] = doc.id
                    profiles.append(profile_data)
                
                return jsonify({"profiles": profiles, "count": len(profiles)})
            except Exception as e:
                print(f"Firebase read error: {e}")
        
        # Fallback to local storage
        profiles = list(dog_profiles.values())
        return jsonify({"profiles": profiles, "count": len(profiles)})
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/breeds', methods=['GET'])
def get_breeds():
    """Get list of available breeds"""
    breeds = list(breed_mapping.keys()) if breed_mapping else []
    return jsonify({"breeds": sorted(breeds)})

@app.route('/api/statistics', methods=['GET'])
def get_statistics():
    """Get system statistics"""
    try:
        total_readings = len(sensor_data_buffer)
        total_alerts = len(alert_history)
        total_dogs = len(dog_profiles)
        
        # Calculate averages
        if sensor_data_buffer:
            temps = [d.get("temperature", 0) for d in sensor_data_buffer if "temperature" in d]
            activities = [d.get("activity_percent", 0) for d in sensor_data_buffer if "activity_percent" in d]
            
            avg_temp = np.mean(temps) if temps else 0
            avg_activity = np.mean(activities) if activities else 0
        else:
            avg_temp = 0
            avg_activity = 0
        
        stats = {
            "total_readings": total_readings,
            "total_alerts": total_alerts,
            "total_dogs_monitored": total_dogs,
            "average_temperature": round(avg_temp, 2),
            "average_activity": round(avg_activity, 2),
            "system_status": "Active",
            "last_updated": datetime.now().isoformat()
        }
        
        return jsonify(stats)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/health', methods=['GET'])
def health():
    """API health check"""
    return jsonify({
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "models_loaded": first_heat_model is not None and next_heat_model is not None
    })

if __name__ == '__main__':
    print("=" * 60)
    print("🐶 CanineCare+ Backend Server Starting...")
    print("=" * 60)
    print(f"📊 Dashboard: http://localhost:5000/dashboard")
    print(f"📱 Mobile App: http://localhost:5000/mobile")
    print(f"🔌 API Docs: http://localhost:5000/api/health")
    print("=" * 60)
    app.run(debug=True, host='0.0.0.0', port=5000)
