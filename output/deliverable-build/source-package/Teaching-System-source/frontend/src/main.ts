import { createApp } from 'vue'
import './style.css'
import './composables/useTheme' // init theme before app mounts
import App from './App.vue'
import router from './router'

async function init() {
  // 等待 index.html 中的加载脚本完成权限检查（若其尚未完成）
  if (!(window as any).__loadingComplete) {
    await new Promise<void>(r => window.addEventListener('loading-complete', () => r(), { once: true }))
  }

  const setProgress = (window as any).__setLoadingProgress as (p: number) => void

  // Phase 2: 初始化 Vue 应用 (35% → 55%)
  setProgress(55)
  const app = createApp(App)
  app.use(router)
  app.mount('#app')

  // Phase 3: 等待初始路由就绪 + 全量组件渲染 (55% → 90%)
  setProgress(90)
  await router.isReady()

  // Phase 4: 淡出加载遮罩 (90% → 100%)
  setProgress(100)
  const ld = document.getElementById('ld-root')
  if (ld) {
    ld.classList.add('ld-done')
    ld.addEventListener('transitionend', () => ld.remove())
  }
  document.documentElement.classList.remove('ld-lock')
}

init()
