import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 保留后端原始路径，确保 HttpOnly 刷新 Cookie 的 Path 与浏览器请求一致。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: Object.fromEntries(['/user', '/test'].map(path => [path, {
      target: process.env.BACKEND_URL || 'http://127.0.0.1:8092', changeOrigin: true,
    }])),
  },
})
