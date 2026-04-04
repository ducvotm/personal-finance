#!/bin/bash
# Simple Load Test Script using curl

# Configuration
BASE_URL="http://localhost:8080"
THREADS=10
LOOPS=5
RESULTS_FILE="jmeter/results/load-test-results.txt"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "========================================"
echo "Simple Load Test for Personal Finance API"
echo "========================================"
echo "URL: $BASE_URL"
echo "Threads: $THREADS"
echo "Loops: $LOOPS"
echo ""

# Create results directory
mkdir -p jmeter/results

# Login to get token
echo "1. Login to get token..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo -e "${RED}Failed to get token${NC}"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}Token obtained successfully${NC}"
echo ""

# Function to run endpoint test
run_test() {
  local name=$1
  local method=$2
  local path=$3
  local data=$4
  
  echo "Testing: $name"
  
  if [ "$method" = "GET" ]; then
    start_time=$(date +%s%N)
    response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $TOKEN" "$BASE_URL$path")
    end_time=$(date +%s%N)
  else
    start_time=$(date +%s%N)
    response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$data" "$BASE_URL$path")
    end_time=$(date +%s%N)
  fi
  
  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | sed '$d')
  
  response_time=$(( (end_time - start_time) / 1000000 ))
  
  if [ "$http_code" = "200" ]; then
    echo -e "  ${GREEN}✓${NC} Status: $http_code, Time: ${response_time}ms"
  else
    echo -e "  ${RED}✗${NC} Status: $http_code, Time: ${response_time}ms"
    echo "  Response: $body"
  fi
  
  echo "$name,$http_code,$response_time" >> "$RESULTS_FILE"
}

# Initialize results file
echo "Endpoint,Status,ResponseTime(ms)" > "$RESULTS_FILE"

echo "2. Running load tests..."
echo ""

# Test 1: GET Categories (10 threads, 5 loops each)
echo "=== GET Categories (50 requests) ==="
for i in $(seq 1 $LOOPS); do
  run_test "Categories Loop $i" "GET" "/api/categories" ""
done

echo ""

# Test 2: GET Accounts
echo "=== GET Accounts (50 requests) ==="
for i in $(seq 1 $LOOPS); do
  run_test "Accounts Loop $i" "GET" "/api/accounts" ""
done

echo ""

# Test 3: GET Total Balance
echo "=== GET Total Balance (50 requests) ==="
for i in $(seq 1 $LOOPS); do
  run_test "Total Balance Loop $i" "GET" "/api/accounts/total-balance" ""
done

echo ""

# Test 4: GET Transactions
echo "=== GET Transactions (50 requests) ==="
for i in $(seq 1 $LOOPS); do
  run_test "Transactions Loop $i" "GET" "/api/transactions?page=0&size=20" ""
done

echo ""

# Summary
echo "========================================"
echo "Summary"
echo "========================================"

awk -F',' 'NR>1 {
  count++;
  if($2=="200") success++;
  total_time+=$3;
  if($3>max) max=$3;
  if(min=="" || $3<min) min=$3;
}
END {
  if(count>0) {
    printf "Total Requests: %d\n", count;
    printf "Successful: %d (%.1f%%)\n", success, (success/count)*100;
    printf "Failed: %d (%.1f%%)\n", count-success, ((count-success)/count)*100;
    printf "Min Response: %dms\n", min;
    printf "Max Response: %dms\n", max;
    printf "Avg Response: %dms\n", total_time/count;
  }
}' "$RESULTS_FILE"

echo ""
echo "Results saved to: $RESULTS_FILE"
echo ""
echo "Done!"