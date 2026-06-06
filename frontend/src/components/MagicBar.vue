<template>
  <div class="magic-bar">
    <Transition name="magic" mode="out-in">
      <div :key="magicKey" class="magic-bar__text">
        <template v-if="magicBar.status">
          <span class="magic-bar__dot" :class="`magic-bar__dot--${magicBar.statusType}`" />
          <span class="magic-bar__status">{{ magicBar.status }}</span>
        </template>
        <template v-else>
          <span class="magic-bar__primary">{{ magicBar.primary }}</span>
          <template v-if="greeting || magicBar.sub">
            <span class="magic-bar__sep">&middot;</span>
            <span v-if="greeting" class="magic-bar__greeting">{{ greeting }}</span>
            <span v-else class="magic-bar__sub">{{ magicBar.sub }}</span>
          </template>
          <template v-if="magicBar.suffix">
            <span class="magic-bar__sep">&middot;</span>
            <span class="magic-bar__suffix" :class="`magic-bar__suffix--${magicBar.suffixType}`">{{
              magicBar.suffix
            }}</span>
          </template>
        </template>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import type { MagicBar } from '../types'

defineProps<{
  magicBar: MagicBar
  greeting: string
  magicKey: string
}>()
</script>

<style lang="scss" scoped>
.magic-bar {
  overflow: hidden;
  height: 28px;

  &__text {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: baseline;
    gap: 6px;
    white-space: nowrap;
  }

  &__primary {
    @include font(18px, 24px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: 0.02em;
  }

  &__sep {
    @include font(18px, 24px);
    color: rgb(var(--md-sys-color-outline));
  }

  &__sub {
    @include font(15px, 24px, 400);
    color: rgb(var(--md-sys-color-on-surface-variant));
    max-width: 420px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__greeting {
    @include font(15px, 24px, 400);
    color: rgb(var(--md-sys-color-on-surface-variant));
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__suffix {
    @include font(14px, 24px, 400);
    white-space: nowrap;

    &--reconnecting {
      color: rgb(var(--md-sys-color-tertiary));
      animation: magic-breathe 3s ease-in-out infinite;
    }

    &--offline {
      color: rgb(var(--md-sys-color-error));
    }

    &--reconnected {
      color: #16a34a;
    }
  }

  &__status {
    @include font(15px, 24px, 400);
    color: rgb(var(--md-sys-color-on-surface-variant));
    max-width: 520px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__dot {
    flex-shrink: 0;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    align-self: center;

    &--loading {
      background: rgb(var(--md-sys-color-primary));
      animation: magic-pulse 1.2s ease-in-out infinite;
    }

    &--success {
      background: #16a34a;
    }

    &--info {
      background: rgb(var(--md-sys-color-tertiary));
      animation: magic-pulse 2s ease-in-out infinite;
    }
  }
}

@keyframes magic-pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.35;
    transform: scale(0.75);
  }
}

@keyframes magic-breathe {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.94);
  }
}
</style>

<style>
/* Transition classes must be unscoped for Vue <Transition> */
.magic-enter-active,
.magic-leave-active {
  transition:
    transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    opacity 0.25s ease;
}
.magic-leave-to {
  transform: translateY(-120%);
  opacity: 0;
}
.magic-enter-from {
  transform: translateY(120%);
  opacity: 0;
}
</style>
