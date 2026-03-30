#!/bin/bash

echo "=== Stopping Infrastructure Services ==="

echo "--- Stopping MySQL ---"
docker compose -f docker-compose/mysql/docker-compose.yml down

echo "--- Stopping Redis ---"
docker compose -f docker-compose/redis/docker-compose.yml down

echo "--- Stopping Prometheus ---"
docker compose -f docker-compose/prometheus/docker-compose.yml down

echo "--- Stopping Grafana ---"
docker compose -f docker-compose/grafana/docker-compose.yml down

echo ""
echo "=== All Services Stopped ==="
