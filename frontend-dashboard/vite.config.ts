import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true
      },
      '/actuator': {
        target: 'http://localhost:8085',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: '../dashboard-service/src/main/resources/static',
    emptyOutDir: true
  }
})
