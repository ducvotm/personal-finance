#!/bin/bash
# JMeter Performance Test Runner

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-testuser}"
PASSWORD="${PASSWORD:-Test@123}"
THREADS="${THREADS:-10}"
LOOPS="${LOOPS:-5}"
RAMP_UP="${RAMP_UP:-2}"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "========================================"
echo "JMeter Performance Test"
echo "========================================"
echo "Base URL: $BASE_URL"
echo "Threads: $THREADS"
echo "Loops: $LOOPS"
echo ""

# Check if JMeter is available
if command -v jmeter &> /dev/null; then
    JMETER_CMD="jmeter"
elif [ -f "$(dirname "$0")/apache-jmeter/bin/jmeter" ]; then
    JMETER_CMD="$(dirname "$0")/apache-jmeter/bin/jmeter"
else
    echo -e "${RED}Error: JMeter not found${NC}"
    echo "Please install JMeter from: https://jmeter.apache.org/download_jmeter.cgi"
    exit 1
fi

# Create results directory
mkdir -p jmeter/results

# Run JMeter in non-GUI mode
echo -e "${YELLOW}Running performance tests...${NC}"
$JMETER_CMD -n \
    -t jmeter/PersonalFinanceAPI.jmx \
    -l jmeter/results/results.jtl \
    -e \
    -o jmeter/results/html-report \
    -JBASE_URL="$BASE_URL" \
    -JUSERNAME="$USERNAME" \
    -JPASSWORD="$PASSWORD"

# Check results
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}Tests completed successfully!${NC}"
    echo "Results saved to: jmeter/results/"
    echo ""
    echo "To view HTML report:"
    echo "  open jmeter/results/html-report/index.html"
else
    echo ""
    echo -e "${RED}Tests failed!${NC}"
    exit 1
fi

# Parse basic stats from results
if command -v awk &> /dev/null; then
    echo ""
    echo "Quick Summary:"
    awk -F',' '
    NR>1 {
        sum += $4
        count++
        if($4>max) max=$4
        if(min==0 || $4<min) min=$4
        if($8>0) errors++
    }
    END {
        if(count>0) {
            printf "  Total Requests: %d\n", count
            printf "  Failed Requests: %d (%.1f%%)\n", errors, (errors/count)*100
            printf "  Min Response: %dms\n", min
            printf "  Max Response: %dms\n", max
            printf "  Avg Response: %dms\n", sum/count
        }
    }
    ' jmeter/results/results.jtl
fi

echo ""
echo "Done!"