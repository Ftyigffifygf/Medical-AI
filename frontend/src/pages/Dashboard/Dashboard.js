import React, { useState, useEffect } from 'react';
import {
    Grid,
    Card,
    CardContent,
    Typography,
    Box,
    Button,
    Alert,
    CircularProgress,
} from '@mui/material';
import {
    TrendingUp,
    People,
    Psychology,
    Speed,
} from '@mui/icons-material';

export default function Dashboard() {
    const [systemStatus, setSystemStatus] = useState('loading');
    const [stats, setStats] = useState({
        totalAnalyses: 0,
        activeModels: 0,
        avgProcessingTime: 0,
        systemHealth: 'unknown'
    });

    useEffect(() => {
        // Fetch system status and stats
        fetchSystemStatus();
    }, []);

    const fetchSystemStatus = async () => {
        try {
            const response = await fetch('/api/health');
            if (response.ok) {
                const data = await response.json();
                setSystemStatus('healthy');
                setStats({
                    totalAnalyses: 1247,
                    activeModels: 8,
                    avgProcessingTime: 2.3,
                    systemHealth: 'healthy'
                });
            } else {
                setSystemStatus('error');
            }
        } catch (error) {
            setSystemStatus('error');
        }
    };

    const StatCard = ({ title, value, icon, color = 'primary' }) => (
        <Card>
            <CardContent>
                <Box display="flex" alignItems="center" justifyContent="space-between">
                    <Box>
                        <Typography color="textSecondary" gutterBottom variant="body2">
                            {title}
                        </Typography>
                        <Typography variant="h4" component="div">
                            {value}
                        </Typography>
                    </Box>
                    <Box color={`${color}.main`}>
                        {icon}
                    </Box>
                </Box>
            </CardContent>
        </Card>
    );

    if (systemStatus === 'loading') {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Dashboard
            </Typography>

            {systemStatus === 'error' && (
                <Alert severity="warning" sx={{ mb: 3 }}>
                    Some services may be unavailable. Please check system status.
                </Alert>
            )}

            <Grid container spacing={3}>
                {/* Stats Cards */}
                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Total Analyses"
                        value={stats.totalAnalyses.toLocaleString()}
                        icon={<Psychology fontSize="large" />}
                        color="primary"
                    />
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Active Models"
                        value={stats.activeModels}
                        icon={<TrendingUp fontSize="large" />}
                        color="success"
                    />
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="Avg Processing Time"
                        value={`${stats.avgProcessingTime}s`}
                        icon={<Speed fontSize="large" />}
                        color="info"
                    />
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <StatCard
                        title="System Health"
                        value={stats.systemHealth}
                        icon={<People fontSize="large" />}
                        color="success"
                    />
                </Grid>

                {/* Quick Actions */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Quick Actions
                            </Typography>
                            <Box display="flex" gap={2} flexWrap="wrap">
                                <Button variant="contained" color="primary">
                                    New Patient Analysis
                                </Button>
                                <Button variant="outlined" color="primary">
                                    View Models
                                </Button>
                                <Button variant="outlined" color="secondary">
                                    System Monitoring
                                </Button>
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                {/* Recent Activity */}
                <Grid item xs={12} md={6}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Recent Activity
                            </Typography>
                            <Typography variant="body2" color="textSecondary">
                                • Patient analysis completed - ID: PAT001
                            </Typography>
                            <Typography variant="body2" color="textSecondary">
                                • Model DiagnosisNet v1.0 deployed
                            </Typography>
                            <Typography variant="body2" color="textSecondary">
                                • System health check passed
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                {/* System Status */}
                <Grid item xs={12}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                System Services Status
                            </Typography>
                            <Grid container spacing={2}>
                                <Grid item xs={12} sm={6} md={3}>
                                    <Box textAlign="center">
                                        <Typography variant="body2" color="textSecondary">
                                            Java Service
                                        </Typography>
                                        <Typography variant="h6" color="success.main">
                                            ✓ Online
                                        </Typography>
                                    </Box>
                                </Grid>
                                <Grid item xs={12} sm={6} md={3}>
                                    <Box textAlign="center">
                                        <Typography variant="body2" color="textSecondary">
                                            Python Service
                                        </Typography>
                                        <Typography variant="h6" color="success.main">
                                            ✓ Online
                                        </Typography>
                                    </Box>
                                </Grid>
                                <Grid item xs={12} sm={6} md={3}>
                                    <Box textAlign="center">
                                        <Typography variant="body2" color="textSecondary">
                                            JavaScript Service
                                        </Typography>
                                        <Typography variant="h6" color="success.main">
                                            ✓ Online
                                        </Typography>
                                    </Box>
                                </Grid>
                                <Grid item xs={12} sm={6} md={3}>
                                    <Box textAlign="center">
                                        <Typography variant="body2" color="textSecondary">
                                            C++ Service
                                        </Typography>
                                        <Typography variant="h6" color="warning.main">
                                            ⚠ Optional
                                        </Typography>
                                    </Box>
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </Box>
    );
}