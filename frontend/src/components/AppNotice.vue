<template>
  <div v-if="visible" class="app-notice" :class="'app-notice--' + type" role="alert">
    <span class="app-notice__icon">{{ icon }}</span>
    <span class="app-notice__msg">{{ message }}</span>
    <button class="app-notice__close" @click="$emit('close')" aria-label="关闭">&times;</button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  message: string;
  type?: 'info' | 'success' | 'warning' | 'error';
  visible?: boolean;
}>(), {
  type: 'info',
  visible: true,
});

defineEmits<{ close: [] }>();

const iconMap: Record<string, string> = {
  info: 'ℹ️', success: '✅', warning: '⚠️', error: '❌',
};
const icon = computed(() => iconMap[props.type] || iconMap.info);
</script>

<style lang="scss" scoped>
.app-notice {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  width: fit-content;
  max-width: 90%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: $shape-sm;
  font: 400 13px/20px $font-family;
  box-shadow: $elevation-3;
  white-space: nowrap;

  &--info {
    background: $primary-container;
    color: $on-primary-container;
    border: 1px solid $primary;
  }
  &--success {
    background: #DCFCE7;
    color: #14532D;
    border: 1px solid $success;
  }
  &--warning {
    background: $warning-container;
    color: #78350F;
    border: 1px solid $warning;
  }
  &--error {
    background: $error-container;
    color: $on-error-container;
    border: 1px solid $error;
  }
}

.app-notice__icon { flex-shrink: 0; font-size: 15px; }
.app-notice__msg { flex: 1; overflow: hidden; text-overflow: ellipsis; }

.app-notice__close {
  flex-shrink: 0;
  width: 22px; height: 22px;
  border: none; border-radius: $shape-xs;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 16px; line-height: 1;
  display: flex; align-items: center; justify-content: center;
  opacity: .7;
  transition: opacity .15s;

  &:hover { opacity: 1; }
}
</style>
