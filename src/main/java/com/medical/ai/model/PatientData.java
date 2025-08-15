package com.medical.ai.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Core patient data model - FHIR compliant
 * Stores all patient information for AI processing
 */
@Entity
@Table(name = "patients")
public class PatientData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true)
    private String patientId;
    
    @NotBlank
    private String name;
    
    private Integer age;
    private String gender;
    
    @ElementCollection
    @CollectionTable(name = "patient_symptoms")
    private List<String> symptoms;
    
    @ElementCollection
    @CollectionTable(name = "patient_medical_history")
    private List<String> medicalHistory;
    
    @ElementCollection
    @CollectionTable(name = "patient_allergies")
    private List<String> allergies;
    
    @ElementCollection
    @CollectionTable(name = "patient_medications")
    private List<String> currentMedications;
    
    // Vital signs
    private Double heartRate;
    private Double bloodPressureSystolic;
    private Double bloodPressureDiastolic;
    private Double temperature;
    private Double oxygenSaturation;
    private Double respiratoryRate;
    
    // AI Processing Results
    @Column(columnDefinition = "TEXT")
    private String symptomAnalysis;
    
    @Column(columnDefinition = "TEXT")
    private String physicalExamResults;
    
    @Column(columnDefinition = "TEXT")
    private String imagingResults;
    
    @Column(columnDefinition = "TEXT")
    private String labResults;
    
    @Column(columnDefinition = "TEXT")
    private String differentialDiagnosis;
    
    @Column(columnDefinition = "TEXT")
    private String treatmentPlan;
    
    @Column(columnDefinition = "TEXT")
    private String prescriptions;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PatientData() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public PatientData(String patientId, String name, Integer age, String gender) {
        this();
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }
    
    public List<String> getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(List<String> medicalHistory) { this.medicalHistory = medicalHistory; }
    
    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }
    
    public List<String> getCurrentMedications() { return currentMedications; }
    public void setCurrentMedications(List<String> currentMedications) { this.currentMedications = currentMedications; }
    
    public Double getHeartRate() { return heartRate; }
    public void setHeartRate(Double heartRate) { this.heartRate = heartRate; }
    
    public Double getBloodPressureSystolic() { return bloodPressureSystolic; }
    public void setBloodPressureSystolic(Double bloodPressureSystolic) { this.bloodPressureSystolic = bloodPressureSystolic; }
    
    public Double getBloodPressureDiastolic() { return bloodPressureDiastolic; }
    public void setBloodPressureDiastolic(Double bloodPressureDiastolic) { this.bloodPressureDiastolic = bloodPressureDiastolic; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public Double getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(Double oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }
    
    public Double getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(Double respiratoryRate) { this.respiratoryRate = respiratoryRate; }
    
    public String getSymptomAnalysis() { return symptomAnalysis; }
    public void setSymptomAnalysis(String symptomAnalysis) { this.symptomAnalysis = symptomAnalysis; }
    
    public String getPhysicalExamResults() { return physicalExamResults; }
    public void setPhysicalExamResults(String physicalExamResults) { this.physicalExamResults = physicalExamResults; }
    
    public String getImagingResults() { return imagingResults; }
    public void setImagingResults(String imagingResults) { this.imagingResults = imagingResults; }
    
    public String getLabResults() { return labResults; }
    public void setLabResults(String labResults) { this.labResults = labResults; }
    
    public String getDifferentialDiagnosis() { return differentialDiagnosis; }
    public void setDifferentialDiagnosis(String differentialDiagnosis) { this.differentialDiagnosis = differentialDiagnosis; }
    
    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }
    
    public String getPrescriptions() { return prescriptions; }
    public void setPrescriptions(String prescriptions) { this.prescriptions = prescriptions; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}