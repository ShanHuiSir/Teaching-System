<template>
  <div class="preview-placeholder">
    <slot name="icon">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <rect x="2" y="3" width="20" height="14" rx="2" />
        <line x1="8" y1="21" x2="16" y2="21" />
        <line x1="12" y1="17" x2="12" y2="21" />
      </svg>
    </slot>
    <span>{{ message }}</span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  message: string
}>()
</script>

<style lang="scss" scoped>
.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: rgb(var(--md-sys-color-outline));

  :slotted(svg),
  svg {
    width: 56px;
    height: 56px;
  }

  span {
    @include font(14px, 20px);
  }
}
</style>
