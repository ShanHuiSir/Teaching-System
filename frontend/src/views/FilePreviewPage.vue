<template>
  <div class="preview-page">
    <!-- Top bar -->
    <header class="preview-bar">
      <div class="preview-bar__info">
        <svg class="preview-bar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
        <span class="preview-bar__name">{{ fileName }}</span>
      </div>
      <div class="fp-tooltip-wrap">
        <button class="preview-bar__btn" title="关闭" @click="closeTab">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
        <span class="fp-tooltip">关闭</span>
      </div>
    </header>

    <!-- Content -->
    <div class="preview-body">
      <div v-if="loading" class="preview-state">
        <div class="fp-spinner" />
        <span>加载中...</span>
      </div>
      <div v-else-if="error" class="preview-state preview-state--error">
        <p>{{ error }}</p>
      </div>
      <pre v-else class="preview-code"><code>{{ content }}</code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const content = ref('')
const fileName = ref('')
const loading = ref(true)
const error = ref('')

function closeTab() {
  window.close()
}

onMounted(async () => {
  const id = route.params.submissionId
  try {
    const res = await fetch(`/api/submissions/${id}/file`, { credentials: 'include' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const disposition = res.headers.get('content-disposition')
    if (disposition) {
      const match = disposition.match(/filename="?(.+?)"?$/i)
      if (match) fileName.value = match[1]
    }
    if (!fileName.value) fileName.value = `submission-${id}`
    content.value = await res.text()
  } catch (e: any) {
    error.value = e.message || '文件加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.preview-page {
  position: fixed; inset: 0;
  display: flex; flex-direction: column;
  background: rgb(var(--md-sys-color-surface-container-lowest));
}

// ── Top bar ──
.preview-bar {
  display: flex; align-items: center; justify-content: space-between;
  height: 48px; padding: 0 12px 0 20px;
  background: rgb(var(--md-sys-color-surface-container));
  border-bottom: 1px solid rgb(var(--md-sys-color-outline-variant));
  flex-shrink: 0;
  &__info { display: flex; align-items: center; gap: 8px; min-width: 0; }
  &__icon { width: 18px; height: 18px; color: rgb(var(--md-sys-color-on-surface-variant)); flex-shrink: 0; }
  &__name {
    font: 500 14px/20px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
    color: rgb(var(--md-sys-color-on-surface));
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  }
  &__btn {
    width: 32px; height: 32px;
    border: none; border-radius: 8px;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    cursor: pointer;
    display: grid; place-items: center;
    transition: background .15s ease;
    svg { width: 16px; height: 16px; }
    &:hover { background: rgb(var(--md-sys-color-error-container)); color: rgb(var(--md-sys-color-on-error-container)); }
  }
}

// ── Tooltip ──
.fp-tooltip-wrap {
  position: relative;
  display: inline-flex;
  &:hover .fp-tooltip {
    opacity: 1;
    visibility: visible;
    transition-delay: .6s;
  }
}
.fp-tooltip {
  position: absolute;
  bottom: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 10px;
  border-radius: 6px;
  background: rgb(var(--md-sys-color-inverse-surface));
  color: rgb(var(--md-sys-color-inverse-on-surface));
  font: 400 12px/18px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  visibility: hidden;
  transition: opacity .15s ease, visibility .15s ease;
  transition-delay: 0s;
  z-index: 10;
}

// ── Body ──
.preview-body {
  flex: 1; overflow: hidden; display: flex; flex-direction: column;
}
.preview-code {
  flex: 1; overflow: auto;
  margin: 0; padding: 20px;
  font: 13px/1.6 'Cascadia Code', 'Consolas', 'Monaco', monospace;
  color: rgb(var(--md-sys-color-on-surface));
  white-space: pre-wrap; word-break: break-all;
  user-select: text;
}
.preview-state {
  display: flex; align-items: center; gap: 12px; justify-content: center;
  padding: 48px 20px;
  color: rgb(var(--md-sys-color-on-surface-variant));
  font: 400 14px/20px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
  &--error { flex-direction: column; color: rgb(var(--md-sys-color-error)); }
}

// ── Spinner ──
.fp-spinner {
  width: 20px; height: 20px;
  border: 2px solid rgb(var(--md-sys-color-outline-variant));
  border-top-color: rgb(var(--md-sys-color-primary));
  border-radius: 50%;
  animation: fp-spin .6s linear infinite;
}
@keyframes fp-spin { to { transform: rotate(360deg); } }
</style>
