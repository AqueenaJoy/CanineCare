@echo off
echo ========================================
echo  CanineCare+ Backend Server
echo ========================================
echo.
echo Starting backend server...
echo Server will be available at: http://localhost:5000
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.

cd /d "%~dp0"
python backend/app.py

pause
