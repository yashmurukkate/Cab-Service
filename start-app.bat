@echo off
echo ===================================================
echo   Starting Cab Booking Application (Local Mode)
echo ===================================================

:: Configuration
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
set MAVEN_HOME=%~dp0apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Java Environment:
java -version
echo.
echo Maven Environment:
call mvn -version
echo.

:: 1. Infrastructure
echo [1/3] Checking Infrastructure (Docker)...
docker-compose up -d mysql zookeeper kafka redis
echo Infrastructure is up.
echo.

:: 2. Backend Services
echo [2/3] Starting Backend Microservices...
echo Starting Eureka Server (Port 8761)...
start "Eureka Server" cmd /k "cd eureka-server && call mvn spring-boot:run"
echo Waiting for Eureka to initialize (20s)...
timeout /t 20 /nobreak >nul

echo Starting API Gateway (Port 8080)...
start "API Gateway" cmd /k "cd api-gateway && call mvn spring-boot:run"

echo Starting Core Services...
start "User Service" cmd /k "cd user-service && call mvn spring-boot:run"
start "Cab Service" cmd /k "cd cab-service && call mvn spring-boot:run"
start "Ride Service" cmd /k "cd ride-service && call mvn spring-boot:run"
start "Billing Service" cmd /k "cd billing-service && call mvn spring-boot:run"
start "Notification Service" cmd /k "cd notification-service && call mvn spring-boot:run"
start "Routing Service" cmd /k "cd routing-service && call mvn spring-boot:run"

:: 3. Frontend
echo [3/3] Starting Frontend...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo ===================================================
echo   Application Started!
echo   Frontend: http://localhost:3000
echo   Gateway:  http://localhost:8080
echo   Eureka:   http://localhost:8761
echo ===================================================
pause
