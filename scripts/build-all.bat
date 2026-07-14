@echo off
echo ========================================
echo Core Identity - Build All
echo ========================================
echo.

echo [1/4] Building Maven projects...
call mvn clean verify -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Maven build failed!
    exit /b 1
)

echo.
echo [2/4] Installing frontend dependencies...
cd core-identity-web
call npm install
cd ..\core-identity-admin-web
call npm install
cd ..

echo.
echo [3/4] Building frontends...
cd core-identity-web
call npm run build
cd ..\core-identity-admin-web
call npm run build
cd ..

echo.
echo [4/4] Collecting distribution artifacts...
if not exist distribution mkdir distribution
copy core-identity-backend\target\core-identity-backend-0.1.0-SNAPSHOT.jar distribution\core-identity-backend.jar
copy core-identity-admin-backend\target\core-identity-admin-backend-0.1.0-SNAPSHOT.jar distribution\core-identity-admin-backend.jar

echo.
echo ========================================
echo Build complete!
echo Artifacts in distribution/
echo ========================================