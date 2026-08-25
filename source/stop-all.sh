#!/bin/bash

# Dung 8 Spring Boot microservices (giu nguyen SQL Server / Redis / RabbitMQ).
# Khong co lsof tren Git Bash/Windows nen dung netstat + taskkill thay the.

set -e
export MSYS_NO_PATHCONV=1

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "Stopping ecommerce microservices..."
echo "================================================"

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

stop_by_port() {
    local service_name=$1
    local port=$2

    echo -e "${YELLOW}Stop $service_name :$port ...${NC}"

    local pids
    pids=$(netstat -ano | grep ":$port " | grep "LISTENING" | awk '{print $NF}' | sort -u || true)

    if [ -n "$pids" ]; then
        for pid in $pids; do
            taskkill /F /PID "$pid" >/dev/null 2>&1 || true
        done
        echo -e "${GREEN}OK $service_name${NC}"
    else
        echo -e "${BLUE}$service_name khong chay${NC}"
    fi
}

stop_by_port "Frontend" 8888
stop_by_port "Order Service" 8084
stop_by_port "Payment Service" 8080
stop_by_port "Basket Service" 8083
stop_by_port "Inventory Service" 8086
stop_by_port "Auth Service" 8085
stop_by_port "Customer Service" 8082
stop_by_port "Catalog Service" 8081

if [ -f ".pids" ]; then
    rm -f .pids
fi

echo "================================================"
echo -e "${GREEN}Da tat 8 service Spring Boot${NC}"
echo "SQL Server / Redis / RabbitMQ giu nguyen - khong bi tat."
echo "Start lai: ./start-all.sh"
echo "================================================"
