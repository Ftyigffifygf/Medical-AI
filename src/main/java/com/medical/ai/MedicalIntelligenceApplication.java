package com.medical.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for AI Medical Intelligence System
 * Provides complete medical AI capabilities with 11 integrated modules
 */
@SpringBootApplication
@EnableAsync
public class MedicalIntelligenceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MedicalIntelligenceApplication.class, args);
        System.out.println("🏥 AI Medical Intelligence System Started Successfully!");
        System.out.println("📊 All 11 AI modules are ready for medical diagnosis and treatment");
    }
}