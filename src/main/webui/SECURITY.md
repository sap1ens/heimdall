# Security Notes

## Current Status

As of the latest update, we've addressed most security vulnerabilities in dependencies. However, some remain that require **breaking changes** to fix.

### Fixed Vulnerabilities ✅

The following have been resolved:
- axios (CSRF, SSRF, DoS vulnerabilities)
- form-data (critical: unsafe random boundary)
- postcss (line return parsing)
- rollup (XSS via DOM clobbering)
- brace-expansion (ReDoS)
- braces (resource consumption)
- follow-redirects (header leakage)
- micromatch (ReDoS)
- nanoid (predictable results)
- @babel/runtime (inefficient RegExp)

### Remaining Vulnerabilities ⚠️

#### 1. Vite / esbuild (Moderate - 3 vulnerabilities)
**Current versions:**
- Vite: 4.3.9
- esbuild: embedded in Vite

**Issues:**
- esbuild enables websites to send requests to dev server

**Fix:**
Upgrade to Vite 7.x (breaking change):
```bash
npm install vite@latest @sveltejs/vite-plugin-svelte@latest --save-dev
```

**Note:** Vite 7 may require configuration changes. Test thoroughly after upgrade.

#### 2. Svelte (Moderate - 1 vulnerability)
**Current version:** 3.58.0

**Issue:**
- Potential mXSS vulnerability due to improper HTML escaping

**Fix:**
Upgrade to Svelte 4.2.19+ or Svelte 5.x (breaking change):
```bash
# Option 1: Svelte 4 (smaller breaking changes)
npm install svelte@^4.2.19 --save-dev

# Option 2: Svelte 5 (major rewrite, significant breaking changes)
npm install svelte@latest --save-dev
```

**Note:** Svelte 5 introduces runes and other significant API changes. See [Svelte 5 migration guide](https://svelte.dev/docs/svelte/v5-migration-guide).

---

## Should I Upgrade Now?

### For Development
The current vulnerabilities are **moderate** severity and primarily affect:
- Development server (not production)
- HTML escaping in specific edge cases

For local development, the current versions are acceptable.

### For Production
These vulnerabilities have minimal impact in production because:
1. **esbuild/Vite** - Only used during build time, not in the final bundle
2. **Svelte mXSS** - Only affects specific HTML escaping scenarios

However, it's good practice to stay updated. Consider upgrading when:
- You have time for thorough testing
- You're ready to handle breaking changes
- You want the latest features

---

## How to Upgrade Safely

### 1. Create a Branch
```bash
git checkout -b upgrade-dependencies
```

### 2. Upgrade Vite First
```bash
cd src/main/webui
npm install vite@latest @sveltejs/vite-plugin-svelte@latest --save-dev
npm run build  # Test the build
npm run dev    # Test dev server
```

### 3. Then Upgrade Svelte (if needed)
```bash
# Read migration guide first!
npm install svelte@^4.2.19 --save-dev
npm run build
npm run dev
```

### 4. Test Everything
- Run the build
- Test all UI components
- Check hot-reload
- Verify production build
- Test in multiple browsers

### 5. Commit and Deploy
```bash
git add .
git commit -m "chore: upgrade to Vite 7 and Svelte 4"
git push
```

---

## Monitoring

Check for new vulnerabilities regularly:
```bash
cd src/main/webui
npm audit
```

Auto-fix non-breaking issues:
```bash
npm audit fix
```

---

## Questions?

- [Vite Migration Guide](https://vitejs.dev/guide/migration)
- [Svelte 4 Release Notes](https://svelte.dev/blog/svelte-4)
- [Svelte 5 Migration Guide](https://svelte.dev/docs/svelte/v5-migration-guide)
- [npm audit documentation](https://docs.npmjs.com/cli/v10/commands/npm-audit)
