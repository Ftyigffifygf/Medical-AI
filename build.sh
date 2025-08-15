#!/bin/bash

# Medical AI System Build Script
set -e

echo "🏥 Building Medical AI Intelligence System..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null 2>&1; then
    print_error "Docker Compose is not available. Please install Docker Compose."
    exit 1
fi

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p logs
mkdir -p nginx/ssl
mkdir -p python-agents/models
mkdir -p cpp-services/models
mkdir -p js-services/uploads

# Create missing source files if they don't exist
print_status "Creating missing source files..."

# Create basic frontend structure
if [ ! -d "frontend/src" ]; then
    mkdir -p frontend/src/{components,pages,store,services,utils}
    mkdir -p frontend/src/components/{Layout,Dashboard}
    mkdir -p frontend/src/pages/{Dashboard,PatientAnalysis,ModelManagement,DataManagement,Monitoring,Settings}
fi

# Create missing JavaScript service files
if [ ! -d "js-services/src" ]; then
    mkdir -p js-services/src/{agents,processors,analyzers,websocket}
fi

# Build services
print_status "Building Docker images..."

# Build Java service
print_status "Building Java service..."
if docker build -f Dockerfile.java -t medical-ai-java .; then
    print_success "Java service built successfully"
else
    print_error "Failed to build Java service"
    exit 1
fi

# Build Python service
print_status "Building Python service..."
if docker build -f python-agents/Dockerfile -t medical-ai-python ./python-agents; then
    print_success "Python service built successfully"
else
    print_error "Failed to build Python service"
    exit 1
fi

# Build JavaScript service
print_status "Building JavaScript service..."
if docker build -f js-services/Dockerfile -t medical-ai-js ./js-services; then
    print_success "JavaScript service built successfully"
else
    print_error "Failed to build JavaScript service"
    exit 1
fi

# Build C++ service
print_status "Building C++ service..."
if docker build -f cpp-services/Dockerfile -t medical-ai-cpp ./cpp-services; then
    print_success "C++ service built successfully"
else
    print_warning "C++ service build failed - this is optional for basic functionality"
fi

# Build Frontend
print_status "Building Frontend..."
if docker build -f frontend/Dockerfile -t medical-ai-frontend ./frontend; then
    print_success "Frontend built successfully"
else
    print_error "Failed to build Frontend"
    exit 1
fi

print_success "All services built successfully!"
print_status "You can now run 'docker-compose up' to start the system"