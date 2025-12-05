"""
CanineCare+ API Testing Script
Test all backend endpoints
"""

import requests
import json
from datetime import datetime

# Backend URL
BACKEND_URL = "http://localhost:5000"

def print_header(title):
    print("\n" + "="*60)
    print(f"  {title}")
    print("="*60)

def test_health_endpoint():
    """Test API health check"""
    print_header("Testing API Health")
    try:
        response = requests.get(f"{BACKEND_URL}/api/health", timeout=5)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_health_check():
    """Test health check endpoint"""
    print_header("Testing Health Check")
    try:
        payload = {
            "dog_name": "Max",
            "temperature": 38.5,
            "activity_percent": 65
        }
        response = requests.post(
            f"{BACKEND_URL}/api/health-check",
            json=payload,
            timeout=5
        )
        print(f"Status Code: {response.status_code}")
        data = response.json()
        print(f"Health Status: {data.get('health_status')}")
        print(f"Severity: {data.get('severity')}")
        print(f"Alerts: {data.get('alerts')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_fertility_prediction():
    """Test fertility prediction endpoint"""
    print_header("Testing Fertility Prediction")
    try:
        payload = {
            "dog_name": "Bella",
            "breed": "Golden Retriever",
            "age_months": 8,
            "weight_kg": 25.5,
            "last_heat_days": None
        }
        response = requests.post(
            f"{BACKEND_URL}/api/predict-fertility",
            json=payload,
            timeout=5
        )
        print(f"Status Code: {response.status_code}")
        data = response.json()
        print(f"Prediction Type: {data.get('prediction_type')}")
        print(f"Predicted Value: {data.get('prediction_value')} {data.get('prediction_unit')}")
        print(f"Estimated Date: {data.get('estimated_date')}")
        print(f"Fertility Status: {data.get('fertility_status')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_behavior_analysis():
    """Test behavior analysis endpoint"""
    print_header("Testing Behavior Analysis")
    try:
        payload = {
            "dog_name": "Max",
            "activity_percent": 75,
            "duration_minutes": 30
        }
        response = requests.post(
            f"{BACKEND_URL}/api/behavior-analysis",
            json=payload,
            timeout=5
        )
        print(f"Status Code: {response.status_code}")
        data = response.json()
        print(f"Behavior Type: {data.get('behavior_type')}")
        print(f"Stress Level: {data.get('stress_level')}")
        print(f"Alerts: {data.get('alerts')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_location_update():
    """Test location update endpoint"""
    print_header("Testing Location Update")
    try:
        payload = {
            "dog_name": "Max",
            "latitude": 12.9716,
            "longitude": 77.5946,
            "safe_zone_lat": 12.9716,
            "safe_zone_lon": 77.5946,
            "safe_zone_radius": 100
        }
        response = requests.post(
            f"{BACKEND_URL}/api/location-update",
            json=payload,
            timeout=5
        )
        print(f"Status Code: {response.status_code}")
        data = response.json()
        print(f"Distance from Safe Zone: {data.get('distance_from_safe_zone')}m")
        print(f"Is Safe: {data.get('is_safe')}")
        print(f"Alert: {data.get('alert_message')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_emergency_check():
    """Test emergency check endpoint"""
    print_header("Testing Emergency Check")
    try:
        payload = {
            "dog_name": "Max",
            "temperature": 40.5,
            "activity_percent": 10,
            "immobile_duration": 3600
        }
        response = requests.post(
            f"{BACKEND_URL}/api/emergency-check",
            json=payload,
            timeout=5
        )
        print(f"Status Code: {response.status_code}")
        data = response.json()
        print(f"Emergency Level: {data.get('emergency_level')}")
        print(f"Emergencies: {len(data.get('emergencies', []))}")
        for emergency in data.get('emergencies', []):
            print(f"  - {emergency.get('type')}: {emergency.get('message')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_get_breeds():
    """Test get breeds endpoint"""
    print_header("Testing Get Breeds")
    try:
        response = requests.get(f"{BACKEND_URL}/api/breeds", timeout=5)
        print(f"Status Code: {response.status_code}")
        data = response.json()
        breeds = data.get('breeds', [])
        print(f"Total Breeds: {len(breeds)}")
        print(f"Sample Breeds: {breeds[:5]}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_statistics():
    """Test statistics endpoint"""
    print_header("Testing Statistics")
    try:
        response = requests.get(f"{BACKEND_URL}/api/statistics", timeout=5)
        print(f"Status Code: {response.status_code}")
        data = response.json()
        print(f"Total Readings: {data.get('total_readings')}")
        print(f"Total Alerts: {data.get('total_alerts')}")
        print(f"Dogs Monitored: {data.get('total_dogs_monitored')}")
        print(f"System Status: {data.get('system_status')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def main():
    print("\n" + "="*60)
    print("  🐶 CanineCare+ API Testing Suite")
    print("="*60)
    print(f"\nBackend URL: {BACKEND_URL}")
    print(f"Test Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Check if backend is running
    print_header("Checking Backend Connection")
    try:
        response = requests.get(f"{BACKEND_URL}/api/health", timeout=3)
        print("✅ Backend is running!")
    except:
        print("❌ Backend is not running!")
        print("\nPlease start the backend server first:")
        print("  python backend/app.py")
        print("  OR")
        print("  Double-click run_backend.bat")
        return
    
    # Run all tests
    results = []
    
    results.append(("API Health", test_health_endpoint()))
    results.append(("Health Check", test_health_check()))
    results.append(("Fertility Prediction", test_fertility_prediction()))
    results.append(("Behavior Analysis", test_behavior_analysis()))
    results.append(("Location Update", test_location_update()))
    results.append(("Emergency Check", test_emergency_check()))
    results.append(("Get Breeds", test_get_breeds()))
    results.append(("Statistics", test_statistics()))
    
    # Print summary
    print_header("Test Summary")
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for test_name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{status} - {test_name}")
    
    print(f"\nTotal: {passed}/{total} tests passed")
    
    if passed == total:
        print("\n🎉 All tests passed successfully!")
    else:
        print(f"\n⚠️ {total - passed} test(s) failed")
    
    print("\n" + "="*60)

if __name__ == "__main__":
    main()
