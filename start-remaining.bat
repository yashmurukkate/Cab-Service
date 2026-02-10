@echo off
echo Starting Remaining Services...
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
set MAVEN_HOME=%~dp0apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo Starting API Gateway (Port 8080)...
start "API Gateway" cmd /k "cd api-gateway && call mvn spring-boot:run"

echo Starting Core Services...
start "Cab Service" cmd /k "cd cab-service && call mvn spring-boot:run"
start "Ride Service" cmd /k "cd ride-service && call mvn spring-boot:run"
start "Billing Service" cmd /k "cd billing-service && call mvn spring-boot:run"
start "Notification Service" cmd /k "cd notification-service && call mvn spring-boot:run"
start "Routing Service" cmd /k "cd routing-service && call mvn spring-boot:run"

echo.
echo All remaining services started!
echo Frontend should be running at http://localhost:3000
pause
