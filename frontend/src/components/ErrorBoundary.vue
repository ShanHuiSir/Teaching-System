<template>
  <slot v-if="!error" />
  <div v-else class="eb">
    <div class="eb__card">
      <div class="eb__emblem">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" /></svg>
      </div>
      <h1 class="eb__title">山重水复疑无路</h1>
      <h2 class="eb__subtitle">柳暗花明又一村</h2>
      <p class="eb__hint">页面出了一点问题，试试重新加载？</p>
      <button class="eb__btn" @click="retry">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" /></svg>
        <span>重新加载</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { onErrorCaptured } from 'vue'

const error = ref(null)
const retryKey = ref(0)

onErrorCaptured((err) => {
  error.value = err
  return false // prevent propagation
})

function retry() {
  error.value = null
  retryKey.value++
}
</script>

<style lang="scss" scoped>
$font-family: "PingFang SC", "Microsoft YaHei", -apple-system, sans-serif;

@mixin font($size, $height, $weight: 400) {
  font: $weight #{$size}/#{$height} $font-family;
}

.eb {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: rgb(var(--md-sys-color-background));

  &__card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    text-align: center;
    padding: 48px 32px;
  }

  &__emblem {
    width: 56px;
    height: 56px;
    margin-bottom: 16px;
    color: rgb(var(--md-sys-color-error) / .4);
  }

  &__title {
    @include font(22px, 32px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: .05em;
  }

  &__subtitle {
    @include font(22px, 32px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: .05em;
  }

  &__hint {
    @include font(14px, 22px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    margin-top: 12px;
  }

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 40px;
    padding: 0 22px;
    margin-top: 24px;
    border: none;
    border-radius: 20px;
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    cursor: pointer;
    @include font(14px, 22px, 500);
    transition: box-shadow .2s ease;

    svg { width: 16px; height: 16px; }

    &:hover {
      box-shadow: 0 0 18px rgb(var(--md-sys-color-primary) / .35);
    }
  }
}
</style>
