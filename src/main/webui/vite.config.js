import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [svelte()],
  base: './',
  server: {
    port: 5173,
    host: '127.0.0.1',
    hmr: {
      // for proxying from quarkus dev
      port: 5173,
      // for testing standalone
      // protocol: 'ws',
      host: '127.0.0.1',
    },
    // Proxy API requests to mock server or backend
    proxy: {
      '/jobs': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/config': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/health': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
