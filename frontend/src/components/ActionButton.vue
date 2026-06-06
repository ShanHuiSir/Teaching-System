<template>
  <button
    class="act-btn"
    :class="`act-btn--${variant}`"
    :disabled="disabled"
    @click="$emit('click')"
  >
    <slot name="icon" />
    <span><slot /></span>
  </button>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  variant?: 'primary' | 'danger' | 'outline'
  disabled?: boolean
}>(), {
  variant: 'primary',
  disabled: false,
})

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
  transition: background .15s ease, opacity .15s ease;

  &:disabled {
    opacity: .4;
    cursor: not-allowed;
  }

  :slotted(svg) { width: 16px; height: 16px; }

  span { @include font(13px, 20px, 500); }

  &--primary {
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    &:hover:not(:disabled) { filter: brightness(.9); }
  }

  &--danger {
    background: transparent;
    color: rgb(var(--md-sys-color-error));
    border: 1px solid rgb(var(--md-sys-color-error));
    &:hover:not(:disabled) { background: rgb(var(--md-sys-color-error) / .08); }
  }

  &--outline {
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    border: 1px solid rgb(var(--md-sys-color-outline));
    &:hover:not(:disabled) { background: rgb(var(--md-sys-color-surface-container-highest)); }
  }
}
</style>
