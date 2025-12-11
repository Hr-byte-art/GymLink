import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
    // 全局配置 Element Plus 组件默认属性
    message: {
        duration: 2000, // 消息显示2秒
        grouping: true  // 相同内容的消息合并
    }
})

app.mount('#app')
