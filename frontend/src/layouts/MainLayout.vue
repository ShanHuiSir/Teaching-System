<template>
  <div class="layout">
    <!-- Top App Bar -->
    <header class="top-bar" :class="{ 'top-bar--ripple': ripple > 0 }">
      <span class="top-bar__ripple-clip">
        <i v-if="ripple > 0" class="top-bar__ripple" :style="{ left: rippleLeft, top: rippleTop }" :key="ripple" @animationend="ripple = 0" />
      </span>
      <div class="magic-bar">
        <Transition name="magic" mode="out-in">
          <div class="magic-bar__text" :key="magicKey">
            <template v-if="magicBar.status">
              <span class="magic-bar__dot" :class="`magic-bar__dot--${magicBar.statusType}`" />
              <span class="magic-bar__status">{{ magicBar.status }}</span>
            </template>
            <template v-else>
              <span class="magic-bar__primary">{{ magicBar.primary }}</span>
              <template v-if="greeting || magicBar.sub">
                <span class="magic-bar__sep">·</span>
                <span v-if="greeting" class="magic-bar__greeting">{{ greeting }}</span>
                <span v-else class="magic-bar__sub">{{ magicBar.sub }}</span>
              </template>
              <template v-if="magicBar.suffix">
                <span class="magic-bar__sep">·</span>
                <span class="magic-bar__suffix" :class="`magic-bar__suffix--${magicBar.suffixType}`">{{ magicBar.suffix }}</span>
              </template>
            </template>
          </div>
        </Transition>
      </div>
      <div class="top-bar__right">
        <button class="top-bar__avatar" @click.stop="menuOpen = !menuOpen">
          {{ avatarLetter }}
        </button>
        <Transition name="menu">
          <div v-if="menuOpen" class="user-menu" @click.stop>
            <div class="user-menu__profile">
              <div class="user-menu__avatar">{{ avatarLetter }}</div>
              <span class="user-menu__name">{{ teacherName }}</span>
              <span class="user-menu__account">{{ accountName }}</span>
            </div>
            <button class="user-menu__btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
              </svg>
              <span>账户设置</span>
            </button>
            <button class="user-menu__btn" :disabled="resetting" @click="onResetDemo">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="1 4 1 10 7 10" />
                <polyline points="23 20 23 14 17 14" />
                <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" />
              </svg>
              <span>{{ resetting ? '恢复中...' : '恢复测试环境' }}</span>
            </button>
            <hr class="user-menu__divider" />
            <button class="user-menu__btn user-menu__btn--debug" @click="forceCrash">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" /></svg>
              <span>强制组件错误</span>
            </button>
            <button class="user-menu__btn" @click="toggleTheme">
              <svg v-if="isDark" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5" /><line x1="12" y1="1" x2="12" y2="3" /><line x1="12" y1="21" x2="12" y2="23" /><line x1="4.22" y1="4.22" x2="5.64" y2="5.64" /><line x1="18.36" y1="18.36" x2="19.78" y2="19.78" /><line x1="1" y1="12" x2="3" y2="12" /><line x1="21" y1="12" x2="23" y2="12" /><line x1="4.22" y1="19.78" x2="5.64" y2="18.36" /><line x1="18.36" y1="5.64" x2="19.78" y2="4.22" /></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" /></svg>
              <span>{{ isDark ? '浅色模式' : '深色模式' }}</span>
            </button>
            <button class="user-menu__btn" @click="logout">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
              <span>退出登录</span>
            </button>
          </div>
        </Transition>
      </div>
    </header>

    <!-- click-outside backdrop -->
    <div v-if="menuOpen" class="menu-backdrop" @click="menuOpen = false" />

    <div class="layout__body">
      <!-- Left Rail -->
      <nav class="rail rail--left" :class="{ 'rail--expanded': leftExpanded }">
        <button class="rail__toggle rail__toggle--left" @click="leftExpanded = !leftExpanded">
          <svg v-if="!leftExpanded" viewBox="0 0 24 24" fill="currentColor" stroke="none">
            <path d="M17.404 13.096L22 8.5l-4.596-4.596-1.414 1.414L19.172 8.5 15.99 11.682l1.414 1.414z" />
            <path d="M21 18v2H3v-2h18zM12 11v2H3v-2h9zm0-7v2H3V4h9z" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="currentColor" stroke="none">
            <path d="M21 18v2H3v-2h18zM20.01 5.318L16.828 8.5l3.182 3.182-1.414 1.414L14 8.5l4.596-4.596 1.414 1.414zM12 11v2H3v-2h9zm0-7v2H3V4h9z" />
          </svg>
        </button>
        <div class="rail__items">
          <div class="nav-indicator" :style="indicatorStyle" />

          <template v-for="(item, i) in navItems" :key="i">
            <hr v-if="item.divider" class="rail__divider" />
            <button
              v-else
              class="nav-item"
              :class="{
                'nav-item--error': item.variant === 'error',
                'nav-item--ghost': item.variant === 'ghost',
                'nav-item--active': item.route === '/' ? route.path === '/' : route.path.startsWith(item.route),
              }"
              @click="go(item.route)"
            >
              <svg class="nav-item__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <template v-if="item.icon === 'dashboard'">
                  <rect x="3" y="3" width="7" height="7" rx="1" />
                  <rect x="14" y="3" width="7" height="7" rx="1" />
                  <rect x="3" y="14" width="7" height="7" rx="1" />
                  <rect x="14" y="14" width="7" height="7" rx="1" />
                </template>
                <template v-else-if="item.icon === 'review'">
                  <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
                  <rect x="9" y="3" width="6" height="4" rx="1" />
                  <path d="m9 14 2 2 4-4" />
                </template>
                <template v-else-if="item.icon === 'classes'">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </template>
                <template v-else-if="item.icon === 'assignments'">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                </template>
              </svg>
              <span class="nav-item__label">{{ item.label }}</span>
            </button>
          </template>
        </div>
      </nav>

      <!-- Content Area -->
      <main class="layout__content">
        <router-view v-slot="{ Component }">
          <KeepAlive>
            <component :is="Component" />
          </KeepAlive>
        </router-view>
      </main>

      <!-- Right Rail -->
      <aside class="rail rail--right" :class="{ 'rail--expanded': rightExpanded }">
        <button class="rail__toggle rail__toggle--right" @click="rightExpanded = !rightExpanded">
          <svg v-if="!rightExpanded" viewBox="0 0 24 24" fill="currentColor" stroke="none">
            <path d="M20.01 5.318L16.828 8.5l3.182 3.182-1.414 1.414L14 8.5l4.596-4.596 1.414 1.414zM12 11v2H3v-2h9zm0-7v2H3V4h9zM21 18v2H3v-2h18z" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="currentColor" stroke="none">
            <path d="M17.404 13.096L22 8.5l-4.596-4.596-1.414 1.414L19.172 8.5 15.99 11.682l1.414 1.414z" />
            <path d="M21 18v2H3v-2h18zM12 11v2H3v-2h9zm0-7v2H3V4h9z" />
          </svg>
        </button>
        <div class="rail__items">
          <!-- Persistent buttons -->
          <button class="nav-item" data-tooltip="刷新" @click="doRefresh">
            <svg class="nav-item__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10" />
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
            </svg>
            <span class="nav-item__label">刷新</span>
          </button>

          <!-- Dynamic buttons (per-page) -->
          <template v-if="rightButtons.length">
            <hr class="rail__divider rail__divider--gap" />
            <template v-for="btn in rightButtons" :key="btn.key">
              <hr v-if="btn.divider" class="rail__divider" :class="{ 'rail__divider--gap': btn.gap }" />
              <button
                v-else
                class="nav-item"
                :class="{ 'nav-item--active': btn.active }"
                :data-tooltip="btn.label"
                @click="btn.action"
              >
              <svg class="nav-item__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <template v-if="btn.icon === 'sort-class'">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M22 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </template>
                <template v-else-if="btn.icon === 'sort-time'">
                  <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
                </template>
                <template v-else-if="btn.icon === 'sort-completion'">
                  <path d="M9 11l3 3L22 4" /><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
                </template>
                <template v-else-if="btn.icon === 'sort-count'">
                  <line x1="4" y1="9" x2="20" y2="9" /><line x1="4" y1="15" x2="20" y2="15" /><line x1="8" y1="5" x2="6" y2="19" /><line x1="16" y1="5" x2="14" y2="19" />
                </template>
                <template v-else-if="btn.icon === 'sort-rate'">
                  <rect x="2" y="2" width="20" height="20" rx="2" /><path d="M7 16l3-6 4 4 3-8" />
                </template>
                <template v-else-if="btn.icon === 'filter-pending'">
                  <circle cx="12" cy="12" r="10" /><polyline points="8 12 11 15 16 9" />
                </template>
                <template v-else-if="btn.icon === 'filter-none'">
                  <circle cx="12" cy="12" r="10" /><line x1="8" y1="12" x2="16" y2="12" />
                </template>
                <template v-else-if="btn.icon === 'filter-unsub'">
                  <circle cx="12" cy="12" r="10" /><line x1="8" y1="8" x2="16" y2="16" /><line x1="16" y1="8" x2="8" y2="16" />
                </template>
                <template v-else-if="btn.icon === 'dash-class'">
                  <rect x="3" y="3" width="7" height="7" rx="1" /><rect x="14" y="3" width="7" height="7" rx="1" /><rect x="3" y="14" width="7" height="7" rx="1" /><rect x="14" y="14" width="7" height="7" rx="1" />
                </template>
                <template v-else-if="btn.icon === 'dash-work'">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /><line x1="16" y1="13" x2="8" y2="13" />
                </template>
                <template v-else-if="btn.icon === 'dash-score'">
                  <line x1="4" y1="20" x2="18" y2="20" /><polyline points="6 20 6 14 10 10 14 16 18 8" />
                </template>
                <template v-else-if="btn.icon === 'dash-dev'">
                  <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
                </template>
                <template v-else-if="btn.icon === 'dash-tw'">
                  <circle cx="12" cy="12" r="10" /><circle cx="12" cy="12" r="4" /><line x1="12" y1="2" x2="12" y2="8" />
                </template>
                <template v-else-if="btn.icon === 'dash-trend'">
                  <polyline points="2 18 6 10 10 14 14 6 18 12 22 12" />
                </template>
              </svg>
              <span class="nav-item__label">{{ btn.label }}</span>
            </button>
          </template>
          </template>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, provide, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCookie, delCookie } from '../utils/cookie.js'
import { useSnackbar } from '../composables/useSnackbar.js'
import http from '../utils/request.js'
import { onConnectionChange } from '../utils/recoveryPoll.js'
import { useTheme } from '../composables/useTheme.js'

const snackbar = useSnackbar()

const router = useRouter()
const route = useRoute()

const leftExpanded = ref(true)
const rightExpanded = ref(false)
const menuOpen = ref(false)

const teacherName = computed(() => getCookie('user_name') || '?')
const accountName = computed(() => getCookie('user_name') || '?')
const avatarLetter = computed(() => teacherName.value.charAt(0).toUpperCase())

const magicBar = reactive({ primary: '仪表盘', sub: '', status: '', statusType: '', count: 0, suffix: '', suffixType: '' })
const ripple = ref(0)
const rippleLeft = ref('50%')
const rippleTop = ref('50%')
function triggerRipple(cx, cy) {
  const bar = document.querySelector('.top-bar')
  const r = bar ? bar.getBoundingClientRect() : { left: 0, top: 0 }
  rippleLeft.value = (cx ?? window.innerWidth / 2) - r.left + 'px'
  rippleTop.value = (cy ?? 32) - r.top + 'px'
  ripple.value++
}
provide('triggerRipple', triggerRipple)
const greeting = ref('')
const magicKey = computed(() => `${magicBar.primary}|${magicBar.sub}|${magicBar.status}|${magicBar.suffix}|${greeting.value}`)
provide('magicBar', magicBar)

// ── Greeting system ──
const POEMS = [
  '学而不厌，诲人不倦',
  '随风潜入夜，润物细无声',
  '桃李不言，下自成蹊',
  '落红不是无情物，化作春泥更护花',
  '令公桃李满天下，何用堂前更种花',
  '新竹高于旧竹枝，全凭老干为扶持',
]

function pickGreeting() {
  const h = new Date().getHours()
  const name = teacherName.value
  const timeGreet = h < 6 ? '夜深了' : h < 9 ? '早上好' : h < 12 ? '上午好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
  const poem = POEMS[Math.floor(Math.random() * POEMS.length)]
  const count = magicBar.count
  const countPart = count > 0 ? `今天还有 ${count} 份作业待批改。` : ''
  return Math.random() < 0.5
    ? `${timeGreet}，${name}老师！${countPart}`
    : `「${poem}」 ${timeGreet}，${name}老师！${countPart}`
}

let greetTimer = null
let greetDismissTimer = null

function showGreeting(pagePrimary) {
  clearTimeout(greetTimer)
  clearTimeout(greetDismissTimer)
  greetTimer = setTimeout(() => {
    magicBar.primary = pagePrimary
    greeting.value = pickGreeting()
    greetDismissTimer = setTimeout(() => {
      greeting.value = ''
    }, 5000)
  }, 300)
}

// ── Rest reminder ──
let restTimer = null
const REST_INTERVAL = 60 * 60 * 1000 // 1 hour

function resetRestTimer() {
  clearTimeout(restTimer)
  restTimer = setTimeout(() => {
    magicBar.status = '已经连续工作一段时间了，起来活动一下吧'
    magicBar.statusType = 'info'
    setTimeout(() => {
      if (magicBar.status === '已经连续工作一段时间了，起来活动一下吧') {
        magicBar.status = ''
      }
      resetRestTimer()
    }, 6000)
  }, REST_INTERVAL)
}

// ── Connection health check (delegates to recoveryPoll) ──
const lostConnection = ref(false)
let reconnectTimer = null

function hasDraftsEnabled() {
  try {
    const prefs = JSON.parse(localStorage.getItem('cookie_prefs') || '{}')
    return prefs.drafts !== false
  } catch { return true }
}

function onReconnected() {
  lostConnection.value = false
  magicBar.suffix = '服务器已恢复连接'
  magicBar.suffixType = 'reconnected'
  magicBar.status = ''
  clearTimeout(reconnectTimer)
  snackbar.show('服务器已恢复连接', { variant: 'info', duration: 2500 })
  setTimeout(() => {
    if (magicBar.suffix === '服务器已恢复连接') magicBar.suffix = ''
  }, 3000)
}

function onDisconnected() {
  lostConnection.value = true
  const draftMsg = hasDraftsEnabled() ? ' · 修改已保存在本地' : ''
  magicBar.status = `啊！与服务器断连了${draftMsg}`
  magicBar.statusType = 'info'
  reconnectTimer = setTimeout(() => {
    magicBar.status = ''
    magicBar.suffix = navigator.onLine ? '积极重连中' : '离线'
    magicBar.suffixType = navigator.onLine ? 'reconnecting' : 'offline'
  }, 4000)
}

onConnectionChange((connected) => {
  if (connected) {
    if (lostConnection.value) onReconnected()
  } else {
    onDisconnected()
  }
})

window.addEventListener('online', () => {
  if (lostConnection.value) {
    magicBar.suffix = '积极重连中'
    magicBar.suffixType = 'reconnecting'
  }
})
window.addEventListener('offline', () => {
  if (lostConnection.value) {
    magicBar.suffix = '离线'
    magicBar.suffixType = 'offline'
  }
})

onMounted(() => {
  resetRestTimer()
  document.addEventListener('click', resetRestTimer, { once: false })
  document.addEventListener('keydown', resetRestTimer, { once: false })
})

onUnmounted(() => {
  clearTimeout(restTimer)
  clearTimeout(greetTimer)
  clearTimeout(greetDismissTimer)
  clearTimeout(reconnectTimer)
  document.removeEventListener('click', resetRestTimer)
  document.removeEventListener('keydown', resetRestTimer)
})

// Expose helpers for pages
provide('showGreeting', showGreeting)

const resetting = ref(false)

async function onResetDemo() {
  if (resetting.value) return
  resetting.value = true
  try {
    await http.post('/dev/reset-demo-data')
    snackbar.show('测试环境已恢复', { variant: 'info' })
  } catch {
    snackbar.show('恢复失败，请检查后端服务', { variant: 'error' })
  } finally {
    resetting.value = false
  }
}

function logout() {
  delCookie('auth_token')
  delCookie('user_name')
  menuOpen.value = false
  router.replace('/login')
}

const { isDark, toggle: toggleTheme } = useTheme()

function forceCrash() {
  menuOpen.value = false
  throw new Error('🔧 手动触发的测试错误 —— 一切正常，这只是个演习！')
}

const navItems = [
  { label: '仪表盘', route: '/', icon: 'dashboard' },
  { divider: true },
  { label: '作业审批', route: '/review', icon: 'review', variant: 'error' },
  { divider: true },
  { label: '作业管理', route: '/assignments', icon: 'assignments', variant: 'ghost' },
  { label: '班级管理', route: '/classes', icon: 'classes', variant: 'ghost' },
]

const ITEM_H = 44
const GAP = 2
const DIVIDER_H = 1
const DIVIDER_M = 12
const PAD = 8

const indicatorStyle = computed(() => {
  let top = PAD
  for (const item of navItems) {
    if (item.route) {
      const active = item.route === '/' ? route.path === '/' : route.path.startsWith(item.route)
      if (active) {
        return { top: `${top}px`, height: `${ITEM_H}px` }
      }
    }
    top += item.divider ? DIVIDER_H + DIVIDER_M + GAP : ITEM_H + GAP
  }
  return { top: '0px', height: '0px' }
})

const refreshTick = ref(0)
const rightButtons = ref([])
provide('refreshTick', refreshTick)
provide('rightButtons', rightButtons)

async function doRefresh() {
  try {
    await http.get('/health')
    refreshTick.value++
    if (lostConnection.value) {
      onReconnected()
    }
    snackbar.show('数据已刷新', { variant: 'info', duration: 2000 })
  } catch {
    snackbar.show('刷新失败，服务器无响应', { variant: 'error', duration: 3000 })
  }
}

function go(path) {
  const active = path === '/' ? route.path === '/' : route.path.startsWith(path)
  if (!active) {
    router.push(path)
  }
}
</script>

<style lang="scss" scoped>
$font-family: "PingFang SC", "Microsoft YaHei", -apple-system, sans-serif;

@mixin font($size, $height, $weight: 400) {
  font: $weight #{$size}/#{$height} $font-family;
}

.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: rgb(var(--md-sys-color-background));
}

/* ── Top App Bar ── */
.top-bar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: rgb(var(--md-sys-color-surface-container));
  border-bottom: 1px solid rgb(var(--md-sys-color-outline-variant));
  flex-shrink: 0;

  &__right {
    position: relative;
  }

  &__ripple-clip {
    position: absolute;
    inset: 0;
    overflow: hidden;
    pointer-events: none;
    border-radius: inherit;
  }

  &__ripple {
    position: absolute;
    width: 10px;
    height: 10px;
    margin-left: -5px;
    margin-top: -5px;
    border-radius: 50%;
    background: rgb(var(--md-sys-color-primary) / .28);
    transform: scale(0);
    animation: magic-ripple .7s ease-out forwards;
  }
}

/* ── MagicBar ── */
.magic-bar {
  overflow: hidden;
  height: 28px;

  &__text {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: baseline;
    gap: 6px;
    white-space: nowrap;
  }

  &__primary {
    @include font(18px, 24px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: .02em;
  }

  &__sep {
    @include font(18px, 24px);
    color: rgb(var(--md-sys-color-outline));
  }

  &__sub {
    @include font(15px, 24px, 400);
    color: rgb(var(--md-sys-color-on-surface-variant));
    max-width: 420px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__greeting {
    @include font(15px, 24px, 400);
    color: rgb(var(--md-sys-color-on-surface-variant));
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__suffix {
    @include font(14px, 24px, 400);
    white-space: nowrap;

    &--reconnecting {
      color: rgb(var(--md-sys-color-tertiary));
      animation: magic-breathe 3s ease-in-out infinite;
    }

    &--offline {
      color: rgb(var(--md-sys-color-error));
    }

    &--reconnected {
      color: #16a34a;
    }
  }

  &__status {
    @include font(15px, 24px, 400);
    color: rgb(var(--md-sys-color-on-surface-variant));
    max-width: 520px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__dot {
    flex-shrink: 0;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    align-self: center;

    &--loading {
      background: rgb(var(--md-sys-color-primary));
      animation: magic-pulse 1.2s ease-in-out infinite;
    }

    &--success {
      background: #16a34a;
    }

    &--info {
      background: rgb(var(--md-sys-color-tertiary));
      animation: magic-pulse 2s ease-in-out infinite;
    }
  }
}

@keyframes magic-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: .35; transform: scale(.75); }
}

@keyframes magic-breathe {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: .6; transform: scale(.94); }
}

@keyframes magic-ripple {
  0% { transform: scale(0); opacity: 1; }
  100% { transform: scale(220); opacity: 0; }
}

.magic-enter-active,
.magic-leave-active {
  transition: transform .3s cubic-bezier(.4, 0, .2, 1), opacity .25s ease;
}
.magic-leave-to {
  transform: translateY(-120%);
  opacity: 0;
}
.magic-enter-from {
  transform: translateY(120%);
  opacity: 0;
}

.top-bar {

  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: none;
    background: rgb(var(--md-sys-color-primary-container));
    color: rgb(var(--md-sys-color-on-primary-container));
    display: flex;
    align-items: center;
    justify-content: center;
    @include font(16px, 20px, 600);
    cursor: pointer;
  }
}

/* ── User Menu ── */
.user-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  padding: 12px;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0 0 0 / .12);
  z-index: 100;
  display: flex;
  flex-direction: column;

  &__profile {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 12px 16px;
    margin-bottom: 8px;
    background: rgb(var(--md-sys-color-secondary-container));
    border-radius: 12px;
  }

  &__avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    display: flex;
    align-items: center;
    justify-content: center;
    @include font(24px, 32px, 600);
    margin-bottom: 12px;
  }

  &__name {
    @include font(16px, 24px, 500);
    color: rgb(var(--md-sys-color-on-primary-container));
  }

  &__account {
    @include font(13px, 20px);
    color: rgb(var(--md-sys-color-on-primary-container));
    opacity: .7;
    margin-top: 2px;
  }

  &__btn {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    height: 40px;
    padding: 0 12px;
    border: none;
    border-radius: 12px;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface));
    cursor: pointer;
    transition: background .15s ease;

    svg {
      width: 18px;
      height: 18px;
      flex-shrink: 0;
    }

    span {
      @include font(14px, 20px, 500);
    }

    &:hover {
      background: rgb(var(--md-sys-color-on-surface) / .08);
    }

    &--debug {
      color: rgb(var(--md-sys-color-error));

      &:hover {
        background: rgb(var(--md-sys-color-error) / .08);
      }
    }
  }

  &__divider {
    border: none;
    height: 1px;
    background: rgb(var(--md-sys-color-outline-variant));
    margin: 4px 0;
  }
}

/* ── Menu backdrop ── */
.menu-backdrop {
  position: fixed;
  inset: 0;
  z-index: 99;
}

/* ── Menu transition ── */
.menu-enter-active {
  transition: opacity .2s ease, transform .2s cubic-bezier(.4, 0, .2, 1);
}
.menu-leave-active {
  transition: opacity .15s ease, transform .15s ease;
}
.menu-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(.96);
}
.menu-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(.98);
}

/* ── Body ── */
.layout__body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ── Rails ── */
.rail {
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-right: 1px solid rgb(var(--md-sys-color-outline-variant));
  transition: width .25s cubic-bezier(.4, 0, .2, 1);
  overflow: hidden;

  &--left {
    width: 64px;
    border-right: 1px solid rgb(var(--md-sys-color-outline-variant));

    &.rail--expanded {
      width: 200px;
    }
  }

  &--right {
    width: 68px;
    border-left: 1px solid rgb(var(--md-sys-color-outline-variant));
    border-right: none;
    overflow: visible;

    .rail__items {
      justify-content: center;
      align-items: center;
      padding: 8px 4px;
    }

    .nav-item {
      width: 44px;
      height: 44px;
      justify-content: flex-start;
      padding: 0 11px;
      border-radius: 50%;
      transition:
        width .25s cubic-bezier(.4, 0, .2, 1),
        padding .25s cubic-bezier(.4, 0, .2, 1),
        border-radius .25s cubic-bezier(.4, 0, .2, 1);

      &__label {
        transition: opacity .2s cubic-bezier(.4, 0, .2, 1);
      }
    }

    .nav-item--active {
      background: rgb(var(--md-sys-color-on-surface) / .12);
    }

    &:not(.rail--expanded) {
      .nav-item__label {
        opacity: 0;
      }

      .nav-item:hover::after {
        content: attr(data-tooltip);
        position: absolute;
        right: calc(100% + 8px);
        top: 50%;
        transform: translateY(-50%);
        padding: 4px 10px;
        border-radius: 4px;
        background: rgb(var(--md-sys-color-inverse-surface));
        color: rgb(var(--md-sys-color-inverse-on-surface));
        box-shadow: 0 1px 3px rgb(var(--md-sys-color-shadow) / .2);
        @include font(12px, 18px, 500);
        white-space: nowrap;
        z-index: 10;
        pointer-events: none;
      }
    }

    &.rail--expanded {
      width: 180px;

      .nav-item {
        width: 100%;
        border-radius: 28px;
        padding: 0 14px;
      }
    }
  }

  &__toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    margin: 12px;
    padding: 0;
    border: none;
    border-radius: 50%;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    cursor: pointer;
    transition: background .15s ease;
    flex-shrink: 0;

    svg {
      width: 20px;
      height: 20px;
    }

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }

    &--left {
      align-self: flex-start;
    }

    &--right {
      align-self: flex-end;
    }
  }

  &__items {
    position: relative;
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 8px;
    gap: 2px;
  }

  &__divider {
    border: none;
    height: 1px;
    background: rgb(var(--md-sys-color-outline-variant));
    margin: 6px 8px;

    &--gap {
      margin-top: 16px;
    }
  }
}

/* ── Active Indicator ── */
.nav-indicator {
  position: absolute;
  left: 8px;
  right: 8px;
  border-radius: 28px;
  background: rgb(var(--md-sys-color-on-surface-variant) / 0.2);
  transition: top .35s cubic-bezier(.4, 0, .2, 1), height .35s cubic-bezier(.4, 0, .2, 1);
  pointer-events: none;
  z-index: 2;
}

/* ── Nav Items ── */
.nav-item {
  position: relative;
  z-index: 1;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 14px;
  height: 44px;
  padding: 0 14px;
  border: none;
  border-radius: 28px;
  background: transparent;
  color: rgb(var(--md-sys-color-on-surface-variant));
  cursor: pointer;
  transition: background .15s ease;
  white-space: nowrap;

  &:hover {
    background: rgb(var(--md-sys-color-surface-container-highest));
  }

  &__icon {
    width: 22px;
    height: 22px;
    flex-shrink: 0;
  }

  &__label {
    @include font(14px, 20px, 500);
    overflow: hidden;
  }

  &--error {
    background: rgb(var(--md-sys-color-error-container));
    color: rgb(var(--md-sys-color-on-error-container));

    &:hover {
      filter: brightness(.95);
    }
  }

  &--ghost {
    color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &--active {
    color: rgb(var(--md-sys-color-on-surface));
  }
}

/* ── Content ── */
.layout__content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>
