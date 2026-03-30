#!/bin/bash
set -e

BASE_URL="http://localhost:8080"
EMAIL="test@example.com"
PASSWORD="Test@123"
USERNAME="testuser"

echo "=== Setting up test user ==="

# Register (ignore error if exists)
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  > /dev/null 2>&1 || echo "User may already exist, continuing..."

# Login
RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo "$RESPONSE" | jq -r '.data.token // empty')
USER_ID=$(echo "$RESPONSE" | jq -r '.data.userId // empty')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "❌ Login failed. Response:"
  echo "$RESPONSE" | jq .
  exit 1
fi

# Save to .env file
echo "TOKEN=$TOKEN" > scripts/.env
echo "USER_ID=$USER_ID" >> scripts/.env

echo "✓ Test user ready: $USERNAME (ID: $USER_ID)"
echo "✓ Token saved to scripts/.env"
echo ""
echo "Usage:"
echo "  source scripts/.env"
echo "  curl -H \"Authorization: Bearer \$TOKEN\" ..."
