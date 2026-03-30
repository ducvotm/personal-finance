#!/bin/bash
source scripts/.env 2>/dev/null || true

echo "=== Redis Cache Keys ==="
docker compose exec -ti redis redis-cli -a Redis@123 KEYS "*" 2>/dev/null

echo ""
echo "=== Cache for User $USER_ID ==="

echo "Categories:"
docker compose exec -ti redis redis-cli -a Redis@123 GET "categories::$USER_ID" 2>/dev/null | head -c 300
echo ""

echo "Accounts:"
docker compose exec -ti redis redis-cli -a Redis@123 GET "accounts::$USER_ID" 2>/dev/null | head -c 300
echo ""

echo "Total Balance:"
docker compose exec -ti redis redis-cli -a Redis@123 GET "totalBalance::total_$USER_ID" 2>/dev/null
echo ""

echo "Refresh Token:"
docker compose exec -ti redis redis-cli -a Redis@123 GET "refresh_token:$USER_ID" 2>/dev/null
