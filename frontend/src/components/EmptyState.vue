<template>
  <div class="empty-state">
    <span v-if="emoji" class="empty-state__emoji">{{ emoji }}</span>
    <span class="empty-state__text">{{ text }}</span>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  emoji?: string
  text: string
}>(), {
  emoji: '¯\\_(ツ)_/¯',
})
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 0;
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 16px;

  &__emoji {
    @include font(28px, 36px);
  }

  &__text {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }
}
</style>
