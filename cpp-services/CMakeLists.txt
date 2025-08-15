cmake_minimum_required(VERSION 3.16)
project(MedicalImagingService)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required packages
find_package(OpenCV REQUIRED)
find_package(Protobuf REQUIRED)
find_package(gRPC REQUIRED)
find_package(PkgConfig REQUIRED)

# ONNX Runtime
set(ONNXRUNTIME_ROOT_PATH "/usr/local/onnxruntime")
set(ONNXRUNTIME_INCLUDE_DIRS "${ONNXRUNTIME_ROOT_PATH}/include")
set(ONNXRUNTIME_LIB "${ONNXRUNTIME_ROOT_PATH}/lib/libonnxruntime.so")

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})
include_directories(${ONNXRUNTIME_INCLUDE_DIRS})
include_directories(${CMAKE_CURRENT_BINARY_DIR})

# Generate gRPC files
set(PROTO_FILES
    proto/medical_imaging.proto
)

foreach(proto_file ${PROTO_FILES})
    get_filename_component(proto_name ${proto_file} NAME_WE)
    set(proto_srcs "${CMAKE_CURRENT_BINARY_DIR}/${proto_name}.pb.cc")
    set(proto_hdrs "${CMAKE_CURRENT_BINARY_DIR}/${proto_name}.pb.h")
    set(grpc_srcs "${CMAKE_CURRENT_BINARY_DIR}/${proto_name}.grpc.pb.cc")
    set(grpc_hdrs "${CMAKE_CURRENT_BINARY_DIR}/${proto_name}.grpc.pb.h")
    
    add_custom_command(
        OUTPUT "${proto_srcs}" "${proto_hdrs}" "${grpc_srcs}" "${grpc_hdrs}"
        COMMAND protobuf::protoc
        ARGS --grpc_out "${CMAKE_CURRENT_BINARY_DIR}"
             --cpp_out "${CMAKE_CURRENT_BINARY_DIR}"
             -I "${CMAKE_CURRENT_SOURCE_DIR}"
             --plugin=protoc-gen-grpc="${gRPC_CPP_PLUGIN_EXECUTABLE}"
             "${CMAKE_CURRENT_SOURCE_DIR}/${proto_file}"
        DEPENDS "${CMAKE_CURRENT_SOURCE_DIR}/${proto_file}")
    
    list(APPEND PROTO_SRCS ${proto_srcs})
    list(APPEND PROTO_HDRS ${proto_hdrs})
    list(APPEND GRPC_SRCS ${grpc_srcs})
    list(APPEND GRPC_HDRS ${grpc_hdrs})
endforeach()

# Source files
set(SOURCES
    src/main.cpp
    src/imaging_service.cpp
    src/dicom_processor.cpp
    src/ai_inference.cpp
    src/image_analyzer.cpp
    ${PROTO_SRCS}
    ${GRPC_SRCS}
)

# Create executable
add_executable(medical_imaging_service ${SOURCES})

# Link libraries
target_link_libraries(medical_imaging_service
    ${OpenCV_LIBS}
    ${ONNXRUNTIME_LIB}
    gRPC::grpc++
    protobuf::libprotobuf
    pthread
)

# Compiler flags
target_compile_options(medical_imaging_service PRIVATE
    -O3
    -march=native
    -fopenmp
)