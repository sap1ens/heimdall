# Heimdall

[![Build Status](https://github.com/next-govejero/heimdall/actions/workflows/build.yml/badge.svg)](https://github.com/next-govejero/heimdall/actions/workflows/build.yml)
[![Docker Image](https://ghcr-badge.egpl.dev/sap1ens/heimdall/latest_tag?trim=major&label=latest)](https://github.com/sap1ens/heimdall/pkgs/container/heimdall)

Heimdall is a dashboard for operating Flink jobs and deployments. The built-in Flink UI is extremely useful when dealing with
a single job, but what if you have 10, 20, or 100 jobs? Heimdall helps you keep track of all your Flink jobs by searching, filtering, sorting, and navigating them from a single unified interface.

Read more in the blog: [Heimdall: Making Operating Flink Deployments a Bit Easier](https://sap1ens.com/blog/2023/07/09/heimdall-making-operating-flink-deployments-a-bit-easier/).

**Note:** Currently, Heimdall only supports Flink jobs deployed with [Flink Kubernetes Operator](https://ci.apache.org/projects/flink/flink-kubernetes-operator-docs-stable/).

![Heimdall Dashboard Demo](docs/assets/demo.gif)

## Features

- 🔍 **Search & Filter** - Quickly find jobs across multiple deployments
- 📊 **Job Overview** - View status, state, and metadata for all jobs at a glance
- 🔗 **Quick Links** - Direct access to Flink UI, metrics, and logs for each job
- 🏷️ **Metadata Support** - Display custom labels and annotations from Kubernetes resources
- 📄 **Pagination** - Navigate through large job lists with configurable page sizes
- ⚡ **Performance** - In-memory caching for fast response times
- 🔐 **Kubernetes Native** - Designed to run alongside Flink Operator in your cluster

## Table of Contents

- [Quick Start](#quick-and-dirty-start)
- [Installation](#installation)
- [Job Locators](#job-locators)
- [Configuration](#configuration)
- [Development](#development)
- [Testing](#testing)
- [Release Workflow](#release-workflow)
- [Sponsors](#sponsors)

## Quick (and Dirty) Start

```bash
kubectl apply -f https://raw.githubusercontent.com/sap1ens/heimdall/main/tools/k8s-operator/service-account.yaml
kubectl run heimdall --image=ghcr.io/sap1ens/heimdall:0.10.0 --port=8080 --overrides='{ "spec": { "serviceAccount": "heimdall-service-account" }  }'
kubectl port-forward heimdall 8080:8080
open http://localhost:8080
```

## Installation

Heimdall is available as a Docker container [here](https://github.com/sap1ens/heimdall/pkgs/container/heimdall). Since only
Flink Kubernetes Operator is available at the moment, you'd typically deploy Heimdall as a pod or deployment in the same Kubernetes cluster where your Flink jobs are running.

### Deployment Requirements

- Kubernetes cluster with [Flink Kubernetes Operator](https://ci.apache.org/projects/flink/flink-kubernetes-operator-docs-stable/) installed
- Service account with read access to `flinkdeployment` custom resources (see [service-account.yaml](tools/k8s-operator/service-account.yaml))
- Port 8080 (default) available for HTTP traffic

### Health Checks

Heimdall exposes Quarkus health endpoints:
- **Liveness:** `/q/health/live`
- **Readiness:** `/q/health/ready`

These endpoints can be used for Kubernetes liveness and readiness probes.

## Job Locators

Job locators (implementing the `FlinkJobLocator` interface) are mechanisms for discovering running Flink jobs. Heimdall uses these locators to dynamically find and track your Flink deployments.

### K8sOperatorFlinkJobLocator

**Default:** Enabled

This locator discovers jobs by reading `FlinkDeployment` custom resources (CRs) created by the Flink Kubernetes Operator.

**Requirements:**
- Service account with read-only access to `flinkdeployment` CRs
- See [tools/k8s-operator/service-account.yaml](tools/k8s-operator/service-account.yaml) for a complete example

**Configuration:**
- Enable/disable: `heimdall.joblocator.k8s-operator.enabled` (default: `true`)
- Namespace(s): `heimdall.joblocator.k8s-operator.namespace-to-watch` (default: `default`)

## Cache

By default, all data received from locators is cached in memory for **5 seconds** to improve performance and reduce load on the Kubernetes API server.

You can configure the cache behavior using Quarkus cache properties. See the [Quarkus Cache Guide](https://quarkus.io/guides/cache#configuring-the-underlying-caching-provider) for advanced configuration options.

**Example:** Adjust cache expiration:
```properties
quarkus.cache.caffeine.flink-jobs.expire-after-write=10S
quarkus.cache.caffeine.flink-jobs.expire-after-access=10S
```

## Configuration

### Common

| Environment variable                      | Default                           | Description                                                                                                                    |
|-------------------------------------------|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| QUARKUS_HTTP_CORS_ORIGINS                 | http://localhost:5173             | Comma separated list of valid origins allowed for CORS. Change to `http://localhost:8001` when using `kubectl proxy`.          |
| HEIMDALL_PATTERNS_DISPLAY_NAME            | $jobName                          | Pattern for showing Flink job name. Metadata fields (e.g. Kubernetes labels) can be accessed via `$metadata.labelName` syntax. |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_FLINK_UI  | http://localhost/$jobName/ui      | Pattern for the Flink UI endpoint. `$jobName` will be substituted. Set to an empty string to disable.                          |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_FLINK_API | http://localhost/$jobName/api     | Pattern for the Flink API endpoint. `$jobName` will be substituted. Set to an empty string to disable.                         |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_METRICS   | http://localhost/$jobName/metrics | Pattern for the Metrics endpoint. `$jobName` will be substituted. Set to an empty string to disable.                           |
| HEIMDALL_ENDPOINT_PATH_PATTERNS_LOGS      | http://localhost/$jobName/logs    | Pattern for the Logs endpoint. `$jobName` will be substituted. Set to an empty string to disable.                              |

#### Custom Endpoints

You can add any custom external endpoints by adding new `HEIMDALL_ENDPOINT_PATH_PATTERNS_*` environment variables. The endpoint name (after the prefix) will be automatically formatted as a display title.

**Examples:**
```bash
# GitHub repository links
HEIMDALL_ENDPOINT_PATH_PATTERNS_GITHUB_REPO=https://github.com/org/$jobName

# Grafana dashboards
HEIMDALL_ENDPOINT_PATH_PATTERNS_GRAFANA=https://grafana.example.com/d/dashboard?var-job=$jobName

# Datadog logs
HEIMDALL_ENDPOINT_PATH_PATTERNS_DATADOG=https://app.datadoghq.com/logs?query=service:$jobName
```

**Display names:** Endpoint keys are automatically converted to readable titles (e.g., `github-repo` → "GitHub Repo", `grafana` → "Grafana")

### K8sOperatorFlinkJobLocator

| Environment variable                                | Default | Description                  |
|-----------------------------------------------------|---------|------------------------------|
| HEIMDALL_JOBLOCATOR_K8S_OPERATOR_ENABLED            | true    | Is this locator enabled?     |
| HEIMDALL_JOBLOCATOR_K8S_OPERATOR_NAMESPACE_TO_WATCH | default | Kubernetes namespace(s) to watch. Supports single namespace or comma-separated list (e.g. "default,prod,staging"). |

## Development

This project uses:

- **Backend:** Java 17 with [Quarkus](https://quarkus.io) framework
- **Frontend:** JavaScript with [Svelte 5](https://svelte.dev) and [Vite](https://vitejs.dev)
- **Build Tool:** Gradle 8.1.1
- **Frontend Integration:** [Quinoa](https://quarkiverse.github.io/quarkiverse-docs/quarkus-quinoa/dev/) (automatically builds frontend during backend build)

### Quick Development Setup

Choose one of the following options to get started quickly:

#### Option 1: Docker Compose (Recommended for Beginners)

Start the development environment with frontend and mock backend:

```bash
# Install dependencies
npm install
cd src/main/webui && npm install && cd ../..

# Start development environment
make dev

# Or manually:
docker-compose up
```

This starts:
- Frontend dev server on http://localhost:5173
- Mock API server on http://localhost:8080

See [docker-compose.yml](docker-compose.yml) for configuration.

#### Option 2: VS Code Dev Container (Recommended for Teams)

Open this repository in VS Code and click "Reopen in Container". All tools and dependencies are pre-configured.

See [.devcontainer/README.md](.devcontainer/README.md) for details.

#### Option 3: Local Setup

**Prerequisites:**
- Java 17 or later
- Node.js 20 or later
- Gradle 8.1.1 (or use the wrapper `./gradlew`)

**Setup:**
```bash
# Install dependencies
npm install
cd src/main/webui && npm install && cd ../..

# Install pre-commit hooks
npm run prepare
```

### Running the application in dev mode

Run the application with live reload enabled for both frontend and backend:

```bash
./gradlew quarkusDev
```

This will:
- Start Quarkus in development mode on port 8080
- Start the Vite dev server for the frontend on port 5173
- Enable hot reload for both backend and frontend code changes
- Open the application at http://localhost:8080

### Frontend Development

#### With Mock Server (No Kubernetes Required)

Perfect for frontend-only development:

```bash
# Terminal 1: Start mock API server
cd src/main/webui
node mock-server.js

# Terminal 2: Start frontend dev server
npm run dev
```

The frontend will be available at http://localhost:5173.

#### With Real Backend

To work with the actual backend:

```bash
# Terminal 1: Start backend
./gradlew quarkusDev

# Terminal 2: Start frontend
cd src/main/webui
npm run dev
```

### Code Quality & Formatting

This project uses automated code formatting and linting:

**Pre-commit Hooks:**
- Automatically format code before each commit
- Run ESLint on JavaScript/Svelte files
- Run Spotless on Java files

**Manual Commands:**
```bash
# Format all code
make format

# Check formatting
make format-check

# Lint frontend code
make lint

# Fix linting issues
make lint-fix
```

**Java Formatting:**
- Uses Spotless with Google Java Format
- Run: `./gradlew spotlessApply`

**Frontend Formatting:**
- Uses ESLint + Prettier
- Configuration: `.eslintrc.cjs`, `.prettierrc`
- Run: `cd src/main/webui && npm run format`

### Makefile Commands

For convenience, common tasks are available via `make`:

```bash
make help           # Show all available commands
make dev            # Start development environment
make test           # Run all tests
make test-coverage  # Run tests with coverage
make format         # Format all code
make lint           # Lint frontend code
make build          # Build the application
make clean          # Clean build artifacts
```

See [Makefile](Makefile) for all available commands.

### Packaging and running the application

The application can be packaged using:

```bash
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```bash
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Testing

### Backend Tests

Run backend tests with coverage:

```bash
./gradlew test jacocoTestReport
```

View the coverage report at `build/reports/jacoco/test/html/index.html`.

### Frontend Tests

Run frontend tests:

```bash
cd src/main/webui
npm test
```

Run tests with coverage:

```bash
npm run test:coverage
```

View the coverage report at `src/main/webui/coverage/index.html`.

### Running All Tests

The CI/CD pipeline runs both backend and frontend tests. To run everything locally:

```bash
# Backend tests
./gradlew test

# Frontend tests
cd src/main/webui && npm test
```

### Docker Image Build

Build a local Docker image:

```bash
./gradlew imageBuild
```

Build and push a multi-architecture image (AMD64/ARM64):

```bash
# 1. Authenticate with GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u your-username --password-stdin

# 2. Create a buildx builder (one-time setup)
docker buildx create --use --name multiarch-builder

# 3. Build and push multi-arch image
docker buildx build --platform linux/amd64,linux/arm64 \
  -f src/main/docker/Dockerfile.jvm \
  -t ghcr.io/your-username/heimdall:latest \
  --push .

# Note: Replace 'your-username' with your GitHub username
```

The Dockerfile uses the distroless base image for minimal size and security.

## Release Workflow

Heimdall uses an automated release workflow that publishes Docker images to GitHub Container Registry (ghcr.io) automatically.

### Automatic Releases

The workflow triggers automatically in two scenarios:

1. **Version Tags** - Creates a release build when you push a version tag (e.g., `v0.10.0`)
   ```bash
   git tag v0.10.0
   git push origin v0.10.0
   ```
   - Publishes: `ghcr.io/<owner>/heimdall:0.10.0` and `ghcr.io/<owner>/heimdall:latest`

2. **Main Branch** - Creates a snapshot build when you push to main
   ```bash
   git push origin main
   ```
   - Publishes: `ghcr.io/<owner>/heimdall:0.10.0-SNAPSHOT`

### Manual Releases

You can also trigger releases manually from GitHub Actions → release workflow.

For detailed information about the release process, version management, and troubleshooting, see [Release Workflow Documentation](docs/RELEASE_WORKFLOW.md).

## Sponsors

This project is sponsored by [Goldsky](https://goldsky.com) ❤️.
