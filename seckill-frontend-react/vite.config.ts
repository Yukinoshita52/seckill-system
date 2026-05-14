import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

function getPackageName(id: string) {
  const match = id.match(/node_modules\/((?:@[^/]+\/)?[^/]+)/);
  return match?.[1] ?? '';
}

export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return;
          }

          const packageName = getPackageName(id);

          if (['react', 'react-dom', 'scheduler'].includes(packageName)) {
            return 'react-core';
          }

          if (['react-router', 'react-router-dom'].includes(packageName)) {
            return 'router';
          }

          if (packageName === '@douyinfe/semi-ui') {
            return 'semi-ui';
          }

          if (packageName === '@douyinfe/semi-foundation') {
            return 'semi-foundation';
          }

          if (packageName === '@douyinfe/semi-icons') {
            return 'semi-icons';
          }

          if (
            ['@douyinfe/semi-animation-react', '@douyinfe/semi-animation'].includes(packageName)
          ) {
            return 'semi-motion';
          }

          if (packageName === 'axios') {
            return 'network-vendor';
          }

          return 'vendor-misc';
        },
      },
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:11010',
        changeOrigin: true,
      },
    },
  },
});
