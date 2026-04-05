@echo off
echo ==============================================
echo    Danjjak Full-Stack Test Launcher
echo ==============================================
echo.
echo NOTE: Please ensure Android Emulator is running
echo or a physical device is connected via USB.
echo.
pause

echo.
echo [1/3] Starting backend server in a new window...
start "Danjjak Backend (Port:3000)" cmd /k "cd backend && npm run dev"

echo.
echo [2/3] Building and installing Android app...
cd frontend
call gradlew installDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] App installation failed.
    echo Possible reasons:
    echo 1. Emulator is not running.
    echo 2. Gradle build error (KSP/Hilt issues).
    echo 3. USB debugging not allowed.
    cd ..
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Launching Danjjak app on device...
adb shell am start -n com.danjjak/.MainActivity

echo.
echo Done! Please check your emulator or device.
cd ..
pause
