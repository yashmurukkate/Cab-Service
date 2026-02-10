@echo off
echo Starting User Service (Debug Mode)...
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
set MAVEN_HOME=%~dp0apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

cd user-service
call mvn spring-boot:run -e
echo.
echo User Service Stopped.
pause
