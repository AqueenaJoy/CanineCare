"""
CanineCare+ ML Model Training Script
Enhanced models with advanced features and ensemble methods
"""

import pandas as pd
import numpy as np
import joblib
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor, VotingRegressor
from sklearn.linear_model import Ridge
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error
import os
import sys

class CanineHealthPredictor:
    """Enhanced ML model for canine health and fertility prediction"""
    
    def __init__(self, data_path=None):
        if data_path is None:
            # Get the directory of this script
            script_dir = os.path.dirname(os.path.abspath(__file__))
            data_path = os.path.join(script_dir, "data", "dog.csv")
        self.data_path = data_path
        self.first_heat_model = None
        self.next_heat_model = None
        self.breed_mapping = None
        self.feature_importance = {}
        
    def load_and_prepare_data(self):
        """Load and prepare dataset"""
        print("📊 Loading dataset...")
        
        if not os.path.exists(self.data_path):
            print(f"❌ Error: Dataset not found at {self.data_path}")
            sys.exit(1)
            
        df = pd.read_csv(self.data_path)
        
        # Encode breed
        df["Breed_enc"] = df["Breed"].astype("category").cat.codes
        self.breed_mapping = {cat: code for code, cat in enumerate(
            df["Breed"].astype("category").cat.categories
        )}
        
        # Remove rows with missing target values
        df = df.dropna(subset=["First_Heat_Age_Months", "Next_Heat_Due_Days"])
        
        print(f"✅ Loaded {len(df)} records")
        print(f"📋 Breeds: {len(self.breed_mapping)}")
        print(f"📊 Features: {df.columns.tolist()}")
        
        return df
    
    def create_advanced_features(self, df):
        """Create advanced features for better prediction"""
        print("🔧 Creating advanced features...")
        
        # Size-based features
        df['Weight_Category'] = pd.cut(df['Weight_kg'], 
                                       bins=[0, 10, 25, 50], 
                                       labels=[0, 1, 2])  # Small, Medium, Large
        
        # Age-based features
        df['Age_Category'] = pd.cut(df['Age_Months'], 
                                    bins=[0, 12, 36, 120], 
                                    labels=[0, 1, 2])  # Puppy, Young, Adult
        
        # Interaction features
        df['Weight_Age_Ratio'] = df['Weight_kg'] / (df['Age_Months'] + 1)
        
        # Fill NaN values
        df['Weight_Category'] = df['Weight_Category'].fillna(1).astype(int)
        df['Age_Category'] = df['Age_Category'].fillna(1).astype(int)
        
        print("✅ Advanced features created")
        return df
    
    def train_first_heat_model(self, X, y):
        """Train optimized first heat prediction model"""
        print("\n" + "="*60)
        print("🔬 Training First Heat Prediction Model...")
        print("="*60)
        
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42
        )
        
        # Ensemble of models
        rf = RandomForestRegressor(
            n_estimators=200,
            max_depth=15,
            min_samples_split=5,
            min_samples_leaf=2,
            random_state=42,
            n_jobs=-1
        )
        
        gb = GradientBoostingRegressor(
            n_estimators=150,
            max_depth=8,
            learning_rate=0.1,
            random_state=42
        )
        
        ridge = Ridge(alpha=1.0)
        
        # Voting ensemble
        ensemble = VotingRegressor([
            ('rf', rf),
            ('gb', gb),
            ('ridge', ridge)
        ])
        
        # Train ensemble
        print("Training ensemble model...")
        ensemble.fit(X_train, y_train)
        
        # Evaluate
        y_pred = ensemble.predict(X_test)
        rmse = np.sqrt(mean_squared_error(y_test, y_pred))
        mae = mean_absolute_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)
        
        print(f"\n✅ First Heat Model Performance:")
        print(f"   RMSE: {rmse:.3f} months")
        print(f"   MAE: {mae:.3f} months")
        print(f"   R² Score: {r2:.3f}")
        
        # Cross-validation
        print("\nPerforming cross-validation...")
        cv_scores = cross_val_score(ensemble, X, y, cv=5, 
                                     scoring='neg_mean_squared_error', n_jobs=-1)
        cv_rmse = np.sqrt(-cv_scores.mean())
        print(f"   CV RMSE: {cv_rmse:.3f} months")
        
        self.first_heat_model = ensemble
        
        # Feature importance (from Random Forest component)
        if hasattr(rf, 'feature_importances_'):
            self.feature_importance['first_heat'] = dict(zip(
                X.columns, rf.feature_importances_
            ))
            print("\n📊 Top 5 Important Features:")
            sorted_features = sorted(self.feature_importance['first_heat'].items(), 
                                   key=lambda x: x[1], reverse=True)[:5]
            for feat, imp in sorted_features:
                print(f"   {feat}: {imp:.4f}")
        
        return ensemble, {'rmse': rmse, 'mae': mae, 'r2': r2, 'cv_rmse': cv_rmse}
    
    def train_next_heat_model(self, X, y):
        """Train optimized next heat prediction model"""
        print("\n" + "="*60)
        print("🔬 Training Next Heat Prediction Model...")
        print("="*60)
        
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42
        )
        
        # Ensemble of models
        rf = RandomForestRegressor(
            n_estimators=200,
            max_depth=15,
            min_samples_split=5,
            min_samples_leaf=2,
            random_state=42,
            n_jobs=-1
        )
        
        gb = GradientBoostingRegressor(
            n_estimators=150,
            max_depth=8,
            learning_rate=0.1,
            random_state=42
        )
        
        ridge = Ridge(alpha=1.0)
        
        # Voting ensemble
        ensemble = VotingRegressor([
            ('rf', rf),
            ('gb', gb),
            ('ridge', ridge)
        ])
        
        # Train ensemble
        print("Training ensemble model...")
        ensemble.fit(X_train, y_train)
        
        # Evaluate
        y_pred = ensemble.predict(X_test)
        rmse = np.sqrt(mean_squared_error(y_test, y_pred))
        mae = mean_absolute_error(y_test, y_pred)
        r2 = r2_score(y_test, y_pred)
        
        print(f"\n✅ Next Heat Model Performance:")
        print(f"   RMSE: {rmse:.3f} days")
        print(f"   MAE: {mae:.3f} days")
        print(f"   R² Score: {r2:.3f}")
        
        # Cross-validation
        print("\nPerforming cross-validation...")
        cv_scores = cross_val_score(ensemble, X, y, cv=5, 
                                     scoring='neg_mean_squared_error', n_jobs=-1)
        cv_rmse = np.sqrt(-cv_scores.mean())
        print(f"   CV RMSE: {cv_rmse:.3f} days")
        
        self.next_heat_model = ensemble
        
        # Feature importance
        if hasattr(rf, 'feature_importances_'):
            self.feature_importance['next_heat'] = dict(zip(
                X.columns, rf.feature_importances_
            ))
            print("\n📊 Top 5 Important Features:")
            sorted_features = sorted(self.feature_importance['next_heat'].items(), 
                                   key=lambda x: x[1], reverse=True)[:5]
            for feat, imp in sorted_features:
                print(f"   {feat}: {imp:.4f}")
        
        return ensemble, {'rmse': rmse, 'mae': mae, 'r2': r2, 'cv_rmse': cv_rmse}
    
    def train_all_models(self):
        """Train all prediction models"""
        # Load data
        df = self.load_and_prepare_data()
        
        # Create advanced features
        df = self.create_advanced_features(df)
        
        # Define features
        features = ["Age_Months", "Weight_kg", "Breed_enc", "Days_Since_Last_Heat",
                   "Weight_Category", "Age_Category", "Weight_Age_Ratio"]
        
        # Prepare data for first heat model
        X_first = df[features].fillna(0)
        y_first = df["First_Heat_Age_Months"]
        
        # Prepare data for next heat model
        X_next = df[features].fillna(0)
        y_next = df["Next_Heat_Due_Days"]
        
        # Train models
        first_model, first_metrics = self.train_first_heat_model(X_first, y_first)
        next_model, next_metrics = self.train_next_heat_model(X_next, y_next)
        
        return {
            'first_heat': first_metrics,
            'next_heat': next_metrics
        }
    
    def save_models(self, output_dir=None):
        """Save trained models"""
        if output_dir is None:
            script_dir = os.path.dirname(os.path.abspath(__file__))
            output_dir = os.path.join(script_dir, "models")
        os.makedirs(output_dir, exist_ok=True)
        
        print("\n" + "="*60)
        print("💾 Saving Models...")
        print("="*60)
        
        if self.first_heat_model:
            path = os.path.join(output_dir, "First_Heat_Best_Model.pkl")
            joblib.dump(self.first_heat_model, path)
            print(f"✅ Saved: {path}")
        
        if self.next_heat_model:
            path = os.path.join(output_dir, "Next_Heat_Best_Model.pkl")
            joblib.dump(self.next_heat_model, path)
            print(f"✅ Saved: {path}")
        
        if self.breed_mapping:
            path = os.path.join(output_dir, "breed_mapping.pkl")
            joblib.dump(self.breed_mapping, path)
            print(f"✅ Saved: {path}")
        
        if self.feature_importance:
            path = os.path.join(output_dir, "feature_importance.pkl")
            joblib.dump(self.feature_importance, path)
            print(f"✅ Saved: {path}")

def main():
    """Main training function"""
    print("\n" + "="*60)
    print("🐶 CanineCare+ ML Model Training")
    print("="*60)
    
    # Initialize predictor
    predictor = CanineHealthPredictor()
    
    # Train models
    metrics = predictor.train_all_models()
    
    # Save models
    predictor.save_models()
    
    # Print summary
    print("\n" + "="*60)
    print("✅ Model Training Complete!")
    print("="*60)
    print("\n📊 Model Performance Summary:")
    print(f"\nFirst Heat Prediction:")
    print(f"  - RMSE: {metrics['first_heat']['rmse']:.3f} months")
    print(f"  - MAE: {metrics['first_heat']['mae']:.3f} months")
    print(f"  - R² Score: {metrics['first_heat']['r2']:.3f}")
    print(f"  - CV RMSE: {metrics['first_heat']['cv_rmse']:.3f} months")
    
    print(f"\nNext Heat Prediction:")
    print(f"  - RMSE: {metrics['next_heat']['rmse']:.3f} days")
    print(f"  - MAE: {metrics['next_heat']['mae']:.3f} days")
    print(f"  - R² Score: {metrics['next_heat']['r2']:.3f}")
    print(f"  - CV RMSE: {metrics['next_heat']['cv_rmse']:.3f} days")
    
    print("\n" + "="*60)
    print("🎉 Training completed successfully!")
    print("="*60)

if __name__ == "__main__":
    main()
