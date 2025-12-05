@echo off
echo ========================================
echo  CanineCare+ API Testing
echo ========================================
echo.
echo Testing all backend endpoints...
echo Make sure backend server is running!
echo.

cd /d "%~dp0"
python test_api.py

echo.
pause
