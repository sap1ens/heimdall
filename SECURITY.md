# Security Policy

## Reporting Security Vulnerabilities

We take the security of Heimdall seriously. If you believe you have found a security vulnerability, please report it to us responsibly.

### How to Report

**Please do NOT report security vulnerabilities through public GitHub issues.**

Instead, please report them via one of the following methods:
- Email: [security@example.com] (Update with actual contact)
- Private Security Advisory: Use GitHub's [private vulnerability reporting](https://github.com/next-govejero/heimdall/security/advisories/new)

Please include the following information in your report:
- Type of vulnerability
- Full paths of source file(s) related to the vulnerability
- Location of the affected source code (tag/branch/commit or direct URL)
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the issue, including how an attacker might exploit it

### Response Timeline

- **Initial Response**: Within 48 hours of report submission
- **Confirmation**: Within 1 week of report submission
- **Fix Timeline**: 90 days for patch development (may vary based on severity)
- **Public Disclosure**: After patch is available, coordinated with reporter

We will keep you informed about our progress throughout the process.

---

## Supported Versions

We currently support the following versions with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 0.10.x  | :white_check_mark: |
| < 0.10  | :x:                |

---

## Security Practices

### Authentication & Authorization

Heimdall currently operates within a Kubernetes cluster and relies on:
- **Kubernetes RBAC**: Service account permissions control access to FlinkDeployment resources
- **Network Policies**: Recommended to restrict access to the Heimdall service

**Note**: The API currently does **not** include built-in authentication. If exposing outside the cluster, consider:
- Implementing an API gateway with authentication (OAuth2, JWT, etc.)
- Using Kubernetes Ingress with authentication middleware
- Implementing Quarkus OIDC or JWT authentication

### CORS Configuration

CORS is configured for local development (`http://localhost:5173`). For production:
```properties
# Configure appropriate origins
quarkus.http.cors.origins=https://your-domain.com
```

### Security Headers

We recommend configuring the following security headers in your deployment (via Ingress, API Gateway, or Quarkus configuration):

```properties
# Content Security Policy
quarkus.http.header."Content-Security-Policy"=default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'

# Prevent MIME type sniffing
quarkus.http.header."X-Content-Type-Options"=nosniff

# Clickjacking protection
quarkus.http.header."X-Frame-Options"=DENY

# XSS Protection (legacy browsers)
quarkus.http.header."X-XSS-Protection"=1; mode=block

# Referrer Policy
quarkus.http.header."Referrer-Policy"=strict-origin-when-cross-origin

# Permissions Policy
quarkus.http.header."Permissions-Policy"=geolocation=(), microphone=(), camera=()
```

### Container Security

Heimdall's Docker image follows security best practices:
- ✅ Non-root user (UID 185)
- ✅ Red Hat UBI8 minimal base image (regularly updated)
- ✅ Minimal dependencies
- ✅ Multi-architecture support (amd64, arm64)

**Recommendations**:
- Run with `runAsNonRoot: true` in Kubernetes SecurityContext
- Use `readOnlyRootFilesystem: true` where possible
- Apply appropriate seccomp and AppArmor profiles
- Regularly update base images

### Dependency Management

We actively monitor and update dependencies:
- **Backend**: Quarkus BOM manages Java dependencies
- **Frontend**: Regular `npm audit` checks

See [src/main/webui/SECURITY.md](src/main/webui/SECURITY.md) for frontend-specific security information.

### Data Protection

Heimdall does not persist any data. All job information is:
- Retrieved in real-time from Kubernetes API
- Cached in-memory for 5 seconds
- Not logged (except debug messages)

**Sensitive Configuration**:
- Store endpoint patterns in environment variables for production
- Use Kubernetes Secrets for any sensitive configuration
- Avoid logging sensitive data

### Rate Limiting

**Current Status**: No built-in rate limiting

**Recommendations**:
- Implement rate limiting at the API Gateway or Ingress level
- Consider using Quarkus rate limiting extension for API protection
- Monitor for unusual API usage patterns

### Input Validation

Heimdall validates and sanitizes:
- Namespace names (Kubernetes naming conventions)
- Job filter parameters
- Configuration values

**Note**: Always validate external input, especially endpoint pattern configurations.

---

## Security Monitoring

### Logging

Heimdall logs security-relevant events:
- Kubernetes API connection failures
- Configuration loading issues
- Request errors

Review logs regularly for suspicious activity.

### Health Checks

Monitor the following endpoints:
- `/q/health/live` - Liveness probe
- `/q/health/ready` - Readiness probe

Unexpected health check failures may indicate security issues.

### Dependency Scanning

We recommend enabling:
- **Dependabot**: Automated dependency updates
- **GitHub Security Advisories**: Vulnerability notifications
- **OWASP Dependency Check**: Regular scans in CI/CD

---

## Deployment Security Checklist

When deploying Heimdall, ensure:

- [ ] Non-root container user enforced
- [ ] Read-only root filesystem (where possible)
- [ ] Network policies restrict access
- [ ] RBAC properly configured for service account
- [ ] Security headers configured (see above)
- [ ] TLS/HTTPS enabled for external access
- [ ] Authentication/authorization added if exposing publicly
- [ ] Secrets stored in Kubernetes Secrets, not ConfigMaps
- [ ] Resource limits configured (CPU, memory)
- [ ] Regular updates and security patches applied
- [ ] Monitoring and alerting configured
- [ ] Backup and disaster recovery plan in place

---

## Known Limitations

### Current Security Limitations

1. **No Built-in Authentication**: API is accessible to anyone with network access
2. **No Rate Limiting**: Vulnerable to API abuse if exposed publicly
3. **No Request Signing**: API requests are not cryptographically signed
4. **Limited Audit Logging**: No comprehensive audit trail of API access

### Mitigation Strategies

These limitations are acceptable for internal cluster deployments. For production use:
- Deploy behind an API gateway with authentication
- Use Kubernetes Network Policies
- Implement comprehensive monitoring
- Consider adding Quarkus OIDC/JWT extensions

---

## Security Updates

### Subscribing to Updates

- Watch this repository for security advisories
- Enable GitHub security alerts
- Subscribe to Quarkus security mailing list
- Monitor [CVE databases](https://cve.mitre.org/) for Java/Kubernetes vulnerabilities

### Update Process

1. Review security advisory
2. Test patch in non-production environment
3. Deploy to production during maintenance window
4. Verify deployment health
5. Monitor for issues post-deployment

---

## Additional Resources

- [Quarkus Security Guide](https://quarkus.io/guides/security)
- [Kubernetes Security Best Practices](https://kubernetes.io/docs/concepts/security/security-best-practices/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CIS Docker Benchmark](https://www.cisecurity.org/benchmark/docker)
- [Frontend Security](src/main/webui/SECURITY.md)

---

## Contact

For security-related questions or concerns:
- Security Issues: [Use private vulnerability reporting]
- General Questions: [Open a GitHub Discussion]
- Security Best Practices: [Refer to documentation]

---

*Last Updated: 2025-11-05*
