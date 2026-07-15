@echo off
echo ==========================================
echo Core Identity - Bootstrap Admin
echo ==========================================
echo.

set "BASE_URL=http://localhost:8101"
set "CLIENT_ID=admin-backend"
set "CLIENT_SECRET=dev-secret-change-in-production"

echo Step 1: Get service token...
for /f "tokens=*" %%a in ('curl -s -X POST "%BASE_URL%/internal/v1/identity/service-tokens" ^
  -H "Content-Type: application/json" ^
  -d "{\"client_id\":\"%CLIENT_ID%\",\"client_secret\":\"%CLIENT_SECRET%\"}"') do set TOKEN_RESPONSE=%%a

echo %TOKEN_RESPONSE%

echo.
echo Step 2: Enter admin email (e.g. admin@example.com):
set /p ADMIN_EMAIL=

echo Step 3: Enter display name:
set /p ADMIN_DISPLAY=

echo.
echo Step 4: Creating admin user...
curl -s -X POST "%BASE_URL%/internal/v1/identity/users" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"%ADMIN_EMAIL%\",\"displayName\":\"%ADMIN_DISPLAY%\"}"

echo.
echo Done! Please set up the admin password via the reset link.
pause