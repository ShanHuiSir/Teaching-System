<template>
  <div class="sk-list">
    <div v-for="i in count" :key="i" class="sk-card">
      <div class="sk-card__top">
        <div class="sk-line sk-line--name" :style="{ width: nameWidths[(i - 1) % nameWidths.length] }" />
        <div class="sk-line sk-line--badge" />
      </div>
      <div class="sk-line sk-line--sub" />
      <div class="sk-card__chips">
        <div class="sk-chip" />
        <div class="sk-chip" :style="{ width: '56px' }" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 5 })
const nameWidths = ['55%', '42%', '68%', '35%', '50%']
</script>

<style lang="scss" scoped>
.sk-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-card {
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;

  &__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__chips {
    display: flex;
    gap: 8px;
  }
}

.sk-line {
  height: 13px;
  background: rgb(var(--md-sys-color-on-surface) / 0.05);
  border-radius: 6px;

  &--name {
    height: 17px;
    border-radius: 8px;
    background: rgb(var(--md-sys-color-on-surface) / 0.07);
  }

  &--badge {
    width: 52px;
    height: 15px;
    border-radius: 8px;
  }

  &--sub {
    width: 68%;
  }
}

.sk-chip {
  width: 42px;
  height: 11px;
  background: rgb(var(--md-sys-color-on-surface) / 0.04);
  border-radius: 5px;
}

/* shimmer */
.sk-line,
.sk-chip {
  @include shimmer;
}
</style>
