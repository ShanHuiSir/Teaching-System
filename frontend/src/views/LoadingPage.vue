<template>
  <div class="ld">
    <div class="ld__pct">{{ pct }}%</div>

    <div class="ld__track">
      <div class="ld__bar" :style="barStyle" />
    </div>

    <p class="ld__label">{{ label }}</p>

    <div class="ld__debug">
      <button class="ld__debug-btn ld__debug-btn--play" @click="play">完整演示</button>
      <button v-for="p in debugSteps" :key="p" class="ld__debug-btn" @click="progress = p">{{ p }}%</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const progress = ref(0)
const debugSteps = [0, 25, 50, 75, 100]

function play() {
  progress.value = 0
  requestAnimationFrame(() => {
    progress.value = 100
  })
}

const pct = computed(() => Math.round(progress.value))

const label = computed(() => {
  const n = pct.value
  if (n < 30) return '加载资源中…'
  if (n < 60) return '权限检查中…'
  return '获取数据中…'
})

const barStyle = computed(() => {
  const p = Math.max(0, Math.min(100, progress.value)) / 100
  return { width: `calc(8px + (100% - 8px) * ${p})` }
})
</script>

<style lang="scss" scoped>
$font-family: "PingFang SC", "Microsoft YaHei", -apple-system, sans-serif;

@mixin font($size, $height, $weight: 400) {
  font: $weight #{$size}/#{$height} $font-family;
}

.ld {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32px;
  height: 100vh;
  background: rgb(var(--md-sys-color-background));

  &__pct {
    @include font(72px, 1, 300);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: .02em;
    font-variant-numeric: tabular-nums;
  }

  &__track {
    position: relative;
    width: 100%;
    height: 24px;
    padding: 0 24px;
  }

  &__bar {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    height: 8px;
    border-radius: 4px;
    background: rgb(var(--md-sys-color-primary));
    transition: width .5s cubic-bezier(.4, 0, .2, 1);
  }

  &__label {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    letter-spacing: .04em;
  }

  &__debug {
    display: flex;
    gap: 8px;
  }

  &__debug-btn {
    height: 32px;
    padding: 0 14px;
    border: 1px solid rgb(var(--md-sys-color-outline-variant));
    border-radius: 16px;
    background: rgb(var(--md-sys-color-surface-container));
    color: rgb(var(--md-sys-color-on-surface-variant));
    font-size: 13px;
    cursor: pointer;
    transition: background .15s;

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-high));
    }

    &--play {
      background: rgb(var(--md-sys-color-primary));
      color: rgb(var(--md-sys-color-on-primary));
      border-color: transparent;

      &:hover {
        background: rgb(var(--md-sys-color-primary) / .85);
      }

      &:disabled {
        opacity: .5;
        cursor: default;
      }
    }
  }
}

</style>
