import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/auth': 'http://localhost:8080',
      '/users': 'http://localhost:8080',
      '/academic-years': 'http://localhost:8080',
      '/classes': 'http://localhost:8080',
      '/subjects': 'http://localhost:8080',
      '/batches': 'http://localhost:8080',
      '/assignments': 'http://localhost:8080',
      '/students': 'http://localhost:8080',
      '/attendance': 'http://localhost:8080',
      '/reports': 'http://localhost:8080'
    }
  }
});