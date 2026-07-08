<template>
  <Teleport to="body">
    <Transition name="float">
      <div
        v-if="modelValue"
        class="fp-window"
        :style="windowStyle"
        @mousedown="onFocus"
        @wheel.stop
      >
          <!-- Title bar (drag handle) -->
          <div class="fp-bar" @mousedown="onDragStart">
            <div class="fp-bar__info">
              <svg class="fp-bar__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              <span class="fp-bar__name">{{ fileName }}</span>
              <span v-if="previewMode === 'office'" class="fp-bar__warn">排版可能与原文件有差异，建议下载查看</span>
            </div>

            <!-- Zoom controls (image mode only) -->
            <div v-if="previewMode === 'image'" class="fp-bar__zoom">
              <div class="fp-tooltip-wrap">
                <button
                  class="fp-bar__btn"
                  :class="{ 'fp-bar__btn--disabled': zoom <= MIN_ZOOM }"
                  :disabled="zoom <= MIN_ZOOM"
                  aria-label="缩小"
                  @click.stop="zoomOut"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                    <line x1="8" y1="11" x2="14" y2="11" />
                  </svg>
                </button>
                <span class="fp-tooltip">缩小</span>
              </div>
              <button class="fp-bar__zoom-pct" @click.stop="resetZoom">{{ zoomPct }}</button>
              <div class="fp-tooltip-wrap">
                <button
                  class="fp-bar__btn"
                  :class="{ 'fp-bar__btn--disabled': zoom >= MAX_ZOOM }"
                  :disabled="zoom >= MAX_ZOOM"
                  aria-label="放大"
                  @click.stop="zoomIn"
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
              <div class="fp-tooltip-wrap" :class="{ 'fp-bar__zoom-reset--hidden': zoom === 1 }">
                <button class="fp-bar__btn" aria-label="复位" @click.stop="resetZoom">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="1 4 1 10 7 10" />
                    <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
                  </svg>
                </button>
                <span class="fp-tooltip">复位</span>
              </div>
            </div>

            <div class="fp-bar__actions">
              <div class="fp-tooltip-wrap">
                <button class="fp-bar__btn" type="button" title="在新标签页中打开" aria-label="在新标签页中打开" @click.stop="openInNewTab">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                    <polyline points="15 3 21 3 21 9" />
                    <line x1="10" y1="14" x2="21" y2="3" />
                  </svg>
                </button>
                <span class="fp-tooltip">在新标签页中打开</span>
              </div>
              <div class="fp-tooltip-wrap">
                <button class="fp-bar__btn fp-bar__btn--close" type="button" title="关闭预览" aria-label="关闭预览" @click.stop="closePreview(true)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
                <span class="fp-tooltip">关闭</span>
              </div>
            </div>
          </div>

          <!-- Content -->
          <div class="fp-body" :class="{ 'fp-body--media': previewMode === 'image' || previewMode === 'video' }">
            <div v-if="loading" class="fp-loading">
              <div class="fp-spinner" />
              <span>加载中...</span>
            </div>
            <div v-else-if="error" class="fp-error">
              <p>{{ error }}</p>
            </div>
            <!-- Image preview -->
