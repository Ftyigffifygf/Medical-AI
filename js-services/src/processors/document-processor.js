/**
 * Document Processor
 * Handles medical document processing and text extraction
 */

const pdf = require('pdf-parse');
const mammoth = require('mammoth');

class DocumentProcessor {
    constructor() {
        this.isInitialized = false;
        this.supportedTypes = [
            'application/pdf',
            'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'text/plain'
        ];
    }

    async initialize() {
        console.log('Initializing Document Processor...');
        this.isInitialized = true;
        console.log('Document Processor initialized successfully');
    }

    isHealthy() {
        return this.isInitialized;
    }

    async processDocument(documentData) {
        const { buffer, mimetype, originalName, patientId, documentType } = documentData;

        console.log(`Processing document: ${originalName} (${mimetype})`);

        let extractedText = '';

        try {
            switch (mimetype) {
                case 'application/pdf':
                    extractedText = await this.processPDF(buffer);
                    break;
                case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
                    extractedText = await this.processDocx(buffer);
                    break;
                case 'text/plain':
                    extractedText = buffer.toString('utf-8');
                    break;
                default:
                    throw new Error(`Unsupported document type: ${mimetype}`);
            }

            const documentId = `doc_${patientId}_${Date.now()}`;

            return {
                documentId,
                text: extractedText,
                originalName,
                mimetype,
                patientId,
                documentType,
                processingTime: 1.2,
                wordCount: extractedText.split(' ').length,
                timestamp: new Date().toISOString()
            };

        } catch (error) {
            console.error('Document processing failed:', error);
            throw new Error(`Document processing failed: ${error.message}`);
        }
    }

    async processPDF(buffer) {
        try {
            const data = await pdf(buffer);
            return data.text;
        } catch (error) {
            throw new Error(`PDF processing failed: ${error.message}`);
        }
    }

    async processDocx(buffer) {
        try {
            const result = await mammoth.extractRawText({ buffer });
            return result.value;
        } catch (error) {
            throw new Error(`DOCX processing failed: ${error.message}`);
        }
    }

    validateDocument(mimetype, size) {
        if (!this.supportedTypes.includes(mimetype)) {
            throw new Error(`Unsupported document type: ${mimetype}`);
        }

        const maxSize = 50 * 1024 * 1024; // 50MB
        if (size > maxSize) {
            throw new Error('Document size exceeds maximum limit (50MB)');
        }

        return true;
    }
}

module.exports = DocumentProcessor;