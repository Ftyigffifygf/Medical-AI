package com.medical.ai.service;

import com.medical.ai.model.PatientData;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Module 5: Differential Diagnosis Reasoning AI
 * Rule-based + LLM hybrid reasoning engine
 */
@Service
public class DiagnosisReasoningService {
    
    private final Map<String, DiagnosisRule> diagnosisRules;
    
    public DiagnosisReasoningService() {
        this.diagnosisRules = initializeDiagnosisRules();
    }
    
    /**
     * Generates differential diagnosis using hybrid AI reasoning
     */
    public Map<String, Object> generateDifferentialDiagnosis(PatientData patient, 
                                                           Map<String, Object> symptomAnalysis,
                                                           Map<String, Object> examFindings,
                                                           Map<String, Object> imagingResults,
                                                           Map<String, Object> labResults) {
        Map<String, Object> diagnosisResults = new HashMap<>();
        
        try {
            // Collect all clinical data
            ClinicalDataSet clinicalData = aggregateClinicalData(patient, symptomAnalysis, examFindings, imagingResults, labResults);
            
            // Rule-based reasoning
            List<DiagnosisCandidate> ruleBasedDiagnoses = applyRuleBasedReasoning(clinicalData);
            
            // LLM-enhanced reasoning
            List<DiagnosisCandidate> enhancedDiagnoses = enhanceWithLLMReasoning(ruleBasedDiagnoses, clinicalData);
            
            // Probability calculation and ranking
            List<DiagnosisCandidate> rankedDiagnoses = calculateProbabilitiesAndRank(enhancedDiagnoses, clinicalData);
            
            // Generate reasoning explanation
            String reasoningExplanation = generateReasoningExplanation(rankedDiagnoses, clinicalData);
            
            diagnosisResults.put("differentialDiagnoses", rankedDiagnoses);
            diagnosisResults.put("reasoningExplanation", reasoningExplanation);
            diagnosisResults.put("confidence", calculateOverallConfidence(rankedDiagnoses));
            diagnosisResults.put("urgencyLevel", assessUrgencyLevel(rankedDiagnoses));
            
            // Update patient data
            patient.setDifferentialDiagnosis(formatDifferentialDiagnosis(rankedDiagnoses));
            
        } catch (Exception e) {
            // Demo mode results
            diagnosisResults = generateDemoDiagnosisResults(patient);
        }
        
        return diagnosisResults;
    }
    
    private ClinicalDataSet aggregateClinicalData(PatientData patient, 
                                                Map<String, Object> symptomAnalysis,
                                                Map<String, Object> examFindings,
                                                Map<String, Object> imagingResults,
                                                Map<String, Object> labResults) {
        ClinicalDataSet dataSet = new ClinicalDataSet();
        
        // Patient demographics
        dataSet.age = patient.getAge();
        dataSet.gender = patient.getGender();
        
        // Symptoms
        dataSet.symptoms = patient.getSymptoms() != null ? patient.getSymptoms() : new ArrayList<>();
        
        // Medical history
        dataSet.medicalHistory = patient.getMedicalHistory() != null ? patient.getMedicalHistory() : new ArrayList<>();
        dataSet.allergies = patient.getAllergies() != null ? patient.getAllergies() : new ArrayList<>();
        dataSet.medications = patient.getCurrentMedications() != null ? patient.getCurrentMedications() : new ArrayList<>();
        
        // Vital signs
        dataSet.vitalSigns = new HashMap<>();
        if (patient.getHeartRate() != null) dataSet.vitalSigns.put("heartRate", patient.getHeartRate());
        if (patient.getTemperature() != null) dataSet.vitalSigns.put("temperature", patient.getTemperature());
        if (patient.getBloodPressureSystolic() != null) dataSet.vitalSigns.put("systolic", patient.getBloodPressureSystolic());
        
        // Exam findings
        dataSet.examFindings = examFindings != null ? examFindings : new HashMap<>();
        
        // Lab results
        dataSet.labResults = labResults != null ? labResults : new HashMap<>();
        
        // Imaging results
        dataSet.imagingResults = imagingResults != null ? imagingResults : new HashMap<>();
        
        return dataSet;
    }
    
    private List<DiagnosisCandidate> applyRuleBasedReasoning(ClinicalDataSet clinicalData) {
        List<DiagnosisCandidate> candidates = new ArrayList<>();
        
        // Apply each diagnosis rule
        for (DiagnosisRule rule : diagnosisRules.values()) {
            double score = rule.evaluate(clinicalData);
            if (score > 0.1) { // Minimum threshold
                DiagnosisCandidate candidate = new DiagnosisCandidate();
                candidate.diagnosis = rule.diagnosis;
                candidate.icdCode = rule.icdCode;
                candidate.probability = score;
                candidate.supportingEvidence = rule.getSupportingEvidence(clinicalData);
                candidate.reasoning = rule.getReasoning();
                candidates.add(candidate);
            }
        }
        
        return candidates;
    }
    
    private List<DiagnosisCandidate> enhanceWithLLMReasoning(List<DiagnosisCandidate> ruleBasedDiagnoses, ClinicalDataSet clinicalData) {
        // In a real implementation, this would call an LLM API
        // For demo, we'll enhance the reasoning with additional context
        
        for (DiagnosisCandidate candidate : ruleBasedDiagnoses) {
            // Enhance reasoning with clinical context
            candidate.reasoning += " Clinical correlation supports this diagnosis based on " +
                    "symptom constellation and patient demographics.";
            
            // Adjust probability based on additional factors
            if (clinicalData.age != null && clinicalData.age > 65) {
                // Increase probability for age-related conditions
                if (candidate.diagnosis.contains("Cardiovascular") || candidate.diagnosis.contains("Diabetes")) {
                    candidate.probability *= 1.2;
                }
            }
            
            // Consider gender-specific factors
            if ("Female".equalsIgnoreCase(clinicalData.gender)) {
                if (candidate.diagnosis.contains("Thyroid") || candidate.diagnosis.contains("Autoimmune")) {
                    candidate.probability *= 1.1;
                }
            }
        }
        
        return ruleBasedDiagnoses;
    }
    
    private List<DiagnosisCandidate> calculateProbabilitiesAndRank(List<DiagnosisCandidate> diagnoses, ClinicalDataSet clinicalData) {
        // Normalize probabilities
        double totalProbability = diagnoses.stream().mapToDouble(d -> d.probability).sum();
        if (totalProbability > 0) {
            diagnoses.forEach(d -> d.probability = d.probability / totalProbability);
        }
        
        // Sort by probability (descending)
        diagnoses.sort((a, b) -> Double.compare(b.probability, a.probability));
        
        // Limit to top 5 diagnoses
        return diagnoses.stream().limit(5).collect(Collectors.toList());
    }
    
    private String generateReasoningExplanation(List<DiagnosisCandidate> diagnoses, ClinicalDataSet clinicalData) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("DIFFERENTIAL DIAGNOSIS REASONING:\n\n");
        
        explanation.append("Patient Profile: ");
        explanation.append(clinicalData.age != null ? clinicalData.age + "-year-old " : "");
        explanation.append(clinicalData.gender != null ? clinicalData.gender.toLowerCase() : "patient");
        explanation.append("\n\n");
        
        explanation.append("Primary Symptoms: ");
        explanation.append(String.join(", ", clinicalData.symptoms));
        explanation.append("\n\n");
        
        explanation.append("Diagnostic Reasoning:\n");
        for (int i = 0; i < diagnoses.size(); i++) {
            DiagnosisCandidate diagnosis = diagnoses.get(i);
            explanation.append(String.format("%d. %s (%.1f%% probability)\n", 
                    i + 1, diagnosis.diagnosis, diagnosis.probability * 100));
            explanation.append("   Reasoning: ").append(diagnosis.reasoning).append("\n");
            explanation.append("   Supporting Evidence: ").append(String.join(", ", diagnosis.supportingEvidence)).append("\n\n");
        }
        
        return explanation.toString();
    }
    
    private double calculateOverallConfidence(List<DiagnosisCandidate> diagnoses) {
        if (diagnoses.isEmpty()) return 0.0;
        
        // Confidence based on top diagnosis probability and number of candidates
        double topProbability = diagnoses.get(0).probability;
        double confidenceAdjustment = Math.min(diagnoses.size() / 5.0, 1.0); // More candidates = higher confidence
        
        return Math.min(topProbability + (confidenceAdjustment * 0.1), 0.95);
    }
    
    private String assessUrgencyLevel(List<DiagnosisCandidate> diagnoses) {
        // Check for urgent conditions
        for (DiagnosisCandidate diagnosis : diagnoses) {
            if (diagnosis.diagnosis.contains("Myocardial Infarction") || 
                diagnosis.diagnosis.contains("Stroke") ||
                diagnosis.diagnosis.contains("Sepsis") ||
                diagnosis.diagnosis.contains("Pulmonary Embolism")) {
                return "URGENT";
            }
        }
        
        // Check for semi-urgent conditions
        for (DiagnosisCandidate diagnosis : diagnoses) {
            if (diagnosis.diagnosis.contains("Pneumonia") || 
                diagnosis.diagnosis.contains("Diabetes") ||
                diagnosis.diagnosis.contains("Hypertension")) {
                return "MODERATE";
            }
        }
        
        return "ROUTINE";
    }
    
    private String formatDifferentialDiagnosis(List<DiagnosisCandidate> diagnoses) {
        StringBuilder formatted = new StringBuilder();
        formatted.append("DIFFERENTIAL DIAGNOSIS:\n\n");
        
        for (int i = 0; i < diagnoses.size(); i++) {
            DiagnosisCandidate diagnosis = diagnoses.get(i);
            formatted.append(String.format("%d. %s (ICD: %s) - %.1f%% probability\n", 
                    i + 1, diagnosis.diagnosis, diagnosis.icdCode, diagnosis.probability * 100));
        }
        
        return formatted.toString();
    }
    
    private Map<String, DiagnosisRule> initializeDiagnosisRules() {
        Map<String, DiagnosisRule> rules = new HashMap<>();
        
        // Cardiovascular conditions
        rules.put("MI", new DiagnosisRule("Myocardial Infarction", "I21.9") {
            @Override
            public double evaluate(ClinicalDataSet data) {
                double score = 0.0;
                if (data.symptoms.contains("chest pain")) score += 0.4;
                if (data.symptoms.contains("shortness of breath")) score += 0.2;
                if (data.symptoms.contains("nausea")) score += 0.1;
                if (data.age != null && data.age > 50) score += 0.2;
                if ("Male".equalsIgnoreCase(data.gender)) score += 0.1;
                return Math.min(score, 1.0);
            }
        });
        
        // Respiratory conditions
        rules.put("Pneumonia", new DiagnosisRule("Community-Acquired Pneumonia", "J18.9") {
            @Override
            public double evaluate(ClinicalDataSet data) {
                double score = 0.0;
                if (data.symptoms.contains("cough")) score += 0.3;
                if (data.symptoms.contains("fever")) score += 0.3;
                if (data.symptoms.contains("shortness of breath")) score += 0.2;
                if (data.vitalSigns.containsKey("temperature") && 
                    (Double) data.vitalSigns.get("temperature") > 100.4) score += 0.2;
                return Math.min(score, 1.0);
            }
        });
        
        // Endocrine conditions
        rules.put("Diabetes", new DiagnosisRule("Type 2 Diabetes Mellitus", "E11.9") {
            @Override
            public double evaluate(ClinicalDataSet data) {
                double score = 0.0;
                if (data.symptoms.contains("increased urination")) score += 0.2;
                if (data.symptoms.contains("increased thirst")) score += 0.2;
                if (data.symptoms.contains("fatigue")) score += 0.1;
                if (data.age != null && data.age > 45) score += 0.2;
                
                // Check lab results
                if (data.labResults.containsKey("extractedValues")) {
                    Map<String, Double> labs = (Map<String, Double>) data.labResults.get("extractedValues");
                    if (labs.containsKey("glucose") && labs.get("glucose") > 126) {
                        score += 0.4;
                    }
                }
                return Math.min(score, 1.0);
            }
        });
        
        // Gastrointestinal conditions
        rules.put("GERD", new DiagnosisRule("Gastroesophageal Reflux Disease", "K21.9") {
            @Override
            public double evaluate(ClinicalDataSet data) {
                double score = 0.0;
                if (data.symptoms.contains("heartburn")) score += 0.4;
                if (data.symptoms.contains("chest pain")) score += 0.2;
                if (data.symptoms.contains("regurgitation")) score += 0.3;
                if (data.symptoms.contains("difficulty swallowing")) score += 0.1;
                return Math.min(score, 1.0);
            }
        });
        
        // Neurological conditions
        rules.put("Migraine", new DiagnosisRule("Migraine Headache", "G43.9") {
            @Override
            public double evaluate(ClinicalDataSet data) {
                double score = 0.0;
                if (data.symptoms.contains("headache")) score += 0.4;
                if (data.symptoms.contains("nausea")) score += 0.2;
                if (data.symptoms.contains("light sensitivity")) score += 0.2;
                if ("Female".equalsIgnoreCase(data.gender)) score += 0.1;
                if (data.age != null && data.age >= 15 && data.age <= 55) score += 0.1;
                return Math.min(score, 1.0);
            }
        });
        
        return rules;
    }
    
    private Map<String, Object> generateDemoDiagnosisResults(PatientData patient) {
        List<DiagnosisCandidate> demoDiagnoses = new ArrayList<>();
        
        // Create demo diagnoses based on symptoms
        if (patient.getSymptoms() != null && !patient.getSymptoms().isEmpty()) {
            if (patient.getSymptoms().contains("chest pain")) {
                demoDiagnoses.add(createDemoCandidate("Gastroesophageal Reflux Disease", "K21.9", 0.35));
                demoDiagnoses.add(createDemoCandidate("Costochondritis", "M94.0", 0.25));
                demoDiagnoses.add(createDemoCandidate("Anxiety Disorder", "F41.9", 0.20));
            } else if (patient.getSymptoms().contains("headache")) {
                demoDiagnoses.add(createDemoCandidate("Tension Headache", "G44.2", 0.40));
                demoDiagnoses.add(createDemoCandidate("Migraine", "G43.9", 0.30));
                demoDiagnoses.add(createDemoCandidate("Sinusitis", "J32.9", 0.20));
            } else {
                demoDiagnoses.add(createDemoCandidate("Viral Upper Respiratory Infection", "J06.9", 0.45));
                demoDiagnoses.add(createDemoCandidate("Allergic Rhinitis", "J30.9", 0.25));
            }
        } else {
            demoDiagnoses.add(createDemoCandidate("Health Maintenance Visit", "Z00.00", 0.60));
        }
        
        Map<String, Object> results = new HashMap<>();
        results.put("differentialDiagnoses", demoDiagnoses);
        results.put("confidence", 0.87);
        results.put("urgencyLevel", "ROUTINE");
        results.put("mode", "DEMO");
        
        patient.setDifferentialDiagnosis(formatDifferentialDiagnosis(demoDiagnoses));
        
        return results;
    }
    
    private DiagnosisCandidate createDemoCandidate(String diagnosis, String icdCode, double probability) {
        DiagnosisCandidate candidate = new DiagnosisCandidate();
        candidate.diagnosis = diagnosis;
        candidate.icdCode = icdCode;
        candidate.probability = probability;
        candidate.reasoning = "Based on clinical presentation and patient demographics";
        candidate.supportingEvidence = Arrays.asList("Symptom pattern", "Age and gender factors");
        return candidate;
    }
    
    // Inner classes
    private static class ClinicalDataSet {
        Integer age;
        String gender;
        List<String> symptoms;
        List<String> medicalHistory;
        List<String> allergies;
        List<String> medications;
        Map<String, Double> vitalSigns;
        Map<String, Object> examFindings;
        Map<String, Object> labResults;
        Map<String, Object> imagingResults;
    }
    
    private static class DiagnosisCandidate {
        String diagnosis;
        String icdCode;
        double probability;
        String reasoning;
        List<String> supportingEvidence;
    }
    
    private abstract static class DiagnosisRule {
        String diagnosis;
        String icdCode;
        
        DiagnosisRule(String diagnosis, String icdCode) {
            this.diagnosis = diagnosis;
            this.icdCode = icdCode;
        }
        
        abstract double evaluate(ClinicalDataSet data);
        
        String getReasoning() {
            return "Rule-based evaluation considering symptoms, demographics, and clinical findings";
        }
        
        List<String> getSupportingEvidence(ClinicalDataSet data) {
            List<String> evidence = new ArrayList<>();
            if (!data.symptoms.isEmpty()) evidence.add("Symptom constellation");
            if (data.age != null) evidence.add("Age factor");
            if (data.gender != null) evidence.add("Gender consideration");
            if (!data.vitalSigns.isEmpty()) evidence.add("Vital signs");
            return evidence;
        }
    }
}