<template>
  <div class="nf">
    <div class="nf__card">
      <div class="nf__code">404</div>
      <h1 class="nf__title">欲投人处宿</h1>
      <h2 class="nf__subtitle">隔水问樵夫</h2>
      <p class="nf__hint">或许我们可以带你回家？</p>
      <button class="nf__btn" @click="goHome">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" /><polyline points="9 22 9 12 15 12 15 22" /></svg>
        <span>返回仪表盘</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.replace('/')
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
    color: rgb(var(--md-sys-color-primary) / .18);
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

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 44px;
    padding: 0 24px;
    margin-top: 32px;
    border: none;
    border-radius: 22px;
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    cursor: pointer;
    @include font(15px, 24px, 500);
    transition: box-shadow .2s ease;

    svg { width: 18px; height: 18px; }

    &:hover {
      box-shadow: 0 0 20px rgb(var(--md-sys-color-primary) / .35);
    }
  }
}
</style>
