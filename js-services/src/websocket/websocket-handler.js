/**
 * WebSocket Handler
 * Manages real-time communication with clients
 */

class WebSocketHandler {
    constructor(io) {
        this.io = io;
        this.connectedClients = new Map();
        this.analysisSubscriptions = new Map();
    }

    handleConnection(socket) {
        console.log(`Client connected: ${socket.id}`);

        this.connectedClients.set(socket.id, {
            socket,
            connectedAt: new Date(),
            subscriptions: new Set()
        });

        // Handle analysis subscription
        socket.on('subscribe-analysis', (data) => {
            this.handleAnalysisSubscription(socket, data);
        });

        // Handle analysis unsubscription
        socket.on('unsubscribe-analysis', (data) => {
            this.handleAnalysisUnsubscription(socket, data);
        });

        // Handle real-time symptom updates
        socket.on('symptom-update', (data) => {
            this.handleSymptomUpdate(socket, data);
        });

        // Handle client ping
        socket.on('ping', () => {
            socket.emit('pong', { timestamp: new Date().toISOString() });
        });
    }

    handleDisconnection(socket) {
        console.log(`Client disconnected: ${socket.id}`);

        // Clean up subscriptions
        const client = this.connectedClients.get(socket.id);
        if (client) {
            client.subscriptions.forEach(analysisId => {
                this.removeAnalysisSubscription(socket.id, analysisId);
            });
        }

        this.connectedClients.delete(socket.id);
    }

    handleAnalysisSubscription(socket, data) {
        const { analysisId, patientId } = data;

        console.log(`Client ${socket.id} subscribing to analysis ${analysisId}`);

        // Add to client subscriptions
        const client = this.connectedClients.get(socket.id);
        if (client) {
            client.subscriptions.add(analysisId);
        }

        // Add to analysis subscriptions
        if (!this.analysisSubscriptions.has(analysisId)) {
            this.analysisSubscriptions.set(analysisId, new Set());
        }
        this.analysisSubscriptions.get(analysisId).add(socket.id);

        // Send confirmation
        socket.emit('subscription-confirmed', {
            analysisId,
            patientId,
            timestamp: new Date().toISOString()
        });
    }

    handleAnalysisUnsubscription(socket, data) {
        const { analysisId } = data;

        console.log(`Client ${socket.id} unsubscribing from analysis ${analysisId}`);

        this.removeAnalysisSubscription(socket.id, analysisId);

        socket.emit('unsubscription-confirmed', {
            analysisId,
            timestamp: new Date().toISOString()
        });
    }

    handleSymptomUpdate(socket, data) {
        const { patientId, symptoms, timestamp } = data;

        console.log(`Received symptom update for patient ${patientId}`);

        // Broadcast to other clients monitoring this patient
        socket.broadcast.emit('symptom-update-broadcast', {
            patientId,
            symptoms,
            timestamp,
            source: socket.id
        });
    }

    removeAnalysisSubscription(socketId, analysisId) {
        // Remove from client subscriptions
        const client = this.connectedClients.get(socketId);
        if (client) {
            client.subscriptions.delete(analysisId);
        }

        // Remove from analysis subscriptions
        const subscribers = this.analysisSubscriptions.get(analysisId);
        if (subscribers) {
            subscribers.delete(socketId);
            if (subscribers.size === 0) {
                this.analysisSubscriptions.delete(analysisId);
            }
        }
    }

    // Emit analysis started event
    emitAnalysisStarted(analysisId, patientId) {
        const subscribers = this.analysisSubscriptions.get(analysisId);
        if (subscribers) {
            subscribers.forEach(socketId => {
                const client = this.connectedClients.get(socketId);
                if (client) {
                    client.socket.emit('analysis-started', {
                        analysisId,
                        patientId,
                        status: 'started',
                        timestamp: new Date().toISOString()
                    });
                }
            });
        }

        // Also broadcast to all connected clients
        this.io.emit('analysis-notification', {
            type: 'started',
            analysisId,
            patientId,
            timestamp: new Date().toISOString()
        });
    }

    // Emit analysis progress event
    emitAnalysisProgress(analysisId, progress) {
        const subscribers = this.analysisSubscriptions.get(analysisId);
        if (subscribers) {
            subscribers.forEach(socketId => {
                const client = this.connectedClients.get(socketId);
                if (client) {
                    client.socket.emit('analysis-progress', {
                        analysisId,
                        progress,
                        timestamp: new Date().toISOString()
                    });
                }
            });
        }
    }

    // Emit analysis completed event
    emitAnalysisCompleted(analysisId, result) {
        const subscribers = this.analysisSubscriptions.get(analysisId);
        if (subscribers) {
            subscribers.forEach(socketId => {
                const client = this.connectedClients.get(socketId);
                if (client) {
                    client.socket.emit('analysis-completed', {
                        analysisId,
                        result,
                        status: 'completed',
                        timestamp: new Date().toISOString()
                    });
                }
            });
        }

        // Also broadcast notification
        this.io.emit('analysis-notification', {
            type: 'completed',
            analysisId,
            timestamp: new Date().toISOString()
        });
    }

    // Emit analysis error event
    emitAnalysisError(analysisId, error) {
        const subscribers = this.analysisSubscriptions.get(analysisId);
        if (subscribers) {
            subscribers.forEach(socketId => {
                const client = this.connectedClients.get(socketId);
                if (client) {
                    client.socket.emit('analysis-error', {
                        analysisId,
                        error: error.message,
                        status: 'error',
                        timestamp: new Date().toISOString()
                    });
                }
            });
        }
    }

    // Get connection statistics
    getConnectionStats() {
        return {
            totalConnections: this.connectedClients.size,
            activeAnalyses: this.analysisSubscriptions.size,
            timestamp: new Date().toISOString()
        };
    }

    // Broadcast system status update
    broadcastSystemStatus(status) {
        this.io.emit('system-status', {
            status,
            timestamp: new Date().toISOString()
        });
    }
}

module.exports = WebSocketHandler;