<template>
  <span class="stat-chip" :title="tooltip || undefined">
    <slot name="icon">
      <svg
        v-if="icon"
        class="stat-chip__icon"
        :viewBox="iconViewBox"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path v-for="(p, i) in iconPaths" :key="i" v-bind="p" />
      </svg>
    </slot>
    <span><slot /></span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { FILE_ICONS } from '../utils/fileIcons'

const props = withDefaults(
  defineProps<{
    icon?: string
    tooltip?: string
  }>(),
  {
    icon: '',
    tooltip: '',
  },
)

const iconViewBox = computed(() => {
  if (!props.icon) return '0 0 24 24'
  return FILE_ICONS[props.icon]?.viewBox || '0 0 24 24'
})

const iconPaths = computed(() => {
  if (!props.icon) return []
  const raw = FILE_ICONS[props.icon]?.paths || []
  return raw.map((p: any) => (typeof p === 'string' ? { d: p } : p))
})
</script>

<style lang="scss" scoped>
.stat-chip {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &__icon,
  :slotted(svg) {
    width: 14px;
    height: 14px;
    color: rgb(var(--md-sys-color-on-surface-variant));
    flex-shrink: 0;
  }

  span {
    @include font(12px, 16px);
    color: rgb(var(--md-sys-color-on-surface));
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

}
</style>
