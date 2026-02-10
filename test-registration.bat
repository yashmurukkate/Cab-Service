@echo off
echo ===================================================
echo   Testing Registration Endpoint (Direct API Call)
echo ===================================================

echo Sending Registration Request to API Gateway (Port 8080)...
curl -v -X POST http://localhost:8080/api/users/register ^
-H "Content-Type: application/json" ^
-d "{\"fullName\":\"Test User\",\"email\":\"test99@example.com\",\"password\":\"password123\",\"role\":\"USER\",\"phone\":\"9999999999\"}"

echo.
echo.
echo ===================================================
echo Check the output above:
echo - HTTP 200/201: Success (Backend works, Frontend issue)
echo - HTTP 403: Forbidden (Backend Security issue)
echo - HTTP 500: Server Error (Backend Logic issue)
echo ===================================================
pause
