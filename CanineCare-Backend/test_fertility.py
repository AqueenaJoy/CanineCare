"""
Quick Fertility Prediction Test
Run this to test if fertility prediction works
"""

import requests
import json

# Backend URL
BACKEND_URL = "http://localhost:5000"

def test_fertility():
    print("="*60)
    print("  Testing Fertility Prediction")
    print("="*60)
    
    # Test data
    payload = {
        "dog_name": "Bella",
        "breed": "Golden Retriever",
        "age_months": 8,
        "weight_kg": 25.5,
        "last_heat_days": None  # First heat prediction
    }
    
    print("\nSending request to backend...")
    print(f"URL: {BACKEND_URL}/api/predict-fertility")
    print(f"Data: {json.dumps(payload, indent=2)}")
    
    try:
        response = requests.post(
            f"{BACKEND_URL}/api/predict-fertility",
            json=payload,
            timeout=5
        )
        
        print(f"\nResponse Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            print("\n✅ SUCCESS! Fertility prediction working!")
            print("\nResults:")
            print(f"  Prediction Type: {data.get('prediction_type')}")
            print(f"  Predicted Value: {data.get('prediction_value')} {data.get('prediction_unit')}")
            print(f"  Estimated Date: {data.get('estimated_date')}")
            print(f"  Fertility Status: {data.get('fertility_status')}")
            print(f"  Alert Level: {data.get('alert_level')}")
        else:
            print(f"\n❌ ERROR: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("\n❌ CONNECTION ERROR!")
        print("\nBackend server is not running!")
        print("\nSOLUTION:")
        print("1. Double-click: run_backend.bat")
        print("2. Wait for server to start")
        print("3. Run this test again")
        
    except Exception as e:
        print(f"\n❌ ERROR: {str(e)}")

if __name__ == "__main__":
    test_fertility()
    input("\nPress Enter to exit...")
