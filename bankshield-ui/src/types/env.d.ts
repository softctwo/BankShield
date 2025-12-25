// Vite环境变量类型定义
declare const __APP_ENV__: string;

// 扩展 Window 接口以支持 __VUE_ROUTER__
interface Window {
  __VUE_ROUTER__?: any;
}

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  readonly VITE_APP_ENV: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
