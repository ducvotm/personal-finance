#!/bin/bash

echo "=== Creating Docker network (if not exists) ==="
docker network create finance-network 2>/dev/null || echo "Network already exists"

echo ""
echo "=== Starting Infrastructure Services ==="

echo "--- Starting MySQL ---"
docker compose -f docker-compose/mysql/docker-compose.yml up -d

echo "--- Starting Redis ---"
docker compose -f docker-compose/redis/docker-compose.yml up -d

echo "--- Starting Prometheus ---"
docker compose -f docker-compose/prometheus/docker-compose.yml up -d

echo "--- Starting Grafana ---"
docker compose -f docker-compose/grafana/docker-compose.yml up -d

echo ""
echo "=== Infrastructure Started ==="
echo "MySQL:     localhost:3306"
echo "Redis:     localhost:6379"
echo "Prometheus: localhost:9090"
echo "Grafana:   localhost:3000 (admin/admin)"
