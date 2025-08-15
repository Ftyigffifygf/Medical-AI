#!/bin/bash

# Medical AI System Deployment Script
set -e

echo "🚀 Deploying Medical AI Intelligence System..."

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

# Check deployment environment
ENVIRONMENT=${1:-"local"}

print_status "Deploying to environment: $ENVIRONMENT"

case $ENVIRONMENT in
    "local")
        print_status "Setting up local development environment..."
        
        # Make scripts executable
        chmod +x build.sh start.sh stop.sh
        
        # Create environment files
        create_env_files
        
        # Build and start services
        ./build.sh
        ./start.sh
        ;;
        
    "staging")
        print_status "Setting up staging environment..."
        deploy_staging
        ;;
        
    "production")
        print_status "Setting up production environment..."
        deploy_production
        ;;
        
    *)
        print_error "Unknown environment: $ENVIRONMENT"
        print_status "Usage: ./deploy.sh [local|staging|production]"
        exit 1
        ;;
esac

create_env_files() {
    print_status "Creating environment configuration files..."
    
    # JavaScript Service .env
    if [ ! -f "js-services/.env" ]; then
        cat > js-services/.env << EOF
NODE_ENV=development
PORT=3000
REDIS_URL=redis://localhost:6379
DATABASE_URL=postgresql://medical_user:medical_pass@localhost:5432/medical_ai
PYTHON_SERVICE_URL=http://localhost:8000
JAVA_SERVICE_URL=http://localhost:8080
FRONTEND_URL=http://localhost:3001
JWT_SECRET=your-jwt-secret-here
UPLOAD_MAX_SIZE=100MB
EOF
        print_success "Created js-services/.env"
    fi
    
    # Python Service .env
    if [ ! -f "python-agents/.env" ]; then
        cat > python-agents/.env << EOF
ENVIRONMENT=development
REDIS_URL=redis://localhost:6379
DATABASE_URL=postgresql://medical_user:medical_pass@localhost:5432/medical_ai
JAVA_SERVICE_URL=http://localhost:8080
JS_SERVICE_URL=http://localhost:3000
CPP_SERVICE_URL=localhost:50051
OPENAI_API_KEY=your-openai-key-here
ANTHROPIC_API_KEY=your-anthropic-key-here
OLLAMA_BASE_URL=http://localhost:11434
LOG_LEVEL=INFO
EOF
        print_success "Created python-agents/.env"
    fi
    
    # Frontend .env
    if [ ! -f "frontend/.env" ]; then
        cat > frontend/.env << EOF
REACT_APP_API_URL=http://localhost:3000
REACT_APP_PYTHON_API_URL=http://localhost:8000
REACT_APP_JAVA_API_URL=http://localhost:8080
REACT_APP_WS_URL=ws://localhost:3000
GENERATE_SOURCEMAP=false
EOF
        print_success "Created frontend/.env"
    fi
}

deploy_staging() {
    print_status "Configuring staging environment..."
    
    # Update environment variables for staging
    export NODE_ENV=staging
    export ENVIRONMENT=staging
    
    # Build with staging configuration
    ./build.sh
    
    # Deploy to staging infrastructure
    print_status "Deploying to staging infrastructure..."
    
    # Use docker-compose with staging override
    if [ -f "docker-compose.staging.yml" ]; then
        docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d
    else
        docker-compose up -d
    fi
    
    print_success "Staging deployment completed"
}

deploy_production() {
    print_warning "Production deployment requires additional security measures"
    
    # Verify production readiness
    check_production_readiness
    
    print_status "Configuring production environment..."
    
    # Update environment variables for production
    export NODE_ENV=production
    export ENVIRONMENT=production
    
    # Build with production configuration
    ./build.sh
    
    # Deploy to production infrastructure
    print_status "Deploying to production infrastructure..."
    
    # Use docker-compose with production override
    if [ -f "docker-compose.production.yml" ]; then
        docker-compose -f docker-compose.yml -f docker-compose.production.yml up -d
    else
        print_warning "No production override found, using default configuration"
        docker-compose up -d
    fi
    
    # Run production health checks
    run_health_checks
    
    print_success "Production deployment completed"
}

check_production_readiness() {
    print_status "Checking production readiness..."
    
    local ready=true
    
    # Check for required environment variables
    if [ -z "$DATABASE_PASSWORD" ]; then
        print_error "DATABASE_PASSWORD not set"
        ready=false
    fi
    
    if [ -z "$JWT_SECRET" ]; then
        print_error "JWT_SECRET not set"
        ready=false
    fi
    
    if [ -z "$OPENAI_API_KEY" ]; then
        print_warning "OPENAI_API_KEY not set - some AI features may not work"
    fi
    
    # Check SSL certificates
    if [ ! -f "nginx/ssl/cert.pem" ] || [ ! -f "nginx/ssl/key.pem" ]; then
        print_warning "SSL certificates not found - HTTPS will not be available"
    fi
    
    if [ "$ready" = false ]; then
        print_error "Production readiness check failed"
        exit 1
    fi
    
    print_success "Production readiness check passed"
}

run_health_checks() {
    print_status "Running health checks..."
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        print_status "Health check attempt $attempt/$max_attempts"
        
        # Check main services
        if curl -s -f "http://localhost/health" > /dev/null 2>&1; then
            print_success "Load balancer is healthy"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            print_error "Health checks failed after $max_attempts attempts"
            exit 1
        fi
        
        sleep 10
        attempt=$((attempt + 1))
    done
    
    print_success "All health checks passed"
}

# Cleanup function
cleanup() {
    print_status "Cleaning up temporary files..."
    # Add cleanup logic here if needed
}

# Set trap for cleanup on exit
trap cleanup EXIT

print_success "Deployment completed successfully!"

if [ "$ENVIRONMENT" = "local" ]; then
    echo ""
    echo "🌐 Access the system at:"
    echo "   Frontend: http://localhost:3001"
    echo "   Load Balancer: http://localhost"
    echo "   JavaScript API: http://localhost:3000"
    echo "   Python API: http://localhost:8000"
    echo "   Java API: http://localhost:8080"
    echo ""
    echo "📊 Monitor with:"
    echo "   docker-compose logs -f"
    echo "   docker-compose ps"
    echo ""
    echo "🛑 Stop with:"
    echo "   ./stop.sh"
fi