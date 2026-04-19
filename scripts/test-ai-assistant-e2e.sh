#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
OLLAMA_URL="${OLLAMA_URL:-http://localhost:11434}"
EMAIL="${TEST_EMAIL:-ai-e2e@example.com}"
PASSWORD="${TEST_PASSWORD:-Test@123}"
USERNAME="${TEST_USERNAME:-ai-e2e-user}"

echo "=== 1) Ollama reachable? ==="
if ! curl -sf "$OLLAMA_URL/api/tags" >/dev/null; then
  echo "FAIL: Ollama not responding at $OLLAMA_URL (start: ollama serve)"
  exit 1
fi
echo "OK"

echo "=== 2) API reachable? ==="
if ! curl -sf "$BASE_URL/actuator/health" >/dev/null; then
  echo "FAIL: Backend not responding at $BASE_URL (need MySQL + mvn spring-boot:run)"
  exit 1
fi
echo "OK"

echo "=== 3) Register (ignore if user exists) ==="
curl -sS -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  >/dev/null 2>&1 || true

echo "=== 4) Login ==="
RESPONSE=$(curl -sS -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo "$RESPONSE" | jq -r '.data.token // empty')
if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "FAIL: login: $RESPONSE"
  exit 1
fi

echo "=== 5) POST /api/ai/assistant/query (Ollama via app) ==="
BODY=$(jq -n \
  --arg q "In one sentence, confirm you can answer budgeting questions." \
  --arg s "2026-04-01" \
  --arg e "2026-04-30" \
  '{question: $q, startDate: $s, endDate: $e}')

OUT=$(curl -sS -X POST "$BASE_URL/api/ai/assistant/query" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$BODY")

echo "$OUT" | jq .

ANS=$(echo "$OUT" | jq -r '.data.answer // empty')
if [ -z "$ANS" ] || [ "$ANS" = "null" ]; then
  echo "FAIL: missing data.answer"
  exit 1
fi

echo ""
echo "OK — assistant returned an answer (length ${#ANS} chars)."
