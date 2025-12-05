"""
CanineCare+ Configuration File
"""

import os
from datetime import timedelta

class Config:
    """Base configuration"""
    
    # Flask Configuration
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'caninecare-secret-key-2024'
    DEBUG = True
    
    # ThingSpeak Configuration
    THINGSPEAK_CHANNEL_ID = "3082777"
    THINGSPEAK_READ_API_KEY = os.environ.get('THINGSPEAK_READ_KEY') or "YOUR_READ_API_KEY"
    THINGSPEAK_WRITE_API_KEY = os.environ.get('THINGSPEAK_WRITE_KEY') or "2X4QY1MZPSMEORJ9"
    
    # Health Thresholds
    TEMP_NORMAL_MIN = 38.0  # °C
    TEMP_NORMAL_MAX = 39.2  # °C
    TEMP_FEVER = 39.5
    TEMP_HYPOTHERMIA = 37.5
    TEMP_CRITICAL_HIGH = 40.0
    TEMP_CRITICAL_LOW = 37.0
    
    # Activity Thresholds
    ACTIVITY_LOW_THRESHOLD = 20  # %
    ACTIVITY_HIGH_THRESHOLD = 80  # %
    
    # Safety Thresholds
    IMMOBILITY_DURATION = 3600  # seconds (1 hour)
    DEFAULT_SAFE_ZONE_RADIUS = 100  # meters
    
    # Data Storage
    MAX_SENSOR_BUFFER = 100  # Store last 100 readings
    MAX_ALERT_HISTORY = 200  # Store last 200 alerts
    
    # API Rate Limiting
    API_RATE_LIMIT = "100 per hour"
    
    # Firebase Configuration (Optional)
    FIREBASE_ENABLED = False
    FIREBASE_CREDENTIALS = os.environ.get('FIREBASE_CREDENTIALS') or 'firebase_config.json'
    
    # ML Model Paths
    MODEL_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ml', 'models')
    FIRST_HEAT_MODEL = os.path.join(MODEL_DIR, 'First_Heat_Best_Model.pkl')
    NEXT_HEAT_MODEL = os.path.join(MODEL_DIR, 'Next_Heat_Best_Model.pkl')
    BREED_MAPPING = os.path.join(MODEL_DIR, 'breed_mapping.pkl')
    
    # Data Paths
    DATA_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ml', 'data')
    TRAINING_DATA = os.path.join(DATA_DIR, 'dog.csv')
    
    # Logging
    LOG_LEVEL = 'INFO'
    LOG_FILE = 'caninecare.log'
    
    # CORS Settings
    CORS_ORIGINS = ['http://localhost:3000', 'http://localhost:5000', '*']
    
    # Session Configuration
    PERMANENT_SESSION_LIFETIME = timedelta(days=7)
    
    # Alert Severity Levels
    SEVERITY_LEVELS = {
        'low': 0,
        'medium': 1,
        'high': 2,
        'critical': 3
    }
    
    # Breed Categories
    SIZE_CATEGORIES = {
        'Small': (0, 10),      # 0-10 kg
        'Medium': (10, 25),    # 10-25 kg
        'Large': (25, 50)      # 25-50 kg
    }
    
    # Notification Settings
    ENABLE_EMAIL_ALERTS = False
    ENABLE_SMS_ALERTS = False
    ENABLE_PUSH_NOTIFICATIONS = False
    
    # Email Configuration (if enabled)
    MAIL_SERVER = 'smtp.gmail.com'
    MAIL_PORT = 587
    MAIL_USE_TLS = True
    MAIL_USERNAME = os.environ.get('MAIL_USERNAME')
    MAIL_PASSWORD = os.environ.get('MAIL_PASSWORD')
    
    @staticmethod
    def init_app(app):
        """Initialize application with config"""
        pass

class DevelopmentConfig(Config):
    """Development configuration"""
    DEBUG = True
    TESTING = False

class ProductionConfig(Config):
    """Production configuration"""
    DEBUG = False
    TESTING = False
    
    # Override with production values
    SECRET_KEY = os.environ.get('SECRET_KEY')
    
class TestingConfig(Config):
    """Testing configuration"""
    TESTING = True
    DEBUG = True

# Configuration dictionary
config = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'testing': TestingConfig,
    'default': DevelopmentConfig
}
