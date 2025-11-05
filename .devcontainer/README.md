# VS Code Dev Container

This directory contains the VS Code Dev Container configuration for Heimdall development.

## What is a Dev Container?

A Dev Container is a fully configured development environment running in a Docker container. It includes all the tools, dependencies, and VS Code extensions needed for Heimdall development.

## Prerequisites

- **Docker Desktop** (or Docker Engine + Docker Compose)
- **Visual Studio Code**
- **Dev Containers extension** (`ms-vscode-remote.remote-containers`)

## Getting Started

### Option 1: Open in Dev Container (Recommended)

1. Open this repository in VS Code
2. Click the green button in the bottom-left corner (or press `F1`)
3. Select "Dev Containers: Reopen in Container"
4. Wait for the container to build and start (first time takes 5-10 minutes)
5. VS Code will reload inside the container with all tools ready

### Option 2: Clone in Container Volume

1. Press `F1` in VS Code
2. Select "Dev Containers: Clone Repository in Container Volume"
3. Enter the repository URL: `https://github.com/next-govejero/heimdall.git`
4. Wait for the container to build
5. Start developing!

## What's Included

### Languages & Runtimes
- ☕ Java 17 (OpenJDK)
- 📦 Node.js 20
- 🐘 Gradle 8.1.1

### Tools
- Git
- Docker CLI (for Docker-in-Docker)
- npm (comes with Node.js)

### VS Code Extensions

**Java Development:**
- Red Hat Java Extension Pack
- Gradle for Java
- Java Debugger
- Java Test Runner

**Frontend Development:**
- Svelte for VS Code
- ESLint
- Prettier
- Tailwind CSS IntelliSense

**General:**
- GitLens
- Docker
- EditorConfig
- Code Spell Checker

## Ports

The following ports are automatically forwarded:

| Port | Service | Description |
|------|---------|-------------|
| 8080 | Backend API | Quarkus backend or mock server |
| 5173 | Frontend | Vite dev server |

## Usage

Once inside the container:

```bash
# Install dependencies (done automatically on container creation)
npm install
cd src/main/webui && npm install

# Start development environment
make dev

# Run backend locally (requires Kubernetes access)
make run-local

# Run frontend only
make run-frontend

# Run tests
make test

# Format code
make format

# See all available commands
make help
```

## Customization

### Add VS Code Extensions

Edit `.devcontainer/devcontainer.json` and add to the `extensions` array:

```json
{
  "customizations": {
    "vscode": {
      "extensions": [
        "your-extension-id"
      ]
    }
  }
}
```

### Change Java/Node Version

Edit the `features` section in `devcontainer.json`:

```json
{
  "features": {
    "ghcr.io/devcontainers/features/java:1": {
      "version": "21"  // Change to Java 21
    },
    "ghcr.io/devcontainers/features/node:1": {
      "version": "22"  // Change to Node 22
    }
  }
}
```

## Troubleshooting

### Container fails to start

1. Check Docker Desktop is running
2. Check you have enough disk space (dev container needs ~2-3 GB)
3. Try rebuilding: `F1` → "Dev Containers: Rebuild Container"

### Extensions not loading

1. Rebuild the container: `F1` → "Dev Containers: Rebuild Container"
2. Check the extension IDs are correct in `devcontainer.json`

### Port already in use

1. Stop any local services using ports 8080 or 5173
2. Or change the port mappings in `devcontainer.json`

### Slow performance

1. For better performance, use "Clone in Container Volume" instead of opening local folder
2. Increase Docker Desktop memory allocation (Settings → Resources)

## Benefits

✅ **Consistent Environment**: Everyone uses the same tools and versions
✅ **Quick Onboarding**: New developers can start coding in minutes
✅ **No Local Setup**: Don't need to install Java, Node, Gradle locally
✅ **Isolated**: Development doesn't affect your local machine
✅ **Reproducible**: Same environment in dev, CI, and production

## Alternative: Codespaces

This dev container configuration also works with **GitHub Codespaces**, providing a cloud-based development environment accessible from any browser.

## Learn More

- [VS Code Dev Containers Documentation](https://code.visualstudio.com/docs/devcontainers/containers)
- [Dev Container Specification](https://containers.dev/)
- [Dev Container Features](https://containers.dev/features)
