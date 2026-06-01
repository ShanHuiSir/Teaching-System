<template>
  <div v-if="lines.length" class="alert" :class="'alert--' + type">
    <span class="alert__icon">{{ icon }}</span>
    <div class="alert__body">
      <div v-for="(line, i) in lines" :key="i">{{ line }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  type?: 'info' | 'warning' | 'error' | 'success';
  items?: string[];
  message?: string;
}>(), {
  type: 'info',
  items: () => [],
  message: '',
});

const configMap: Record<string, { icon: string }> = {
  info:    { icon: 'ℹ️' },
  warning: { icon: '⚠️' },
  error:   { icon: '❌' },
  success: { icon: '✅' },
};

const icon = computed(() => (configMap[props.type] || configMap.info).icon);
const lines = computed(() => props.items.length ? props.items : (props.message ? [props.message] : []));
</script>

<style lang="scss" scoped>
.alert {
  border-radius: $shape-sm;
  padding: 12px 16px;
  font: 400 13px/20px $font-family;
  display: flex;
  gap: 8px;
  align-items: flex-start;

  &--info {
    background: #DBE4FF;
    border: 1px solid #1A56DB;
    color: #001A41;
  }
  &--warning {
    background: #FEF3C7;
    border: 1px solid $warning;
    color: #78350F;
  }
  &--error {
    background: $error-container;
    border: 1px solid $error;
    color: $on-error-container;
  }
  &--success {
    background: #DCFCE7;
    border: 1px solid $success;
    color: #14532D;
  }
}

.alert__icon { flex-shrink: 0; font-size: 16px; line-height: 20px; }
.alert__body { flex: 1; }
</style>
