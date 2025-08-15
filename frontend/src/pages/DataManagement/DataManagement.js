import React from 'react';
import { Typography, Card, CardContent, Alert } from '@mui/material';

export default function DataManagement() {
    return (
        <div>
            <Typography variant="h4" gutterBottom>
                Data Management
            </Typography>
            <Card>
                <CardContent>
                    <Alert severity="info">
                        Data management interface coming soon. This will allow you to:
                        <ul>
                            <li>Upload and manage datasets</li>
                            <li>Data quality monitoring</li>
                            <li>Data pipeline management</li>
                            <li>Data lineage tracking</li>
                        </ul>
                    </Alert>
                </CardContent>
            </Card>
        </div>
    );
}