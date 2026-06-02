<template>
  <div
    v-if="visible"
    class="snackbar"
    role="alert"
    @mouseenter="pauseTimer"
    @mouseleave="resumeTimer"
  >
    <!-- Segment 1: icon — fixed left, matches active sidebar item -->
    <svg class="snackbar__icon" viewBox="0 0 24 24" fill="currentColor" :style="{ color: statusIcon.color }" v-html="statusIcon.path" />

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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';

const props = withDefaults(defineProps<{
  message: string;
  actionText?: string;
  duration?: number;
  status?: 'pending' | 'ai' | 'done' | 'default';
}>(), {
  actionText: '',
  duration: 0,
  status: 'default',
});

// ── Status-aware icon: mirrors the active sidebar nav item ──
const STATUS_CONFIG: Record<string, { path: string; color: string }> = {
  pending: {
    path: '<path d="M17 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm1 3.8h-1.2v-2.5h-1.2v3.7h2.4v-1.2zM14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h4v-2H6V4h7v5h5v3h2V8l-6-6z"/>',
    color: '#f5222d',
  },
  ai: {
    path: '<path d="M9 11.75c-.69 0-1.25.56-1.25 1.25s.56 1.25 1.25 1.25 1.25-.56 1.25-1.25-.56-1.25-1.25-1.25zm6 0c-.69 0-1.25.56-1.25 1.25s.56 1.25 1.25 1.25 1.25-.56 1.25-1.25-.56-1.25-1.25-1.25zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-12.5c-2.67 0-4.85 2.03-4.85 4.5h1.8c0-1.66 1.34-3 3.05-3 1.66 0 3.05 1.32 3.05 3h1.8c0-2.47-2.18-4.5-4.85-4.5z"/>',
    color: '#1890ff',
  },
  done: {
    path: '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>',
    color: '#52c41a',
  },
  default: {
    path: '<circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>',
    color: '#595959',
  },
};

const statusIcon = computed(() => STATUS_CONFIG[props.status] || STATUS_CONFIG.default);

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

  // ── Segment 1: icon — fixed left, color driven by :style ──
  &__icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
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
