# Testing Guide

This document describes the testing strategy and how to run tests for the Heimdall project.

## Overview

Heimdall has comprehensive test coverage across both backend (Java) and frontend (JavaScript/Svelte) components:

- **Backend Tests**: JUnit 5 with Mockito for Java code
- **Frontend Unit Tests**: Vitest with Testing Library for Svelte components
- **E2E Tests**: Playwright for end-to-end browser testing
- **Coverage Reporting**: JaCoCo for Java, c8/v8 for JavaScript

## Test Structure

```
heimdall/
├── src/
│   ├── test/java/                    # Backend tests
│   │   └── com/sap1ens/heimdall/
│   │       ├── api/                  # API endpoint tests
│   │       ├── service/              # Service layer tests
│   │       └── kubernetes/           # Kubernetes client tests
│   └── main/webui/
│       ├── src/test/                 # Frontend unit tests
│       │   ├── components/           # Component tests
│       │   ├── stores/               # Store tests
│       │   └── setup.js              # Test setup
│       └── e2e/                      # E2E tests
│           ├── homepage.spec.js
│           └── jobs.spec.js
```

## Running Tests

### Backend Tests

Run all backend tests:
```bash
./gradlew test
```

Run tests with coverage:
```bash
./gradlew test jacocoTestReport
```

View coverage report:
```bash
open build/reports/jacoco/test/html/index.html
```

Run a specific test:
```bash
./gradlew test --tests "com.sap1ens.heimdall.api.FlinkJobResourceTest"
```

### Frontend Unit Tests

Install dependencies first (if not already done):
```bash
cd src/main/webui
npm install
```

Run all unit tests:
```bash
npm test
```

Run tests in watch mode:
```bash
npm run test:watch
```

Run tests with coverage:
```bash
npm run test:coverage
```

View coverage report:
```bash
open src/main/webui/coverage/index.html
```

### E2E Tests

Install Playwright browsers (first time only):
```bash
cd src/main/webui
npx playwright install
```

Run E2E tests:
```bash
npm run test:e2e
```

Run E2E tests with UI:
```bash
npm run test:e2e:ui
```

## Coverage Thresholds

### Backend (Java)
- Minimum overall coverage: 60%
- Minimum class coverage: 50%
- Excludes: model/record classes

### Frontend (JavaScript)
- Lines: 15%
- Functions: 40%
- Branches: 70%
- Statements: 15%
- Excludes: test files, config files, node_modules, E2E tests
- Note: Thresholds are intentionally conservative to focus on test reliability over coverage percentage

## Test Categories

### Backend

#### API Tests
- **FlinkJobResourceTest**: Tests for `/jobs` endpoint
- **FlinkJobResourceEdgeCasesTest**: Edge cases (null values, large datasets, special characters)
- **AppConfigResourceTest**: Tests for `/config` endpoint

#### Service Tests
- **K8sOperatorFlinkJobLocatorTest**: Job discovery and transformation logic
- **AppConfigTest**: Configuration parsing and validation

#### Client Tests
- **FlinkDeploymentClientTest**: Kubernetes client integration

### Frontend

#### Component Tests
- **JobType.test.js**: Job type indicator component
- **Modal.test.js**: Modal dialog component

#### Store Tests
- **settings.test.js**: Settings persistence and updates
- **appConfig.test.js**: App configuration loading
- **flinkJobs.test.js**: Job data fetching and auto-refresh

#### E2E Tests
- **homepage.spec.js**: Homepage loading and responsiveness
- **jobs.spec.js**: Job listing, filtering, and settings

## Writing New Tests

### Backend Test Example

```java
@QuarkusTest
public class MyServiceTest {
    @Inject
    MyService myService;

    @InjectMock
    MyDependency myDependency;

    @Test
    public void testSomething() {
        Mockito.when(myDependency.doSomething())
            .thenReturn("expected");

        var result = myService.execute();

        assertEquals("expected", result);
    }
}
```

### Frontend Unit Test Example

```javascript
import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/svelte';
import MyComponent from './MyComponent.svelte';

describe('MyComponent', () => {
  it('should render correctly', () => {
    const { getByText } = render(MyComponent, {
      props: { title: 'Test' }
    });

    expect(getByText('Test')).toBeInTheDocument();
  });
});
```

### E2E Test Example

```javascript
import { test, expect } from '@playwright/test';

test('my feature works', async ({ page }) => {
  await page.goto('/');

  await page.click('button[title="My Button"]');

  await expect(page.locator('.result')).toBeVisible();
});
```

## CI/CD Integration

Tests run automatically on every push and pull request via GitHub Actions:

1. **Frontend unit tests** run first with coverage
2. **Backend tests** run with Gradle build
3. **Coverage reports** are uploaded to Codecov
4. **Test results and coverage** are archived as artifacts

View the workflow: `.github/workflows/build.yml`

## Debugging Failed Tests

### Backend

Enable verbose logging:
```bash
./gradlew test --info
```

Run with debug:
```bash
./gradlew test --debug-jvm
```

### Frontend

Run specific test file:
```bash
npm test -- src/test/components/JobType.test.js
```

Debug in UI mode:
```bash
npm run test:watch
```

### E2E

Run with headed browser:
```bash
npx playwright test --headed
```

Debug mode:
```bash
npx playwright test --debug
```

## Best Practices

1. **Write tests for new features**: All new code should include tests
2. **Keep tests isolated**: Each test should be independent
3. **Use descriptive names**: Test names should describe what they test
4. **Mock external dependencies**: Don't make real API calls or database queries
5. **Test edge cases**: Include tests for error conditions and boundary values
6. **Maintain coverage**: Don't let coverage drop below thresholds
7. **Keep tests fast**: Unit tests should run in milliseconds
8. **Clean up**: Always clean up resources in test teardown

## Common Issues

### Backend

**Issue**: Tests fail with "Port already in use"
- **Solution**: Kill the process using the port or use a different test profile

**Issue**: Mock not being used
- **Solution**: Ensure `@InjectMock` is used and mock is configured before test execution

### Frontend

**Issue**: `localStorage is not defined`
- **Solution**: Ensure `setup.js` is configured in `vitest.config.js`

**Issue**: Component styles not working
- **Solution**: Import styles in test or use `@testing-library/svelte` properly

### E2E

**Issue**: Browser not installed
- **Solution**: Run `npx playwright install`

**Issue**: Timeout errors
- **Solution**: Increase timeout in `playwright.config.js` or use `page.waitForSelector()`

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Vitest Documentation](https://vitest.dev/)
- [Testing Library](https://testing-library.com/docs/svelte-testing-library/intro/)
- [Playwright Documentation](https://playwright.dev/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
