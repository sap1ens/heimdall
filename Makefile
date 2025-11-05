.PHONY: help dev dev-up dev-down dev-logs build test format lint clean

help: ## Show this help message
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

dev: ## Start development environment (frontend + mock server)
	@echo "🚀 Starting Heimdall development environment..."
	docker-compose up -d
	@echo "✅ Development environment started!"
	@echo "   Frontend: http://localhost:5173"
	@echo "   Mock API: http://localhost:8080"
	@echo ""
	@echo "Run 'make dev-logs' to see logs"
	@echo "Run 'make dev-down' to stop"

dev-up: dev ## Alias for 'dev'

dev-down: ## Stop development environment
	@echo "🛑 Stopping development environment..."
	docker-compose down
	@echo "✅ Environment stopped"

dev-logs: ## Show logs from development environment
	docker-compose logs -f

build: ## Build the Java application
	@echo "🔨 Building Heimdall..."
	./gradlew build
	@echo "✅ Build complete"

build-image: ## Build Docker image for backend
	@echo "🐳 Building Docker image..."
	./gradlew build -x test
	docker build -f src/main/docker/Dockerfile.jvm -t heimdall:latest .
	@echo "✅ Docker image built: heimdall:latest"

test: ## Run all tests (Java + Frontend)
	@echo "🧪 Running backend tests..."
	./gradlew test
	@echo "🧪 Running frontend tests..."
	cd src/main/webui && npm run test
	@echo "✅ All tests passed"

test-backend: ## Run backend tests only
	@echo "🧪 Running backend tests..."
	./gradlew test

test-frontend: ## Run frontend tests only
	@echo "🧪 Running frontend tests..."
	cd src/main/webui && npm run test

test-coverage: ## Run tests with coverage
	@echo "📊 Running tests with coverage..."
	./gradlew test jacocoTestReport
	cd src/main/webui && npm run test:coverage
	@echo "✅ Coverage reports generated"
	@echo "   Backend: open build/reports/jacoco/test/html/index.html"
	@echo "   Frontend: open src/main/webui/coverage/index.html"

format: ## Format all code (Java + Frontend)
	@echo "✨ Formatting code..."
	./gradlew spotlessApply
	cd src/main/webui && npm run format
	@echo "✅ Code formatted"

format-check: ## Check code formatting
	@echo "🔍 Checking code formatting..."
	./gradlew spotlessCheck
	cd src/main/webui && npm run format:check
	@echo "✅ Format check complete"

lint: ## Lint frontend code
	@echo "🔍 Linting frontend code..."
	cd src/main/webui && npm run lint
	@echo "✅ Lint complete"

lint-fix: ## Fix linting issues
	@echo "🔧 Fixing linting issues..."
	cd src/main/webui && npm run lint:fix
	@echo "✅ Linting fixed"

install: ## Install all dependencies
	@echo "📦 Installing dependencies..."
	npm install
	cd src/main/webui && npm install
	@echo "✅ Dependencies installed"

clean: ## Clean build artifacts
	@echo "🧹 Cleaning build artifacts..."
	./gradlew clean
	rm -rf src/main/webui/dist
	rm -rf src/main/webui/node_modules
	rm -rf node_modules
	@echo "✅ Clean complete"

run-local: ## Run backend locally (requires Kubernetes access)
	@echo "🚀 Running Heimdall backend locally..."
	./gradlew quarkusDev

run-frontend: ## Run frontend dev server
	@echo "🚀 Running frontend dev server..."
	cd src/main/webui && npm run dev

setup: install ## Complete project setup
	@echo "🎉 Heimdall setup complete!"
	@echo ""
	@echo "Quick start:"
	@echo "  make dev       - Start development environment"
	@echo "  make test      - Run all tests"
	@echo "  make format    - Format all code"
	@echo ""
	@echo "For more commands, run: make help"
