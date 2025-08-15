/**
 * Medical AI Web Service
 * Node.js/Express server with real-time WebSocket communication
 * Handles web interface, file uploads, and real-time updates
 */

const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const morgan = require('morgan');
const multer = require('multer');
const path = require('path');
require('dotenv').config();

const MedicalAIAgent = require('./agents/medical-ai-agent');
const DocumentProcessor = require('./processors/document-processor');
const ImageProcessor = require('./processors/image-processor');
const RealtimeAnalyzer = require('./analyzers/realtime-analyzer');
const WebSocketHandler = require('./websocket/websocket-handler');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: process.env.FRONTEND_URL || "http://localhost:3001",
        methods: ["GET", "POST"]
    }
});

// Initialize services
const medicalAIAgent = new MedicalAIAgent();
const documentProcessor = new DocumentProcessor();
const imageProcessor = new ImageProcessor();
const realtimeAnalyzer = new RealtimeAnalyzer();
const wsHandler = new WebSocketHandler(io);

// Middleware
app.use(helmet());
app.use(compression());
app.use(morgan('combined'));
app.use(cors({
    origin: process.env.FRONTEND_URL || "http://localhost:3001",
    credentials: true
}));
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// File upload configuration
const storage = multer.memoryStorage();
const upload = multer({
    storage: storage,
    limits: {
        fileSize: 100 * 1024 * 1024 // 100MB limit
    },
    fileFilter: (req, file, cb) => {
        // Allow medical file types
        const allowedTypes = [
            'image/jpeg', 'image/png', 'image/dicom',
            'application/pdf', 'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'text/plain', 'application/json'
        ];

        if (allowedTypes.includes(file.mimetype)) {
            cb(null, true);
        } else {
            cb(new Error('Invalid file type for medical analysis'), false);
        }
    }
});

// Routes

/**
 * Health check endpoint
 */
app.get('/health', (req, res) => {
    res.json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        services: {
            'medical-ai': medicalAIAgent.isHealthy(),
            'document-processor': documentProcessor.isHealthy(),
            'image-processor': imageProcessor.isHealthy(),
            'realtime-analyzer': realtimeAnalyzer.isHealthy()
        },
        uptime: process.uptime()
    });
});

/**
 * Medical analysis endpoint
 */
app.post('/api/medical/analyze', async (req, res) => {
    try {
        const { patientData, analysisType = 'complete', priority = 'normal' } = req.body;

        if (!patientData) {
            return res.status(400).json({ error: 'Patient data is required' });
        }

        // Start analysis
        const analysisId = `js_analysis_${Date.now()}`;

        // Emit real-time update
        wsHandler.emitAnalysisStarted(analysisId, patientData.patient_id);

        // Perform analysis using AI agent
        const result = await medicalAIAgent.analyzePatient(patientData, {
            analysisType,
            priority,
            analysisId
        });

        // Emit completion
        wsHandler.emitAnalysisCompleted(analysisId, result);

        res.json({
            analysisId,
            status: 'completed',
            result,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error('Medical analysis failed:', error);
        res.status(500).json({
            error: 'Analysis failed',
            message: error.message,
            timestamp: new Date().toISOString()
        });
    }
});

/**
 * Document upload and processing
 */
app.post('/api/documents/upload', upload.single('document'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No document uploaded' });
        }

        const { patientId, documentType = 'medical_record' } = req.body;

        // Process document
        const processingResult = await documentProcessor.processDocument({
            buffer: req.file.buffer,
            mimetype: req.file.mimetype,
            originalName: req.file.originalname,
            patientId,
            documentType
        });

        // Extract medical information using AI
        const extractedData = await medicalAIAgent.extractMedicalInfo(processingResult.text);

        res.json({
            documentId: processingResult.documentId,
            extractedData,
            processingTime: processingResult.processingTime,
            confidence: extractedData.confidence,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error('Document processing failed:', error);
        res.status(500).json({
            error: 'Document processing failed',
            message: error.message
        });
    }
});

/**
 * Medical image upload and analysis
 */
app.post('/api/images/analyze', upload.single('image'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No image uploaded' });
        }

        const { patientId, imageType = 'xray', symptoms = [] } = req.body;

        // Process image
        const processedImage = await imageProcessor.processImage({
            buffer: req.file.buffer,
            mimetype: req.file.mimetype,
            patientId,
            imageType
        });

        // Analyze with AI
        const analysisResult = await medicalAIAgent.analyzeImage(processedImage, {
            symptoms: Array.isArray(symptoms) ? symptoms : symptoms.split(','),
            imageType
        });

        res.json({
            imageId: processedImage.imageId,
            analysis: analysisResult,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error('Image analysis failed:', error);
        res.status(500).json({
            error: 'Image analysis failed',
            message: error.message
        });
    }
});

/**
 * Real-time symptom analysis
 */
app.post('/api/symptoms/analyze', async (req, res) => {
    try {
        const { symptoms, patientContext = {} } = req.body;

        if (!symptoms || !Array.isArray(symptoms)) {
            return res.status(400).json({ error: 'Symptoms array is required' });
        }

        // Real-time symptom analysis
        const analysis = await realtimeAnalyzer.analyzeSymptoms(symptoms, patientContext);

        res.json({
            analysis,
            urgencyLevel: analysis.urgencyLevel,
            recommendations: analysis.recommendations,
            confidence: analysis.confidence,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error('Symptom analysis failed:', error);
        res.status(500).json({
            error: 'Symptom analysis failed',
            message: error.message
        });
    }
});

/**
 * Forward requests to Python orchestrator
 */
app.post('/api/forward/python', async (req, res) => {
    try {
        const axios = require('axios');

        const response = await axios.post(
            `${process.env.PYTHON_SERVICE_URL || 'http://localhost:8000'}/analyze`,
            req.body,
            {
                timeout: 60000,
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        );

        res.json(response.data);

    } catch (error) {
        console.error('Python service forwarding failed:', error);
        res.status(503).json({
            error: 'Python service unavailable',
            message: error.message
        });
    }
});

/**
 * Forward requests to Java services
 */
app.post('/api/forward/java', async (req, res) => {
    try {
        const axios = require('axios');

        const response = await axios.post(
            `${process.env.JAVA_SERVICE_URL || 'http://localhost:8080'}/api/medical/analyze`,
            req.body,
            {
                timeout: 60000,
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        );

        res.json(response.data);

    } catch (error) {
        console.error('Java service forwarding failed:', error);
        res.status(503).json({
            error: 'Java service unavailable',
            message: error.message
        });
    }
});

/**
 * Get available AI models
 */
app.get('/api/models', async (req, res) => {
    try {
        const models = await medicalAIAgent.getAvailableModels();
        res.json(models);
    } catch (error) {
        console.error('Failed to get models:', error);
        res.status(500).json({
            error: 'Failed to retrieve models',
            message: error.message
        });
    }
});

// WebSocket connection handling
io.on('connection', (socket) => {
    console.log('Client connected:', socket.id);

    wsHandler.handleConnection(socket);

    socket.on('disconnect', () => {
        console.log('Client disconnected:', socket.id);
        wsHandler.handleDisconnection(socket);
    });
});

// Error handling middleware
app.use((error, req, res, next) => {
    console.error('Unhandled error:', error);

    if (error instanceof multer.MulterError) {
        if (error.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).json({
                error: 'File too large',
                message: 'Maximum file size is 100MB'
            });
        }
    }

    res.status(500).json({
        error: 'Internal server error',
        message: process.env.NODE_ENV === 'development' ? error.message : 'Something went wrong'
    });
});

// 404 handler
app.use('*', (req, res) => {
    res.status(404).json({
        error: 'Endpoint not found',
        path: req.originalUrl
    });
});

// Start server
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
    console.log(`Medical AI Web Service listening on port ${PORT}`);
    console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);

    // Initialize services
    medicalAIAgent.initialize();
    documentProcessor.initialize();
    imageProcessor.initialize();
    realtimeAnalyzer.initialize();

    console.log('All services initialized successfully');
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('SIGTERM received, shutting down gracefully');
    server.close(() => {
        console.log('Server closed');
        process.exit(0);
    });
});

module.exports = app;