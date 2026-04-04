#!/bin/bash
# Parallel Load Test Script

BASE_URL="http://localhost:8080"
THREADS=10
LOOPS=5

echo "========================================"
echo "Parallel Load Test for Personal Finance API"
echo "========================================"
echo "URL: $BASE_URL"
echo "Threads: $THREADS, Loops: $LOOPS"
echo ""

# Get token
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Failed to get token"
  exit 1
fi

echo "Token obtained"
echo ""

# Function to run parallel requests
run_parallel() {
  local endpoint=$1
  local path=$2
  
  echo "Testing: $endpoint"
  
  for i in $(seq 1 $LOOPS); do
    curl -s -o /dev/null -w "$endpoint,%{http_code},%{time_total}\n" \
      -H "Authorization: Bearer $TOKEN" \
      "$BASE_URL$path" &
  done
}

# Run parallel tests
run_parallel "GET /api/categories" "/api/categories"
run_parallel "GET /api/accounts" "/api/accounts"
run_parallel "GET /api/accounts/total-balance" "/api/accounts/total-balance"
run_parallel "GET /api/transactions" "/api/transactions?page=0&size=20"

# Wait for all to complete
wait

echo ""
echo "Done! Check results above."