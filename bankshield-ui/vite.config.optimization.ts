import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { visualizer } from 'rollup-plugin-visualizer'
import viteCompression from 'vite-plugin-compression'

// 前端性能优化配置
export default defineConfig({
  plugins: [
    vue(),
    // Gzip压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240, // 10KB以上才压缩
      algorithm: 'gzip',
      ext: '.gz',
    }),
    // Brotli压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'brotliCompress',
      ext: '.br',
    }),
    // 打包分析
    visualizer({
      open: true,
      gzipSize: true,
      brotliSize: true,
    }),
  ],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  
  build: {
    // 代码分割优化
    rollupOptions: {
      output: {
        // 手动分包
        manualChunks: {
          // Vue核心
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Element Plus
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          // 图表库
          'charts': ['echarts'],
          // 工具库
          'utils': ['axios', 'dayjs', 'js-cookie'],
        },
        // 分包命名
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
      },
    },
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境移除console
        drop_debugger: true,
      },
    },
    // 分块大小警告阈值
    chunkSizeWarningLimit: 1000,
    // 启用CSS代码分割
    cssCodeSplit: true,
    // 构建后是否生成source map
    sourcemap: false,
  },
  
  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'element-plus',
      '@element-plus/icons-vue',
      'echarts',
      'axios',
      'dayjs',
    ],
  },
  
  // 服务器配置
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
