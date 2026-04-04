# JMeter Performance Test Plan

## Overview
This test plan contains performance tests for the Personal Finance API endpoints.

## Prerequisites
1. Download JMeter: https://jmeter.apache.org/download_jmeter.cgi
2. Extract the archive
3. Run `./bin/jmeter.sh` (Linux/Mac) or `bin\jmeter.bat` (Windows)

## Test Plan Structure

### 1. Auth Flow - Get Token
- **Purpose**: Login and extract JWT token for subsequent requests
- **Thread**: 1 thread, 1 loop
- **Endpoint**: `POST /api/auth/login`

### 2. GET Categories
- **Purpose**: Test category list endpoint
- **Load**: 10 threads, 5 loops each (50 requests)
- **Ramp-up**: 2 seconds
- **Endpoint**: `GET /api/categories`

### 3. GET Accounts
- **Purpose**: Test account list endpoint
- **Load**: 10 threads, 5 loops each (50 requests)
- **Ramp-up**: 2 seconds
- **Endpoint**: `GET /api/accounts`

### 4. GET Total Balance
- **Purpose**: Test total balance calculation (cached)
- **Load**: 10 threads, 5 loops each (50 requests)
- **Ramp-up**: 2 seconds
- **Endpoint**: `GET /api/accounts/total-balance`

### 5. GET Transactions
- **Purpose**: Test transaction list with pagination
- **Load**: 10 threads, 5 loops each (50 requests)
- **Ramp-up**: 2 seconds
- **Endpoint**: `GET /api/transactions?page=0&size=20`

### 6. POST Create Transaction
- **Purpose**: Test write operation
- **Load**: 5 threads, 3 loops each (15 requests)
- **Ramp-up**: 1 second
- **Endpoint**: `POST /api/transactions`

## How to Run

### Option 1: GUI Mode
1. Open JMeter: `./bin/jmeter.sh`
2. Open this test plan: File → Open → `jmeter/PersonalFinanceAPI.jmx`
3. Click "Start" (green play button)
4. View results in "View Results Tree" or "Summary Report"

### Option 2: Command Line (Non-GUI)
```bash
# Generate test results
./bin/jmeter -n -t jmeter/PersonalFinanceAPI.jmx -l results.jtl -e -o html-report

# Parameters
# -n : Non-GUI mode
# -t : Test plan file
# -l : Results file
# -e : Generate HTML report after test
# -o : Output directory for HTML report
```

## Configuration

### User Variables
Edit these in the Test Plan → User Defined Variables:
| Variable | Default | Description |
|----------|---------|-------------|
| BASE_URL | http://localhost:8080 | API base URL |
| USERNAME | testuser | Test username |
| PASSWORD | Test@123 | Test password |

## Expected Results

### Good Performance Indicators
- **Response Time**: < 200ms for reads
- **Error Rate**: < 1%
- **Throughput**: > 50 req/sec

### Performance Issues to Watch
- High response time on transactions (N+1 query)
- Slow total-balance without cache
- Authentication overhead

## Interpreting Results

### Summary Report
- **Average**: Mean response time
- **Min/Max**: Response time range
- **90th %**: 90% of requests under this time
- **Error %**: Failed requests percentage
- **Throughput**: Requests per second

### View Results Tree
- Shows individual request/response
- Useful for debugging failures
- Green = success, Red = failure