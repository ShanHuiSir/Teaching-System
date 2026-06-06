import { createApp } from 'vue'
import './style.css'
import './composables/useTheme.js' // init theme before app mounts
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(router)
app.mount('#app')
