import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 定义 @ 别名指向 src 目录，跨平台路径引用的标准做法
      '@': path.resolve(__dirname, './src'),
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 使用 @ 别名进行注入，确保能通过相对项目根目录的路径找到文件
        additionalData: `@import "@/styles/variables.scss";`
      }
    }
  },
  server: {
    port: 3002,
    open: true,
    host: '0.0.0.0', // 允许外部访问
    strictPort:true,
    proxy: {
      '/api': {
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/uploads': {
        target: process.env.VITE_UPLOAD_BASE_URL || 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      }
    }
  },
  // 生产环境配置
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    rollupOptions: {
      output: {
        // 强制生成新的文件哈希，避免缓存问题
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]',
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus']
        }
      }
    }
  }
})
