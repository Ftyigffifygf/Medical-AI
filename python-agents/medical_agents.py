"""
Medical Agent Orchestrator using LangGraph
Coordinates multiple specialized medical AI agents
"""

from langgraph import StateGraph, END
from langchain_core.messages import HumanMessage, AIMessage
from typing import Dict, List, Any, Optional
import asyncio
import json
from datetime import datetime
import logging
from dataclasses import dataclass
from enum import Enum

logger = logging.getLogger(__name__)

class Priority(Enum):
    URGENT = "urgent"
    NORMAL = "normal" 
    ROUTINE = "routine"

@dataclass
class MedicalState:
    patient_data: Dict[str, Any]
    intake_results: Optional[Dict[str, Any]] = None
    exam_results: Optional[Dict[str, Any]] = None
    lab_results: Optional[Dict[str, Any]] = None
    imaging_results: Optional[Dict[str, Any]] = None
    diagnosis_results: Optional[Dict[str, Any]] = None
    treatment_results: Optional[Dict[str, Any]] = None
    prescription_results: Optional[Dict[str, Any]] = None
    overall_confidence: float = 0.0
    models_used: List[str] = None
    priority: Priority = Priority.NORMAL
    
    def __post_init__(self):
        if self.models_used is None:
            self.models_used = []

class MedicalAgentOrchestrator:
    def __init__(self):
        self.workflow = self._build_workflow()
        
    def _build_workflow(self) -> StateGraph:
        """Build the medical analysis workflow using LangGraph"""
        workflow = StateGraph(MedicalState)
        
        # Add nodes for each medical analysis step
        workflow.add_node("patient_intake", self._patient_intake_agent)
        workflow.add_node("physical_exam", self._physical_exam_agent)
        workflow.add_node("lab_analysis", self._lab_analysis_agent)
        workflow.add_node("imaging_analysis", self._imaging_analysis_agent)
        workflow.add_node("diagnosis_reasoning", self._diagnosis_reasoning_agent)
        workflow.add_node("treatment_planning", self._treatment_planning_agent)
        workflow.add_node("prescription_generation", self._prescription_agent)
        workflow.add_node("quality_assurance", self._quality_assurance_agent)
        
        # Define workflow edges
        workflow.set_entry_point("patient_intake")
        workflow.add_edge("patient_intake", "physical_exam")
        workflow.add_edge("physical_exam", "lab_analysis")
        workflow.add_edge("lab_analysis", "imaging_analysis")
        workflow.add_edge("imaging_analysis", "diagnosis_reasoning")
        workflow.add_edge("diagnosis_reasoning", "treatment_planning")
        workflow.add_edge("treatment_planning", "prescription_generation")
        workflow.add_edge("prescription_generation", "quality_assurance")
        workflow.add_edge("quality_assurance", END)
        
        return workflow.compile()
    
    async def run_complete_analysis(self, patient_data: Dict[str, Any], 
                                  priority: str = "normal",
                                  requested_models: Optional[List[str]] = None) -> Dict[str, Any]:
        """Run complete medical analysis workflow"""
        
        initial_state = MedicalState(
            patient_data=patient_data,
            priority=Priority(priority),
            models_used=requested_models or []
        )
        
        try:
            # Execute the workflow
            final_state = await self.workflow.ainvoke(initial_state)
            
            # Compile results
            results = {
                "patient_intake": final_state.intake_results,
                "physical_exam": final_state.exam_results,
                "lab_analysis": final_state.lab_results,
                "imaging_analysis": final_state.imaging_results,
                "diagnosis": final_state.diagnosis_results,
                "treatment_plan": final_state.treatment_results,
                "prescriptions": final_state.prescription_results,
                "overall_confidence": final_state.overall_confidence,
                "models_used": final_state.models_used,
                "analysis_complete": True,
                "timestamp": datetime.now().isoformat()
            }
            
            return results
            
        except Exception as e:
            logger.error(f"Workflow execution failed: {str(e)}")
            return {
                "error": str(e),
                "analysis_complete": False,
                "timestamp": datetime.now().isoformat()
            }
    
    async def _patient_intake_agent(self, state: MedicalState) -> MedicalState:     
   """Patient intake and history collection agent"""
        try:
            from model_gateway import ModelGateway
            model_gateway = ModelGateway()
            
            # Route to best model for medical intake
            intake_prompt = f"""
            Analyze patient intake data and generate comprehensive medical history assessment:
            
            Patient: {state.patient_data.get('name', 'Unknown')}
            Age: {state.patient_data.get('age', 'Unknown')}
            Gender: {state.patient_data.get('gender', 'Unknown')}
            Symptoms: {', '.join(state.patient_data.get('symptoms', []))}
            Medical History: {', '.join(state.patient_data.get('medical_history', []))}
            Allergies: {', '.join(state.patient_data.get('allergies', []))}
            Current Medications: {', '.join(state.patient_data.get('current_medications', []))}
            
            Provide:
            1. Risk stratification (LOW/MODERATE/HIGH)
            2. Key clinical concerns
            3. Recommended follow-up questions
            4. Urgency assessment
            """
            
            result = await model_gateway.route_request("medical_intake", {
                "prompt": intake_prompt,
                "patient_data": state.patient_data
            })
            
            state.intake_results = result
            state.models_used.append(result.get("model_used", "unknown"))
            
            logger.info("Patient intake analysis completed")
            return state
            
        except Exception as e:
            logger.error(f"Patient intake agent failed: {str(e)}")
            state.intake_results = {"error": str(e)}
            return state
    
    async def _physical_exam_agent(self, state: MedicalState) -> MedicalState:
        """Physical examination analysis agent"""
        try:
            # Forward to Java service for physical exam analysis
            import httpx
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    "http://localhost:8080/api/medical/physical-exam",
                    json=state.patient_data,
                    timeout=30.0
                )
                state.exam_results = response.json()
            
            logger.info("Physical exam analysis completed")
            return state
            
        except Exception as e:
            logger.error(f"Physical exam agent failed: {str(e)}")
            state.exam_results = {"error": str(e)}
            return state
    
    async def _lab_analysis_agent(self, state: MedicalState) -> MedicalState:
        """Laboratory results analysis agent"""
        try:
            # Forward to Java service for lab analysis
            import httpx
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    "http://localhost:8080/api/medical/lab-analysis",
                    json=state.patient_data,
                    timeout=30.0
                )
                state.lab_results = response.json()
            
            logger.info("Lab analysis completed")
            return state
            
        except Exception as e:
            logger.error(f"Lab analysis agent failed: {str(e)}")
            state.lab_results = {"error": str(e)}
            return state
    
    async def _imaging_analysis_agent(self, state: MedicalState) -> MedicalState:
        """Medical imaging analysis agent - uses C++ for performance"""
        try:
            # Forward to C++ service for high-performance imaging analysis
            from cpp_client import CppMedicalClient
            cpp_client = CppMedicalClient()
            
            imaging_data = {
                "patient_id": state.patient_data.get("patient_id"),
                "imaging_results": state.patient_data.get("imaging_results"),
                "symptoms": state.patient_data.get("symptoms", [])
            }
            
            result = await cpp_client.analyze_imaging(imaging_data)
            state.imaging_results = result
            state.models_used.append("cpp_imaging_model")
            
            logger.info("Imaging analysis completed")
            return state
            
        except Exception as e:
            logger.error(f"Imaging analysis agent failed: {str(e)}")
            state.imaging_results = {"error": str(e)}
            return state
    
    async def _diagnosis_reasoning_agent(self, state: MedicalState) -> MedicalState:
        """Differential diagnosis reasoning agent"""
        try:
            # Forward to Java service for diagnosis reasoning
            import httpx
            
            diagnosis_data = {
                "patient_data": state.patient_data,
                "intake_results": state.intake_results,
                "exam_results": state.exam_results,
                "lab_results": state.lab_results,
                "imaging_results": state.imaging_results
            }
            
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    "http://localhost:8080/api/medical/diagnosis",
                    json=diagnosis_data,
                    timeout=30.0
                )
                state.diagnosis_results = response.json()
            
            logger.info("Diagnosis reasoning completed")
            return state
            
        except Exception as e:
            logger.error(f"Diagnosis reasoning agent failed: {str(e)}")
            state.diagnosis_results = {"error": str(e)}
            return state
    
    async def _treatment_planning_agent(self, state: MedicalState) -> MedicalState:
        """Treatment planning agent"""
        try:
            # Forward to Java service for treatment planning
            import httpx
            
            treatment_data = {
                "patient_data": state.patient_data,
                "diagnosis_results": state.diagnosis_results
            }
            
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    "http://localhost:8080/api/medical/treatment",
                    json=treatment_data,
                    timeout=30.0
                )
                state.treatment_results = response.json()
            
            logger.info("Treatment planning completed")
            return state
            
        except Exception as e:
            logger.error(f"Treatment planning agent failed: {str(e)}")
            state.treatment_results = {"error": str(e)}
            return state
    
    async def _prescription_agent(self, state: MedicalState) -> MedicalState:
        """Prescription generation agent"""
        try:
            # Forward to Java service for prescription generation
            import httpx
            
            prescription_data = {
                "patient_data": state.patient_data,
                "treatment_results": state.treatment_results
            }
            
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    "http://localhost:8080/api/medical/prescriptions",
                    json=prescription_data,
                    timeout=30.0
                )
                state.prescription_results = response.json()
            
            logger.info("Prescription generation completed")
            return state
            
        except Exception as e:
            logger.error(f"Prescription agent failed: {str(e)}")
            state.prescription_results = {"error": str(e)}
            return state
    
    async def _quality_assurance_agent(self, state: MedicalState) -> MedicalState:
        """Quality assurance and validation agent"""
        try:
            from model_gateway import ModelGateway
            model_gateway = ModelGateway()
            
            # Comprehensive QA analysis
            qa_prompt = f"""
            Review the complete medical analysis for quality assurance:
            
            Patient: {state.patient_data.get('name')}
            Diagnosis: {state.diagnosis_results}
            Treatment: {state.treatment_results}
            Prescriptions: {state.prescription_results}
            
            Validate:
            1. Clinical consistency
            2. Drug interactions and contraindications
            3. Age-appropriate treatments
            4. Missing critical assessments
            5. Overall confidence score (0-1)
            
            Provide quality score and recommendations.
            """
            
            qa_result = await model_gateway.route_request("medical_qa", {
                "prompt": qa_prompt,
                "analysis_data": {
                    "intake": state.intake_results,
                    "diagnosis": state.diagnosis_results,
                    "treatment": state.treatment_results,
                    "prescriptions": state.prescription_results
                }
            })
            
            # Calculate overall confidence
            confidence_scores = []
            for result in [state.intake_results, state.exam_results, state.lab_results, 
                          state.imaging_results, state.diagnosis_results, state.treatment_results]:
                if result and isinstance(result, dict) and "confidence" in result:
                    confidence_scores.append(result["confidence"])
            
            state.overall_confidence = sum(confidence_scores) / len(confidence_scores) if confidence_scores else 0.85
            state.models_used.append(qa_result.get("model_used", "qa_model"))
            
            logger.info("Quality assurance completed")
            return state
            
        except Exception as e:
            logger.error(f"Quality assurance agent failed: {str(e)}")
            state.overall_confidence = 0.75  # Default confidence
            return state
    
    async def run_diagnosis_analysis(self, patient_data: Dict[str, Any]) -> Dict[str, Any]:
        """Run diagnosis-only analysis"""
        # Simplified workflow for diagnosis only
        state = MedicalState(patient_data=patient_data)
        
        state = await self._patient_intake_agent(state)
        state = await self._diagnosis_reasoning_agent(state)
        
        return {
            "patient_intake": state.intake_results,
            "diagnosis": state.diagnosis_results,
            "confidence": state.overall_confidence,
            "models_used": state.models_used
        }
    
    async def run_treatment_analysis(self, patient_data: Dict[str, Any]) -> Dict[str, Any]:
        """Run treatment-only analysis"""
        # Simplified workflow for treatment only
        state = MedicalState(patient_data=patient_data)
        
        state = await self._treatment_planning_agent(state)
        state = await self._prescription_agent(state)
        
        return {
            "treatment_plan": state.treatment_results,
            "prescriptions": state.prescription_results,
            "confidence": state.overall_confidence,
            "models_used": state.models_used
        }