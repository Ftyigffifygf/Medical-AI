#!/bin/bash

# Medical AI System Startup Script
set -e

echo "🚀 Starting Medical AI Intelligence System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if build script has been run
if ! docker images | grep -q "medical-ai-java"; then
    print_warning "Docker images not found. Running build script first..."
    chmod +x build.sh
    ./build.sh
fi

# Start services
print_status "Starting services with Docker Compose..."

# Use docker-compose or docker compose based on availability
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    COMPOSE_CMD="docker compose"
fi

# Start the services
$COMPOSE_CMD up -d

# Wait for services to be ready
print_status "Waiting for services to start..."
sleep 10

# Check service health
print_status "Checking service health..."

check_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            print_success "$service_name is healthy"
            return 0
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            print_warning "$service_name is not responding after $max_attempts attempts"
            return 1
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
}

# Check individual services
check_service "Database" "http://localhost:5432" || true
check_service "Redis" "http://localhost:6379" || true
check_service "JavaScript Service" "http://localhost:3000/health"
check_service "Python Service" "http://localhost:8000/health"
check_service "Java Service" "http://localhost:8080/health" || true
check_service "Frontend" "http://localhost:3001/health" || true
check_service "Load Balancer" "http://localhost:80/health"

print_success "Medical AI System is starting up!"
echo ""
echo "🌐 Access the system at:"
echo "   Frontend: http://localhost:3001"
echo "   Load Balancer: http://localhost"
echo "   JavaScript API: http://localhost:3000"
echo "   Python API: http://localhost:8000"
echo "   Java API: http://localhost:8080"
echo ""
echo "📊 Monitor services with:"
echo "   docker-compose logs -f [service-name]"
echo "   docker-compose ps"
echo ""
echo "🛑 Stop the system with:"
echo "   docker-compose down"