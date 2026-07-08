<template>
  <button type="button" class="act-btn" :class="[`act-btn--${variant}`, `act-btn--${size}`]" :disabled="disabled" @click="$emit('click')">
    <slot name="icon" />
    <span><slot /></span>
  </button>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'primary' | 'danger' | 'outline' | 'secondary'
    size?: 'sm' | 'md' | 'lg'
    disabled?: boolean
  }>(),
  {
    variant: 'primary',
    size: 'md',
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

  /* ── Sizes ── */
  &--sm {
    height: 32px;
    padding: 0 12px;
    span { @include font(12px, 18px, 500); }
  }
  &--md {
    height: 36px;
    padding: 0 16px;
    span { @include font(13px, 20px, 500); }
  }
  &--lg {
    height: 44px;
    padding: 0 24px;
    span { @include font(14px, 22px, 500); }
  }

  /* ── Variants ── */
  &--primary {
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    &:hover:not(:disabled) { background-image: linear-gradient(rgb(0 0 0 / 0.08), rgb(0 0 0 / 0.08)); }
    &:active:not(:disabled) { background-image: linear-gradient(rgb(0 0 0 / 0.16), rgb(0 0 0 / 0.16)); }
  }

  &--danger {
    background: transparent;
    color: rgb(var(--md-sys-color-error));
    border: 1px solid rgb(var(--md-sys-color-error));
    &:hover:not(:disabled) { background: rgb(var(--md-sys-color-error) / 0.08); }
    &:active:not(:disabled) { background: rgb(var(--md-sys-color-error) / 0.16); }
  }

  &--outline {
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    border: 1px solid rgb(var(--md-sys-color-outline));
    &:hover:not(:disabled) { background: rgb(var(--md-sys-color-surface-container-highest)); }
    &:active:not(:disabled) { background: rgb(var(--md-sys-color-on-surface) / 0.1); }
  }

  &--secondary {
    background: rgb(var(--md-sys-color-secondary-container));
    color: rgb(var(--md-sys-color-on-secondary-container));
    &:hover:not(:disabled) { background-image: linear-gradient(rgb(0 0 0 / 0.06), rgb(0 0 0 / 0.06)); }
    &:active:not(:disabled) { background-image: linear-gradient(rgb(0 0 0 / 0.12), rgb(0 0 0 / 0.12)); }
  }
}
</style>
