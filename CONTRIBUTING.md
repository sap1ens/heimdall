# Contributing to Heimdall

Thank you for your interest in contributing to Heimdall! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Standards](#code-standards)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Project Structure](#project-structure)
- [Reporting Issues](#reporting-issues)

---

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before contributing.

---

## Getting Started

### Prerequisites

Ensure you have the following installed:
- **Java 17** or later (JDK)
- **Gradle 8.1.1** or later
- **Node.js 20** or later
- **npm** (comes with Node.js)
- **Git**

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/heimdall.git
   cd heimdall
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/next-govejero/heimdall.git
   ```

---

## Development Setup

### Initial Setup

1. **Install dependencies**:
   ```bash
   # Root dependencies (husky, lint-staged)
   npm install

   # Frontend dependencies
   cd src/main/webui
   npm install
   cd ../../..
   ```

2. **Verify setup**:
   ```bash
   # Test Java build
   ./gradlew build

   # Test frontend build
   cd src/main/webui && npm run build && cd ../../..
   ```

3. **Pre-commit hooks**:
   Pre-commit hooks are automatically installed via husky. They will:
   - Format Java code with Spotless
   - Lint and format JavaScript/Svelte code with ESLint and Prettier

### Local Development

See [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md) for detailed development instructions, including:
- Running the backend
- Running the frontend
- Using the mock server
- Docker Compose setup

---

## Code Standards

### Java Code

**Style Guide**: Google Java Format (enforced by Spotless)

**Key Principles**:
- Use Java 17 features (records, pattern matching, etc.)
- Prefer immutability (use `final`, records, and immutable collections)
- Write self-documenting code with clear variable/method names
- Use dependency injection via `@Inject`
- Follow single responsibility principle

**Formatting**:
```bash
# Check formatting
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply
```

**Example**:
```java
@ApplicationScoped
public class FlinkJobService {
    private final FlinkJobLocator jobLocator;

    @Inject
    public FlinkJobService(FlinkJobLocator jobLocator) {
        this.jobLocator = jobLocator;
    }

    public List<FlinkJob> findAll() {
        return jobLocator.locateJobs();
    }
}
```

### JavaScript/Svelte Code

**Style Guide**: ESLint + Prettier (automatically enforced)

**Key Principles**:
- Use ES6+ features (arrow functions, destructuring, async/await)
- Prefer `const` over `let`, avoid `var`
- Use Svelte stores for shared state
- Keep components small and focused
- Use semantic HTML

**Formatting**:
```bash
cd src/main/webui

# Check linting
npm run lint

# Fix linting issues
npm run lint:fix

# Check formatting
npm run format:check

# Apply formatting
npm run format
```

**Example**:
```javascript
// Svelte component
<script>
  import { flinkJobs } from './stores/flinkJobs.js';

  export let filterText = '';

  $: filteredJobs = $flinkJobs.filter(job =>
    job.name.toLowerCase().includes(filterText.toLowerCase())
  );
</script>

<div>
  {#each filteredJobs as job (job.name)}
    <JobCard {job} />
  {/each}
</div>
```

### Configuration Files

- **YAML**: 2 spaces for indentation
- **JSON**: 2 spaces for indentation (enforced by Prettier)
- **Properties**: Follow existing format in `application.properties`

---

## Testing Requirements

### Backend Tests

**Target Coverage** (not enforced automatically):
- Overall: 60%
- Per class: 50%

**Note**: JaCoCo coverage verification is disabled due to Quarkus bytecode augmentation incompatibility. Coverage reports are generated and should be reviewed manually. Please ensure your changes maintain or improve test coverage.

**Test Structure**:
```java
@QuarkusTest
class FlinkJobResourceTest {
    @Test
    void shouldReturnAllJobs() {
        given()
            .when().get("/jobs")
            .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }
}
```

**Running Tests**:
```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Frontend Tests

**Minimum Coverage**:
- Lines: 40%
- Functions: 40%
- Branches: 50%

**Test Types**:
1. **Unit Tests** (Vitest):
   ```javascript
   import { describe, it, expect } from 'vitest';
   import { render } from '@testing-library/svelte';
   import JobCard from './JobCard.svelte';

   describe('JobCard', () => {
     it('displays job name', () => {
       const { getByText } = render(JobCard, {
         props: { job: { name: 'test-job' } }
       });
       expect(getByText('test-job')).toBeInTheDocument();
     });
   });
   ```

2. **E2E Tests** (Playwright):
   ```javascript
   test('should display jobs list', async ({ page }) => {
     await page.goto('/');
     await expect(page.locator('.job-card')).toHaveCount(5);
   });
   ```

**Running Tests**:
```bash
cd src/main/webui

# Unit tests
npm run test

# With coverage
npm run test:coverage

# E2E tests
npm run test:e2e

# E2E with UI
npm run test:e2e:ui
```

### Test Requirements for PRs

All pull requests must:
- ✅ Include tests for new functionality
- ✅ Maintain or improve code coverage
- ✅ Pass all existing tests
- ✅ Include E2E tests for UI changes (when applicable)

---

## Pull Request Process

### Before Creating a PR

1. **Sync with upstream**:
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**:
   - Follow code standards
   - Write tests
   - Update documentation

4. **Run all checks locally**:
   ```bash
   # Java formatting and tests
   ./gradlew spotlessCheck test

   # Frontend linting, formatting, and tests
   cd src/main/webui
   npm run lint
   npm run format:check
   npm run test
   npm run build
   cd ../../..
   ```

5. **Commit your changes**:
   - Pre-commit hooks will automatically run
   - Follow [commit message guidelines](#commit-message-guidelines)

### Creating the PR

1. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Open a Pull Request** on GitHub with:
   - **Title**: Clear, descriptive title
   - **Description**:
     - What changes were made
     - Why these changes are needed
     - Any breaking changes
     - Screenshots (for UI changes)
   - **Link related issues**: Use "Fixes #123" or "Relates to #123"

3. **PR Template**:
   ```markdown
   ## Description
   Brief description of changes

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update

   ## Testing
   - [ ] Added unit tests
   - [ ] Added E2E tests (if applicable)
   - [ ] All tests pass locally
   - [ ] Coverage maintained or improved

   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Self-review completed
   - [ ] Documentation updated
   - [ ] No new warnings introduced
   ```

### Review Process

1. **Automated Checks**:
   - GitHub Actions will run CI pipeline
   - All checks must pass

2. **Code Review**:
   - At least one maintainer approval required
   - Address all review comments
   - Keep discussions focused and respectful

3. **Merging**:
   - Maintainers will merge approved PRs
   - Squash and merge is preferred for feature branches

---

## Commit Message Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/) specification:

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, missing semicolons, etc.)
- **refactor**: Code refactoring (no functional changes)
- **test**: Adding or updating tests
- **chore**: Maintenance tasks (dependencies, build, etc.)
- **perf**: Performance improvements
- **ci**: CI/CD changes

### Examples
```bash
# Good commits
feat(api): add pagination support for job listings
fix(ui): correct job status badge color for failed jobs
docs(readme): update installation instructions
test(service): add edge case tests for job locator
chore(deps): upgrade Quarkus to 3.2.11

# Bad commits
fixed stuff
WIP
update
```

### Scope
Use one of:
- `api`: Backend API changes
- `ui`: Frontend UI changes
- `service`: Service layer changes
- `k8s`: Kubernetes client changes
- `deps`: Dependency updates
- `ci`: CI/CD pipeline
- `docs`: Documentation

---

## Project Structure

```
heimdall/
├── src/
│   ├── main/
│   │   ├── java/com/sap1ens/heimdall/
│   │   │   ├── api/           # REST endpoints
│   │   │   ├── service/       # Business logic
│   │   │   ├── kubernetes/    # K8s client
│   │   │   └── model/         # Data models (records)
│   │   ├── resources/
│   │   │   └── application.properties
│   │   ├── webui/             # Frontend (Svelte)
│   │   │   ├── src/
│   │   │   │   ├── lib/       # Components and stores
│   │   │   │   ├── App.svelte
│   │   │   │   └── main.js
│   │   │   └── package.json
│   │   └── docker/            # Dockerfiles
│   └── test/                  # Backend tests
├── build.gradle               # Java build configuration
├── package.json               # Root npm config (husky, lint-staged)
├── CONTRIBUTING.md            # This file
├── SECURITY.md               # Security policy
└── README.md                 # Project overview
```

---

## Reporting Issues

### Bug Reports

Use the [GitHub Issues](https://github.com/next-govejero/heimdall/issues) with the bug report template:

**Include**:
- Clear, descriptive title
- Steps to reproduce
- Expected behavior
- Actual behavior
- Environment details (OS, Java version, Kubernetes version)
- Screenshots or logs (if applicable)

### Feature Requests

Use GitHub Issues with the feature request template:

**Include**:
- Clear use case description
- Proposed solution
- Alternative solutions considered
- Additional context

### Security Issues

**DO NOT** report security issues publicly. See [SECURITY.md](SECURITY.md) for reporting instructions.

---

## Development Tips

### Quick Commands

```bash
# Format all code (Java + JS)
npm run format:java && cd src/main/webui && npm run format && cd ../../..

# Run all tests
./gradlew test && cd src/main/webui && npm run test && cd ../../..

# Full build
./gradlew build
```

### Useful Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Svelte Documentation](https://svelte.dev/docs)
- [Vite Documentation](https://vitejs.dev/guide/)
- [Kubernetes Client Documentation](https://github.com/fabric8io/kubernetes-client)
- [Flink Kubernetes Operator](https://nightlies.apache.org/flink/flink-kubernetes-operator-docs-main/)

### Getting Help

- **Documentation**: Check [README.md](README.md) and [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md)
- **Issues**: Search existing issues before creating new ones
- **Discussions**: Use GitHub Discussions for questions
- **Code Examples**: Review existing tests and code

---

## Recognition

Contributors will be recognized in:
- GitHub Contributors page
- Release notes (for significant contributions)
- Project documentation (for major features)

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

Thank you for contributing to Heimdall! 🎉
