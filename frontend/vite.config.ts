import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          'vendor-query': ['@tanstack/react-query'],
          'vendor-ui': ['lucide-react', 'react-hot-toast'],
          'admin': [
            './src/pages/admin/AdminDashboard.tsx',
            './src/pages/admin/AdminProducts.tsx',
            './src/pages/admin/AdminOrders.tsx',
            './src/pages/admin/AdminUsers.tsx',
            './src/pages/admin/AdminCache.tsx',
            './src/pages/admin/AdminLayout.tsx',
          ],
        },
      },
    },
  },
})
