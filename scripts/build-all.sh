#!/bin/bash
set -e

echo "========================================"
echo "Core Identity - Build All"
echo "========================================"

echo "[1/4] Building Maven projects..."
mvn clean verify -DskipTests

echo "[2/4] Installing frontend dependencies..."
cd core-identity-web && npm install && cd ..
cd core-identity-admin-web && npm install && cd ..

echo "[3/4] Building frontends..."
cd core-identity-web && npm run build && cd ..
cd core-identity-admin-web && npm run build && cd ..

echo "[4/4] Collecting distribution artifacts..."
mkdir -p distribution
cp core-identity-backend/target/core-identity-backend-0.1.0-SNAPSHOT.jar distribution/core-identity-backend.jar
cp core-identity-admin-backend/target/core-identity-admin-backend-0.1.0-SNAPSHOT.jar distribution/core-identity-admin-backend.jar

echo "========================================"
echo "Build complete!"
echo "Artifacts in distribution/"
echo "========================================"