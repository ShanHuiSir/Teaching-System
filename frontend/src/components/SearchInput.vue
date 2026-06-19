<template>
  <div class="search-input">
    <svg
      class="search-input__icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      <circle cx="11" cy="11" r="8" />
      <line x1="21" y1="21" x2="16.65" y2="16.65" />
    </svg>
    <input
      :value="modelValue"
      class="search-input__input"
      type="text"
      :placeholder="placeholder"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <button v-if="modelValue" class="search-input__clear" @click="$emit('update:modelValue', '')">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <line x1="18" y1="6" x2="6" y2="18" />
        <line x1="6" y1="6" x2="18" y2="18" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: string
  placeholder?: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<style lang="scss" scoped>
.search-input {
  position: relative;
  display: flex;
  align-items: center;
  flex: 0 1 280px;
  min-width: 0;

  &__icon {
    position: absolute;
    left: 12px;
    width: 16px;
    height: 16px;
    color: rgb(var(--md-sys-color-on-surface-variant));
    pointer-events: none;
  }

  &__input {
    width: 100%;
    height: 36px;
    padding: 0 36px 0 36px;
    border: 1px solid transparent;
    border-radius: 10px;
    background: rgb(var(--md-sys-color-surface-container));
    color: rgb(var(--md-sys-color-on-surface));
    @include font(13px, 20px);
    outline: none;
    transition:
      border-color 0.2s ease,
      background 0.2s ease;

    &::placeholder {
      color: rgb(var(--md-sys-color-on-surface-variant) / 0.5);
    }

    &:focus {
      border-color: rgb(var(--md-sys-color-primary));
      background: rgb(var(--md-sys-color-surface-container-lowest));
    }

    &:focus-visible {
      outline: none;
    }
  }

  &__clear {
    position: absolute;
    right: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 8px;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    cursor: pointer;
    transition: background 0.15s ease;

    svg {
      width: 14px;
      height: 14px;
    }

    &:hover {
      background: rgb(var(--md-sys-color-on-surface-variant) / 0.12);
    }
  }
}
</style>
