<template>
  <div
    v-if="visible"
    class="snackbar"
    role="alert"
    @mouseenter="pauseTimer"
    @mouseleave="resumeTimer"
  >
    <!-- Segment 1: icon — fixed left, matches sidebar pending color -->
    <svg class="snackbar__icon" viewBox="0 0 24 24" fill="currentColor">
      <path d="M17 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm1 3.8h-1.2v-2.5h-1.2v3.7h2.4v-1.2zM14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h4v-2H6V4h7v5h5v3h2V8l-6-6z"/>
    </svg>

    <!-- Segment 2: text — centered within its own wrapper -->
    <div class="snackbar__text-wrapper">
      <span class="snackbar__text">{{ message }}</span>
    </div>

    <!-- Segment 3: action button — fixed right -->
    <button
      v-if="actionText"
      class="snackbar__action-btn"
      @click="onAction"
    >{{ actionText }}</button>

    <button class="snackbar__close" @click="dismiss" aria-label="关闭">&times;</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';

const props = withDefaults(defineProps<{
  message: string;
  actionText?: string;
  duration?: number;
}>(), {
  actionText: '',
  duration: 0,
});

const emit = defineEmits<{ close: []; action: [] }>();

const visible = ref(true);
let timer: ReturnType<typeof setTimeout> | null = null;

function dismiss() { visible.value = false; clearTimer(); emit('close'); }
function onAction() { visible.value = false; clearTimer(); emit('action'); }
function clearTimer() { if (timer) { clearTimeout(timer); timer = null; } }
function pauseTimer() { if (timer) { clearTimeout(timer); timer = null; } }
function resumeTimer() { if (!timer && props.duration > 0) timer = setTimeout(dismiss, props.duration); }

onMounted(() => { if (props.duration > 0) timer = setTimeout(dismiss, props.duration); });
onBeforeUnmount(() => clearTimer());
</script>

<style lang="scss" scoped>
// ── Tokens ──
$banner-bg:         #ffffff;
$banner-border:     #f0f0f0;
$banner-text:       #222222;
$pending-red:       #f5222d;
$btn-bg:            #fafafa;
$btn-border:        #d9d9d9;
$btn-hover-shadow:  rgba(241, 112, 112, .3);

.snackbar {
  position: absolute;
  top: 56px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;

  width: calc(100% * 2 / 3);
  max-width: 90%;
  padding: 12px 16px;
  border-radius: 6px;

  background: $banner-bg;
  border: 1px solid $banner-border;
  box-shadow: 0 4px 12px rgba(0, 0, 0, .06);

  // Three-segment layout: icon | text | button ×
  display: flex;
  align-items: center;
  gap: 8px;

  // ── Segment 1: icon — fixed left, pending red ──
  &__icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    color: $pending-red;
  }

  // ── Segment 2: text wrapper — fills remaining space, centers text inside ──
  &__text-wrapper {
    flex: 1;
    min-width: 0;
    display: flex;
    justify-content: center;
    overflow: hidden;
  }

  &__text {
    font-weight: 600;
    font-size: 14px;
    line-height: 22px;
    color: $banner-text;
    font-family: $font-family;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  // ── Segment 3: action button — fixed right, elevated feel ──
  &__action-btn {
    flex-shrink: 0;
    padding: 4px 14px;
    border: 1px solid $btn-border;
    border-radius: 4px;
    background: $btn-bg;
    color: $banner-text;
    font-weight: 400;
    font-size: 13px;
    line-height: 20px;
    font-family: $font-family;
    cursor: pointer;
    transition:
      border-color .2s ease,
      background .2s ease,
      color .2s ease,
      transform .2s ease,
      box-shadow .2s ease;

    // Hover: red glow + subtle lift
    &:hover {
      border-color: $pending-red;
      background: #fff1f0;
      color: $pending-red;
      transform: translateY(-1px);
      box-shadow: 0 2px 6px rgba(245, 34, 45, .15);
    }

    // Active: press down
    &:active {
      transform: translateY(0);
      opacity: .85;
    }

    // Focus: red ring
    &:focus-visible {
      outline: none;
      box-shadow: 0 0 0 3px $btn-hover-shadow;
    }
  }

  // ── Close button ──
  &__close {
    flex-shrink: 0;
    width: 22px; height: 22px;
    border: none; border-radius: 4px;
    background: transparent;
    color: #999999;
    cursor: pointer;
    font-size: 16px;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background .15s, color .15s;

    &:hover { background: #f5f5f5; color: $banner-text; }
  }
}
</style>
