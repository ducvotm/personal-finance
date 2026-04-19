#!/usr/bin/env bash
set -euo pipefail

# Optional fallback helper.
# Primary M1 workflow is Codex/Cursor prompts using schema.md.

BASE_URL="${BASE_URL:-http://localhost:8080}"
JQ_BIN="${JQ_BIN:-jq}"

if [[ -z "${TOKEN:-}" ]]; then
  if [[ -f "scripts/.env" ]]; then
    # shellcheck disable=SC1091
    source "scripts/.env"
  fi
fi

if [[ -z "${TOKEN:-}" ]]; then
  echo "Missing TOKEN. Run scripts/setup-test-user.sh first or export TOKEN manually."
  exit 1
fi

if ! command -v "$JQ_BIN" >/dev/null 2>&1; then
  echo "Missing jq. Install jq first (brew install jq) or set JQ_BIN to a compatible binary."
  exit 1
fi

if ! curl -sf "$BASE_URL/actuator/health" >/dev/null 2>&1; then
  echo "API is not reachable at $BASE_URL."
  echo "Start backend first, e.g.: mvn spring-boot:run"
  exit 1
fi

usage() {
  cat <<'EOF'
Usage:
  scripts/wiki-ops.sh ingest
  scripts/wiki-ops.sh lint
  scripts/wiki-ops.sh query "your question" [startDate] [endDate]

Env vars:
  TOKEN     JWT token (or keep in scripts/.env as TOKEN=...)
  BASE_URL  API base URL (default: http://localhost:8080)

Examples:
  scripts/wiki-ops.sh ingest
  scripts/wiki-ops.sh lint
  scripts/wiki-ops.sh query "Where should I cut spending first this month?" 2026-04-01 2026-04-30
EOF
}

cmd="${1:-}"
case "$cmd" in
  ingest)
    curl -sS -X POST "$BASE_URL/api/ai/wiki/ingest" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" | "$JQ_BIN" .
    ;;
  lint)
    curl -sS -X POST "$BASE_URL/api/ai/wiki/lint" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" | "$JQ_BIN" .
    ;;
  query)
    question="${2:-}"
    start_date="${3:-$(date +%Y-%m-01)}"
    end_date="${4:-$(date +%Y-%m-%d)}"
    if [[ -z "$question" ]]; then
      echo "Missing question."
      usage
      exit 1
    fi
    curl -sS -X POST "$BASE_URL/api/ai/assistant/query" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "$("$JQ_BIN" -n \
        --arg q "$question" \
        --arg s "$start_date" \
        --arg e "$end_date" \
        '{question:$q,startDate:$s,endDate:$e}')" | "$JQ_BIN" .
    ;;
  *)
    usage
    exit 1
    ;;
esac

