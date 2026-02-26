@echo off
echo ==========================================
echo     Appointment Booking API Server
echo ==========================================
echo.
echo Compiling Java files...
javac -cp "lib/*" src/*.java

if errorlevel 1 (
    echo.
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.
echo Starting API server on http://localhost:8080...
echo.
echo 📡 Available endpoints:
echo    GET  /api/doctors
echo    POST /api/patients
echo    GET  /api/patients
echo    POST /api/appointments
echo    GET  /api/appointments
echo    PUT  /api/appointments/{id}
echo    DELETE /api/appointments/{id}
echo.
echo Press Ctrl+C to stop the server
echo ==========================================
echo.

java -cp "lib/*;src" AppointmentAPI

echo.
echo Server stopped.
pause
