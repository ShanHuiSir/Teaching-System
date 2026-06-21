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

      <!-- Zoom controls (image mode only) -->
      <div v-if="previewMode === 'image'" class="preview-bar__zoom">
        <div class="fp-tooltip-wrap">
          <button
            class="preview-bar__btn"
            :class="{ 'preview-bar__btn--disabled': zoom <= MIN_ZOOM }"
            :disabled="zoom <= MIN_ZOOM"
            aria-label="缩小"
            @click="zoomOut"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
              <line x1="8" y1="11" x2="14" y2="11" />
            </svg>
          </button>
          <span class="fp-tooltip">缩小</span>
        </div>
        <button class="preview-bar__zoom-pct" @click="resetZoom">{{ zoomPct }}</button>
        <div class="fp-tooltip-wrap">
          <button
            class="preview-bar__btn"
            :class="{ 'preview-bar__btn--disabled': zoom >= MAX_ZOOM }"
            :disabled="zoom >= MAX_ZOOM"
            aria-label="放大"
            @click="zoomIn"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
              <line x1="11" y1="8" x2="11" y2="14" />
              <line x1="8" y1="11" x2="14" y2="11" />
            </svg>
          </button>
          <span class="fp-tooltip">放大</span>
        </div>
        <div class="fp-tooltip-wrap" :class="{ 'preview-bar__zoom-reset--hidden': zoom === 1 }">
          <button class="preview-bar__btn" aria-label="复位" @click="resetZoom">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="1 4 1 10 7 10" />
              <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
            </svg>
          </button>
          <span class="fp-tooltip">复位</span>
        </div>
      </div>

      <div class="fp-tooltip-wrap">
        <button class="preview-bar__btn" aria-label="关闭" @click="closeTab">
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
      <div
        v-else-if="previewMode === 'image'"
        ref="viewportRef"
        class="preview-media"
        :class="{ 'preview-media--pannable': zoom > 1, 'preview-media--panning': isPanning }"
        @wheel.prevent="onImageWheel"
        @mousedown="onImagePanStart"
        @dblclick="onImageDblClick"
      >
        <img
          ref="imgRef"
          :src="previewUrl(submissionId)"
          :alt="fileName"
          :style="imageStyle"
          draggable="false"
          @load="clampPan"
        />
      </div>
      <div v-else-if="previewMode === 'video'" class="preview-media">
        <video :src="previewUrl(submissionId)" controls playsinline>
          您的浏览器不支持视频播放。
        </video>
      </div>
      <div v-else-if="previewMode === 'unsupported'" class="preview-state">
        <svg class="preview-state__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
        <p>此文件类型不支持在线预览</p>
        <button class="preview-download-btn" @click="triggerDownload">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="7 10 12 15 17 10" />
            <line x1="12" y1="15" x2="12" y2="3" />
          </svg>
          <span>下载文件</span>
        </button>
      </div>
      <pre v-else-if="previewMode === 'text'" class="preview-code"><code>{{ content }}</code></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { detectFileType } from '../utils/fileIcons'

const route = useRoute()
const router = useRouter()
const content = ref('')
const fileName = ref('')
const loading = ref(true)
const error = ref('')
const previewMode = ref<'text' | 'image' | 'video' | 'unsupported'>('unsupported')
const downloadUrl = ref('')
const submissionId = computed(() => route.params.submissionId as string)
const fileId = computed(() => route.query.fileId as string | undefined)

function previewUrl(id: string): string {
  const base = `/api/submissions/${id}/preview`
  return fileId.value ? `${base}?fileId=${fileId.value}` : base
}

function fileUrl(id: string): string {
  const base = `/api/submissions/${id}/file`
  return fileId.value ? `${base}?fileId=${fileId.value}` : base
}

const IMAGE_EXTS = new Set(['png', 'jpg', 'jpeg', 'webp', 'gif', 'bmp'])
const VIDEO_EXTS = new Set(['mp4', 'webm', 'mov', 'avi'])

// ── Image zoom/pan state ──
const ZOOM_STEP = 0.25
const MIN_ZOOM = 1
const MAX_ZOOM = 5
const zoom = ref(1)
const panX = ref(0)
const panY = ref(0)
const isPanning = ref(false)
const imgRef = ref<HTMLImageElement | null>(null)
const viewportRef = ref<HTMLElement | null>(null)

const zoomPct = computed(() => Math.round(zoom.value * 100) + '%')

const imageStyle = computed(() => ({
  transform: `translate(${panX.value}px, ${panY.value}px) scale(${zoom.value})`,
}))

function isTextType(contentType: string): boolean {
  return /^text\//.test(contentType) ||
    /\bapplication\/(json|xml|javascript|ld\+json|x-httpd-php|x-sh|x-perl|x-python|x-yaml|x-www-form-urlencoded)\b/.test(contentType)
}

function fileExt(fileName: string): string {
  return (fileName || '').split('.').pop()?.toLowerCase() || ''
}

function triggerDownload() {
  const url = downloadUrl.value || fileUrl(submissionId.value)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName.value
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

function closeTab() {
  if (window.history.length > 1) {
    router.back()
  } else {
    window.close()
  }
}

function clamp(v: number, lo: number, hi: number) {
  return Math.max(lo, Math.min(hi, v))
}

// ── Image zoom/pan ──
function zoomIn() {
  zoom.value = Math.min(MAX_ZOOM, zoom.value + ZOOM_STEP)
}

function zoomOut() {
  zoom.value = Math.max(MIN_ZOOM, zoom.value - ZOOM_STEP)
}

function resetZoom() {
  zoom.value = 1
  panX.value = 0
  panY.value = 0
}

function clampPan() {
  const vp = viewportRef.value
  const img = imgRef.value
  if (!vp || !img) return
  const vpW = vp.clientWidth
  const vpH = vp.clientHeight
  const fitW = img.offsetWidth
  const fitH = img.offsetHeight
  const displayW = fitW * zoom.value
  const displayH = fitH * zoom.value
  const marginX = vpW * 0.15
  const marginY = vpH * 0.15
  const maxX = Math.max(0, (displayW - vpW) / 2 + marginX)
  const maxY = Math.max(0, (displayH - vpH) / 2 + marginY)
  panX.value = clamp(panX.value, -maxX, maxX)
  panY.value = clamp(panY.value, -maxY, maxY)
}

function onImageWheel(e: WheelEvent) {
  const delta = e.deltaY > 0 ? -ZOOM_STEP : ZOOM_STEP
  const newZoom = clamp(zoom.value + delta, MIN_ZOOM, MAX_ZOOM)
  if (newZoom === zoom.value) return

  const vp = e.currentTarget as HTMLElement
  const rect = vp.getBoundingClientRect()
  const cx = e.clientX - rect.left - rect.width / 2
  const cy = e.clientY - rect.top - rect.height / 2

  // Keep the point under cursor stationary
  const ix = (cx - panX.value) / zoom.value
  const iy = (cy - panY.value) / zoom.value
  panX.value = cx - ix * newZoom
  panY.value = cy - iy * newZoom
  zoom.value = newZoom
  clampPan()
}

function onImagePanStart(e: MouseEvent) {
  if (zoom.value <= 1) return
  const startX = e.clientX
  const startY = e.clientY
  const startPanX = panX.value
  const startPanY = panY.value
  let started = false

  function onMove(ev: MouseEvent) {
    const dx = ev.clientX - startX
    const dy = ev.clientY - startY
    if (!started && Math.abs(dx) < 3 && Math.abs(dy) < 3) return
    started = true
    isPanning.value = true
    panX.value = startPanX + dx
    panY.value = startPanY + dy
    clampPan()
  }

  function onUp() {
    isPanning.value = false
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

function onImageDblClick() {
  if (zoom.value > 1) {
    resetZoom()
  } else {
    const targetZoom = clamp(2, MIN_ZOOM, MAX_ZOOM)
    if (targetZoom === zoom.value) return
    zoom.value = targetZoom
    panX.value = 0
    panY.value = 0
  }
}

onMounted(async () => {
  const id = submissionId.value
  try {
    const res = await fetch(fileUrl(id), { credentials: 'include' })
    if (res.status === 401 || res.status === 403) {
      router.replace('/forbidden')
      return
    }
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const disposition = res.headers.get('content-disposition')
    if (disposition) {
      const match = disposition.match(/filename[^*]=?"?(.+?)"?$/i)
      if (match) fileName.value = match[1]
    }
    if (!fileName.value) fileName.value = `submission-${id}`

    const ext = fileExt(fileName.value)
    const contentType = res.headers.get('content-type') || ''

    if (IMAGE_EXTS.has(ext)) {
      previewMode.value = 'image'
      downloadUrl.value = fileUrl(id)
    } else if (VIDEO_EXTS.has(ext)) {
      previewMode.value = 'video'
      downloadUrl.value = fileUrl(id)
    } else if (isTextType(contentType)) {
      previewMode.value = 'text'
      content.value = await res.text()
    } else {
      previewMode.value = 'unsupported'
      const blob = await res.blob()
      downloadUrl.value = URL.createObjectURL(blob)
    }
  } catch (e: any) {
    error.value = e.message || '文件加载失败'
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  if (downloadUrl.value && downloadUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(downloadUrl.value)
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
    &--disabled { opacity: 0.3; pointer-events: none; }
  }
}

// ── Zoom controls (image mode) ──
.preview-bar__zoom {
  display: flex; align-items: center; gap: 2px; flex-shrink: 0;
}
.preview-bar__zoom-reset--hidden {
  visibility: hidden;
  pointer-events: none;
}
.preview-bar__zoom-pct {
  min-width: 44px; height: 28px;
  border: none; border-radius: 6px;
  background: transparent;
  color: rgb(var(--md-sys-color-on-surface-variant));
  cursor: pointer;
  font: 500 12px/28px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
  font-variant-numeric: tabular-nums;
  text-align: center;
  padding: 0 4px;
  transition: background .15s ease;
  &:hover { background: rgb(var(--md-sys-color-surface-container-highest)); }
}

// ── Tooltip ──
.fp-tooltip-wrap {
  position: relative;
  display: inline-flex;
  &:hover .fp-tooltip {
    opacity: 1;
    visibility: visible;
  }
}
.fp-tooltip {
  position: absolute;
  top: calc(100% + 6px);
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
  flex-direction: column;
  &--error { flex-direction: column; color: rgb(var(--md-sys-color-error)); }
  &__icon {
    width: 48px; height: 48px;
    color: rgb(var(--md-sys-color-on-surface-variant) / 0.5);
    margin-bottom: 8px;
  }
}

.preview-media {
  flex: 1; display: flex; align-items: center; justify-content: center;
  background: #000; overflow: hidden;
  img, video { max-width: 100%; max-height: 100%; object-fit: contain; }
  video:focus { outline: none; }
  &--pannable { cursor: grab; }
  &--panning { cursor: grabbing; }
}

.preview-download-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 22px;
  margin-top: 16px;
  border: 1px solid rgb(var(--md-sys-color-outline));
  border-radius: 20px;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  color: rgb(var(--md-sys-color-primary));
  cursor: pointer;
  font: 500 14px/22px 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
  transition: background 0.15s ease;
  svg { width: 16px; height: 16px; }
  &:hover { background: rgb(var(--md-sys-color-surface-container-high)); }
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
