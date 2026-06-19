<template>
  <button type="button" class="act-btn" :class="`act-btn--${variant}`" :disabled="disabled" @click="$emit('click')">
    <slot name="icon" />
    <span><slot /></span>
  </button>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'primary' | 'danger' | 'outline' | 'secondary'
    disabled?: boolean
  }>(),
  {
    variant: 'primary',
    disabled: false,
  },
)

defineEmits<{
  click: []
}>()
</script>

<style lang="scss" scoped>
.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition:
    background 0.15s ease,
    opacity 0.15s ease;

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  :slotted(svg) {
    width: 16px;
    height: 16px;
  }

  span {
    @include font(13px, 20px, 500);
  }

  &--primary {
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    &:hover:not(:disabled) {
      filter: brightness(0.9);
    }
  }

  &--danger {
    background: transparent;
    color: rgb(var(--md-sys-color-error));
    border: 1px solid rgb(var(--md-sys-color-error));
    &:hover:not(:disabled) {
      background: rgb(var(--md-sys-color-error) / 0.08);
    }
  }

  &--outline {
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    border: 1px solid rgb(var(--md-sys-color-outline));
    &:hover:not(:disabled) {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }
  }

  &--secondary {
    background: rgb(var(--md-sys-color-secondary-container));
    color: rgb(var(--md-sys-color-on-secondary-container));
    &:hover:not(:disabled) {
      filter: brightness(0.95);
    }
  }
}
</style>
