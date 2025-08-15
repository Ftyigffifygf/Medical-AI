FROM ubuntu:22.04

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    pkg-config \
    libopencv-dev \
    libprotobuf-dev \
    protobuf-compiler \
    libgrpc++-dev \
    libgrpc-dev \
    protobuf-compiler-grpc \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install ONNX Runtime
RUN wget https://github.com/microsoft/onnxruntime/releases/download/v1.16.3/onnxruntime-linux-x64-1.16.3.tgz \
    && tar -xzf onnxruntime-linux-x64-1.16.3.tgz \
    && mv onnxruntime-linux-x64-1.16.3 /usr/local/onnxruntime \
    && rm onnxruntime-linux-x64-1.16.3.tgz

# Set working directory
WORKDIR /app

# Copy source code
COPY . .

# Create build directory and build
RUN mkdir build && cd build \
    && cmake .. \
    && make -j$(nproc)

# Create necessary directories
RUN mkdir -p /app/models /app/logs

# Expose gRPC port
EXPOSE 50051

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD ./build/medical_imaging_service --health-check || exit 1

# Run application
CMD ["./build/medical_imaging_service"]