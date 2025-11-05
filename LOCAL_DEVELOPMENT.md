# Local Development Guide

This guide will help you develop and preview the Heimdall UI locally without needing a Kubernetes cluster or running the full Java backend.

## Overview

Heimdall consists of two main components:
- **Frontend**: Svelte + Vite (development server on port 5173)
- **Backend**: Quarkus Java app (runs on port 8080, connects to Kubernetes)

For UI development, you can run just the frontend with a mock backend server.

---

## Quick Start - Frontend Only Development

### Option 1: Using the Mock Server (Recommended)

This is the fastest way to develop and see your UI changes without any backend dependencies.

#### Prerequisites
- Node.js 16+ and npm

#### Steps

1. **Navigate to the webui directory:**
   ```bash
   cd src/main/webui
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the mock backend server** (in a separate terminal):
   ```bash
   node mock-server.js
   ```

   The mock server will run on port 8080 and serve sample Flink job data.

4. **Start the frontend development server:**
   ```bash
   npm run dev
   ```

   The UI will be available at: **http://localhost:5173**

5. **Make your changes!**
   - Edit files in `src/` directory
   - Changes will hot-reload automatically in your browser
   - No need to restart the server

#### Customizing Mock Data

Edit `mock-data.json` to add/modify sample Flink jobs for testing different scenarios:
- Different job statuses (RUNNING, FAILED, PENDING, etc.)
- Multiple namespaces
- Various resource configurations
- Job metadata and labels

---

### Option 2: Using Vite Proxy (No Mock Server)

If you prefer, you can use Vite's built-in proxy without running a separate mock server.

1. **Create a `vite.config.js` if it doesn't exist:**
   ```javascript
   import { defineConfig } from 'vite'
   import { svelte } from '@sveltejs/vite-plugin-svelte'

   export default defineConfig({
     plugins: [svelte()],
     server: {
       proxy: {
         '/jobs': {
           target: 'http://localhost:8080',
           changeOrigin: true,
         },
         '/config': {
           target: 'http://localhost:8080',
           changeOrigin: true,
         }
       }
     }
   })
   ```

2. Then run either:
   - The mock server on port 8080, OR
   - The full Java backend (see below)

---

## Running the Full Application Locally

If you need to test with real Kubernetes integration or backend logic:

### Prerequisites
- Java 17+
- Gradle (included via wrapper)
- Access to a Kubernetes cluster with Flink Operator deployed
- `kubectl` configured to access your cluster

### Steps

1. **Build and run the backend in dev mode:**
   ```bash
   ./gradlew quarkusDev
   ```

   This will:
   - Start the Quarkus backend on port 8080
   - Automatically rebuild the frontend
   - Enable hot-reload for Java code
   - Connect to your Kubernetes cluster

   Access the full application at: **http://localhost:8080**

2. **Configure Kubernetes access** (if needed):

   Set environment variables:
   ```bash
   export HEIMDALL_JOBLOCATOR_K8S_OPERATOR_NAMESPACE_TO_WATCH=default
   # Or watch multiple namespaces
   export HEIMDALL_JOBLOCATOR_K8S_OPERATOR_NAMESPACE_TO_WATCH=default,prod,staging
   ```

### Frontend Development with Backend Running

If you want to develop the frontend separately while the backend is running:

1. **Terminal 1** - Run the backend:
   ```bash
   ./gradlew quarkusDev
   ```

2. **Terminal 2** - Run the frontend dev server:
   ```bash
   cd src/main/webui
   npm run dev
   ```

   Access the frontend at: **http://localhost:5173** (with hot-reload)
   Backend APIs will be proxied to port 8080 automatically (CORS is pre-configured)

---

## Development Workflow

### Making UI Changes

1. **Edit frontend files:**
   - Components: `src/main/webui/src/lib/*.svelte`
   - Main app: `src/main/webui/src/App.svelte`
   - Styles: `src/main/webui/src/app.css`
   - Tailwind config: `src/main/webui/tailwind.config.js`

2. **Changes auto-reload** in the browser (Vite HMR)

3. **Build for production** (when ready):
   ```bash
   cd src/main/webui
   npm run build
   ```

### Testing Different Scenarios

With the mock server, you can easily test:

**Different Job States:**
- Edit `mock-data.json` to change job statuses
- Add jobs with different configurations
- Test empty states, error states, loading states

**Filter and Search:**
- Add jobs with different names and namespaces
- Test filtering by status and namespace
- Test sorting functionality

**Responsive Design:**
- Use browser dev tools to test different screen sizes
- Test card view and table view layouts

**Dark Mode / Themes:**
- Modify Tailwind config and CSS
- Test color schemes instantly

---

## API Endpoints Reference

The frontend expects these endpoints:

### `GET /jobs`
Returns an array of Flink job objects.

**Example Response:**
```json
[
  {
    "id": "123",
    "name": "my-flink-job",
    "namespace": "default",
    "status": "RUNNING",
    "type": "APPLICATION",
    "parallelism": 4,
    "flinkVersion": "1.17.1",
    "image": "flink:1.17.1-scala_2.12-java11",
    "shortImage": "flink:1.17.1",
    "startTime": 1699564800000,
    "metadata": {
      "env": "production",
      "team": "data-platform"
    },
    "resources": {
      "jm": {
        "replicas": 1,
        "cpu": "1",
        "mem": "2Gi"
      },
      "tm": {
        "replicas": 2,
        "cpu": "2",
        "mem": "4Gi"
      }
    }
  }
]
```

### `GET /config`
Returns application configuration.

**Example Response:**
```json
{
  "appVersion": "0.10.0",
  "patterns": {
    "display-name": "$jobName ($metadata.env)"
  },
  "endpointPathPatterns": {
    "flink-ui": "http://localhost:8081/#/job/$jobName/overview",
    "flink-api": "http://localhost:8081/jobs/$jobName",
    "metrics": "http://localhost:9090/graph?g0.expr=flink_job{job='$jobName'}",
    "logs": "http://localhost:3000/explore?query=$jobName"
  }
}
```

---

## Troubleshooting

### Port Already in Use

If port 5173 or 8080 is already in use:

**Frontend (Vite):**
```bash
npm run dev -- --port 3000
```

**Mock Server:**
Edit `mock-server.js` and change the PORT variable.

### CORS Issues

If you see CORS errors:
- Make sure the backend has `QUARKUS_HTTP_CORS_ORIGINS=http://localhost:5173` set
- Or use the mock server which has CORS enabled by default

### Hot Reload Not Working

1. Make sure you're editing files in the correct directory (`src/main/webui/src/`)
2. Check the terminal for any compilation errors
3. Try restarting the Vite dev server

### Mock Server Not Responding

1. Check if the mock server is running: `curl http://localhost:8080/jobs`
2. Make sure Node.js is installed: `node --version`
3. Check for port conflicts: `lsof -i :8080` (macOS/Linux) or `netstat -ano | findstr :8080` (Windows)

---

## Tips for Faster Development

1. **Use browser DevTools** - Svelte DevTools extension is very helpful
2. **Tailwind IntelliSense** - Install the VS Code extension for autocomplete
3. **Component Isolation** - Test components individually by temporarily hardcoding props
4. **Mock Different States** - Create multiple mock data files (success, error, empty) and swap them
5. **Use `console.log`** - Add logs in Svelte components to debug reactivity

---

## Building for Production

When you're ready to deploy:

1. **Build the full application:**
   ```bash
   ./gradlew build
   ```

   This builds both backend and frontend into a single JAR.

2. **Build Docker image:**
   ```bash
   ./gradlew imageBuild
   ```

3. **Run the JAR:**
   ```bash
   java -jar build/quarkus-app/quarkus-run.jar
   ```

4. **Or run the Docker container:**
   ```bash
   docker run -p 8080:8080 ghcr.io/sap1ens/heimdall:latest
   ```

---

## Additional Resources

- [Svelte Documentation](https://svelte.dev/docs)
- [Vite Documentation](https://vitejs.dev/guide/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Quarkus Dev Mode](https://quarkus.io/guides/dev-mode-differences)
- [Main README](./README.md) - For deployment and configuration

---

## Need Help?

- Check existing issues: https://github.com/sap1ens/heimdall/issues
- Create a new issue if you find a bug or have a feature request
- Refer to the main README for deployment and production configuration
