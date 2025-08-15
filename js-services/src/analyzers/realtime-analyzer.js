/**
 * Real-time Analyzer
 * Handles real-time symptom analysis and risk assessment
 */

class RealtimeAnalyzer {
    constructor() {
        this.isInitialized = false;
        this.symptomDatabase = this.initializeSymptomDatabase();
    }

    async initialize() {
        console.log('Initializing Realtime Analyzer...');
        this.isInitialized = true;
        console.log('Realtime Analyzer initialized successfully');
    }

    isHealthy() {
        return this.isInitialized;
    }

    async analyzeSymptoms(symptoms, patientContext = {}) {
        console.log(`Analyzing symptoms: ${symptoms.join(', ')}`);

        // Simulate real-time analysis
        await this.simulateProcessing(500);

        const analysis = {
            symptoms: symptoms,
            urgencyLevel: this.assessUrgency(symptoms),
            possibleConditions: this.matchConditions(symptoms),
            recommendations: this.generateRecommendations(symptoms, patientContext),
            riskFactors: this.assessRiskFactors(symptoms, patientContext),
            confidence: this.calculateConfidence(symptoms),
            processingTime: 0.5,
            timestamp: new Date().toISOString()
        };

        return analysis;
    }

    assessUrgency(symptoms) {
        const urgentSymptoms = [
            'chest pain',
            'difficulty breathing',
            'severe headache',
            'loss of consciousness',
            'severe bleeding',
            'high fever'
        ];

        const hasUrgentSymptom = symptoms.some(symptom =>
            urgentSymptoms.some(urgent =>
                symptom.toLowerCase().includes(urgent.toLowerCase())
            )
        );

        if (hasUrgentSymptom) return 'urgent';
        if (symptoms.length > 5) return 'moderate';
        return 'low';
    }

    matchConditions(symptoms) {
        const conditions = [];

        // Simple symptom matching logic
        if (symptoms.some(s => s.toLowerCase().includes('fever')) &&
            symptoms.some(s => s.toLowerCase().includes('cough'))) {
            conditions.push({
                condition: 'Upper Respiratory Infection',
                probability: 0.75,
                matchedSymptoms: symptoms.filter(s =>
                    s.toLowerCase().includes('fever') ||
                    s.toLowerCase().includes('cough')
                )
            });
        }

        if (symptoms.some(s => s.toLowerCase().includes('headache'))) {
            conditions.push({
                condition: 'Tension Headache',
                probability: 0.60,
                matchedSymptoms: symptoms.filter(s =>
                    s.toLowerCase().includes('headache')
                )
            });
        }

        if (symptoms.some(s => s.toLowerCase().includes('nausea')) &&
            symptoms.some(s => s.toLowerCase().includes('vomiting'))) {
            conditions.push({
                condition: 'Gastroenteritis',
                probability: 0.65,
                matchedSymptoms: symptoms.filter(s =>
                    s.toLowerCase().includes('nausea') ||
                    s.toLowerCase().includes('vomiting')
                )
            });
        }

        return conditions.sort((a, b) => b.probability - a.probability);
    }

    generateRecommendations(symptoms, patientContext) {
        const recommendations = [];
        const urgency = this.assessUrgency(symptoms);

        if (urgency === 'urgent') {
            recommendations.push('Seek immediate medical attention');
            recommendations.push('Consider calling emergency services');
        } else if (urgency === 'moderate') {
            recommendations.push('Schedule appointment with healthcare provider');
            recommendations.push('Monitor symptoms closely');
        } else {
            recommendations.push('Rest and stay hydrated');
            recommendations.push('Monitor symptoms for changes');
            recommendations.push('Consider over-the-counter remedies if appropriate');
        }

        // Add specific recommendations based on symptoms
        if (symptoms.some(s => s.toLowerCase().includes('fever'))) {
            recommendations.push('Take temperature regularly');
            recommendations.push('Use fever-reducing medication if needed');
        }

        if (symptoms.some(s => s.toLowerCase().includes('cough'))) {
            recommendations.push('Stay hydrated to help thin mucus');
            recommendations.push('Consider cough suppressants if dry cough');
        }

        return recommendations;
    }

    assessRiskFactors(symptoms, patientContext) {
        const riskFactors = [];

        if (patientContext.age && patientContext.age > 65) {
            riskFactors.push('Advanced age increases risk of complications');
        }

        if (patientContext.chronicConditions) {
            riskFactors.push('Chronic conditions may complicate treatment');
        }

        if (symptoms.length > 7) {
            riskFactors.push('Multiple symptoms may indicate complex condition');
        }

        return riskFactors;
    }

    calculateConfidence(symptoms) {
        // Simple confidence calculation based on symptom clarity and count
        let confidence = 0.5; // Base confidence

        if (symptoms.length >= 3) confidence += 0.2;
        if (symptoms.length >= 5) confidence += 0.1;

        // Reduce confidence for vague symptoms
        const vagueSymptoms = ['pain', 'discomfort', 'feeling unwell'];
        const hasVagueSymptoms = symptoms.some(symptom =>
            vagueSymptoms.some(vague =>
                symptom.toLowerCase().includes(vague)
            )
        );

        if (hasVagueSymptoms) confidence -= 0.15;

        return Math.max(0.3, Math.min(0.95, confidence));
    }

    initializeSymptomDatabase() {
        return {
            'fever': { urgency: 'moderate', commonConditions: ['infection', 'flu'] },
            'cough': { urgency: 'low', commonConditions: ['cold', 'bronchitis'] },
            'headache': { urgency: 'low', commonConditions: ['tension', 'migraine'] },
            'chest pain': { urgency: 'urgent', commonConditions: ['cardiac', 'pulmonary'] },
            'difficulty breathing': { urgency: 'urgent', commonConditions: ['asthma', 'pneumonia'] }
        };
    }

    async simulateProcessing(duration) {
        return new Promise(resolve => setTimeout(resolve, duration));
    }
}

module.exports = RealtimeAnalyzer;