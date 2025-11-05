# Release Workflow

This document describes the automatic release workflow for publishing Heimdall to GitHub Container Registry (ghcr.io).

## Overview

The release workflow automatically builds and publishes Docker images to your GitHub registry (`ghcr.io`) when certain events occur. The workflow supports three trigger modes:

### 1. Automatic Release (Version Tags)

When you push a version tag, the workflow automatically creates a **release build** and publishes it to GitHub Container Registry.

**How to trigger:**
```bash
# Create and push a version tag
git tag v0.10.0
git push origin v0.10.0

# Or without the 'v' prefix
git tag 0.10.0
git push origin 0.10.0
```

**What happens:**
- Builds the application with the release version (strips `-SNAPSHOT`)
- Publishes Docker image: `ghcr.io/<your-username>/heimdall:0.10.0`
- Also tags as `latest`: `ghcr.io/<your-username>/heimdall:latest`
- Creates a Git tag if it doesn't exist
- Generates a release summary in the GitHub Actions UI

**Tag patterns supported:**
- `v*` (e.g., v0.10.0, v1.0.0)
- `[0-9]+.[0-9]+.[0-9]+` (e.g., 0.10.0, 1.0.0)

### 2. Automatic Snapshot Release (Main Branch)

When you push to the `main` branch, the workflow automatically creates a **snapshot build** and publishes it to GitHub Container Registry.

**How to trigger:**
```bash
# Push to main branch
git push origin main
```

**What happens:**
- Builds the application with the snapshot version (keeps or adds `-SNAPSHOT`)
- Publishes Docker image: `ghcr.io/<your-username>/heimdall:0.10.0-SNAPSHOT`
- Does NOT tag as `latest`
- Does NOT create a Git tag
- Generates a release summary in the GitHub Actions UI

### 3. Manual Release

You can still manually trigger releases from the GitHub Actions UI.

**How to trigger:**
1. Go to **Actions** → **release** workflow
2. Click **Run workflow**
3. Select if you want a snapshot version (true/false)
4. Click **Run workflow**

**What happens:**
- Depends on your snapshot selection
- If snapshot=false: creates a release build (same as automatic release)
- If snapshot=true: creates a snapshot build (same as automatic snapshot)

## Docker Image Tagging Strategy

### Release Builds (from tags)
- `ghcr.io/<owner>/heimdall:<version>` (e.g., `0.10.0`)
- `ghcr.io/<owner>/heimdall:latest`

### Snapshot Builds (from main branch)
- `ghcr.io/<owner>/heimdall:<version>-SNAPSHOT` (e.g., `0.10.0-SNAPSHOT`)

## Configuration

The workflow is configured in `.github/workflows/release.yml`.

Key environment variables:
- `REGISTRY`: `ghcr.io` (GitHub Container Registry)
- `IMAGE_NAME`: `${{ github.repository }}` (automatically uses your repo name)

## Permissions

The workflow requires the following permissions:
- `contents: write` - For creating Git tags
- `packages: write` - For publishing to GitHub Container Registry

These are automatically provided by GitHub Actions using `GITHUB_TOKEN`.

## Version Management

**Git-Based Versioning**: This project uses [axion-release-plugin](https://github.com/allegro/axion-release-plugin) for automatic version management based on git tags.

**How it works:**
- Version is automatically derived from the latest git tag
- No manual version updates needed in `gradle.properties`
- Tags are the single source of truth for versions

**Version determination:**
- **On a tag** (e.g., `v0.10.0`): Version is `0.10.0` (release)
- **On main branch**: Version is `<latest-tag>-SNAPSHOT` (e.g., `0.10.0-SNAPSHOT`)
- **On feature branches**: Version is `<latest-tag>-SNAPSHOT`

**For releases:**
- Just create and push a tag (e.g., `v0.11.0`)
- The version is automatically derived: `0.11.0`
- No need to update any files manually
- The next commit on main automatically becomes `0.11.0-SNAPSHOT`

## Troubleshooting

### Release not triggered
- Ensure you pushed the tag: `git push origin <tag-name>`
- Check the tag matches the pattern: `v*` or `[0-9]+.[0-9]+.[0-9]+`
- Verify GitHub Actions are enabled in your repository settings

### Permission denied when pushing to registry
- The workflow uses `GITHUB_TOKEN` which should have sufficient permissions
- Ensure GitHub Packages is enabled for your repository
- Check repository settings → Actions → General → Workflow permissions

### Image not appearing in GitHub Packages
- Check the workflow run in the Actions tab for errors
- Verify the build completed successfully
- Images may take a few moments to appear in the package registry

### Version showing as "0.1.0-SNAPSHOT" unexpectedly
- Ensure git tags are fetched: `git fetch --tags`
- The version is based on the latest git tag in your repository
- If no tags exist, axion-release defaults to `0.1.0`
- Create an initial tag to set the version: `git tag v0.10.0 && git push origin v0.10.0`

### Check current version locally
```bash
# See what version will be used
./gradlew currentVersion

# Or check project version
./gradlew properties | grep "^version:"
```

## Example Release Process

Here's a complete example of releasing version 0.11.0 with git-based versioning:

```bash
# 1. Make your changes and push to main
git add .
git commit -m "feat: add new feature"
git push origin main
# This automatically publishes: ghcr.io/<owner>/heimdall:0.10.0-SNAPSHOT
# (Uses previous tag version with -SNAPSHOT)

# 2. When ready for release, create and push tag
git tag v0.11.0
git push origin v0.11.0
# This automatically publishes:
#   - ghcr.io/<owner>/heimdall:0.11.0
#   - ghcr.io/<owner>/heimdall:latest

# 3. Continue development on main
git push origin main
# This automatically publishes: ghcr.io/<owner>/heimdall:0.11.0-SNAPSHOT
# (Uses the new tag version with -SNAPSHOT)
```

**That's it!** No manual version updates needed. The git tag is the only version management step.

## GitHub Actions UI Summary

After each release, the workflow generates a summary in the GitHub Actions UI showing:
- Version published
- Registry location
- List of Docker images published
- Pull command for the new image

This makes it easy to verify the release and share the image location with others.
