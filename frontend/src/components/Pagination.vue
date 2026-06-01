<template>
  <div v-if="totalPages > 1" class="pagination">
    <button class="btn btn--outlined btn--sm" :disabled="!hasPrevious" @click="$emit('change', page - 1)">
      上一页
    </button>

    <template v-if="startPage > 0">
      <button class="pagination__num" :class="{ active: page === 0 }" @click="$emit('change', 0)">1</button>
      <span v-if="startPage > 1" class="pagination__dots">...</span>
    </template>

    <button
      v-for="n in visiblePages"
      :key="n"
      class="pagination__num"
      :class="{ active: n === page }"
      @click="$emit('change', n)"
    >{{ n + 1 }}</button>

    <template v-if="endPage < totalPages - 1">
      <span v-if="endPage < totalPages - 2" class="pagination__dots">...</span>
      <button
        class="pagination__num"
        :class="{ active: page === totalPages - 1 }"
        @click="$emit('change', totalPages - 1)"
      >{{ totalPages }}</button>
    </template>

    <button class="btn btn--outlined btn--sm" :disabled="!hasNext" @click="$emit('change', page + 1)">
      下一页
    </button>

    <span class="pagination__info">第 {{ page + 1 }}/{{ totalPages }} 页</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  page: number;
  totalPages: number;
  hasPrevious: boolean;
  hasNext: boolean;
}>();
defineEmits<{
  change: [page: number];
}>();

const startPage = computed(() => Math.max(0, props.page - 2));
const endPage = computed(() => Math.min(props.totalPages - 1, props.page + 2));

const visiblePages = computed(() => {
  const pages: number[] = [];
  for (let i = startPage.value; i <= endPage.value; i++) pages.push(i);
  return pages;
});
</script>

<style lang="scss" scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 0;

  &__num {
    width: 32px; height: 32px;
    border: none;
    border-radius: $shape-full;
    cursor: pointer;
    font: 500 13px/20px $font-family;
    background: transparent;
    color: $on-surface-variant;
    transition: background .15s;

    &:hover { background: $surface-container-highest; }

    &.active {
      background: $primary;
      color: $on-primary;
      &:hover { background: $primary; }
    }
  }

  &__dots {
    color: $on-surface-variant;
    padding: 0 4px;
  }

  &__info {
    margin-left: 12px;
    color: $on-surface-variant;
    font: 400 12px/16px $font-family;
  }
}
</style>
