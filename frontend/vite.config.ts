import { resolve } from 'path'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

const root = process.cwd()

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /^\/api\//,
            handler: 'NetworkOnly',
          },
          {
            urlPattern: /^\/ai\//,
            handler: 'NetworkOnly',
          },
        ],
      },
      manifest: {
        name: '教学评价系统',
        short_name: 'AITAS',
        description: 'AI 辅助教学评价系统',
        theme_color: '#006a6a',
        background_color: '#f4fbfa',
        icons: [
          { src: '/favicon.png', sizes: '72x72', type: 'image/png' },
        ],
        start_url: '/',
        display: 'standalone',
      },
    }),
  ],
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
