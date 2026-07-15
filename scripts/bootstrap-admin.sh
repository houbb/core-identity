#!/bin/bash
echo "=========================================="
echo "Core Identity - Bootstrap Admin"
echo "=========================================="
echo ""

BASE_URL="http://localhost:8101"
CLIENT_ID="admin-backend"
CLIENT_SECRET="dev-secret-change-in-production"

echo "Step 1: Get service token..."
TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/internal/v1/identity/service-tokens" \
  -H "Content-Type: application/json" \
  -d "{\"client_id\":\"$CLIENT_ID\",\"client_secret\":\"$CLIENT_SECRET\"}")
echo "$TOKEN_RESPONSE"

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

echo ""
echo "Step 2: Enter admin email (e.g. admin@example.com):"
read ADMIN_EMAIL
echo "Step 3: Enter display name:"
read ADMIN_DISPLAY

echo ""
echo "Step 4: Creating admin user..."
curl -s -X POST "$BASE_URL/internal/v1/identity/users" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"displayName\":\"$ADMIN_DISPLAY\"}"

echo ""
echo "Step 5: Creating platform operator..."
# To be completed: also create platform_operator record
echo "User created. Run the following SQL to grant admin:"
echo "INSERT INTO identity_platform_operator (id, user_id, operator_role, status, granted_at, created_at, updated_at, version)"
echo "VALUES (hex(randomblob(16)), '<user_id>', 'SUPER_ADMIN', 'ACTIVE', unixepoch()*1000, unixepoch()*1000, unixepoch()*1000, 1);"

echo ""
echo "Done! Please set up the admin password via the reset link."
