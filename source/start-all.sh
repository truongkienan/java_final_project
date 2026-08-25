#!/bin/bash

# Khoi dong 8 Spring Boot microservices theo dung thu tu phu thuoc.
# Ha tang: SQL Server (Windows Service MSSQLSERVER, cong 1433),
#          Redis + RabbitMQ (Docker container local-redis / rabbitmq-dev, cong 6379 / 5672)
# Thu tu: Catalog -> Customer -> Auth -> Inventory -> Basket -> Payment -> Order -> Frontend
#
# Luu y: build jar truoc (mvn package, chay foreground) roi chay tung service bang "java -jar",
# KHONG dung "mvn spring-boot:run" trong background - tren Windows/Git Bash, mvn thuc chat la
# file .cmd (batch script phuc tap), chay ngam (&) hay bi loi launcher (ClassNotFoundException:
# org.codehaus.plexus.classworlds.launcher.Launcher) vi MSYS2 khong gan console that cho no.
# java.exe la file thuc thi goc nen chay ngam on dinh hon nhieu.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

echo "Starting ecommerce microservices..."
echo "================================================"

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

check_port() {
    local port=$1
    if netstat -ano | grep -q ":$port .*LISTENING"; then
        echo -e "${RED}Port $port dang duoc dung${NC}"
        return 1
    fi
    return 0
}

# Khong co actuator trong project nay - kiem tra bang cach thu ket noi TCP truc tiep
# (bash ho tro pseudo-device /dev/tcp san co, khong can nc/lsof)
port_open() {
    local port=$1
    bash -c "echo >/dev/tcp/127.0.0.1/$port" >/dev/null 2>&1
}

wait_for_service() {
    local service_name=$1
    local port=$2
    local log_file=$3
    local max_attempts=60
    local attempt=1

    echo -e "${YELLOW}Cho $service_name :$port ...${NC}"

    while [ $attempt -le $max_attempts ]; do
        if port_open "$port"; then
            echo -e "${GREEN}OK $service_name${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    echo -e "${RED}Loi: $service_name khong len sau $((max_attempts * 2))s${NC}"
    if [ -n "${log_file:-}" ] && [ -f "$log_file" ]; then
        echo "Xem log: $log_file"
        tail -n 40 "$log_file" || true
    fi
    return 1
}

# Tim jar da build cua 1 module (bo qua -sources/-javadoc), roi chay ngam bang java -jar.
# Ket qua PID duoc luu vao bien global LAST_PID.
run_service() {
    local display_name=$1
    local module_dir=$2
    local port=$3
    local log_file="$LOG_DIR/ms-$module_dir.log"

    local jar
    jar=$(find "$ROOT/$module_dir/target" -maxdepth 1 -name "*.jar" \
          ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -n 1)

    if [ -z "$jar" ]; then
        echo -e "${RED}Khong tim thay jar cho $module_dir trong target/ - build that bai o buoc truoc?${NC}"
        exit 1
    fi

    echo -e "${YELLOW}${display_name} :$port${NC}"
    java -jar "$jar" >"$log_file" 2>&1 &
    LAST_PID=$!
    wait_for_service "$display_name" "$port" "$log_file"
}

echo -e "${BLUE}Kiem tra moi truong...${NC}"

if ! command -v java >/dev/null 2>&1; then
    echo -e "${RED}Chua cai Java${NC}"
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo -e "${RED}Chua cai Maven${NC}"
    exit 1
fi

# "./start-all.sh" chay bash o che do non-interactive - khong tu source ~/.bashrc,
# nen PATH co the resolve nham sang 1 ban mvn khac ban dang dung khi go tay trong
# terminal tuong tac. Uu tien duong dan tuyet doi da xac nhan dung tren may nay
# (tu "mvn -version": Maven home: C:\Program Files\apache-maven-3.9.16), fallback
# ve PATH-based "mvn" neu may khac khong co duong dan nay.
if [ -x "/c/Program Files/apache-maven-3.9.16/bin/mvn" ]; then
    MVN_CMD="/c/Program Files/apache-maven-3.9.16/bin/mvn"
else
    MVN_CMD="mvn"
fi
echo -e "${BLUE}Dung Maven tai: $(command -v "$MVN_CMD" 2>/dev/null || echo "$MVN_CMD")${NC}"

echo -e "${BLUE}Kiem tra SQL Server (Windows Service MSSQLSERVER, cong 1433)...${NC}"
if port_open 1433; then
    echo -e "${GREEN}SQL Server dang chay${NC}"
else
    echo -e "${RED}SQL Server chua chay!${NC}"
    echo "Mo Services.msc, bat MSSQLSERVER, roi chay lai ./start-all.sh"
    exit 1
fi

echo -e "${BLUE}Kiem tra Redis + RabbitMQ (Docker container)...${NC}"
docker start local-redis rabbitmq-dev >/dev/null 2>&1 || true
if port_open 6379 && port_open 5672; then
    echo -e "${GREEN}Redis (:6379) + RabbitMQ (:5672) dang chay${NC}"
else
    echo -e "${RED}Redis hoac RabbitMQ chua san sang. Kiem tra Docker Desktop / container local-redis, rabbitmq-dev.${NC}"
    exit 1
fi

echo -e "${GREEN}OK moi truong${NC}"

echo -e "${BLUE}Kiem tra port Spring Boot...${NC}"
for port in 8080 8081 8082 8083 8084 8085 8086 8888; do
    if ! check_port "$port"; then
        echo "Tat process dang giu port $port (hoac ./stop-all.sh) roi chay lai"
        exit 1
    fi
done
echo -e "${GREEN}OK port 8080-8086, 8888 trong${NC}"

echo -e "${BLUE}Build toan bo 8 service (mvn clean package, co the mat vai phut lan dau)...${NC}"
"$MVN_CMD" clean package -DskipTests
echo -e "${GREEN}Build xong${NC}"

LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"
echo -e "${BLUE}Start service (log: $LOG_DIR/ms-*.log)...${NC}"

run_service "Catalog" "catalog-service" 8081
CATALOG_PID=$LAST_PID

run_service "Customer" "customer-service" 8082
CUSTOMER_PID=$LAST_PID

run_service "Auth" "auth-service" 8085
AUTH_PID=$LAST_PID

run_service "Inventory" "inventory-service" 8086
INVENTORY_PID=$LAST_PID

run_service "Basket" "basket-service" 8083
BASKET_PID=$LAST_PID

run_service "Payment" "payment-service" 8080
PAYMENT_PID=$LAST_PID

run_service "Order" "order-service" 8084
ORDER_PID=$LAST_PID

run_service "Frontend" "frontend-service" 8888
FRONTEND_PID=$LAST_PID

{
    echo "CATALOG_PID=$CATALOG_PID"
    echo "CUSTOMER_PID=$CUSTOMER_PID"
    echo "AUTH_PID=$AUTH_PID"
    echo "INVENTORY_PID=$INVENTORY_PID"
    echo "BASKET_PID=$BASKET_PID"
    echo "PAYMENT_PID=$PAYMENT_PID"
    echo "ORDER_PID=$ORDER_PID"
    echo "FRONTEND_PID=$FRONTEND_PID"
} > "$ROOT/.pids"

echo "================================================"
echo -e "${GREEN}Da start 8 service${NC}"
echo ""
echo "Frontend (trang khach hang + admin): http://localhost:8888"
echo "Catalog:    http://localhost:8081"
echo "Customer:   http://localhost:8082"
echo "Basket:     http://localhost:8083"
echo "Order:      http://localhost:8084"
echo "Auth:       http://localhost:8085"
echo "Inventory:  http://localhost:8086"
echo "Payment:    http://localhost:8080"
echo ""
echo "Log:"
echo "  $LOG_DIR/ms-catalog-service.log"
echo "  $LOG_DIR/ms-customer-service.log"
echo "  $LOG_DIR/ms-auth-service.log"
echo "  $LOG_DIR/ms-inventory-service.log"
echo "  $LOG_DIR/ms-basket-service.log"
echo "  $LOG_DIR/ms-payment-service.log"
echo "  $LOG_DIR/ms-order-service.log"
echo "  $LOG_DIR/ms-frontend-service.log"
echo "Stop: ./stop-all.sh  (khong dung SQL Server / Redis / RabbitMQ)"
echo "================================================"
