<template>
  <transition name="notice-fade">
    <div v-if="visible" class="system-notice" role="alert">
      <span class="system-notice__icon">{{ iconMap[type] }}</span>
      <span class="system-notice__message">{{ message }}</span>
      <button
        v-if="actionText"
        class="system-notice__action"
        @click="$emit('action')"
      >{{ actionText }}</button>
      <button
        class="system-notice__close"
        @click="$emit('close')"
        aria-label="关闭"
      >&times;</button>
    </div>
  </transition>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  message: string;
  type?: 'info' | 'success' | 'warning' | 'error' | 'pending' | 'ai' | 'done';
  actionText?: string;
  visible?: boolean;
}>(), {
  type: 'info',
  actionText: '',
  visible: true,
});

defineEmits<{ close: []; action: [] }>();

const iconMap: Record<string, string> = {
  info: 'ℹ️', success: '✅', warning: '⚠️', error: '❌',
  pending: '📋', ai: '🤖', done: '✅',
};
</script>

<style lang="scss" scoped>
// ── Design tokens (synced with system variables.scss) ──
$notice-bg:       #ffffff;
$notice-border:   #f0f0f0;
$notice-text:     #222222;
$notice-action:   #1890ff;
$notice-close:    #999999;
$notice-shadow:   0 2px 8px rgba(0, 0, 0, .06);
$notice-radius:   8px;

.system-notice {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
  max-width: 90%;
  padding: 12px 16px;
  border-radius: $notice-radius;
  background: $notice-bg;
  border: 1px solid $notice-border;
  box-shadow: $notice-shadow;

  &__icon {
    flex-shrink: 0;
    font-size: 16px;
    line-height: 1;
  }

  &__message {
    flex: 1;
    font-weight: 600;
    font-size: 14px;
    line-height: 22px;
    color: $notice-text;
    font-family: $font-family;
    white-space: pre-line;
    word-break: break-word;
  }

  // Action link — text-only, no background, underline on hover
  &__action {
    flex-shrink: 0;
    padding: 0;
    border: none;
    border-radius: 0;
    background: none;
    color: $notice-action;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    font-family: $font-family;
    cursor: pointer;
    text-decoration: none;
    transition: text-decoration .15s;

    &:hover {
      text-decoration: underline;
    }
  }

  &__close {
    flex-shrink: 0;
    width: 22px;
    height: 22px;
    border: none;
    border-radius: 4px;
    background: transparent;
    color: $notice-close;
    cursor: pointer;
    font-size: 16px;
    line-height: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background .15s, color .15s;

    &:hover {
      background: #f5f5f5;
      color: $notice-text;
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
