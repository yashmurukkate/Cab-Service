@echo off
echo Starting API Gateway (Debug Mode)...
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
set MAVEN_HOME=%~dp0apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd api-gateway
call mvn spring-boot:run -e
echo.
echo API Gateway Stopped.
pause
