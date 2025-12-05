@echo off
echo ========================================
echo  CanineCare+ Mobile App
echo ========================================
echo.
echo Starting mobile app...
echo.
echo Make sure backend server is running!
echo ========================================
echo.

cd /d "%~dp0\mobile-app"
python main.py

pause
