import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'
import { installAccessDirective } from './utils/access'

const app = createApp(App)
const pinia = createPinia()

for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}

app.use(pinia)
installAccessDirective(app)
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')

useThemeStore().initTheme()
