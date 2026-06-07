<template>
  <div class="skeleton">
    <!-- Panel 1: List -->
    <div class="skeleton__panel skeleton__panel--list">
      <div class="skeleton__toolbar">
        <div class="skeleton__block skeleton__block--tab" />
        <div class="skeleton__block skeleton__block--search" />
      </div>
      <div class="skeleton__cards">
        <div v-for="i in listCount" :key="i" class="skeleton__card">
          <div class="skeleton__line skeleton__line--title" :style="{ width: titleWidths[(i - 1) % titleWidths.length] }" />
          <div class="skeleton__line skeleton__line--sub" />
          <div class="skeleton__chips">
            <div class="skeleton__chip" />
            <div class="skeleton__chip" :style="{ width: '60px' }" />
          </div>
        </div>
      </div>
    </div>

    <!-- Panel 2: Preview -->
    <div class="skeleton__panel skeleton__panel--preview">
      <div class="skeleton__card skeleton__card--detail">
        <div class="skeleton__bar">
          <div class="skeleton__block skeleton__block--btn" />
          <div class="skeleton__block skeleton__block--btn" />
        </div>
        <div class="skeleton__grid">
          <div v-for="i in 3" :key="i" class="skeleton__field">
            <div class="skeleton__line skeleton__line--label" />
            <div class="skeleton__line skeleton__line--value" />
          </div>
        </div>
        <div class="skeleton__line skeleton__line--label" />
        <div class="skeleton__block skeleton__block--note" />
      </div>
    </div>

    <!-- Panel 3: Form (only visible in editing state, shown as dim placeholder) -->
    <div class="skeleton__panel skeleton__panel--form">
      <div class="skeleton__card skeleton__card--form">
        <div class="skeleton__bar">
          <div class="skeleton__block skeleton__block--btn" />
          <div class="skeleton__block skeleton__block--btn" style="margin-left: auto" />
        </div>
        <div class="skeleton__block skeleton__block--score" />
        <div class="skeleton__block skeleton__block--editor" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    listCount?: number
  }>(),
  {
    listCount: 5,
  },
)

const titleWidths = ['60%', '45%', '72%', '38%', '55%']
</script>

<style lang="scss" scoped>
.skeleton {
  display: flex;
  height: 100%;
  overflow: hidden;
  gap: 0;

  &__panel {
    overflow: hidden;

    &--list {
      flex: 0 0 50%;
      padding-right: 12px;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    &--preview {
      flex: 0 0 25%;
      padding: 0 12px;
    }

    &--form {
      flex: 0 0 25%;
      padding-left: 12px;
    }
  }

  &__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    background: rgb(var(--md-sys-color-surface-container-lowest));
    border-radius: 16px;
    padding: 12px 16px;
  }

  &__block {
    background: rgb(var(--md-sys-color-on-surface) / 0.06);
    border-radius: 10px;

    &--tab {
      width: 180px;
      height: 36px;
      border-radius: 12px;
    }

    &--search {
      width: 280px;
      height: 36px;
      border-radius: 10px;
    }

    &--btn {
      width: 80px;
      height: 36px;
      border-radius: 10px;
    }

    &--note {
      height: 64px;
      border-radius: 10px;
    }

    &--score {
      height: 44px;
      border-radius: 10px;
    }

    &--editor {
      height: 160px;
      border-radius: 10px;
    }
  }

  &__cards {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__card {
    background: rgb(var(--md-sys-color-surface-container-lowest));
    border-radius: 16px;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;

    &--detail {
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    &--form {
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
  }

  &__bar {
    display: flex;
    gap: 8px;
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-bottom: 4px;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  &__line {
    height: 14px;
    background: rgb(var(--md-sys-color-on-surface) / 0.05);
    border-radius: 6px;

    &--title {
      height: 18px;
      border-radius: 8px;
      background: rgb(var(--md-sys-color-on-surface) / 0.07);
    }

    &--sub {
      width: 65%;
    }

    &--label {
      width: 40px;
      height: 12px;
      border-radius: 4px;
      background: rgb(var(--md-sys-color-on-surface) / 0.04);
    }

    &--value {
      width: 70%;
      background: rgb(var(--md-sys-color-on-surface) / 0.06);
    }
  }

  &__chips {
    display: flex;
    gap: 8px;
  }

  &__chip {
    width: 48px;
    height: 12px;
    background: rgb(var(--md-sys-color-on-surface) / 0.04);
    border-radius: 6px;
  }
}

/* ── Shimmer animation ── */
.skeleton__block,
.skeleton__line,
.skeleton__chip {
  animation: skeleton-shimmer 1.8s ease-in-out infinite;
  background: linear-gradient(
    90deg,
    rgb(var(--md-sys-color-on-surface) / 0.04) 25%,
    rgb(var(--md-sys-color-on-surface) / 0.08) 40%,
    rgb(var(--md-sys-color-on-surface) / 0.04) 55%
  );
  background-size: 300% 100%;
}

@keyframes skeleton-shimmer {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: -50% 0;
  }
}
</style>
