@echo off
echo Starting Infrastructure Containers ONLY (MySQL, Kafka, Zookeeper, Redis)...
docker-compose up -d mysql zookeeper kafka redis
echo.
echo Infrastructure started!
echo Now you can run 'start-app.bat' to start the backend services.
pause
