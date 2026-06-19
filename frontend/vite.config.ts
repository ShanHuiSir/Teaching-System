import { resolve } from 'path'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

const root = process.cwd()

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': resolve(root, 'src') },
  },
  test: {
    environment: 'jsdom',
    globals: true,
  },
  css: {
    preprocessorOptions: {
      scss: {
        loadPaths: [resolve(root, 'src/styles')],
        additionalData: `@use "mixins" as *;\n`,
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/ai': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ai/, ''),
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes, req) => {
            if (req.url?.includes('/heartbeat')) {
              proxyRes.headers['connection'] = 'keep-alive'
              proxyRes.headers['cache-control'] = 'no-cache'
              proxyRes.headers['x-accel-buffering'] = 'no'
            }
          })
        },
      },
    },
  },
})
