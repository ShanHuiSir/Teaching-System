<template>
  <div class="nf">
    <div class="nf__card">
      <div class="nf__code">403</div>
      <h1 class="nf__title">侯门一入深如海</h1>
      <h2 class="nf__subtitle">从此萧郎是路人</h2>
      <p v-if="hasToken" class="nf__hint">您似乎没有权限访问这里，需要换个身份吗？</p>
      <p v-else class="nf__hint">您似乎没有登录凭证，请尝试登录</p>
      <div class="nf__btns">
        <button v-if="hasToken" class="nf__btn" @click="goHome">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6" /></svg>
          <span>返回首页</span>
        </button>
        <button class="nf__btn" :class="{ 'nf__btn--outline': hasToken }" @click="goLogin">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" /><polyline points="10 17 15 12 10 7" /><line x1="15" y1="12" x2="3" y2="12" /></svg>
          <span>登录系统</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getCookie, delCookie } from '../utils/cookie'

const router = useRouter()
const hasToken = computed(() => !!getCookie('auth_token'))

function goHome() {
  router.replace('/')
}

function goLogin() {
  delCookie('auth_token')
  delCookie('user_name')
  router.replace('/login')
}
</script>

<style lang="scss" scoped>
$font-family: "PingFang SC", "Microsoft YaHei", -apple-system, sans-serif;

@mixin font($size, $height, $weight: 400) {
  font: $weight #{$size}/#{$height} $font-family;
}

.nf {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: rgb(var(--md-sys-color-background));

  &__card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    text-align: center;
    padding: 64px 48px;
  }

  &__code {
    @include font(96px, 1, 300);
    color: rgb(var(--md-sys-color-error) / .15);
    letter-spacing: .04em;
    margin-bottom: 16px;
    user-select: none;
  }

  &__title {
    @include font(28px, 40px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: .06em;
  }

  &__subtitle {
    @include font(28px, 40px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: .06em;
  }

  &__hint {
    @include font(15px, 24px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    margin-top: 16px;
  }

  &__btns {
    display: flex;
    gap: 12px;
    margin-top: 32px;
  }

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 44px;
    padding: 0 24px;
    border: none;
    border-radius: 22px;
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    cursor: pointer;
    @include font(15px, 24px, 500);
    transition: box-shadow .2s ease, background .2s ease;

    svg { width: 18px; height: 18px; }

    &:hover {
      box-shadow: 0 0 20px rgb(var(--md-sys-color-primary) / .35);
    }

    &--outline {
      background: transparent;
      color: rgb(var(--md-sys-color-primary));
      border: 1px solid rgb(var(--md-sys-color-outline));

      &:hover {
        background: rgb(var(--md-sys-color-primary) / .08);
        box-shadow: none;
      }
    }
  }
}
</style>
