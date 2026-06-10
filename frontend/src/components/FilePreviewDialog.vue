<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="modelValue" class="preview-overlay" @click.self="close">
        <div class="preview-dialog">
          <div class="preview-dialog__header">
            <div class="preview-dialog__title-wrap">
              <svg class="preview-dialog__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <span class="preview-dialog__title">{{ fileName }}</span>
            </div>
            <button class="preview-dialog__close-btn" title="关闭" @click="close">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <div class="preview-dialog__body">
            <div v-if="loading" class="preview-dialog__state">
              <div class="preview-dialog__spinner" />
              <span>加载中...</span>
            </div>
            <div v-else-if="error" class="preview-dialog__state preview-dialog__state--error">
              <p>{{ error }}</p>
              <button class="preview-dialog__btn preview-dialog__btn--primary" @click="$emit('download')">下载文件</button>
            </div>
            <pre v-else class="preview-dialog__code"><code>{{ content }}</code></pre>
          </div>

          <div class="preview-dialog__footer">
            <button class="preview-dialog__btn" @click="close">关闭</button>
            <button class="preview-dialog__btn preview-dialog__btn--primary" @click="$emit('download')">下载</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: boolean
  fileName: string
  content: string
  loading: boolean
  error: string
}>()

const emit = defineEmits<{
  'update:modelValue': [v: boolean]
  closed: []
  download: []
}>()

function close() {
  emit('update:modelValue', false)
  emit('closed')
}
</script>

<style lang="scss" scoped>
.preview-overlay {
  position: fixed; inset: 0; z-index: 2100;
  background: rgba(0, 0, 0, .4);
  display: grid; place-items: center;
}
.preview-dialog {
  width: 720px; max-width: calc(100vw - 48px); max-height: calc(100vh - 80px);
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border: 1px solid rgb(var(--md-sys-color-outline-variant));
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, .14);
  display: flex; flex-direction: column;
  overflow: hidden;

  &__header {
    display: flex; align-items: center; justify-content: space-between;
    height: 48px; padding: 0 12px 0 20px;
    background: rgb(var(--md-sys-color-surface-container));
    border-bottom: 1px solid rgb(var(--md-sys-color-outline-variant));
    flex-shrink: 0;
  }
  &__title-wrap { display: flex; align-items: center; gap: 8px; min-width: 0; }
  &__icon { width: 18px; height: 18px; color: rgb(var(--md-sys-color-on-surface-variant)); flex-shrink: 0; }
  &__title {
    font: 500 15px/22px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
    color: rgb(var(--md-sys-color-on-surface));
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  }
  &__close-btn {
    width: 32px; height: 32px;
    border: none; border-radius: 8px;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    cursor: pointer;
    display: grid; place-items: center;
    svg { width: 16px; height: 16px; }
    &:hover { background: rgb(var(--md-sys-color-error-container)); color: rgb(var(--md-sys-color-on-error-container)); }
  }

  &__body {
    flex: 1; overflow: hidden; display: flex; flex-direction: column;
  }
  &__code {
    flex: 1; overflow: auto;
    margin: 0; padding: 20px;
    font: 13px/1.6 'Cascadia Code', 'Consolas', 'Monaco', monospace;
    color: rgb(var(--md-sys-color-on-surface));
    white-space: pre-wrap; word-break: break-all;
    user-select: text;
  }
  &__state {
    display: flex; align-items: center; gap: 12px; justify-content: center;
    padding: 48px 20px;
    color: rgb(var(--md-sys-color-on-surface-variant));
    font: 400 14px/20px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
    &--error { flex-direction: column; color: rgb(var(--md-sys-color-error)); }
  }

  &__footer {
    display: flex; justify-content: flex-end; gap: 8px;
    padding: 12px 20px;
    border-top: 1px solid rgb(var(--md-sys-color-outline-variant));
    flex-shrink: 0;
  }

  &__btn {
    height: 36px; padding: 0 20px;
    border: 1px solid rgb(var(--md-sys-color-outline));
    border-radius: 10px;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface));
    font: 500 14px/20px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
    cursor: pointer;
    transition: background .15s ease;
    &:hover { background: rgb(var(--md-sys-color-surface-container-highest)); }
    &--primary {
      border: none;
      background: rgb(var(--md-sys-color-primary));
      color: rgb(var(--md-sys-color-on-primary));
      &:hover { filter: brightness(0.9); }
    }
  }

  &__spinner {
    width: 20px; height: 20px;
    border: 2px solid rgb(var(--md-sys-color-outline-variant));
    border-top-color: rgb(var(--md-sys-color-primary));
    border-radius: 50%;
    animation: preview-spin .6s linear infinite;
  }
}
@keyframes preview-spin { to { transform: rotate(360deg); } }

.dialog-enter-active { transition: opacity .2s ease, transform .2s cubic-bezier(.4, 0, .2, 1); }
.dialog-leave-active { transition: opacity .15s ease, transform .15s cubic-bezier(.4, 0, .2, 1); }
.dialog-enter-from { opacity: 0; transform: scale(.95); }
.dialog-leave-to   { opacity: 0; transform: scale(.95); }
</style>
