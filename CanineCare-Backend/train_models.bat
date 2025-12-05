@echo off
echo ========================================
echo  CanineCare+ ML Model Training
echo ========================================
echo.
echo Training machine learning models...
echo This will take 1-2 minutes...
echo.

cd /d "%~dp0"
python ml/train_models.py

echo.
echo ========================================
echo Training Complete!
echo ========================================
pause
