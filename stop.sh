#!/bin/bash

# Medical AI System Stop Script
set -e

echo "🛑 Stopping Medical AI Intelligence System..."

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

# Use docker-compose or docker compose based on availability
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    COMPOSE_CMD="docker compose"
fi

# Stop services
print_status "Stopping all services..."
$COMPOSE_CMD down

# Option to remove volumes (data)
read -p "Do you want to remove all data volumes? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Removing all data volumes..."
    $COMPOSE_CMD down -v
    docker volume prune -f
    print_success "All data volumes removed"
fi

# Option to remove images
read -p "Do you want to remove Docker images? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Removing Docker images..."
    docker rmi medical-ai-java medical-ai-python medical-ai-js medical-ai-cpp medical-ai-frontend 2>/dev/null || true
    print_success "Docker images removed"
fi

print_success "Medical AI System stopped successfully!"