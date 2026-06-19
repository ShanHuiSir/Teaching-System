<template>
  <div class="layout">
    <!-- Top App Bar -->
    <header class="top-bar" :class="{ 'top-bar--ripple': ripple > 0 }">
      <span class="top-bar__ripple-clip">
        <i
          v-if="ripple > 0"
          :key="ripple"
          class="top-bar__ripple"
          :style="{ left: rippleLeft, top: rippleTop }"
          @animationend="ripple = 0"
        />
      </span>
      <MagicBar :magic-bar="magicBar" :greeting="greeting" :magic-key="magicKey" />
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
              <AppIcon name="settings" />
              <span>账户设置</span>
            </button>
            <button class="user-menu__btn" :disabled="resetting" @click="onResetDemo">
              <AppIcon name="reset" />
              <span>{{ resetting ? '恢复中...' : '恢复测试环境' }}</span>
            </button>
            <hr class="user-menu__divider" />
            <button class="user-menu__btn user-menu__btn--debug" @click="forceCrash">
              <AppIcon name="debug" />
              <span>强制组件错误</span>
            </button>
            <button class="user-menu__btn" @click="toggleTheme">
              <AppIcon :name="isDark ? 'sun' : 'moon'" />
              <span>{{ isDark ? '浅色模式' : '深色模式' }}</span>
            </button>
            <button class="user-menu__btn" @click="logout">
              <AppIcon name="logout" />
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
          <AppIcon :name="leftExpanded ? 'rail-left-expanded' : 'rail-left-collapsed'" />
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
                'nav-item--active': route.path.startsWith(item.route),
              }"
              @click="go(item.route)"
            >
              <AppIcon :name="item.icon" class="nav-item__icon" />
              <span class="nav-item__label">{{ item.label }}</span>
            </button>
          </template>
        </div>
      </nav>

      <!-- Content Area -->
      <main class="layout__content">
        <router-view v-slot="{ Component }">
          <KeepAlive :max="4">
            <component :is="Component" />
          </KeepAlive>
        </router-view>
      </main>

      <!-- Right Rail -->
      <aside class="rail rail--right" :class="{ 'rail--expanded': rightExpanded }">
        <button class="rail__toggle rail__toggle--right" @click="rightExpanded = !rightExpanded">
          <AppIcon :name="rightExpanded ? 'rail-right-expanded' : 'rail-right-collapsed'" />
        </button>
        <div class="rail__items">
          <!-- Persistent buttons -->
          <button class="nav-item" data-tooltip="刷新" @click="doRefresh">
            <AppIcon name="refresh" class="nav-item__icon" />
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
                <AppIcon :name="btn.icon" class="nav-item__icon" />
                <span class="nav-item__label">{{ btn.label }}</span>
              </button>
            </template>
          </template>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, provide } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCookie, delCookie } from '../utils/cookie'
import { useNotify } from '../composables/useNotify'
import http from '../utils/request'
import { clearSessionUser } from '../utils/session'
import { useTheme } from '../composables/useTheme'
import { useMagicBar } from '../composables/useMagicBar'
import { MAGIC_BAR_KEY, TRIGGER_RIPPLE_KEY, REFRESH_TICK_KEY, RIGHT_BUTTONS_KEY, SHOW_GREETING_KEY, DATA_VERSION_KEY } from '../types'
import MagicBar from '../components/MagicBar.vue'
import AppIcon from '../components/AppIcon.vue'

const { notify } = useNotify()

const router = useRouter()
const route = useRoute()

const leftExpanded = ref(true)
const rightExpanded = ref(false)
const menuOpen = ref(false)

const teacherName = computed(() => getCookie('user_name') || '?')
const accountName = computed(() => getCookie('user_name') || '?')
const avatarLetter = computed(() => teacherName.value.charAt(0).toUpperCase())

// ── MagicBar ──
const {
  magicBar,
  greeting,
  ripple,
  rippleLeft,
  rippleTop,
  magicKey,
  triggerRipple,
  showGreeting,
} = useMagicBar(teacherName)

provide(MAGIC_BAR_KEY, magicBar)
provide(TRIGGER_RIPPLE_KEY, triggerRipple)


// Expose helpers for pages
provide(SHOW_GREETING_KEY, showGreeting)

const resetting = ref(false)

async function onResetDemo() {
  if (resetting.value) return
  resetting.value = true
  try {
    await http.post('/dev/reset-demo-data')
    notify({ type: 'success', snackbar: '测试环境已恢复' })
  } catch {
    notify({ type: 'error', snackbar: '恢复失败，请检查后端服务', magicbar: '重置演示数据时遇到了问题' })
  } finally {
    resetting.value = false
  }
}

async function logout() {
  try {
    await http.post('/auth/logout')
  } catch {
    // Local logout still clears the visible user state.
  }
  delCookie('user_name')
  clearSessionUser()
  menuOpen.value = false
  router.replace('/login')
}

const { isDark, toggle: toggleTheme } = useTheme()

function forceCrash() {
  menuOpen.value = false
  throw new Error('🔧 手动触发的测试错误 —— 一切正常，这只是个演习！')
}

const navItems = [
  { label: '仪表盘', route: '/dashboard', icon: 'dashboard' },
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
      const active = route.path.startsWith(item.route)
      if (active) {
        return { top: `${top}px`, height: `${ITEM_H}px` }
      }
    }
    top += item.divider ? DIVIDER_H + DIVIDER_M + GAP : ITEM_H + GAP
  }
  return { top: '0px', height: '0px' }
})

const refreshTick = ref(0)
const dataVersion = ref(0)
const rightButtons = ref([])
provide(REFRESH_TICK_KEY, refreshTick)
provide(DATA_VERSION_KEY, dataVersion)
provide(RIGHT_BUTTONS_KEY, rightButtons)

async function doRefresh() {
  try {
    await http.get('/health')
    refreshTick.value++
    notify({ type: 'success', snackbar: '数据已刷新', snackbarDuration: 2000 })
  } catch {
    notify({ type: 'error', snackbar: '刷新失败，服务器无响应', magicbar: '刷新数据时遇到了问题', snackbarDuration: 3000 })
  }
}

function go(path) {
  const active = route.path.startsWith(path)
  if (!active) {
    router.push(path)
  }
}
</script>

<style lang="scss" scoped>
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
    background: rgb(var(--md-sys-color-primary) / 0.28);
    transform: scale(0);
    animation: magic-ripple 0.7s ease-out forwards;
  }
}

@keyframes magic-ripple {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(220);
    opacity: 0;
  }
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
  box-shadow: 0 4px 16px rgba(0 0 0 / 0.12);
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
    opacity: 0.7;
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
    transition: background 0.15s ease;

    svg {
      width: 18px;
      height: 18px;
      flex-shrink: 0;
    }

    span {
      @include font(14px, 20px, 500);
    }

    &:hover {
      background: rgb(var(--md-sys-color-on-surface) / 0.08);
    }

    &--debug {
      color: rgb(var(--md-sys-color-error));

      &:hover {
        background: rgb(var(--md-sys-color-error) / 0.08);
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
  transition:
    opacity 0.2s ease,
    transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.menu-leave-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}
.menu-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}
.menu-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
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
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
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
        width 0.25s cubic-bezier(0.4, 0, 0.2, 1),
        padding 0.25s cubic-bezier(0.4, 0, 0.2, 1),
        border-radius 0.25s cubic-bezier(0.4, 0, 0.2, 1);

      &__label {
        transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      }
    }

    .nav-item--active {
      background: rgb(var(--md-sys-color-on-surface) / 0.12);
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
        box-shadow: 0 1px 3px rgb(var(--md-sys-color-shadow) / 0.2);
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
    transition: background 0.15s ease;
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
  transition:
    top 0.35s cubic-bezier(0.4, 0, 0.2, 1),
    height 0.35s cubic-bezier(0.4, 0, 0.2, 1);
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
  transition: background 0.15s ease;
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
      filter: brightness(0.95);
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
