import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Provider } from 'react-redux';
import { store } from './store/store';

import Layout from './components/Layout/Layout';
import Dashboard from './pages/Dashboard/Dashboard';
import PatientAnalysis from './pages/PatientAnalysis/PatientAnalysis';
import ModelManagement from './pages/ModelManagement/ModelManagement';
import DataManagement from './pages/DataManagement/DataManagement';
import Monitoring from './pages/Monitoring/Monitoring';
import Settings from './pages/Settings/Settings';

const theme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#1976d2',
        },
        secondary: {
            main: '#dc004e',
        },
        background: {
            default: '#f5f5f5',
        },
    },
    typography: {
        fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
        h4: {
            fontWeight: 600,
        },
        h5: {
            fontWeight: 600,
        },
        h6: {
            fontWeight: 600,
        },
    },
    components: {
        MuiCard: {
            styleOverrides: {
                root: {
                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                    borderRadius: 12,
                },
            },
        },
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                    textTransform: 'none',
                    fontWeight: 600,
                },
            },
        },
    },
});

function App() {
    return (
        <Provider store={store}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <Router>
                    <Layout>
                        <Routes>
                            <Route path="/" element={<Dashboard />} />
                            <Route path="/dashboard" element={<Dashboard />} />
                            <Route path="/analysis" element={<PatientAnalysis />} />
                            <Route path="/models" element={<ModelManagement />} />
                            <Route path="/data" element={<DataManagement />} />
                            <Route path="/monitoring" element={<Monitoring />} />
                            <Route path="/settings" element={<Settings />} />
                        </Routes>
                    </Layout>
                </Router>
            </ThemeProvider>
        </Provider>
    );
}

export default App;