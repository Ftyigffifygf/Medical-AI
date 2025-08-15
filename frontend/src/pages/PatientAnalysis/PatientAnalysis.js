import React, { useState } from 'react';
import {
    Box,
    Card,
    CardContent,
    Typography,
    TextField,
    Button,
    Grid,
    Alert,
    CircularProgress,
    Chip,
    Divider,
} from '@mui/material';

export default function PatientAnalysis() {
    const [patientData, setPatientData] = useState({
        patient_id: '',
        name: '',
        age: '',
        gender: '',
        symptoms: '',
        medical_history: '',
        allergies: '',
        current_medications: '',
    });

    const [analysisResult, setAnalysisResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleInputChange = (field) => (event) => {
        setPatientData({
            ...patientData,
            [field]: event.target.value,
        });
    };

    const handleAnalyze = async () => {
        setLoading(true);
        setError(null);

        try {
            const requestData = {
                patient_data: {
                    ...patientData,
                    symptoms: patientData.symptoms.split(',').map(s => s.trim()).filter(s => s),
                    medical_history: patientData.medical_history.split(',').map(s => s.trim()).filter(s => s),
                    allergies: patientData.allergies.split(',').map(s => s.trim()).filter(s => s),
                    current_medications: patientData.current_medications.split(',').map(s => s.trim()).filter(s => s),
                    age: patientData.age ? parseInt(patientData.age) : null,
                },
                analysis_type: 'complete',
                priority: 'normal',
            };

            const response = await fetch('/python/analyze', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData),
            });

            if (!response.ok) {
                throw new Error('Analysis failed');
            }

            const result = await response.json();
            setAnalysisResult(result);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Patient Analysis
            </Typography>

            <Grid container spacing={3}>
                {/* Patient Data Input */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Patient Information
                            </Typography>

                            <Grid container spacing={2}>
                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Patient ID"
                                        value={patientData.patient_id}
                                        onChange={handleInputChange('patient_id')}
                                        required
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Patient Name"
                                        value={patientData.name}
                                        onChange={handleInputChange('name')}
                                        required
                                    />
                                </Grid>

                                <Grid item xs={6}>
                                    <TextField
                                        fullWidth
                                        label="Age"
                                        type="number"
                                        value={patientData.age}
                                        onChange={handleInputChange('age')}
                                    />
                                </Grid>

                                <Grid item xs={6}>
                                    <TextField
                                        fullWidth
                                        label="Gender"
                                        value={patientData.gender}
                                        onChange={handleInputChange('gender')}
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Symptoms (comma-separated)"
                                        multiline
                                        rows={3}
                                        value={patientData.symptoms}
                                        onChange={handleInputChange('symptoms')}
                                        placeholder="fever, headache, fatigue"
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Medical History (comma-separated)"
                                        multiline
                                        rows={2}
                                        value={patientData.medical_history}
                                        onChange={handleInputChange('medical_history')}
                                        placeholder="diabetes, hypertension"
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Allergies (comma-separated)"
                                        value={patientData.allergies}
                                        onChange={handleInputChange('allergies')}
                                        placeholder="penicillin, peanuts"
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Current Medications (comma-separated)"
                                        multiline
                                        rows={2}
                                        value={patientData.current_medications}
                                        onChange={handleInputChange('current_medications')}
                                        placeholder="metformin, lisinopril"
                                    />
                                </Grid>

                                <Grid item xs={12}>
                                    <Button
                                        variant="contained"
                                        color="primary"
                                        onClick={handleAnalyze}
                                        disabled={loading || !patientData.patient_id || !patientData.name}
                                        fullWidth
                                        size="large"
                                    >
                                        {loading ? <CircularProgress size={24} /> : 'Analyze Patient'}
                                    </Button>
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Analysis Results */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Analysis Results
                            </Typography>

                            {error && (
                                <Alert severity="error" sx={{ mb: 2 }}>
                                    {error}
                                </Alert>
                            )}

                            {loading && (
                                <Box display="flex" justifyContent="center" p={4}>
                                    <CircularProgress />
                                    <Typography variant="body2" sx={{ ml: 2 }}>
                                        Analyzing patient data...
                                    </Typography>
                                </Box>
                            )}

                            {analysisResult && (
                                <Box>
                                    <Typography variant="subtitle1" gutterBottom>
                                        Analysis ID: {analysisResult.analysis_id}
                                    </Typography>

                                    <Typography variant="body2" color="textSecondary" gutterBottom>
                                        Status: {analysisResult.status}
                                    </Typography>

                                    <Typography variant="body2" color="textSecondary" gutterBottom>
                                        Confidence Score: {(analysisResult.confidence_score * 100).toFixed(1)}%
                                    </Typography>

                                    <Typography variant="body2" color="textSecondary" gutterBottom>
                                        Processing Time: {analysisResult.processing_time.toFixed(2)}s
                                    </Typography>

                                    <Divider sx={{ my: 2 }} />

                                    {analysisResult.models_used && analysisResult.models_used.length > 0 && (
                                        <Box mb={2}>
                                            <Typography variant="subtitle2" gutterBottom>
                                                Models Used:
                                            </Typography>
                                            <Box display="flex" gap={1} flexWrap="wrap">
                                                {analysisResult.models_used.map((model, index) => (
                                                    <Chip key={index} label={model} size="small" />
                                                ))}
                                            </Box>
                                        </Box>
                                    )}

                                    {analysisResult.results && (
                                        <Box>
                                            <Typography variant="subtitle2" gutterBottom>
                                                Analysis Summary:
                                            </Typography>
                                            <Typography variant="body2">
                                                {JSON.stringify(analysisResult.results, null, 2)}
                                            </Typography>
                                        </Box>
                                    )}
                                </Box>
                            )}

                            {!analysisResult && !loading && !error && (
                                <Typography variant="body2" color="textSecondary" textAlign="center" py={4}>
                                    Enter patient information and click "Analyze Patient" to get started.
                                </Typography>
                            )}
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
}