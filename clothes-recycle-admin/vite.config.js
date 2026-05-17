import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3100,
    proxy: {
      '/api': {
        // target: 'http://localhost:8080',
        target: 'http://47.116.79.134:8080',
        changeOrigin: true
      },
      '/uploads': {
        // target: 'http://localhost:8080',
        target: 'http://47.116.79.134:8080',
        changeOrigin: true
      }
    }
  }
})