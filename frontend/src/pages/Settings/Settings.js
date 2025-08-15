import React from 'react';
import { Typography, Card, CardContent, Alert } from '@mui/material';

export default function Settings() {
    return (
        <div>
            <Typography variant="h4" gutterBottom>
                Settings
            </Typography>
            <Card>
                <CardContent>
                    <Alert severity="info">
                        Settings interface coming soon. This will allow you to:
                        <ul>
                            <li>Configure system settings</li>
                            <li>Manage user preferences</li>
                            <li>API configuration</li>
                            <li>Security settings</li>
                        </ul>
                    </Alert>
                </CardContent>
            </Card>
        </div>
    );
}