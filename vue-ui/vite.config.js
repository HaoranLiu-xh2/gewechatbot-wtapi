import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 配置 @ 指向 src 目录
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    // 开发服务器端口
    port: 5173,
    // 自动打开浏览器
    open: true,
    proxy: {
      // 代理后端接口
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      // 代理 WebSocket
      '/ws': {
        target: 'ws://127.0.0.1:8080',
        changeOrigin: true,
        ws: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern',
        additionalData: `@use "@/styles/variables.scss" as *;`
      }
    }
  }
})
