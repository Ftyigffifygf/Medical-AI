/**
 * High-Performance Medical Imaging Service
 * C++ gRPC service for medical image analysis using ONNX Runtime
 */

#include <iostream>
#include <memory>
#include <string>
#include <grpcpp/grpcpp.h>
#include <grpcpp/health_check_service_interface.h>
#include <grpcpp/ext/proto_server_reflection_plugin.h>

#include "imaging_service.h"
#include "medical_imaging.grpc.pb.h"

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;

class MedicalImagingServiceImpl final : public medical_imaging::MedicalImagingService::Service {
private:
    std::unique_ptr<ImagingService> imaging_service_;
    
public:
    MedicalImagingServiceImpl() {
        imaging_service_ = std::make_unique<ImagingService>();
    }
    
    Status AnalyzeImage(ServerContext* context,
                       const medical_imaging::ImageAnalysisRequest* request,
                       medical_imaging::ImageAnalysisResponse* response) override {
        
        std::cout << "Analyzing image for patient: " << request->patient_id() << std::endl;
        
        try {
            auto start_time = std::chrono::high_resolution_clock::now();
            
            // Process the image analysis request
            auto result = imaging_service_->analyzeImage(
                request->patient_id(),
                request->image_type(),
                request->image_data(),
                std::vector<std::string>(request->symptoms().begin(), request->symptoms().end()),
                request->priority()
            );
            
            auto end_time = std::chrono::high_resolution_clock::now();
            auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
            
            // Populate response
            response->set_analysis_id(result.analysis_id);
            response->set_patient_id(request->patient_id());
            response->set_confidence_score(result.confidence_score);
            response->set_interpretation(result.interpretation);
            response->set_urgency_level(result.urgency_level);
            response->set_processing_time_ms(duration.count());
            response->set_model_used(result.model_used);
            response->set_success(true);
            
            // Add findings
            for (const auto& finding : result.findings) {
                auto* finding_proto = response->add_findings();
                finding_proto->set_type(finding.type);
                finding_proto->set_description(finding.description);
                finding_proto->set_location(finding.location);
                finding_proto->set_confidence(finding.confidence);
                finding_proto->set_severity(finding.severity);
                
                // Set bounding box if available
                if (finding.has_bounding_box) {
                    auto* bbox = finding_proto->mutable_bounding_box();
                    bbox->set_x(finding.bbox.x);
                    bbox->set_y(finding.bbox.y);
                    bbox->set_width(finding.bbox.width);
                    bbox->set_height(finding.bbox.height);
                }
            }
            
            // Add recommendations
            for (const auto& recommendation : result.recommendations) {
                response->add_recommendations(recommendation);
            }
            
            std::cout << "Image analysis completed in " << duration.count() << "ms" << std::endl;
            return Status::OK;
            
        } catch (const std::exception& e) {
            std::cerr << "Image analysis failed: " << e.what() << std::endl;
            response->set_success(false);
            response->set_error_message(e.what());
            return Status(grpc::StatusCode::INTERNAL, e.what());
        }
    }
    
    Status ProcessDicom(ServerContext* context,
                       const medical_imaging::DicomProcessingRequest* request,
                       medical_imaging::DicomProcessingResponse* response) override {
        
        std::cout << "Processing DICOM for patient: " << request->patient_id() << std::endl;
        
        try {
            auto result = imaging_service_->processDicom(
                request->patient_id(),
                request->dicom_data(),
                std::vector<std::string>(request->analysis_types().begin(), request->analysis_types().end())
            );
            
            response->set_patient_id(request->patient_id());
            response->set_success(true);
            
            // Add DICOM metadata
            for (const auto& [key, value] : result.metadata) {
                (*response->mutable_dicom_metadata())[key] = value;
            }
            
            // Add processed images
            for (const auto& image : result.processed_images) {
                auto* processed_image = response->add_processed_images();
                processed_image->set_series_uid(image.series_uid);
                processed_image->set_image_data(image.image_data);
                processed_image->set_modality(image.modality);
                
                for (const auto& [key, value] : image.metadata) {
                    (*processed_image->mutable_metadata())[key] = value;
                }
            }
            
            return Status::OK;
            
        } catch (const std::exception& e) {
            std::cerr << "DICOM processing failed: " << e.what() << std::endl;
            response->set_success(false);
            response->set_error_message(e.what());
            return Status(grpc::StatusCode::INTERNAL, e.what());
        }
    }
    
    Status HealthCheck(ServerContext* context,
                      const medical_imaging::HealthCheckRequest* request,
                      medical_imaging::HealthCheckResponse* response) override {
        
        auto health_info = imaging_service_->getHealthInfo();
        
        response->set_status("healthy");
        response->set_uptime_seconds(health_info.uptime_seconds);
        response->set_processed_images(health_info.processed_images);
        response->set_average_processing_time(health_info.average_processing_time);
        
        return Status::OK;
    }
};

void RunServer() {
    std::string server_address("0.0.0.0:50051");
    MedicalImagingServiceImpl service;
    
    grpc::EnableDefaultHealthCheckService(true);
    grpc::reflection::InitProtoReflectionServerBuilderPlugin();
    
    ServerBuilder builder;
    
    // Listen on the given address without any authentication mechanism
    builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
    
    // Register "service" as the instance through which we'll communicate with
    // clients. In this case it corresponds to an *synchronous* service.
    builder.RegisterService(&service);
    
    // Set max message size (for large medical images)
    builder.SetMaxReceiveMessageSize(100 * 1024 * 1024); // 100MB
    builder.SetMaxSendMessageSize(100 * 1024 * 1024);    // 100MB
    
    // Finally assemble the server
    std::unique_ptr<Server> server(builder.BuildAndStart());
    std::cout << "Medical Imaging Service listening on " << server_address << std::endl;
    
    // Wait for the server to shutdown. Note that some other thread must be
    // responsible for shutting down the server for this call to ever return.
    server->Wait();
}

int main(int argc, char** argv) {
    std::cout << "Starting Medical Imaging Service..." << std::endl;
    
    try {
        RunServer();
    } catch (const std::exception& e) {
        std::cerr << "Server failed to start: " << e.what() << std::endl;
        return 1;
    }
    
    return 0;
}