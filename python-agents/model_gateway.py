"""
Multi-Model Gateway
Routes requests to the best AI model based on task type and requirements
"""

import asyncio
import httpx
import json
from typing import Dict, List, Any, Optional
import logging
from dataclasses import dataclass
from enum import Enum
import time

logger = logging.getLogger(__name__)

class ModelType(Enum):
    MEDICAL_REASONING = "medical_reasoning"
    CODE_GENERATION = "code_generation"
    GENERAL_REASONING = "general_reasoning"
    MEDICAL_QA = "medical_qa"
    MEDICAL_INTAKE = "medical_intake"

@dataclass
class ModelConfig:
    name: str
    endpoint: str
    model_type: ModelType
    max_tokens: int
    temperature: float
    cost_per_token: float
    performance_score: float
    specialties: List[str]

class ModelGateway:
    def __init__(self):
        self.models = self._initialize_models()
        self.model_cache = {}
        self.performance_metrics = {}
    
    def _initialize_models(self) -> Dict[str, ModelConfig]:
        """Initialize available AI models"""
        return {
            # Medical Reasoning Models
            "med_palm_2": ModelConfig(
                name="Med-PaLM 2",
                endpoint="http://localhost:8001/v1/medical/reasoning",
                model_type=ModelType.MEDICAL_REASONING,
                max_tokens=4096,
                temperature=0.1,
                cost_per_token=0.002,
                performance_score=0.95,
                specialties=["diagnosis", "treatment", "medical_qa"]
            ),
            
            "clinical_bert": ModelConfig(
                name="ClinicalBERT",
                endpoint="http://localhost:8002/v1/clinical/analyze",
                model_type=ModelType.MEDICAL_REASONING,
                max_tokens=2048,
                temperature=0.0,
                cost_per_token=0.001,
                performance_score=0.88,
                specialties=["medical_intake", "symptom_analysis"]
            ),
            
            # General Reasoning Models
            "gpt4_medical": ModelConfig(
                name="GPT-4 Medical",
                endpoint="https://api.openai.com/v1/chat/completions",
                model_type=ModelType.GENERAL_REASONING,
                max_tokens=8192,
                temperature=0.2,
                cost_per_token=0.03,
                performance_score=0.92,
                specialties=["complex_reasoning", "medical_qa", "treatment_planning"]
            ),
            
            "claude3_medical": ModelConfig(
                name="Claude-3 Medical",
                endpoint="https://api.anthropic.com/v1/messages",
                model_type=ModelType.GENERAL_REASONING,
                max_tokens=4096,
                temperature=0.1,
                cost_per_token=0.025,
                performance_score=0.90,
                specialties=["medical_reasoning", "safety_analysis"]
            ),
            
            # Local Models via Ollama
            "llama2_medical": ModelConfig(
                name="Llama2-Medical-7B",
                endpoint="http://localhost:11434/api/generate",
                model_type=ModelType.MEDICAL_REASONING,
                max_tokens=4096,
                temperature=0.1,
                cost_per_token=0.0,  # Local model
                performance_score=0.82,
                specialties=["medical_intake", "basic_diagnosis"]
            ),
            
            "codellama": ModelConfig(
                name="CodeLlama-34B",
                endpoint="http://localhost:11434/api/generate",
                model_type=ModelType.CODE_GENERATION,
                max_tokens=4096,
                temperature=0.1,
                cost_per_token=0.0,
                performance_score=0.87,
                specialties=["code_generation", "medical_software"]
            )
        }
    
    async def route_request(self, task: str, data: Dict[str, Any]) -> Dict[str, Any]:
        """Route request to the best model for the task"""
        try:
            # Select best model for task
            model = self._select_best_model(task, data)
            
            # Check cache first
            cache_key = self._generate_cache_key(task, data)
            if cache_key in self.model_cache:
                logger.info(f"Cache hit for task: {task}")
                return self.model_cache[cache_key]
            
            # Make request to selected model
            start_time = time.time()
            result = await self._make_model_request(model, data)
            processing_time = time.time() - start_time
            
            # Update performance metrics
            self._update_performance_metrics(model.name, processing_time, result)
            
            # Cache result
            result["model_used"] = model.name
            result["processing_time"] = processing_time
            self.model_cache[cache_key] = result
            
            return result
            
        except Exception as e:
            logger.error(f"Model routing failed: {str(e)}")
            return {"error": str(e), "model_used": "none"}
    
    def _select_best_model(self, task: str, data: Dict[str, Any]) -> ModelConfig:
        """Select the best model based on task requirements"""
        
        # Task-specific model selection
        task_model_map = {
            "medical_intake": ["clinical_bert", "llama2_medical", "med_palm_2"],
            "medical_reasoning": ["med_palm_2", "gpt4_medical", "claude3_medical"],
            "medical_qa": ["med_palm_2", "gpt4_medical", "clinical_bert"],
            "diagnosis": ["med_palm_2", "gpt4_medical", "claude3_medical"],
            "treatment_planning": ["gpt4_medical", "med_palm_2", "claude3_medical"],
            "code_generation": ["codellama", "gpt4_medical"],
            "safety_analysis": ["claude3_medical", "gpt4_medical"]
        }
        
        candidate_models = task_model_map.get(task, ["gpt4_medical"])
        
        # Score models based on performance, cost, and availability
        best_model = None
        best_score = -1
        
        for model_name in candidate_models:
            if model_name in self.models:
                model = self.models[model_name]
                
                # Calculate composite score
                performance_weight = 0.4
                cost_weight = 0.3
                availability_weight = 0.3
                
                performance_score = model.performance_score
                cost_score = 1.0 - min(model.cost_per_token / 0.05, 1.0)  # Normalize cost
                availability_score = self._get_availability_score(model_name)
                
                composite_score = (
                    performance_weight * performance_score +
                    cost_weight * cost_score +
                    availability_weight * availability_score
                )
                
                if composite_score > best_score:
                    best_score = composite_score
                    best_model = model
        
        return best_model or self.models["llama2_medical"]  # Fallback
    
    async def _make_model_request(self, model: ModelConfig, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make request to specific model"""
        
        if "ollama" in model.endpoint:
            return await self._make_ollama_request(model, data)
        elif "openai" in model.endpoint:
            return await self._make_openai_request(model, data)
        elif "anthropic" in model.endpoint:
            return await self._make_anthropic_request(model, data)
        else:
            return await self._make_custom_request(model, data)
    
    async def _make_ollama_request(self, model: ModelConfig, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make request to Ollama local model"""
        try:
            async with httpx.AsyncClient() as client:
                payload = {
                    "model": model.name.lower().replace("-", "_"),
                    "prompt": data.get("prompt", ""),
                    "stream": False,
                    "options": {
                        "temperature": model.temperature,
                        "num_predict": model.max_tokens
                    }
                }
                
                response = await client.post(
                    model.endpoint,
                    json=payload,
                    timeout=60.0
                )
                
                result = response.json()
                return {
                    "response": result.get("response", ""),
                    "model": model.name,
                    "tokens_used": len(result.get("response", "").split()),
                    "confidence": 0.85
                }
                
        except Exception as e:
            logger.error(f"Ollama request failed: {str(e)}")
            return {"error": str(e)}
    
    async def _make_openai_request(self, model: ModelConfig, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make request to OpenAI API"""
        try:
            import openai
            
            messages = [
                {"role": "system", "content": "You are a medical AI assistant."},
                {"role": "user", "content": data.get("prompt", "")}
            ]
            
            response = await openai.ChatCompletion.acreate(
                model="gpt-4",
                messages=messages,
                temperature=model.temperature,
                max_tokens=model.max_tokens
            )
            
            return {
                "response": response.choices[0].message.content,
                "model": model.name,
                "tokens_used": response.usage.total_tokens,
                "confidence": 0.92
            }
            
        except Exception as e:
            logger.error(f"OpenAI request failed: {str(e)}")
            return {"error": str(e)}
    
    async def _make_anthropic_request(self, model: ModelConfig, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make request to Anthropic Claude API"""
        try:
            import anthropic
            
            client = anthropic.AsyncAnthropic()
            
            response = await client.messages.create(
                model="claude-3-sonnet-20240229",
                max_tokens=model.max_tokens,
                temperature=model.temperature,
                messages=[
                    {"role": "user", "content": data.get("prompt", "")}
                ]
            )
            
            return {
                "response": response.content[0].text,
                "model": model.name,
                "tokens_used": response.usage.input_tokens + response.usage.output_tokens,
                "confidence": 0.90
            }
            
        except Exception as e:
            logger.error(f"Anthropic request failed: {str(e)}")
            return {"error": str(e)}
    
    async def _make_custom_request(self, model: ModelConfig, data: Dict[str, Any]) -> Dict[str, Any]:
        """Make request to custom model endpoint"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    model.endpoint,
                    json=data,
                    timeout=60.0
                )
                return response.json()
                
        except Exception as e:
            logger.error(f"Custom model request failed: {str(e)}")
            return {"error": str(e)}
    
    def _generate_cache_key(self, task: str, data: Dict[str, Any]) -> str:
        """Generate cache key for request"""
        import hashlib
        content = f"{task}_{json.dumps(data, sort_keys=True)}"
        return hashlib.md5(content.encode()).hexdigest()
    
    def _get_availability_score(self, model_name: str) -> float:
        """Get model availability score based on recent performance"""
        if model_name in self.performance_metrics:
            recent_failures = self.performance_metrics[model_name].get("recent_failures", 0)
            return max(0.1, 1.0 - (recent_failures * 0.2))
        return 1.0
    
    def _update_performance_metrics(self, model_name: str, processing_time: float, result: Dict[str, Any]):
        """Update performance metrics for model"""
        if model_name not in self.performance_metrics:
            self.performance_metrics[model_name] = {
                "total_requests": 0,
                "total_time": 0.0,
                "recent_failures": 0,
                "success_rate": 1.0
            }
        
        metrics = self.performance_metrics[model_name]
        metrics["total_requests"] += 1
        metrics["total_time"] += processing_time
        
        if "error" in result:
            metrics["recent_failures"] = min(5, metrics["recent_failures"] + 1)
        else:
            metrics["recent_failures"] = max(0, metrics["recent_failures"] - 1)
        
        metrics["success_rate"] = 1.0 - (metrics["recent_failures"] / 5.0)
    
    async def get_available_models(self) -> List[Dict[str, Any]]:
        """Get list of available models with their capabilities"""
        models_info = []
        
        for name, config in self.models.items():
            model_info = {
                "name": config.name,
                "type": config.model_type.value,
                "specialties": config.specialties,
                "performance_score": config.performance_score,
                "cost_per_token": config.cost_per_token,
                "availability": self._get_availability_score(name)
            }
            models_info.append(model_info)
        
        return models_info
    
    async def health_check(self) -> Dict[str, Any]:
        """Check health of all models"""
        health_status = {}
        
        for name, config in self.models.items():
            try:
                # Simple health check request
                test_data = {"prompt": "Health check"}
                result = await asyncio.wait_for(
                    self._make_model_request(config, test_data),
                    timeout=10.0
                )
                health_status[name] = "healthy" if "error" not in result else "unhealthy"
            except:
                health_status[name] = "unhealthy"
        
        return health_status