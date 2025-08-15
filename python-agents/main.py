"""
Multi-Agent Medical AI Orchestrator
Central Python service that coordinates all medical AI agents
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Dict, List, Optional, Any
import asyncio
import httpx
import json
from datetime import datetime
import logging
from langgraph import StateGraph, END
from langchain_core.messages import HumanMessage, AIMessage
from medical_agents import MedicalAgentOrchestrator
from model_gateway import ModelGateway

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Medical AI Agent Orchestrator", version="1.0.0")

# Initialize components
agent_orchestrator = MedicalAgentOrchestrator()
model_gateway = ModelGateway()

class PatientData(BaseModel):
    patient_id: str
    name: str
    age: Optional[int] = None
    gender: Optional[str] = None
    symptoms: Optional[List[str]] = []
    medical_history: Optional[List[str]] = []
    allergies: Optional[List[str]] = []
    current_medications: Optional[List[str]] = []
    vital_signs: Optional[Dict[str, float]] = {}
    lab_results: Optional[str] = None
    imaging_results: Optional[str] = None

class MedicalAnalysisRequest(BaseModel):
    patient_data: PatientData
    analysis_type: str = "complete"  # complete, diagnosis_only, treatment_only
    priority: str = "normal"  # urgent, normal, routine
    requested_models: Optional[List[str]] = None

class MedicalAnalysisResponse(BaseModel):
    analysis_id: str
    patient_id: str
    status: str
    results: Dict[str, Any]
    confidence_score: float
    processing_time: float
    models_used: List[str]
    timestamp: datetime

@app.post("/analyze", response_model=MedicalAnalysisResponse)
async def analyze_patient(request: MedicalAnalysisRequest):
    """
    Main endpoint for comprehensive medical AI analysis
    """
    try:
        start_time = datetime.now()
        
        # Generate unique analysis ID
        analysis_id = f"analysis_{request.patient_data.patient_id}_{int(start_time.timestamp())}"
        
        logger.info(f"Starting medical analysis {analysis_id}")
        
        # Route to appropriate analysis workflow
        if request.analysis_type == "complete":
            results = await agent_orchestrator.run_complete_analysis(
                request.patient_data.dict(),
                priority=request.priority,
                requested_models=request.requested_models
            )
        elif request.analysis_type == "diagnosis_only":
            results = await agent_orchestrator.run_diagnosis_analysis(
                request.patient_data.dict()
            )
        elif request.analysis_type == "treatment_only":
            results = await agent_orchestrator.run_treatment_analysis(
                request.patient_data.dict()
            )
        else:
            raise HTTPException(status_code=400, detail="Invalid analysis type")
        
        processing_time = (datetime.now() - start_time).total_seconds()
        
        return MedicalAnalysisResponse(
            analysis_id=analysis_id,
            patient_id=request.patient_data.patient_id,
            status="completed",
            results=results,
            confidence_score=results.get("overall_confidence", 0.85),
            processing_time=processing_time,
            models_used=results.get("models_used", []),
            timestamp=datetime.now()
        )
        
    except Exception as e:
        logger.error(f"Analysis failed: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

@app.post("/agents/java/forward")
async def forward_to_java_service(data: Dict[str, Any]):
    """Forward requests to Java medical services"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                "http://localhost:8080/api/medical/analyze",
                json=data,
                timeout=30.0
            )
            return response.json()
    except Exception as e:
        logger.error(f"Java service communication failed: {str(e)}")
        raise HTTPException(status_code=503, detail="Java service unavailable")

@app.post("/agents/cpp/forward")
async def forward_to_cpp_service(data: Dict[str, Any]):
    """Forward requests to C++ performance-critical services"""
    try:
        # Use gRPC for C++ communication
        from cpp_client import CppMedicalClient
        cpp_client = CppMedicalClient()
        result = await cpp_client.analyze_imaging(data)
        return result
    except Exception as e:
        logger.error(f"C++ service communication failed: {str(e)}")
        raise HTTPException(status_code=503, detail="C++ service unavailable")

@app.post("/agents/js/forward")
async def forward_to_js_service(data: Dict[str, Any]):
    """Forward requests to JavaScript/Node.js services"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                "http://localhost:3000/api/medical/analyze",
                json=data,
                timeout=30.0
            )
            return response.json()
    except Exception as e:
        logger.error(f"JavaScript service communication failed: {str(e)}")
        raise HTTPException(status_code=503, detail="JavaScript service unavailable")

@app.get("/models/available")
async def get_available_models():
    """Get list of available AI models"""
    return await model_gateway.get_available_models()

@app.post("/models/route")
async def route_to_best_model(task: str, data: Dict[str, Any]):
    """Route request to the best model for the task"""
    return await model_gateway.route_request(task, data)

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "timestamp": datetime.now(),
        "services": {
            "java": await check_service_health("http://localhost:8080/health"),
            "cpp": await check_cpp_service_health(),
            "javascript": await check_service_health("http://localhost:3000/health"),
            "models": await model_gateway.health_check()
        }
    }

async def check_service_health(url: str) -> bool:
    """Check if a service is healthy"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(url, timeout=5.0)
            return response.status_code == 200
    except:
        return False

async def check_cpp_service_health() -> bool:
    """Check C++ service health via gRPC"""
    try:
        from cpp_client import CppMedicalClient
        cpp_client = CppMedicalClient()
        return await cpp_client.health_check()
    except:
        return False

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)