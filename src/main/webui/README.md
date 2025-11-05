# Heimdall Frontend

The Heimdall frontend is built with [Svelte](https://svelte.dev) and [Tailwind CSS](https://tailwindcss.com).

## Quick Start - UI Development

No Kubernetes or backend required! Just start the mock server and frontend:

```bash
# Install dependencies
npm install

# Terminal 1: Start mock backend
node mock-server.js

# Terminal 2: Start frontend dev server
npm run dev
```

Open http://localhost:5173 and start coding! 🎉

## Available Scripts

- `npm run dev` - Start development server with hot-reload (port 5173)
- `npm run build` - Build for production
- `npm run preview` - Preview production build locally

## Project Structure

```
src/
├── App.svelte              # Main app component
├── app.css                 # Global styles and Tailwind
├── main.js                 # App entry point
├── assets/                 # Static assets (logo, images)
└── lib/                    # Components and utilities
    ├── FlinkJobs.svelte    # Main dashboard component
    ├── Modal.svelte        # Settings modal
    ├── JobType.svelte      # Job type badge
    ├── ExternalEndpoint.svelte  # External link button
    └── stores/             # Svelte stores (state management)
        ├── flinkJobs.js    # Jobs data store
        ├── settings.js     # User settings store
        └── appConfig.js    # App configuration store
```

## Making Changes

### Styling

- **Tailwind Classes**: Use utility classes directly in components
- **Custom Styles**: Edit `src/app.css` for global styles
- **Theme Config**: Modify `tailwind.config.js` for colors, animations, etc.

### Components

All Svelte components support hot-reload. Just edit and save!

Example:
```svelte
<script>
  let count = 0;
</script>

<button class="btn-primary" on:click={() => count++}>
  Clicked {count} times
</button>
```

### Mock Data

Edit `mock-data.json` to test with different:
- Job statuses (RUNNING, FAILED, PENDING, etc.)
- Namespaces
- Resource configurations
- Metadata

The mock server watches for changes and reloads automatically!

## Tips

1. **Browser DevTools** - Use React DevTools or Svelte DevTools extension
2. **Tailwind IntelliSense** - Install VS Code extension for autocomplete
3. **Hot Reload** - Changes appear instantly, no refresh needed
4. **Console Logs** - Add `console.log()` in components to debug reactivity

## Need More?

See the full [Local Development Guide](../../../LOCAL_DEVELOPMENT.md) for:
- Running with the real backend
- Building for production
- Troubleshooting
- API reference

## Questions?

Check the [main README](../../../README.md) or create an issue on GitHub!
