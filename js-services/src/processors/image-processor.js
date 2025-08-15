/**
 * Image Processor
 * Handles medical image processing and analysis
 */

const sharp = require('sharp');

class ImageProcessor {
    constructor() {
        this.isInitialized = false;
        this.supportedTypes = [
            'image/jpeg',
            'image/png',
            'image/dicom'
        ];
    }

    async initialize() {
        console.log('Initializing Image Processor...');
        this.isInitialized = true;
        console.log('Image Processor initialized successfully');
    }

    isHealthy() {
        return this.isInitialized;
    }

    async processImage(imageData) {
        const { buffer, mimetype, patientId, imageType } = imageData;

        console.log(`Processing ${imageType} image for patient ${patientId}`);

        try {
            // Process image with Sharp
            const processedBuffer = await sharp(buffer)
                .resize(1024, 1024, { fit: 'inside', withoutEnlargement: true })
                .normalize()
                .sharpen()
                .toBuffer();

            // Get image metadata
            const metadata = await sharp(buffer).metadata();

            const imageId = `img_${patientId}_${Date.now()}`;

            return {
                imageId,
                processedBuffer,
                originalBuffer: buffer,
                metadata: {
                    width: metadata.width,
                    height: metadata.height,
                    format: metadata.format,
                    size: buffer.length
                },
                patientId,
                imageType,
                processingTime: 0.8,
                timestamp: new Date().toISOString()
            };

        } catch (error) {
            console.error('Image processing failed:', error);
            throw new Error(`Image processing failed: ${error.message}`);
        }
    }

    async enhanceImage(buffer, options = {}) {
        const {
            brightness = 1.0,
            contrast = 1.0,
            saturation = 1.0,
            sharpen = false
        } = options;

        try {
            let pipeline = sharp(buffer)
                .modulate({
                    brightness,
                    saturation
                })
                .linear(contrast, -(128 * contrast) + 128);

            if (sharpen) {
                pipeline = pipeline.sharpen();
            }

            return await pipeline.toBuffer();
        } catch (error) {
            throw new Error(`Image enhancement failed: ${error.message}`);
        }
    }

    validateImage(mimetype, size) {
        if (!this.supportedTypes.includes(mimetype)) {
            throw new Error(`Unsupported image type: ${mimetype}`);
        }

        const maxSize = 100 * 1024 * 1024; // 100MB
        if (size > maxSize) {
            throw new Error('Image size exceeds maximum limit (100MB)');
        }

        return true;
    }

    async extractImageFeatures(buffer) {
        try {
            const metadata = await sharp(buffer).metadata();
            const stats = await sharp(buffer).stats();

            return {
                dimensions: {
                    width: metadata.width,
                    height: metadata.height
                },
                colorSpace: metadata.space,
                channels: metadata.channels,
                statistics: stats
            };
        } catch (error) {
            throw new Error(`Feature extraction failed: ${error.message}`);
        }
    }
}

module.exports = ImageProcessor;