<template>
  <Transition name="snack">
    <div v-if="visible" class="snackbar" :class="`snackbar--${variant}`">
      <span class="snackbar__text">{{ message }}</span>
      <button v-if="actionLabel" class="snackbar__action" @click="doAction">{{ actionLabel }}</button>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useSnackbar } from '../composables/useSnackbar'

const { message, visible, variant, actionLabel, doAction } = useSnackbar()
</script>

<style lang="scss" scoped>
.snackbar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 32px);
  max-width: 600px;
  padding: 14px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  z-index: 9999;

  &__text {
    @include font(14px, 20px, 500);
    letter-spacing: .02em;
  }

  &__action {
    @include font(14px, 20px, 500);
    background: transparent;
    border: none;
    color: inherit;
    cursor: pointer;
    flex-shrink: 0;
    padding: 4px 14px;
    border-radius: 999px;
    transition: background .15s ease;

    &:hover {
      background: rgb(var(--md-sys-color-on-secondary) / .15);
    }
  }

  &--error {
    background: rgb(var(--md-sys-color-error-container));
    color: rgb(var(--md-sys-color-on-error-container));

    .snackbar__action:hover {
      background: rgb(var(--md-sys-color-on-error-container) / .15);
    }
  }

  &--warning {
    background: rgb(var(--md-sys-color-tertiary));
    color: rgb(var(--md-sys-color-on-tertiary));

    .snackbar__action:hover {
      background: rgb(var(--md-sys-color-on-tertiary) / .15);
    }
  }

  &--info {
    background: rgb(var(--md-sys-color-secondary));
    color: rgb(var(--md-sys-color-on-secondary));

    .snackbar__action:hover {
      background: rgb(var(--md-sys-color-on-secondary) / .15);
    }
  }
}

.snack-enter-active {
  transition: all .25s cubic-bezier(.4, 0, .2, 1);
}
.snack-leave-active {
  transition: all .2s ease-in;
}
.snack-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(16px);
}
.snack-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(8px);
}
</style>
