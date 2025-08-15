package com.medical.ai.service;

import com.medical.ai.model.PatientData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Central AI Reasoning Engine - Orchestrates all medical AI modules
 * Provides comprehensive medical intelligence and decision support
 */
@Service
public class CentralReasoningService {
    
    @Autowired
    private PatientIntakeService patientIntakeService;
    
    @Autowired
    private PhysicalExamService physicalExamService;
    
    @Autowired
    private DiagnosticImagingService diagnosticImagingService;
    
    @Autowired
    private LabReportService labReportService;
    
    @Autowired
    private DiagnosisReasoningService diagnosisReasoningService;
    
    @Autowired
    private TreatmentPlanningService treatmentPlanningService;
    
    @Autowired
    private PrescriptionService prescriptionService;
    
    /**
     * Complete medical AI analysis workflow
     * Orchestrates all AI modules for comprehensive patient assessment
     */
    public Map<String, Object> performCompleteAnalysis(PatientData patient) {
        Map<String, Object> completeAnalysis = new HashMap<>();
        
        try {
            // Phase 1: Patient Intake and History
            Map<String, Object> intakeResults = patientIntakeService.conductPatientIntake(patient);
            completeAnalysis.put("patientIntake", intakeResults);
            
            // Phase 2: Physical Examination Analysis
            Map<String, Object> examResults = physicalExamService.analyzePhysicalExam(patient);
            completeAnalysis.put("physicalExam", examResults);
            
            // Phase 3: Diagnostic Imaging Analysis (if available)
            Map<String, Object> imagingResults = new HashMap<>();
            if (hasImagingData(patient)) {
                imagingResults = diagnosticImagingService.analyzeImagingStudies(patient, null);
            }
            completeAnalysis.put("diagnosticImaging", imagingResults);
            
            // Phase 4: Laboratory Results Analysis (if available)
            Map<String, Object> labResults = new HashMap<>();
            if (hasLabData(patient)) {
                labResults = labReportService.analyzeLabResults(patient, null);
            }
            completeAnalysis.put("laboratoryResults", labResults);
            
            // Phase 5: Differential Diagnosis Reasoning
            Map<String, Object> diagnosisResults = diagnosisReasoningService.generateDifferentialDiagnosis(
                patient, intakeResults, examResults, imagingResults, labResults);
            completeAnalysis.put("differentialDiagnosis", diagnosisResults);
            
            // Phase 6: Treatment Planning
            List<Map<String, Object>> diagnoses = (List<Map<String, Object>>) diagnosisResults.get("differentialDiagnoses");
            Map<String, Object> treatmentResults = treatmentPlanningService.createTreatmentPlan(patient, diagnoses);
            completeAnalysis.put("treatmentPlan", treatmentResults);
            
            // Phase 7: Prescription Generation
            List<Map<String, Object>> treatments = (List<Map<String, Object>>) treatmentResults.get("treatmentRecommendations");
            Map<String, Object> prescriptionResults = prescriptionService.generatePrescriptions(patient, treatments);
            completeAnalysis.put("prescriptions", prescriptionResults);
            
            // Generate comprehensive summary
            Map<String, Object> clinicalSummary = generateClinicalSummary(patient, completeAnalysis);
            completeAnalysis.put("clinicalSummary", clinicalSummary);
            
            // Calculate overall confidence and risk assessment
            Map<String, Object> riskAssessment = performRiskAssessment(patient, completeAnalysis);
            completeAnalysis.put("riskAssessment", riskAssessment);
            
            completeAnalysis.put("analysisComplete", true);
            completeAnalysis.put("timestamp", new Date());
            
        } catch (Exception e) {
            completeAnalysis.put("error", "Analysis failed: " + e.getMessage());
            completeAnalysis.put("analysisComplete", false);
        }
        
        return completeAnalysis;
    }
    
    /**
     * Generates clinical decision support recommendations
     */
    public Map<String, Object> generateClinicalDecisionSupport(PatientData patient, Map<String, Object> analysisResults) {
        Map<String, Object> decisionSupport = new HashMap<>();
        
        // Extract key findings
        List<String> keyFindings = extractKeyFindings(analysisResults);
        
        // Generate recommendations
        List<String> recommendations = generateRecommendations(patient, analysisResults);
        
        // Identify red flags
        List<String> redFlags = identifyRedFlags(patient, analysisResults);
        
        // Suggest additional testing
        List<String> additionalTests = suggestAdditionalTesting(patient, analysisResults);
        
        // Generate follow-up plan
        Map<String, Object> followUpPlan = generateFollowUpPlan(patient, analysisResults);
        
        decisionSupport.put("keyFindings", keyFindings);
        decisionSupport.put("recommendations", recommendations);
        decisionSupport.put("redFlags", redFlags);
        decisionSupport.put("additionalTests", additionalTests);
        decisionSupport.put("followUpPlan", followUpPlan);
        decisionSupport.put("confidence", calculateDecisionConfidence(analysisResults));
        
        return decisionSupport;
    }
    
    private boolean hasImagingData(PatientData patient) {
        return patient.getImagingResults() != null && !patient.getImagingResults().trim().isEmpty();
    }
    
    private boolean hasLabData(PatientData patient) {
        return patient.getLabResults() != null && !patient.getLabResults().trim().isEmpty();
    }
    
    private Map<String, Object> generateClinicalSummary(PatientData patient, Map<String, Object> analysis) {
        Map<String, Object> summary = new HashMap<>();
        
        StringBuilder clinicalSummary = new StringBuilder();
        clinicalSummary.append("CLINICAL SUMMARY\n");
        clinicalSummary.append("================\n\n");
        
        // Patient demographics
        clinicalSummary.append("Patient: ").append(patient.getName()).append("\n");
        clinicalSummary.append("Age: ").append(patient.getAge()).append(" years\n");
        clinicalSummary.append("Gender: ").append(patient.getGender()).append("\n\n");
        
        // Chief complaint
        if (patient.getSymptoms() != null && !patient.getSymptoms().isEmpty()) {
            clinicalSummary.append("Chief Complaint: ").append(String.join(", ", patient.getSymptoms())).append("\n\n");
        }
        
        // Key findings from each module
        if (analysis.containsKey("differentialDiagnosis")) {
            Map<String, Object> diagnosis = (Map<String, Object>) analysis.get("differentialDiagnosis");
            if (diagnosis.containsKey("differentialDiagnoses")) {
                List<Map<String, Object>> diagnoses = (List<Map<String, Object>>) diagnosis.get("differentialDiagnoses");
                if (!diagnoses.isEmpty()) {
                    clinicalSummary.append("Most Likely Diagnosis: ").append(diagnoses.get(0).get("diagnosis")).append("\n");
                    clinicalSummary.append("Confidence: ").append(String.format("%.1f%%", 
                        (Double) diagnoses.get(0).get("probability") * 100)).append("\n\n");
                }
            }
        }
        
        // Treatment recommendations
        if (analysis.containsKey("treatmentPlan")) {
            clinicalSummary.append("Primary Treatment Approach: Evidence-based therapy with personalized modifications\n\n");
        }
        
        // Risk factors
        clinicalSummary.append("Risk Stratification: ");
        if (patient.getAge() != null && patient.getAge() > 65) {
            clinicalSummary.append("Elevated risk due to age. ");
        }
        if (patient.getMedicalHistory() != null && !patient.getMedicalHistory().isEmpty()) {
            clinicalSummary.append("Consider past medical history. ");
        }
        clinicalSummary.append("\n\n");
        
        summary.put("clinicalSummaryText", clinicalSummary.toString());
        summary.put("analysisModulesUsed", analysis.keySet().size());
        summary.put("dataCompleteness", calculateDataCompleteness(patient));
        
        return summary;
    }
    
    private Map<String, Object> performRiskAssessment(PatientData patient, Map<String, Object> analysis) {
        Map<String, Object> riskAssessment = new HashMap<>();
        
        int riskScore = 0;
        List<String> riskFactors = new ArrayList<>();
        
        // Age-based risk
        if (patient.getAge() != null) {
            if (patient.getAge() > 65) {
                riskScore += 2;
                riskFactors.add("Advanced age (>65)");
            }
            if (patient.getAge() > 80) {
                riskScore += 1;
                riskFactors.add("Very advanced age (>80)");
            }
        }
        
        // Symptom-based risk
        if (patient.getSymptoms() != null) {
            for (String symptom : patient.getSymptoms()) {
                if (symptom.toLowerCase().contains("chest pain")) {
                    riskScore += 3;
                    riskFactors.add("Chest pain - cardiac risk");
                }
                if (symptom.toLowerCase().contains("shortness of breath")) {
                    riskScore += 2;
                    riskFactors.add("Dyspnea - cardiopulmonary risk");
                }
                if (symptom.toLowerCase().contains("severe headache")) {
                    riskScore += 2;
                    riskFactors.add("Severe headache - neurological risk");
                }
            }
        }
        
        // Medical history risk
        if (patient.getMedicalHistory() != null) {
            for (String condition : patient.getMedicalHistory()) {
                if (condition.toLowerCase().contains("diabetes")) {
                    riskScore += 1;
                    riskFactors.add("Diabetes mellitus");
                }
                if (condition.toLowerCase().contains("hypertension")) {
                    riskScore += 1;
                    riskFactors.add("Hypertension");
                }
                if (condition.toLowerCase().contains("heart")) {
                    riskScore += 2;
                    riskFactors.add("Cardiac history");
                }
            }
        }
        
        // Determine risk level
        String riskLevel;
        if (riskScore >= 6) {
            riskLevel = "HIGH";
        } else if (riskScore >= 3) {
            riskLevel = "MODERATE";
        } else {
            riskLevel = "LOW";
        }
        
        riskAssessment.put("riskScore", riskScore);
        riskAssessment.put("riskLevel", riskLevel);
        riskAssessment.put("riskFactors", riskFactors);
        riskAssessment.put("recommendedAction", getRecommendedAction(riskLevel));
        
        return riskAssessment;
    }
    
    private String getRecommendedAction(String riskLevel) {
        switch (riskLevel) {
            case "HIGH":
                return "Immediate physician evaluation required. Consider emergency department if unstable.";
            case "MODERATE":
                return "Urgent physician evaluation within 24 hours. Monitor closely.";
            case "LOW":
                return "Routine follow-up as scheduled. Continue monitoring symptoms.";
            default:
                return "Standard care protocol.";
        }
    }
    
    private List<String> extractKeyFindings(Map<String, Object> analysisResults) {
        List<String> keyFindings = new ArrayList<>();
        
        // Extract from each analysis module
        if (analysisResults.containsKey("patientIntake")) {
            keyFindings.add("Comprehensive patient history obtained");
        }
        
        if (analysisResults.containsKey("physicalExam")) {
            keyFindings.add("Physical examination findings documented");
        }
        
        if (analysisResults.containsKey("differentialDiagnosis")) {
            Map<String, Object> diagnosis = (Map<String, Object>) analysisResults.get("differentialDiagnosis");
            if (diagnosis.containsKey("differentialDiagnoses")) {
                List<Map<String, Object>> diagnoses = (List<Map<String, Object>>) diagnosis.get("differentialDiagnoses");
                if (!diagnoses.isEmpty()) {
                    keyFindings.add("Primary diagnosis identified: " + diagnoses.get(0).get("diagnosis"));
                }
            }
        }
        
        return keyFindings;
    }
    
    private List<String> generateRecommendations(PatientData patient, Map<String, Object> analysisResults) {
        List<String> recommendations = new ArrayList<>();
        
        recommendations.add("Continue comprehensive medical evaluation");
        recommendations.add("Monitor patient response to treatment");
        recommendations.add("Ensure medication compliance and follow-up");
        recommendations.add("Patient education regarding condition and treatment");
        
        // Risk-based recommendations
        if (patient.getAge() != null && patient.getAge() > 65) {
            recommendations.add("Consider geriatric-specific care protocols");
        }
        
        return recommendations;
    }
    
    private List<String> identifyRedFlags(PatientData patient, Map<String, Object> analysisResults) {
        List<String> redFlags = new ArrayList<>();
        
        // Symptom-based red flags
        if (patient.getSymptoms() != null) {
            for (String symptom : patient.getSymptoms()) {
                if (symptom.toLowerCase().contains("chest pain") && 
                    symptom.toLowerCase().contains("severe")) {
                    redFlags.add("Severe chest pain - rule out acute coronary syndrome");
                }
                if (symptom.toLowerCase().contains("difficulty breathing")) {
                    redFlags.add("Respiratory distress - assess airway and oxygenation");
                }
            }
        }
        
        // Vital sign red flags
        if (patient.getTemperature() != null && patient.getTemperature() > 103.0) {
            redFlags.add("High fever - risk of sepsis or serious infection");
        }
        
        return redFlags;
    }
    
    private List<String> suggestAdditionalTesting(PatientData patient, Map<String, Object> analysisResults) {
        List<String> additionalTests = new ArrayList<>();
        
        // Based on symptoms
        if (patient.getSymptoms() != null) {
            for (String symptom : patient.getSymptoms()) {
                if (symptom.toLowerCase().contains("chest pain")) {
                    additionalTests.add("ECG and cardiac enzymes");
                    additionalTests.add("Chest X-ray");
                }
                if (symptom.toLowerCase().contains("headache")) {
                    additionalTests.add("Neurological imaging if indicated");
                }
            }
        }
        
        // Age-based screening
        if (patient.getAge() != null && patient.getAge() > 50) {
            additionalTests.add("Age-appropriate cancer screening");
            additionalTests.add("Cardiovascular risk assessment");
        }
        
        return additionalTests;
    }
    
    private Map<String, Object> generateFollowUpPlan(PatientData patient, Map<String, Object> analysisResults) {
        Map<String, Object> followUp = new HashMap<>();
        
        followUp.put("timeframe", "1-2 weeks");
        followUp.put("provider", "Primary care physician");
        followUp.put("focus", "Treatment response and symptom monitoring");
        followUp.put("additionalSpecialists", determineSpecialistReferrals(patient, analysisResults));
        
        return followUp;
    }
    
    private List<String> determineSpecialistReferrals(PatientData patient, Map<String, Object> analysisResults) {
        List<String> specialists = new ArrayList<>();
        
        if (patient.getSymptoms() != null) {
            for (String symptom : patient.getSymptoms()) {
                if (symptom.toLowerCase().contains("chest pain")) {
                    specialists.add("Cardiology");
                }
                if (symptom.toLowerCase().contains("headache") && 
                    symptom.toLowerCase().contains("severe")) {
                    specialists.add("Neurology");
                }
            }
        }
        
        return specialists;
    }
    
    private double calculateDecisionConfidence(Map<String, Object> analysisResults) {
        double totalConfidence = 0.0;
        int moduleCount = 0;
        
        // Average confidence from all modules
        for (Map.Entry<String, Object> entry : analysisResults.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> moduleResult = (Map<String, Object>) entry.getValue();
                if (moduleResult.containsKey("confidence")) {
                    totalConfidence += (Double) moduleResult.get("confidence");
                    moduleCount++;
                }
            }
        }
        
        return moduleCount > 0 ? totalConfidence / moduleCount : 0.85;
    }
    
    private double calculateDataCompleteness(PatientData patient) {
        int totalFields = 10; // Total possible data fields
        int completedFields = 0;
        
        if (patient.getName() != null) completedFields++;
        if (patient.getAge() != null) completedFields++;
        if (patient.getGender() != null) completedFields++;
        if (patient.getSymptoms() != null && !patient.getSymptoms().isEmpty()) completedFields++;
        if (patient.getMedicalHistory() != null && !patient.getMedicalHistory().isEmpty()) completedFields++;
        if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) completedFields++;
        if (patient.getCurrentMedications() != null && !patient.getCurrentMedications().isEmpty()) completedFields++;
        if (patient.getHeartRate() != null) completedFields++;
        if (patient.getBloodPressureSystolic() != null) completedFields++;
        if (patient.getTemperature() != null) completedFields++;
        
        return (double) completedFields / totalFields;
    }
}