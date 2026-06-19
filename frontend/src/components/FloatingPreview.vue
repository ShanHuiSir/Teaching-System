<template>
  <Teleport to="body">
    <Transition name="float">
      <div v-if="modelValue" class="fp-overlay" @mousedown.self="closePreview(false)">
        <div
          class="fp-window"
          :style="windowStyle"
          @mousedown="onFocus"
        >
          <!-- Title bar (drag handle) -->
          <div class="fp-bar" @mousedown="onDragStart">
            <div class="fp-bar__info">
              <svg class="fp-bar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <span class="fp-bar__name">{{ fileName }}</span>
            </div>
            <div class="fp-bar__actions">
              <div class="fp-tooltip-wrap">
                <button class="fp-bar__btn" @click.stop="openInNewTab">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                    <polyline points="15 3 21 3 21 9" />
                    <line x1="10" y1="14" x2="21" y2="3" />
                  </svg>
                </button>
                <span class="fp-tooltip">在新标签页中打开</span>
              </div>
              <div class="fp-tooltip-wrap">
                <button class="fp-bar__btn fp-bar__btn--close" @click.stop="closePreview(true)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
                <span class="fp-tooltip">关闭</span>
              </div>
            </div>
          </div>

          <!-- Content -->
          <div class="fp-body">
            <div v-if="loading" class="fp-loading">
              <div class="fp-spinner" />
              <span>加载中...</span>
            </div>
            <div v-else-if="error" class="fp-error">
              <p>{{ error }}</p>
            </div>
            <div v-else class="fp-scroll">
              <pre class="fp-code"><code>{{ content }}</code></pre>
            </div>
          </div>

          <!-- Resize handles -->
          <div class="fp-r fp-r--n" @mousedown.stop="onResizeStart($event, 'n')" />
          <div class="fp-r fp-r--e" @mousedown.stop="onResizeStart($event, 'e')" />
          <div class="fp-r fp-r--s" @mousedown.stop="onResizeStart($event, 's')" />
          <div class="fp-r fp-r--w" @mousedown.stop="onResizeStart($event, 'w')" />
          <div class="fp-r fp-r--ne" @mousedown.stop="onResizeStart($event, 'ne')" />
          <div class="fp-r fp-r--se" @mousedown.stop="onResizeStart($event, 'se')" />
          <div class="fp-r fp-r--sw" @mousedown.stop="onResizeStart($event, 'sw')" />
          <div class="fp-r fp-r--nw" @mousedown.stop="onResizeStart($event, 'nw')" />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount } from 'vue'

const emit = defineEmits<{
  'update:modelValue': [v: boolean]
  closed: []
}>()

const props = defineProps<{
  modelValue: boolean
  fileName: string
  content: string
  loading: boolean
  error: string
  submissionId?: number
}>()

const x = ref(80)
const y = ref(60)
const w = ref(720)
const h = ref(500)
const zIndex = ref(2000)
let globalZ = 2000

const MIN_W = 360
const MIN_H = 240

const windowStyle = computed(() => ({
  left: `${x.value}px`, top: `${y.value}px`, width: `${w.value}px`, height: `${h.value}px`, zIndex: zIndex.value,
}))

function onFocus() {
  zIndex.value = ++globalZ
}

function openInNewTab() {
  if (props.submissionId != null) {
    window.open(`/preview/${props.submissionId}`, '_blank')
  }
}

function closePreview(emitClosed: boolean) {
  emit('update:modelValue', false)
  if (emitClosed) emit('closed')
}

function clamp(v: number, lo: number, hi: number) {
  return Math.max(lo, Math.min(hi, v))
}

// ── Drag ──
let dragging = false
let dragSX = 0
let dragSY = 0
let origX = 0
let origY = 0

function onDragStart(e: MouseEvent) {
  dragging = true
  dragSX = e.screenX
  dragSY = e.screenY
  origX = x.value
  origY = y.value
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', onDragEnd)
}

function onDrag(e: MouseEvent) {
  if (!dragging) return
  x.value = origX + (e.screenX - dragSX)
  y.value = clamp(origY + (e.screenY - dragSY), -8, window.innerHeight - 40)
}

function onDragEnd() {
  dragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', onDragEnd)
}

// ── Resize ──
let resizing = false
let rDir = ''
let rSX = 0
let rSY = 0
let rW = 0
let rH = 0
let rX = 0
let rY = 0

function onResizeStart(e: MouseEvent, dir: string) {
  resizing = true
  rDir = dir
  rSX = e.screenX
  rSY = e.screenY
  rW = w.value
  rH = h.value
  rX = x.value
  rY = y.value
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', onResizeEnd)
}

function onResize(e: MouseEvent) {
  if (!resizing) return
  const dx = e.screenX - rSX
  const dy = e.screenY - rSY
  if (rDir.includes('e')) w.value = Math.max(MIN_W, rW + dx)
  if (rDir.includes('w')) { w.value = Math.max(MIN_W, rW - dx); x.value = rX + dx }
  if (rDir.includes('s')) h.value = Math.max(MIN_H, rH + dy)
  if (rDir.includes('n')) { h.value = Math.max(MIN_H, rH - dy); y.value = rY + dy }
}

function onResizeEnd() {
  resizing = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', onResizeEnd)
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', onResizeEnd)
})
</script>

<style lang="scss" scoped>
.fp-overlay {
  position: fixed; inset: 0; z-index: 1999;
  background: transparent;
}
.fp-window {
  position: fixed;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border: 1px solid rgb(var(--md-sys-color-outline-variant));
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, .14);
  display: flex; flex-direction: column;
  overflow: hidden;
  pointer-events: auto;
  user-select: none;
}

// ── Title bar ──
.fp-bar {
  display: flex; align-items: center; justify-content: space-between;
  height: 44px; padding: 0 12px 0 16px;
  background: rgb(var(--md-sys-color-surface-container));
  border-bottom: 1px solid rgb(var(--md-sys-color-outline-variant));
  cursor: move;
  flex-shrink: 0;
  &__info { display: flex; align-items: center; gap: 8px; min-width: 0; }
  &__icon { width: 18px; height: 18px; color: rgb(var(--md-sys-color-on-surface-variant)); flex-shrink: 0; }
  &__name {
    font: 500 14px/20px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
    color: rgb(var(--md-sys-color-on-surface));
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  }
  &__actions { display: flex; gap: 4px; flex-shrink: 0; }
  &__btn {
    width: 32px; height: 32px;
    border: none; border-radius: 8px;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    cursor: pointer;
    display: grid; place-items: center;
    transition: background .15s ease;
    svg { width: 16px; height: 16px; }
    &:hover { background: rgb(var(--md-sys-color-surface-container-highest)); }
    &--close:hover { background: rgb(var(--md-sys-color-error-container)); color: rgb(var(--md-sys-color-on-error-container)); }
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
.fp-body {
  flex: 1; overflow: hidden; display: flex; flex-direction: column;
  background: rgb(var(--md-sys-color-surface-container-lowest));
}
.fp-scroll {
  flex: 1; overflow: auto;
}
.fp-code {
  margin: 0; padding: 20px;
  font: 13px/1.6 'Cascadia Code', 'Consolas', 'Monaco', monospace;
  color: rgb(var(--md-sys-color-on-surface));
  white-space: pre-wrap; word-break: break-all;
  user-select: text;
}
.fp-loading {
  display: flex; align-items: center; gap: 12px;
  padding: 40px; justify-content: center;
  color: rgb(var(--md-sys-color-on-surface-variant));
  font: 400 14px/20px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
}
.fp-error {
  padding: 40px; text-align: center;
  color: rgb(var(--md-sys-color-error));
  font: 400 14px/20px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
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

// ── Resize handles ──
.fp-r {
  position: absolute;
  &--n  { top: 0;    left: 8px; right: 8px;  height: 4px; cursor: n-resize; }
  &--e  { top: 8px;  right: 0;   bottom: 8px; width: 4px;  cursor: e-resize; }
  &--s  { bottom: 0; left: 8px;  right: 8px;  height: 4px; cursor: s-resize; }
  &--w  { top: 8px;  left: 0;    bottom: 8px; width: 4px;  cursor: w-resize; }
  &--ne { top: 0;    right: 0;   width: 12px; height: 12px; cursor: ne-resize; }
  &--se { bottom: 0; right: 0;   width: 12px; height: 12px; cursor: se-resize; }
  &--sw { bottom: 0; left: 0;    width: 12px; height: 12px; cursor: sw-resize; }
  &--nw { top: 0;    left: 0;    width: 12px; height: 12px; cursor: nw-resize; }
}

// ── Transition ──
.float-enter-active { transition: opacity .2s ease, transform .2s cubic-bezier(.4, 0, .2, 1); }
.float-leave-active { transition: opacity .15s ease, transform .15s cubic-bezier(.4, 0, .2, 1); }
.float-enter-from { opacity: 0; transform: scale(.95); }
.float-leave-to   { opacity: 0; transform: scale(.95); }
</style>
