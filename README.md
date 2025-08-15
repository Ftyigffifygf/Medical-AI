# Medical AI Intelligence System

A comprehensive, multi-language medical AI platform that provides advanced healthcare AI capabilities through integrated Java, Python, JavaScript, and C++ services.

## 🏥 Features

- **Multi-Agent AI System**: Coordinated AI agents for different medical specialties
- **Real-time Analysis**: Live patient data analysis and monitoring
- **Multi-Modal Processing**: Support for text, images, and structured medical data
- **Scalable Architecture**: Microservices-based design with Docker containerization
- **Web Interface**: Modern React-based dashboard for healthcare professionals
- **API Gateway**: RESTful APIs with real-time WebSocket communication
- **Security**: HIPAA-compliant data handling and encryption

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Git
- 8GB+ RAM recommended
- 10GB+ free disk space

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd medical-ai-system
   ```

2. **Make scripts executable**
   ```bash
   chmod +x build.sh start.sh stop.sh
   ```

3. **Build the system**
   ```bash
   ./build.sh
   ```

4. **Start the system**
   ```bash
   ./start.sh
   ```

5. **Access the application**
   - Frontend: http://localhost:3001
   - Load Balancer: http://localhost
   - JavaScript API: http://localhost:3000
   - Python API: http://localhost:8000
   - Java API: http://localhost:8080

### Default Login
- Email: `admin@medical-ai.com`
- Password: `admin123`

## 🏗️ Architecture

### Services

| Service | Technology | Port | Purpose |
|---------|------------|------|---------|
| Frontend | React + Material-UI | 3001 | Web interface |
| JavaScript Service | Node.js + Express | 3000 | Web API & real-time communication |
| Python Service | FastAPI + LangChain | 8000 | AI orchestration & ML models |
| Java Service | Spring Boot | 8080 | Enterprise services & FHIR |
| C++ Service | gRPC + OpenCV | 50051 | High-performance image processing |
| PostgreSQL | Database | 5432 | Primary data storage |
| Redis | Cache/Queue | 6379 | Caching & message queuing |
| Nginx | Load Balancer | 80/443 | Reverse proxy & load balancing |

### Data Flow

```
Frontend → Nginx → JavaScript Service → Python Orchestrator
                                     ↓
                              Java Service ← → Database
                                     ↓
                              C++ Service (Images)
```

## 📋 Usage

### Patient Analysis

1. Navigate to "Patient Analysis" in the web interface
2. Enter patient information:
   - Patient ID and name (required)
   - Age, gender, symptoms
   - Medical history, allergies, medications
3. Click "Analyze Patient" to start AI analysis
4. View results including:
   - Primary diagnosis with confidence score
   - Differential diagnoses
   - Treatment recommendations
   - Risk assessment

### Document Processing

Upload medical documents (PDF, DOCX, images) through the API:

```bash
curl -X POST http://localhost:3000/api/documents/upload \
  -F "document=@medical_report.pdf" \
  -F "patientId=PAT001" \
  -F "documentType=medical_record"
```

### Image Analysis

Analyze medical images:

```bash
curl -X POST http://localhost:3000/api/images/analyze \
  -F "image=@xray.jpg" \
  -F "patientId=PAT001" \
  -F "imageType=xray" \
  -F "symptoms=chest pain,cough"
```

### Real-time Monitoring

Connect to WebSocket for real-time updates:

```javascript
const socket = io('http://localhost:3000');

socket.on('analysis-started', (data) => {
  console.log('Analysis started:', data);
});

socket.on('analysis-completed', (data) => {
  console.log('Analysis completed:', data);
});
```

## 🔧 Configuration

### Environment Variables

Create `.env` files in each service directory:

**JavaScript Service (.env)**
```
NODE_ENV=production
REDIS_URL=redis://redis:6379
DATABASE_URL=postgresql://medical_user:medical_pass@postgres:5432/medical_ai
PYTHON_SERVICE_URL=http://python-service:8000
JAVA_SERVICE_URL=http://java-service:8080
```

**Python Service (.env)**
```
REDIS_URL=redis://redis:6379
DATABASE_URL=postgresql://medical_user:medical_pass@postgres:5432/medical_ai
OPENAI_API_KEY=your_openai_key_here
ANTHROPIC_API_KEY=your_anthropic_key_here
```

### Database Configuration

The system uses PostgreSQL with the following default settings:
- Database: `medical_ai`
- Username: `medical_user`
- Password: `medical_pass`

## 🛠️ Development

### Running Individual Services

**JavaScript Service**
```bash
cd js-services
npm install
npm run dev
```

**Python Service**
```bash
cd python-agents
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

**Java Service**
```bash
mvn spring-boot:run
```

**Frontend**
```bash
cd frontend
npm install
npm start
```

### Adding New AI Models

1. Add model files to the appropriate service's `models/` directory
2. Update the model registry in the service configuration
3. Implement model-specific processing logic
4. Update API documentation

### Custom Medical Agents

Create new medical agents by extending the base agent classes:

```python
# Python Agent Example
from medical_agents import BaseMedicalAgent

class CustomDiagnosisAgent(BaseMedicalAgent):
    def __init__(self):
        super().__init__("CustomDiagnosis", "1.0.0")
    
    async def analyze(self, patient_data):
        # Custom analysis logic
        return analysis_result
```

## 📊 Monitoring

### Health Checks

- System health: `GET /health`
- Service status: `GET /api/health`
- Database status: Check PostgreSQL connection
- Redis status: Check Redis connection

### Logs

View service logs:
```bash
docker-compose logs -f [service-name]
```

### Metrics

The system provides metrics for:
- Request latency and throughput
- Model inference times
- Error rates
- Resource utilization

## 🔒 Security

### Data Protection

- All patient data is encrypted at rest and in transit
- HIPAA-compliant data handling procedures
- Audit logging for all data access
- Role-based access control (RBAC)

### API Security

- JWT-based authentication
- Rate limiting on all endpoints
- Input validation and sanitization
- CORS protection

### Network Security

- TLS/SSL encryption for all communications
- Network isolation between services
- Firewall rules for port access
- Security headers in HTTP responses

## 🧪 Testing

### Running Tests

**JavaScript Tests**
```bash
cd js-services
npm test
```

**Python Tests**
```bash
cd python-agents
pytest
```

**Java Tests**
```bash
mvn test
```

### Load Testing

Test system performance:
```bash
# Install Artillery
npm install -g artillery

# Run load test
artillery run load-test.yml
```

## 🚨 Troubleshooting

### Common Issues

**Services not starting**
- Check Docker is running: `docker info`
- Verify ports are available: `netstat -tulpn`
- Check logs: `docker-compose logs [service]`

**Database connection errors**
- Ensure PostgreSQL is running: `docker-compose ps postgres`
- Check database credentials in environment variables
- Verify network connectivity between services

**Memory issues**
- Increase Docker memory allocation (8GB+ recommended)
- Monitor resource usage: `docker stats`
- Consider scaling down services for development

**AI Model errors**
- Verify model files are present in `models/` directories
- Check API keys for external AI services
- Review model configuration files

### Performance Optimization

**Database**
- Add indexes for frequently queried columns
- Use connection pooling
- Optimize query performance

**Caching**
- Enable Redis caching for frequent requests
- Cache model predictions
- Use CDN for static assets

**Scaling**
- Increase service replicas: `docker-compose up --scale python-service=3`
- Use horizontal pod autoscaling in Kubernetes
- Implement load balancing strategies

## 📚 API Documentation

### REST API Endpoints

**Patient Analysis**
- `POST /python/analyze` - Comprehensive patient analysis
- `GET /python/models/available` - List available AI models
- `POST /python/models/route` - Route request to best model

**Document Processing**
- `POST /api/documents/upload` - Upload and process medical documents
- `GET /api/documents/{id}` - Retrieve processed document
- `DELETE /api/documents/{id}` - Delete document

**Image Analysis**
- `POST /api/images/analyze` - Analyze medical images
- `GET /api/images/{id}` - Retrieve image analysis results
- `POST /api/images/enhance` - Enhance image quality

**Real-time Communication**
- WebSocket endpoint: `ws://localhost:3000`
- Events: `analysis-started`, `analysis-progress`, `analysis-completed`

### GraphQL API

Access GraphQL playground at `http://localhost:8080/graphql`

Example query:
```graphql
query GetPatientAnalysis($patientId: String!) {
  patient(id: $patientId) {
    id
    name
    analyses {
      id
      type
      results
      confidence
      createdAt
    }
  }
}
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Make changes and add tests
4. Commit changes: `git commit -am 'Add new feature'`
5. Push to branch: `git push origin feature/new-feature`
6. Submit a pull request

### Code Standards

- Follow language-specific style guides
- Add comprehensive tests for new features
- Update documentation for API changes
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue on GitHub
- Check the troubleshooting section
- Review the API documentation
- Contact the development team

## 🔄 Updates

To update the system:

1. Pull latest changes: `git pull origin main`
2. Rebuild services: `./build.sh`
3. Restart system: `./stop.sh && ./start.sh`

## 🎯 Roadmap

- [ ] Advanced ML model integration
- [ ] Mobile application support
- [ ] Enhanced security features
- [ ] Multi-tenant architecture
- [ ] Advanced analytics dashboard
- [ ] Integration with EHR systems
- [ ] Voice-to-text capabilities
- [ ] Telemedicine features

---

**⚠️ Important**: This system is for educational and development purposes. For production medical use, ensure compliance with all relevant healthcare regulations and obtain proper certifications.