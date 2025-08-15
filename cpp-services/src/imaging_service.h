/**
 * Medical Imaging Service Header
 * High-performance medical image analysis using AI models
 */

#pragma once

#include <string>
#include <vector>
#include <map>
#include <memory>
#include <chrono>
#include <opencv2/opencv.hpp>
#include <onnxruntime_cxx_api.h>

struct BoundingBox {
    int x, y, width, height;
};

struct Finding {
    std::string type;
    std::string description;
    std::string location;
    double confidence;
    std::string severity;
    bool has_bounding_box = false;
    BoundingBox bbox;
};

struct ImageAnalysisResult {
    std::string analysis_id;
    std::vector<Finding> findings;
    double confidence_score;
    std::string interpretation;
    std::vector<std::string> recommendations;
    std::string urgency_level;
    std::string model_used;
};

struct ProcessedImage {
    std::string series_uid;
    std::string image_data;
    std::string modality;
    std::map<std::string, std::string> metadata;
};

struct DicomProcessingResult {
    std::map<std::string, std::string> metadata;
    std::vector<ProcessedImage> processed_images;
};

struct HealthInfo {
    double uptime_seconds;
    int processed_images;
    double average_processing_time;
};

class AIInference;
class DicomProcessor;
class ImageAnalyzer;

class ImagingService {
private:
    std::unique_ptr<AIInference> ai_inference_;
    std::unique_ptr<DicomProcessor> dicom_processor_;
    std::unique_ptr<ImageAnalyzer> image_analyzer_;
    
    std::chrono::steady_clock::time_point start_time_;
    int total_processed_images_;
    double total_processing_time_;
    
public:
    ImagingService();
    ~ImagingService();
    
    ImageAnalysisResult analyzeImage(
        const std::string& patient_id,
        const std::string& image_type,
        const std::string& image_data,
        const std::vector<std::string>& symptoms,
        const std::string& priority
    );
    
    DicomProcessingResult processDicom(
        const std::string& patient_id,
        const std::string& dicom_data,
        const std::vector<std::string>& analysis_types
    );
    
    HealthInfo getHealthInfo() const;
    
private:
    cv::Mat decodeImage(const std::string& image_data);
    std::string generateAnalysisId(const std::string& patient_id);
    std::string determineUrgencyLevel(const std::vector<Finding>& findings, 
                                    const std::vector<std::string>& symptoms);
    std::vector<std::string> generateRecommendations(const std::vector<Finding>& findings,
                                                   const std::string& image_type);
};