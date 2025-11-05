/**
 * Simple Mock Server for Heimdall Frontend Development
 *
 * This server mocks the backend API endpoints so you can develop
 * the frontend without needing Kubernetes or the Java backend.
 *
 * Usage: node mock-server.js
 */

import http from 'http';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 8080;
const MOCK_DATA_FILE = path.join(__dirname, 'mock-data.json');

// Load mock data
let mockData;
try {
    const data = fs.readFileSync(MOCK_DATA_FILE, 'utf8');
    mockData = JSON.parse(data);
    console.log(`✅ Loaded mock data from ${MOCK_DATA_FILE}`);
} catch (err) {
    console.error(`❌ Error loading mock data: ${err.message}`);
    console.error('Make sure mock-data.json exists in the webui directory');
    process.exit(1);
}

const server = http.createServer((req, res) => {
    // Enable CORS for local development
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    // Handle preflight requests
    if (req.method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }

    // Log request
    console.log(`${req.method} ${req.url}`);

    // Route handlers
    if (req.url === '/jobs' && req.method === 'GET') {
        // Return mock jobs
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(mockData.jobs));
    }
    else if (req.url === '/config' && req.method === 'GET') {
        // Return mock config
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(mockData.config));
    }
    else if (req.url === '/health' && req.method === 'GET') {
        // Health check endpoint
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ status: 'UP', message: 'Mock server is running' }));
    }
    else {
        // 404 for unknown routes
        res.writeHead(404, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            error: 'Not Found',
            message: `Route ${req.url} not found`,
            availableRoutes: ['/jobs', '/config', '/health']
        }));
    }
});

server.listen(PORT, () => {
    console.log('\n🚀 Heimdall Mock Server is running!\n');
    console.log(`   Local:   http://localhost:${PORT}`);
    console.log(`   Jobs:    http://localhost:${PORT}/jobs`);
    console.log(`   Config:  http://localhost:${PORT}/config`);
    console.log(`   Health:  http://localhost:${PORT}/health\n`);
    console.log('📝 Edit mock-data.json to change the data\n');
    console.log('Press Ctrl+C to stop\n');
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n\n👋 Shutting down mock server...');
    server.close(() => {
        console.log('✅ Server stopped');
        process.exit(0);
    });
});

// Handle file watch (optional - reload data when file changes)
if (fs.watch) {
    fs.watch(MOCK_DATA_FILE, (eventType) => {
        if (eventType === 'change') {
            try {
                const data = fs.readFileSync(MOCK_DATA_FILE, 'utf8');
                mockData = JSON.parse(data);
                console.log('🔄 Mock data reloaded!');
            } catch (err) {
                console.error('❌ Error reloading mock data:', err.message);
            }
        }
    });
    console.log('👀 Watching mock-data.json for changes...\n');
}
