import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
import pinia from './stores'

// 引入全局样式
import '@/styles/index.less'
import '@/styles/element-plus.less'

// 引入NProgress样式
import 'nprogress/nprogress.css'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用Element Plus（中文环境）
app.use(ElementPlus, {
  locale: zhCn,
})

app.use(pinia)
app.use(router)

app.mount('#app')