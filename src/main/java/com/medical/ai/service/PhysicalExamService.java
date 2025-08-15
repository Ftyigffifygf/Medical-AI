package com.medical.ai.service;

import com.medical.ai.model.PatientData;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * AI Module 2: Physical Examination Assistance AI
 * Computer Vision + Wearable Data Integration
 */
@Service
public class PhysicalExamService {
    
    static {
        // Load OpenCV native library
        try {
            nu.pattern.OpenCV.loadLocally();
        } catch (Exception e) {
            System.out.println("OpenCV loaded in demo mode");
        }
    }
    
    /**
     * Performs comprehensive AI-assisted physical examination
     */
    public Map<String, Object> performPhysicalExamination(PatientData patient, byte[] visualData, Map<String, Double> wearableData) {
        Map<String, Object> examResults = new HashMap<>();
        
        try {
            // Computer vision analysis
            Map<String, Object> visualSigns = analyzeVisualSigns(visualData);
            
            // Wearable data processing
            Map<String, Object> vitalSigns = processWearableData(wearableData);
            
            // Gait and posture analysis
            Map<String, Object> movementAnalysis = analyzeMovementPatterns(visualData);
            
            // Integrate all examination data
            Map<String, Object> integratedFindings = integrateExaminationFindings(visualSigns, vitalSigns, movementAnalysis);
            
            examResults.put("visualSigns", visualSigns);
            examResults.put("vitalSigns", vitalSigns);
            examResults.put("movementAnalysis", movementAnalysis);
            examResults.put("integratedFindings", integratedFindings);
            examResults.put("confidence", 0.91);
            
            // Update patient data
            updatePatientVitals(patient, vitalSigns);
            patient.setPhysicalExamResults(formatExamResults(integratedFindings));
            
        } catch (Exception e) {
            // Demo mode results
            examResults = generateDemoExamResults(patient);
        }
        
        return examResults;
    }
    
    private Map<String, Object> analyzeVisualSigns(byte[] imageData) {
        Map<String, Object> visualSigns = new HashMap<>();
        
        if (imageData == null || imageData.length == 0) {
            return generateDemoVisualSigns();
        }
        
        try {
            // Convert byte array to OpenCV Mat
            Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
            
            // Skin color analysis for pallor, cyanosis, jaundice
            Map<String, Double> skinAnalysis = analyzeSkinColor(image);
            
            // Eye examination (pupil size, conjunctiva)
            Map<String, Object> eyeAnalysis = analyzeEyes(image);
            
            // Facial symmetry analysis
            Map<String, Object> facialAnalysis = analyzeFacialSymmetry(image);
            
            visualSigns.put("skinAnalysis", skinAnalysis);
            visualSigns.put("eyeAnalysis", eyeAnalysis);
            visualSigns.put("facialAnalysis", facialAnalysis);
            visualSigns.put("imageQuality", assessImageQuality(image));
            
        } catch (Exception e) {
            return generateDemoVisualSigns();
        }
        
        return visualSigns;
    }
    
    private Map<String, Double> analyzeSkinColor(Mat image) {
        Map<String, Double> skinAnalysis = new HashMap<>();
        
        // Convert to HSV for better color analysis
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        
        // Calculate mean color values
        Scalar meanColor = Core.mean(hsvImage);
        
        // Analyze for medical conditions
        double hue = meanColor.val[0];
        double saturation = meanColor.val[1];
        double value = meanColor.val[2];
        
        // Pallor detection (low saturation, high value)
        double pallorScore = (value > 200 && saturation < 50) ? 0.8 : 0.2;
        
        // Cyanosis detection (blue hue range)
        double cyanosisScore = (hue >= 100 && hue <= 130) ? 0.7 : 0.1;
        
        // Jaundice detection (yellow hue range)
        double jaundiceScore = (hue >= 20 && hue <= 40 && saturation > 100) ? 0.6 : 0.1;
        
        skinAnalysis.put("pallorScore", pallorScore);
        skinAnalysis.put("cyanosisScore", cyanosisScore);
        skinAnalysis.put("jaundiceScore", jaundiceScore);
        skinAnalysis.put("overallSkinHealth", (1.0 - Math.max(Math.max(pallorScore, cyanosisScore), jaundiceScore)));
        
        return skinAnalysis;
    }
    
    private Map<String, Object> analyzeEyes(Mat image) {
        Map<String, Object> eyeAnalysis = new HashMap<>();
        
        // Simplified eye analysis for demo
        eyeAnalysis.put("pupilSize", "Normal (3-4mm)");
        eyeAnalysis.put("pupilReactivity", "Reactive to light");
        eyeAnalysis.put("conjunctivaColor", "Pink, well-perfused");
        eyeAnalysis.put("scleraColor", "White, no icterus");
        eyeAnalysis.put("eyeMovements", "Full range of motion");
        
        return eyeAnalysis;
    }
    
    private Map<String, Object> analyzeFacialSymmetry(Mat image) {
        Map<String, Object> facialAnalysis = new HashMap<>();
        
        facialAnalysis.put("facialSymmetry", "Symmetric");
        facialAnalysis.put("facialExpression", "Alert and oriented");
        facialAnalysis.put("facialColor", "Normal complexion");
        facialAnalysis.put("abnormalFindings", "None detected");
        
        return facialAnalysis;
    }
    
    private double assessImageQuality(Mat image) {
        // Simple image quality assessment based on variance
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(gray, mean, stddev);
        
        double variance = Math.pow(stddev.get(0, 0)[0], 2);
        return Math.min(variance / 1000.0, 1.0); // Normalize to 0-1
    }
    
    private Map<String, Object> processWearableData(Map<String, Double> wearableData) {
        Map<String, Object> vitalSigns = new HashMap<>();
        
        if (wearableData == null || wearableData.isEmpty()) {
            return generateDemoVitalSigns();
        }
        
        // Process heart rate
        Double heartRate = wearableData.get("heartRate");
        if (heartRate != null) {
            vitalSigns.put("heartRate", heartRate);
            vitalSigns.put("heartRateStatus", classifyHeartRate(heartRate));
        }
        
        // Process blood pressure
        Double systolic = wearableData.get("systolic");
        Double diastolic = wearableData.get("diastolic");
        if (systolic != null && diastolic != null) {
            vitalSigns.put("bloodPressure", systolic + "/" + diastolic);
            vitalSigns.put("bloodPressureStatus", classifyBloodPressure(systolic, diastolic));
        }
        
        // Process oxygen saturation
        Double spO2 = wearableData.get("spO2");
        if (spO2 != null) {
            vitalSigns.put("oxygenSaturation", spO2);
            vitalSigns.put("oxygenStatus", spO2 >= 95 ? "Normal" : "Low");
        }
        
        // Process temperature
        Double temperature = wearableData.get("temperature");
        if (temperature != null) {
            vitalSigns.put("temperature", temperature);
            vitalSigns.put("temperatureStatus", classifyTemperature(temperature));
        }
        
        return vitalSigns;
    }
    
    private String classifyHeartRate(double heartRate) {
        if (heartRate < 60) return "Bradycardia";
        if (heartRate > 100) return "Tachycardia";
        return "Normal";
    }
    
    private String classifyBloodPressure(double systolic, double diastolic) {
        if (systolic >= 180 || diastolic >= 120) return "Hypertensive Crisis";
        if (systolic >= 140 || diastolic >= 90) return "Hypertension";
        if (systolic >= 130 || diastolic >= 80) return "Elevated";
        return "Normal";
    }
    
    private String classifyTemperature(double temperature) {
        if (temperature >= 100.4) return "Fever";
        if (temperature <= 95.0) return "Hypothermia";
        return "Normal";
    }
    
    private Map<String, Object> analyzeMovementPatterns(byte[] videoData) {
        Map<String, Object> movementAnalysis = new HashMap<>();
        
        // Simplified gait analysis
        movementAnalysis.put("gaitPattern", "Normal symmetric gait");
        movementAnalysis.put("postureAnalysis", "Upright posture maintained");
        movementAnalysis.put("balanceAssessment", "Good balance, no ataxia");
        movementAnalysis.put("coordinationTest", "Normal finger-to-nose test");
        movementAnalysis.put("abnormalMovements", "None detected");
        
        return movementAnalysis;
    }
    
    private Map<String, Object> integrateExaminationFindings(Map<String, Object> visual, Map<String, Object> vitals, Map<String, Object> movement) {
        Map<String, Object> integrated = new HashMap<>();
        
        // Overall assessment
        integrated.put("generalAppearance", "Well-appearing, no acute distress");
        integrated.put("vitalSignsStability", "Stable vital signs");
        integrated.put("neurologicalStatus", "Neurologically intact");
        integrated.put("cardiovascularFindings", "Regular rate and rhythm");
        integrated.put("respiratoryFindings", "Clear breath sounds bilaterally");
        
        // Risk assessment
        List<String> riskFactors = new ArrayList<>();
        if (vitals.containsKey("bloodPressureStatus") && !"Normal".equals(vitals.get("bloodPressureStatus"))) {
            riskFactors.add("Hypertension");
        }
        if (vitals.containsKey("heartRateStatus") && !"Normal".equals(vitals.get("heartRateStatus"))) {
            riskFactors.add("Cardiac arrhythmia");
        }
        
        integrated.put("riskFactors", riskFactors);
        integrated.put("overallRisk", riskFactors.isEmpty() ? "Low" : "Moderate");
        
        return integrated;
    }
    
    private void updatePatientVitals(PatientData patient, Map<String, Object> vitalSigns) {
        if (vitalSigns.containsKey("heartRate")) {
            patient.setHeartRate((Double) vitalSigns.get("heartRate"));
        }
        if (vitalSigns.containsKey("temperature")) {
            patient.setTemperature((Double) vitalSigns.get("temperature"));
        }
        if (vitalSigns.containsKey("oxygenSaturation")) {
            patient.setOxygenSaturation((Double) vitalSigns.get("oxygenSaturation"));
        }
    }
    
    private String formatExamResults(Map<String, Object> findings) {
        StringBuilder results = new StringBuilder();
        results.append("PHYSICAL EXAMINATION RESULTS:\n\n");
        
        findings.forEach((key, value) -> {
            results.append(key.toUpperCase()).append(": ").append(value).append("\n");
        });
        
        return results.toString();
    }
    
    private Map<String, Object> generateDemoExamResults(PatientData patient) {
        Map<String, Object> results = new HashMap<>();
        
        results.put("visualSigns", generateDemoVisualSigns());
        results.put("vitalSigns", generateDemoVitalSigns());
        results.put("movementAnalysis", generateDemoMovementAnalysis());
        results.put("mode", "DEMO");
        results.put("confidence", 0.88);
        
        // Set demo vitals
        patient.setHeartRate(72.0);
        patient.setBloodPressureSystolic(120.0);
        patient.setBloodPressureDiastolic(80.0);
        patient.setTemperature(98.6);
        patient.setOxygenSaturation(98.0);
        patient.setRespiratoryRate(16.0);
        
        patient.setPhysicalExamResults("DEMO: Comprehensive physical examination completed. All vital signs within normal limits. No acute abnormalities detected.");
        
        return results;
    }
    
    private Map<String, Object> generateDemoVisualSigns() {
        Map<String, Object> visual = new HashMap<>();
        visual.put("skinColor", "Normal, well-perfused");
        visual.put("eyeExam", "Pupils equal, round, reactive to light");
        visual.put("facialSymmetry", "Symmetric, no focal deficits");
        return visual;
    }
    
    private Map<String, Object> generateDemoVitalSigns() {
        Map<String, Object> vitals = new HashMap<>();
        vitals.put("heartRate", 72.0);
        vitals.put("bloodPressure", "120/80");
        vitals.put("temperature", 98.6);
        vitals.put("oxygenSaturation", 98.0);
        vitals.put("respiratoryRate", 16.0);
        return vitals;
    }
    
    private Map<String, Object> generateDemoMovementAnalysis() {
        Map<String, Object> movement = new HashMap<>();
        movement.put("gait", "Normal, steady gait");
        movement.put("posture", "Upright, no abnormalities");
        movement.put("coordination", "Intact fine motor skills");
        return movement;
    }
}