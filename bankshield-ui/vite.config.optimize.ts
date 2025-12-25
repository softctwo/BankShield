import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { visualizer } from 'rollup-plugin-visualizer'
import viteCompression from 'vite-plugin-compression'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // 自动按需引入组件
    Components({
      resolvers: [ElementPlusResolver()],
      dts: true,
    }),
    // Gzip压缩
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
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
  
  // 构建优化配置
  build: {
    target: 'es2015',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
      format: {
        comments: false,
      },
    },
    rollupOptions: {
      output: {
        // 代码分割策略
        manualChunks: {
          // Vue相关库
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Element Plus
          'element-plus': ['element-plus'],
          // 工具库
          'utils': ['axios', 'dayjs', 'lodash-es'],
          // ECharts
          'echarts': ['echarts'],
          // 加密库
          'crypto': ['crypto-js'],
        },
        // 优化chunk大小
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
      },
    },
    // 启用CSS代码分割
    cssCodeSplit: true,
    // 启用sourcemap
    sourcemap: false,
    //  chunk大小警告的限制（单位kb）
    chunkSizeWarningLimit: 1000,
  },
  
  // 优化依赖预构建
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'element-plus',
      'axios',
      'dayjs',
      'echarts',
      'crypto-js',
    ],
    exclude: [],
  },
  
  // 服务器配置
  server: {
    host: '0.0.0.0',
    port: 3000,
    open: true,
    // 启用压缩
    compression: true,
    // 代理配置（开发环境）
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
    },
  },
  
  // 预览配置
  preview: {
    host: '0.0.0.0',
    port: 4173,
    open: true,
    compression: true,
  },
  
  // CSS预处理器配置
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@import "@/styles/variables.scss";`,
      },
    },
  },
})