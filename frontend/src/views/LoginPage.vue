<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-card__track" :class="{ 'is-cookie': activeLayer === 'cookie', 'is-key': activeLayer === 'key' }">
        <!-- Layer 1: Cookie管理 -->
        <div class="login-card__layer layer-cookie">
          <button class="layer-cookie__back" @click.stop="activeLayer = 'welcome'">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15 18l-6-6 6-6" />
            </svg>
          </button>
          <svg class="layer-cookie__icon" viewBox="0 0 24 24" fill="currentColor" stroke="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C12.7139 2 13.4187 2.07494 14.1059 2.22228C14.6865 2.34679 14.899 3.06471 14.4797 3.48521C14.0148 3.95137 13.75 4.57868 13.75 5.25C13.75 6.42043 14.5612 7.42718 15.6858 7.68625C16.0559 7.7715 16.3039 8.1199 16.2632 8.49747C16.2544 8.5787 16.25 8.66307 16.25 8.75C16.25 10.1307 17.3693 11.25 18.75 11.25C19.4766 11.25 20.1513 10.9393 20.6235 10.4053C21.0526 9.92011 21.8536 10.1704 21.9301 10.8137C21.9766 11.2048 22 11.6009 22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2ZM12 3.5C7.30558 3.5 3.5 7.30558 3.5 12C3.5 16.6944 7.30558 20.5 12 20.5C16.4367 20.5 20.0795 17.1008 20.4661 12.7646L20.485 12.5085L20.492 12.351L20.2985 12.4391C19.9679 12.5779 19.6173 12.6725 19.2549 12.7183L18.9811 12.7434L18.75 12.75C16.7439 12.75 15.0828 11.2732 14.7943 9.34752L14.7694 9.14675L14.755 8.96L14.6101 8.89964C13.3259 8.32272 12.4199 7.09599 12.2715 5.66565L12.2549 5.44962L12.25 5.25C12.25 4.80313 12.3238 4.36764 12.4636 3.95777L12.5553 3.71503L12.64 3.525L12.3637 3.50763L12 3.5ZM15 16C15.5523 16 16 16.4477 16 17C16 17.5523 15.5523 18 15 18C14.4477 18 14 17.5523 14 17C14 16.4477 14.4477 16 15 16ZM8 15C8.55228 15 9 15.4477 9 16C9 16.5523 8.55228 17 8 17C7.44772 17 7 16.5523 7 16C7 15.4477 7.44772 15 8 15ZM12 11C12.5523 11 13 11.4477 13 12C13 12.5523 12.5523 13 12 13C11.4477 13 11 12.5523 11 12C11 11.4477 11.4477 11 12 11ZM7 8C7.55228 8 8 8.44772 8 9C8 9.55228 7.55228 10 7 10C6.44772 10 6 9.55228 6 9C6 8.44772 6.44772 8 7 8Z" />
          </svg>
          <h2 class="layer-cookie__title">Cookie 政策管理</h2>
          <div class="layer-cookie__list">
            <div v-for="cat in categories" :key="cat.key" class="cookie-cat">
              <div class="cookie-cat__head">
                <div class="cookie-cat__info">
                  <span class="cookie-cat__label">{{ cat.label }}</span>
                  <span class="cookie-cat__desc">{{ cat.desc }}</span>
                </div>
                <label v-if="cat.required" class="toggle toggle--locked">
                  <input type="checkbox" checked disabled />
                  <span class="toggle__track" />
                </label>
                <label v-else class="toggle">
                  <input type="checkbox" :checked="prefs[cat.key] !== false" @change="onToggle(cat.key)" />
                  <span class="toggle__track" />
                </label>
              </div>
            </div>
          </div>
          <button class="layer-cookie__clear-btn" @click="onClearAll">清除所有Cookie</button>
        </div>

        <!-- Layer 2: Welcome -->
        <div class="login-card__layer layer-welcome">
          <div class="welcome">
            <h1 class="welcome__greeting">{{ greeting }}</h1>
            <p class="welcome__sub">也许我们该从登录开始</p>
          </div>

          <div class="actions">
            <button class="cookie-policy-btn" @click="onCookiePolicy">
              <svg class="cookie-policy-btn__icon" viewBox="0 0 24 24" fill="currentColor" stroke="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C12.7139 2 13.4187 2.07494 14.1059 2.22228C14.6865 2.34679 14.899 3.06471 14.4797 3.48521C14.0148 3.95137 13.75 4.57868 13.75 5.25C13.75 6.42043 14.5612 7.42718 15.6858 7.68625C16.0559 7.7715 16.3039 8.1199 16.2632 8.49747C16.2544 8.5787 16.25 8.66307 16.25 8.75C16.25 10.1307 17.3693 11.25 18.75 11.25C19.4766 11.25 20.1513 10.9393 20.6235 10.4053C21.0526 9.92011 21.8536 10.1704 21.9301 10.8137C21.9766 11.2048 22 11.6009 22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2ZM12 3.5C7.30558 3.5 3.5 7.30558 3.5 12C3.5 16.6944 7.30558 20.5 12 20.5C16.4367 20.5 20.0795 17.1008 20.4661 12.7646L20.485 12.5085L20.492 12.351L20.2985 12.4391C19.9679 12.5779 19.6173 12.6725 19.2549 12.7183L18.9811 12.7434L18.75 12.75C16.7439 12.75 15.0828 11.2732 14.7943 9.34752L14.7694 9.14675L14.755 8.96L14.6101 8.89964C13.3259 8.32272 12.4199 7.09599 12.2715 5.66565L12.2549 5.44962L12.25 5.25C12.25 4.80313 12.3238 4.36764 12.4636 3.95777L12.5553 3.71503L12.64 3.525L12.3637 3.50763L12 3.5ZM15 16C15.5523 16 16 16.4477 16 17C16 17.5523 15.5523 18 15 18C14.4477 18 14 17.5523 14 17C14 16.4477 14.4477 16 15 16ZM8 15C8.55228 15 9 15.4477 9 16C9 16.5523 8.55228 17 8 17C7.44772 17 7 16.5523 7 16C7 15.4477 7.44772 15 8 15ZM12 11C12.5523 11 13 11.4477 13 12C13 12.5523 12.5523 13 12 13C11.4477 13 11 12.5523 11 12C11 11.4477 11.4477 11 12 11ZM7 8C7.55228 8 8 8.44772 8 9C8 9.55228 7.55228 10 7 10C6.44772 10 6 9.55228 6 9C6 8.44772 6.44772 8 7 8Z" />
              </svg>
              <span>管理您的Cookie政策</span>
            </button>
            <button class="key-btn" @click="onKeyLogin">
              <svg class="key-btn__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.78 7.78 5.5 5.5 0 0 1 7.78-7.78zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4" />
              </svg>
              <span>通过密钥登录</span>
            </button>
          </div>
        </div>

        <!-- Layer 3: Key Login -->
        <div class="login-card__layer layer-key">
          <button class="layer-key__back" @click.stop="activeLayer = 'welcome'">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15 18l-6-6 6-6" />
            </svg>
          </button>

          <svg class="layer-key__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.78 7.78 5.5 5.5 0 0 1 7.78-7.78zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4" />
          </svg>
          <h2 class="layer-key__title">通过密钥登录</h2>

          <div class="layer-key__fields">
            <div class="input-group">
              <input v-model="account" class="input-group__field" type="text" placeholder="请输入账户名" autocomplete="username" />
            </div>
            <div class="input-group">
              <input v-model="secretKey" class="input-group__field" type="password" placeholder="请输入密钥" autocomplete="current-password" />
            </div>
          </div>

          <label class="remember-row">
            <input v-model="rememberMe" type="checkbox" class="remember-row__check" />
            <span>记住我</span>
          </label>

          <button class="layer-key__login-btn" :disabled="loading" @click="handleLogin">
            <span v-if="!loading">登 录</span>
            <span v-else class="spinner" />
          </button>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { setCookie, getCookie, delCookie } from '../utils/cookie.js'
import { CATEGORIES, loadPrefs, savePrefs, clearCategory } from '../utils/cookiePrefs.js'
import { useSnackbar } from '../composables/useSnackbar.js'

const router = useRouter()
const snackbar = useSnackbar()

const activeLayer = ref('welcome')
const account = ref('')
const secretKey = ref('')
const rememberMe = ref(true)
const loading = ref(false)

onMounted(() => {
  activeLayer.value = 'welcome'
})

const categories = Object.values(CATEGORIES)
const prefs = ref(loadPrefs())

function onToggle(catKey) {
  const enabled = prefs.value[catKey] !== false
  prefs.value[catKey] = !enabled
  savePrefs(prefs.value)
  if (!enabled) clearCategory(catKey)
}

function onClearAll() {
  document.cookie.split(';').forEach(c => {
    const name = c.trim().split('=')[0]
    if (name) delCookie(name)
  })
  snackbar.show('已清除全部Cookie', { variant: 'info' })
}

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

function onCookiePolicy() {
  activeLayer.value = 'cookie'
}

function onKeyLogin() {
  activeLayer.value = 'key'
}

async function handleLogin() {
  if (!account.value.trim() || !secretKey.value.trim()) {
    snackbar.show('请输入账户名和密钥', { variant: 'error' })
    return
  }
  if (account.value !== 'teacher' || secretKey.value !== '123456') {
    snackbar.show('账户名或密钥错误', { variant: 'error' })
    return
  }

  loading.value = true
  await new Promise(r => setTimeout(r, 800))

  if (rememberMe.value) {
    setCookie('auth_token', 'teacher-token', 7)
    setCookie('user_name', account.value, 7)
  } else {
    setCookie('auth_token', 'teacher-token', 0)
    setCookie('user_name', account.value, 0)
  }

  loading.value = false
  snackbar.show('登录成功', { variant: 'info' })
  router.replace('/')
}
</script>

<style lang="scss" scoped>
$font-family: "PingFang SC", "Microsoft YaHei", -apple-system, sans-serif;

@mixin font($size, $height, $weight: 400) {
  font: $weight #{$size}/#{$height} $font-family;
}

.login-page {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(var(--md-sys-color-background));
}

.login-card {
  position: relative;
  width: 360px;
  height: 480px;
  background: rgb(var(--md-sys-color-surface-container));
  border: 1px solid rgb(var(--md-sys-color-outline-variant));
  border-radius: 28px;
  overflow: hidden;
}

.login-card__track {
  display: flex;
  width: 300%;
  height: 100%;
  transform: translateX(-33.333%);
  transition: transform .35s cubic-bezier(.4, 0, .2, 1);

  &.is-cookie {
    transform: translateX(0);
  }

  &.is-key {
    transform: translateX(-66.667%);
  }
}

.login-card__layer {
  flex: 0 0 33.333%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 48px 32px 40px;
}

/* ── Layer 1: Welcome ── */
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 48px;
}

.welcome__greeting {
  @include font(28px, 36px, 500);
  color: rgb(var(--md-sys-color-on-surface));
  letter-spacing: .02em;
}

.welcome__sub {
  @include font(13px, 20px);
  color: rgb(var(--md-sys-color-on-surface-variant));
}

.actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.key-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 44px;
  padding: 0 24px;
  border: 1px solid rgb(var(--md-sys-color-outline));
  border-radius: 20px;
  background: rgb(var(--md-sys-color-surface-container-high));
  color: rgb(var(--md-sys-color-on-surface));
  cursor: pointer;
  transition: background .15s ease, border-color .15s ease;

  &:hover {
    background: rgb(var(--md-sys-color-surface-container-highest));
    border-color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &__icon {
    width: 20px;
    height: 20px;
    color: rgb(var(--md-sys-color-primary));
  }

  span {
    @include font(14px, 20px, 500);
    letter-spacing: .04em;
  }
}

/* ── Cookie Policy Button ── */
.cookie-policy-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 44px;
  padding: 0 24px;
  margin-bottom: 12px;
  border: 1px solid rgb(var(--md-sys-color-outline));
  border-radius: 20px;
  background: rgb(var(--md-sys-color-surface-container-high));
  color: rgb(var(--md-sys-color-on-surface));
  cursor: pointer;
  transition: background .15s ease, border-color .15s ease;

  &:hover {
    background: rgb(var(--md-sys-color-surface-container-highest));
    border-color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &__icon {
    width: 20px;
    height: 20px;
    color: rgb(var(--md-sys-color-tertiary));
  }

  span {
    @include font(14px, 20px, 500);
    letter-spacing: .04em;
  }
}

/* ── Layer: Cookie Management ── */
.layer-cookie {
  position: relative;
  justify-content: flex-start;
  padding-top: 48px;
  padding-bottom: 32px;
  overflow-y: auto;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }

  &__back {
    position: absolute;
    top: 20px;
    left: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    padding: 0;
    border: none;
    border-radius: 50%;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    cursor: pointer;
    transition: background .15s ease;
    z-index: 1;

    svg {
      width: 22px;
      height: 22px;
    }

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }
  }

  &__icon {
    width: 56px;
    height: 56px;
    flex-shrink: 0;
    color: rgb(var(--md-sys-color-tertiary));
    margin-top: 36px;
  }

  &__title {
    @include font(22px, 28px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    margin-top: 16px;
    letter-spacing: .02em;
  }

  &__list {
    width: 100%;
    margin-top: 24px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__clear-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 48px;
    flex-shrink: 0;
    margin-top: 28px;
    border: none;
    border-radius: 24px;
    background: rgb(var(--md-sys-color-error-container));
    color: rgb(var(--md-sys-color-on-error-container));
    @include font(16px, 22px, 500);
    letter-spacing: .05em;
    cursor: pointer;
    transition: box-shadow .15s ease;

    &:hover {
      box-shadow: 0 0 16px rgb(var(--md-sys-color-error-container) / .6);
    }
  }
}

/* ── Cookie Category Item ── */
.cookie-cat {
  background: rgb(var(--md-sys-color-surface-container-high));
  border-radius: 12px;
  padding: 14px 16px;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  &__info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__label {
    @include font(13px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface));
  }

  &__desc {
    @include font(12px, 16px);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }
}

/* ── Toggle Switch ── */
.toggle {
  position: relative;
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  cursor: pointer;

  input {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
  }

  &__track {
    width: 44px;
    height: 26px;
    border-radius: 13px;
    background: rgb(var(--md-sys-color-outline-variant));
    transition: background .2s ease;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      top: 3px;
      left: 3px;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #fff;
      box-shadow: 0 1px 3px rgb(var(--md-sys-color-shadow) / .2);
      transition: transform .2s cubic-bezier(.4, 0, .2, 1);
    }
  }

  input:checked + &__track {
    background: rgb(var(--md-sys-color-primary));

    &::after {
      transform: translateX(18px);
    }
  }

  &--locked {
    cursor: not-allowed;
    opacity: .6;

    .toggle__track {
      background: rgb(var(--md-sys-color-primary) / .5);
    }

    input:checked + .toggle__track {
      background: rgb(var(--md-sys-color-primary) / .5);
    }
  }
}

/* ── Layer 2: Key Login ── */
.layer-key {
  position: relative;

  &__back {
    position: absolute;
    top: 20px;
    left: 14px;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    padding: 0;
    border: none;
    border-radius: 50%;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    cursor: pointer;
    transition: background .15s ease;

    svg {
      width: 22px;
      height: 22px;
    }

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }
  }

  &__icon {
    width: 48px;
    height: 48px;
    color: rgb(var(--md-sys-color-primary));
    margin-top: 40px;
  }

  &__title {
    @include font(22px, 28px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    margin-top: 20px;
    letter-spacing: .02em;
  }

  &__fields {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  &__login-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 44px;
    margin-top: 28px;
    border: none;
    border-radius: 20px;
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    @include font(15px, 20px, 500);
    letter-spacing: .3em;
    cursor: pointer;
    transition: box-shadow .15s ease;

    &:hover:not(:disabled) {
      box-shadow: 0 0 16px rgb(var(--md-sys-color-primary) / .3);
    }

    &:disabled {
      opacity: .5;
      cursor: not-allowed;
    }
  }
}

/* ── Shared: input fields ── */
.input-group {
  width: 100%;

  &__field {
    width: 100%;
    height: 44px;
    padding: 0 16px;
    border: 1px solid rgb(var(--md-sys-color-outline-variant));
    border-radius: 8px;
    background: rgb(var(--md-sys-color-surface-container-highest));
    color: rgb(var(--md-sys-color-on-surface));
    @include font(14px, 20px);
    outline: none;
    transition: border-color .15s ease;

    &::placeholder {
      color: rgb(var(--md-sys-color-on-surface-variant) / .6);
    }

    &:focus {
      border-color: rgb(var(--md-sys-color-primary));
    }
  }
}

/* ── Remember me ── */
.remember-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 12px;
  cursor: pointer;
  @include font(13px, 20px);
  color: rgb(var(--md-sys-color-on-surface-variant));

  &__check {
    accent-color: rgb(var(--md-sys-color-primary));
    width: 15px;
    height: 15px;
    cursor: pointer;
  }
}

/* ── Spinner ── */
.spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255 255 255 / .3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
