<template>
  <div class="info-field">
    <span class="info-field__label">{{ label }}</span>
    <span class="info-field__value"><slot /></span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
}>()
</script>

<style lang="scss" scoped>
.info-field {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &__label {
    @include font(12px, 16px, 500);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &__value {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface));
  }
}
</style>
