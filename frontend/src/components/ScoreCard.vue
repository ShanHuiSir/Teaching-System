<template>
  <div class="card score-card">
    <div class="score-card__label">{{ label }}</div>
    <div class="score-card__value" :style="{ color }">
      {{ score != null ? score : '--' }}
    </div>
    <div v-if="score != null" class="score-card__unit">分</div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  label: string;
  score?: number | null;
  color?: string;
}>(), {
  color: 'var(--primary)',
});
</script>

<style lang="scss" scoped>
.score-card {
  text-align: center;
  padding: 24px 16px;

  &__label {
    font: 400 13px/20px $font-family;
    color: $on-surface-variant;
    margin-bottom: 8px;
  }
  &__value {
    font: 700 48px/1.2 $font-family;
  }
  &__unit {
    color: $on-surface-variant;
    font-size: 13px;
  }
}
</style>
