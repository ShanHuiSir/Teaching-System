<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="modal-overlay" @click.self="$emit('cancel')">
        <div class="modal-card">
          <svg class="modal-card__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" /></svg>
          <p class="modal-card__text">{{ message }}</p>
          <div class="modal-card__btns">
            <button class="modal-card__btn modal-card__btn--cancel" @click="$emit('cancel')">{{ cancelLabel }}</button>
            <button class="modal-card__btn modal-card__btn--confirm" @click="$emit('confirm')">{{ confirmLabel }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  open: boolean
  message: string
  confirmLabel?: string
  cancelLabel?: string
}>(), {
  confirmLabel: '确认删除',
  cancelLabel: '取消',
})

defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9998;
  backdrop-filter: blur(4px);
}

.modal-card {
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 16px;
  padding: 32px;
  width: 360px;
  max-width: calc(100vw - 32px);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;

  &__icon {
    width: 40px;
    height: 40px;
    color: rgb(var(--md-sys-color-error));
  }

  &__text {
    @include font(14px, 22px);
    color: rgb(var(--md-sys-color-on-surface));
    text-align: center;
    margin: 0;
  }

  &__btns {
    display: flex;
    gap: 12px;
    width: 100%;
    margin-top: 8px;
  }

  &__btn {
    flex: 1;
    height: 40px;
    border: none;
    border-radius: 12px;
    cursor: pointer;
    @include font(14px, 20px, 500);
    transition: background .15s ease;

    &--cancel {
      background: rgb(var(--md-sys-color-surface-container-high));
      color: rgb(var(--md-sys-color-on-surface));
      &:hover { background: rgb(var(--md-sys-color-surface-container-highest)); }
    }

    &--confirm {
      background: rgb(var(--md-sys-color-error));
      color: rgb(var(--md-sys-color-on-error));
      &:hover { opacity: .9; }
    }
  }
}

.modal-enter-active {
  transition: opacity .2s ease;
  .modal-card { transition: transform .2s cubic-bezier(.4, 0, .2, 1); }
}
.modal-leave-active {
  transition: opacity .15s ease;
  .modal-card { transition: transform .15s ease-in; }
}
.modal-enter-from {
  opacity: 0;
  .modal-card { transform: scale(.9); }
}
.modal-leave-to {
  opacity: 0;
  .modal-card { transform: scale(.95); }
}
</style>
