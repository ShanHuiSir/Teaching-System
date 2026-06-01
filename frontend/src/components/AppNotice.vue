<template>
  <transition name="notice-fade">
    <div v-if="visible" class="notice-card" role="alert">
      <!-- Left: message content -->
      <div class="notice-card__body">
        <p class="notice-card__text">{{ message }}</p>
      </div>

      <!-- Right: type badge + close -->
      <div class="notice-card__right">
        <div class="notice-card__badge" :class="'notice-card__badge--' + type">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" v-html="badgeIcon" />
        </div>
        <button class="notice-card__close" @click="$emit('close')" aria-label="关闭">&times;</button>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  message: string;
  type?: 'pending' | 'ai' | 'done' | 'info' | 'success' | 'warning' | 'error';
  visible?: boolean;
}>(), {
  type: 'info',
  visible: true,
});

defineEmits<{ close: [] }>();

// SVG paths matching sidebar icons
const iconPaths: Record<string, string> = {
  pending: '<path d="M17 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm1 3.8h-1.2v-2.5h-1.2v3.7h2.4v-1.2zM14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h4v-2H6V4h7v5h5v3h2V8l-6-6z"/>',
  ai:      '<path d="M9 11.75c-.69 0-1.25.56-1.25 1.25s.56 1.25 1.25 1.25 1.25-.56 1.25-1.25-.56-1.25-1.25-1.25zm6 0c-.69 0-1.25.56-1.25 1.25s.56 1.25 1.25 1.25 1.25-.56 1.25-1.25-.56-1.25-1.25-1.25zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-12.5c-2.67 0-4.85 2.03-4.85 4.5h1.8c0-1.66 1.34-3 3.05-3 1.66 0 3.05 1.32 3.05 3h1.8c0-2.47-2.18-4.5-4.85-4.5z"/>',
  done:    '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>',
  info:    '<circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>',
  success: '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>',
  warning: '<path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>',
  error:   '<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>',
};

const badgeIcon = computed(() => iconPaths[props.type] || iconPaths.info);
</script>

<style lang="scss" scoped>
.notice-card {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 500;
  display: flex;
  align-items: center;
  gap: 16px;
  width: fit-content;
  max-width: 90%;
  padding: 14px 20px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, .1);

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__text {
    margin: 0;
    font-weight: 600;
    font-size: 14px;
    line-height: 22px;
    color: #333333;
    font-family: "PingFang SC", "Microsoft YaHei", -apple-system, sans-serif;
    white-space: pre-line;
    word-break: break-word;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
  }

  // Type badge — 36×36 rounded square, solid color, white icon
  &__badge {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: #ffffff;

    &--pending { background: #ff4d4f; }
    &--error   { background: #ff4d4f; }
    &--ai      { background: #1A56DB; }
    &--info    { background: #1A56DB; }
    &--done    { background: #16A34A; }
    &--success { background: #16A34A; }
    &--warning { background: #F59E0B; }
  }

  &__close {
    flex-shrink: 0;
    width: 24px; height: 24px;
    border: none;
    border-radius: 8px;
    background: transparent;
    color: #999;
    cursor: pointer;
    font-size: 16px;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background .15s, color .15s;
    align-self: flex-start;

    &:hover {
      background: #f5f5f5;
      color: #333;
    }
  }
}

// Animation
.notice-fade-enter-active { animation: noticeIn .3s cubic-bezier(.2, 0, 0, 1); }
.notice-fade-leave-active { animation: noticeOut .2s cubic-bezier(.4, 0, 1, 1); }

@keyframes noticeIn {
  from { opacity: 0; transform: translateX(-50%) translateY(-12px); }
  to   { opacity: 1; transform: translateX(-50%) translateY(0); }
}
@keyframes noticeOut {
  from { opacity: 1; transform: translateX(-50%) translateY(0); }
  to   { opacity: 0; transform: translateX(-50%) translateY(-8px); }
}
</style>
