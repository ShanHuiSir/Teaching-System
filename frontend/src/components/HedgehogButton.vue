<template>
  <span
    ref="wrapRef"
    class="sv-btn-wrap"
    :style="{ transform: `translate(${repelX}px, ${repelY}px)` }"
  >
    <button
      ref="btnRef"
      class="sv-btn"
      :class="[
        `sv-btn--${variant}`,
        `sv-btn--${size}`,
        {
          'sv-btn--pop': popping,
          'sv-btn--shaking': shaking,
          'sv-btn--cancelling': cancelling
        }
      ]"
      :disabled="disabled"
      @mousedown.prevent="onDown"
      @mouseup="onUp"
      @mouseenter="onEnter"
      @mouseleave="onLeave"
      @touchstart.prevent="onDown"
      @touchend="onTouchEnd"
      @touchcancel="onTouchCancel"
    >
      <span class="sv-btn__content">
        <slot />
      </span>
      <svg
        class="sv-btn__icon"
        viewBox="0 0 24 24"
        fill="none"
        aria-hidden="true"
      >
        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
        <line x1="5" y1="5" x2="19" y2="19" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
      </svg>
      <span class="sv-btn__bubble sv-btn__bubble--a" style="left: 12%; --b-delay: 0s;    --b-dur: 2.8s; --b-sway: 2.4s"></span>
      <span class="sv-btn__bubble sv-btn__bubble--b" style="left: 28%; --b-delay: 0.8s;  --b-dur: 3.2s; --b-sway: 2.8s"></span>
      <span class="sv-btn__bubble sv-btn__bubble--c" style="left: 44%; --b-delay: 0.3s;  --b-dur: 2.5s; --b-sway: 2.2s"></span>
      <span class="sv-btn__bubble sv-btn__bubble--a" style="left: 58%; --b-delay: 1.3s;  --b-dur: 3.0s; --b-sway: 2.6s"></span>
      <span class="sv-btn__bubble sv-btn__bubble--b" style="left: 72%; --b-delay: 0.6s;  --b-dur: 2.7s; --b-sway: 2.3s"></span>
      <span class="sv-btn__bubble sv-btn__bubble--c" style="left: 86%; --b-delay: 1.7s;  --b-dur: 3.5s; --b-sway: 3.0s"></span>
    </button>
  </span>

  <Teleport to="body">
    <Transition name="warning">
      <div v-if="shaking" class="sv-warning">
        <p class="sv-warning__hint">移出按钮以取消点击</p>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const emit = defineEmits<{
  complete: []
}>()

const props = withDefaults(defineProps<{
  variant?: 'primary' | 'outline' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
}>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
})

const LONG_PRESS = 400
const POP_DURATION = 360
const REPEL_RADIUS = 130
const REPEL_STRENGTH = 28

const wrapRef = ref<HTMLElement | null>(null)
const btnRef = ref<HTMLElement | null>(null)
const pressing = ref(false)
const popping = ref(false)
const shaking = ref(false)
const cancelling = ref(false)
const isHovering = ref(false)
const repelX = ref(0)
const repelY = ref(0)
let holdTimer: ReturnType<typeof setTimeout> | null = null
let repelFrame: ReturnType<typeof requestAnimationFrame> | null = null
let repelTargetX = 0
let repelTargetY = 0

/* ---------- press lifecycle ---------- */

function onDown() {
  if (props.disabled) return
  pressing.value = true
  isHovering.value = true
  cancelling.value = false
  holdTimer = setTimeout(() => {
    shaking.value = true
    window.addEventListener('mouseup', onWindowUp)
  }, LONG_PRESS)
}

function onUp() {
  if (props.disabled || !pressing.value) return
  finishPress(true)
}

function onWindowUp(e: MouseEvent) {
  const el = btnRef.value
  if (el) {
    const rect = el.getBoundingClientRect()
    const inside = e.clientX >= rect.left && e.clientX <= rect.right &&
                   e.clientY >= rect.top  && e.clientY <= rect.bottom
    if (inside) return
  }
  finishPress(false)
}

function onEnter() {
  isHovering.value = true
  cancelling.value = false
}

function onLeave() {
  isHovering.value = false
  if (!shaking.value) {
    finishPress(false)
  } else {
    cancelling.value = true
  }
}

function onTouchEnd(e: TouchEvent) {
  if (props.disabled || !pressing.value) return
  const touch = e.changedTouches[0]
  const el = btnRef.value
  let inside = false
  if (el && touch) {
    const rect = el.getBoundingClientRect()
    inside = touch.clientX >= rect.left && touch.clientX <= rect.right &&
             touch.clientY >= rect.top  && touch.clientY <= rect.bottom
  }
  finishPress(inside)
}

function onTouchCancel() {
  finishPress(false)
}

function finishPress(completed: boolean) {
  window.removeEventListener('mouseup', onWindowUp)
  if (holdTimer) {
    clearTimeout(holdTimer)
    holdTimer = null
  }
  pressing.value = false
  isHovering.value = false
  cancelling.value = false
  if (shaking.value) {
    shaking.value = false
  }
  if (completed) {
    popping.value = true
    emit('complete')
    setTimeout(() => {
      popping.value = false
    }, POP_DURATION)
  }
}

/* ---------- repel ---------- */

function onMouseMove(e: MouseEvent) {
  if (props.disabled || pressing.value) {
    repelTargetX = 0
    repelTargetY = 0
  } else {
    const el = wrapRef.value
    if (!el) return
    const rect = el.getBoundingClientRect()
    const cx = rect.left + rect.width / 2
    const cy = rect.top + rect.height / 2
    const dx = e.clientX - cx
    const dy = e.clientY - cy
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (dist < REPEL_RADIUS && dist > 1) {
      const strength = (1 - dist / REPEL_RADIUS) * REPEL_STRENGTH
      repelTargetX = (-dx / dist) * strength
      repelTargetY = (-dy / dist) * strength
    } else {
      repelTargetX = 0
      repelTargetY = 0
    }
  }
  if (!repelFrame) {
    repelFrame = requestAnimationFrame(() => {
      repelX.value = repelTargetX
      repelY.value = repelTargetY
      repelFrame = null
    })
  }
}

onMounted(() => window.addEventListener('mousemove', onMouseMove, { passive: true }))
onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onWindowUp)
  if (repelFrame) cancelAnimationFrame(repelFrame)
  if (holdTimer) clearTimeout(holdTimer)
})
</script>

<style scoped>
.sv-btn-wrap {
  display: inline-flex;
  transition: transform 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.sv-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-family: inherit;
  font-weight: 500;
  line-height: 1;
  cursor: pointer;
  user-select: none;
  border: 2px solid transparent;
  border-radius: 999px;
  outline: none;
  overflow: hidden;
}

.sv-btn__content {
  position: relative;
  z-index: 1;
  transition: color 0.25s ease;
}

/* ---------- slash highlight ---------- */
.sv-btn::before {
  content: '';
  position: absolute;
  top: -30%;
  left: -80%;
  width: 50%;
  height: 160%;
  background: linear-gradient(
    115deg,
    transparent 0%,
    transparent 35%,
    rgba(255, 255, 255, 0.5) 48%,
    rgba(255, 255, 255, 0.5) 52%,
    transparent 65%,
    transparent 100%
  );
  transform: skewX(-12deg);
  transition: left 0.55s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

.sv-btn:hover::before {
  left: 150%;
}

/* ---------- prohibited icon ---------- */
.sv-btn__icon {
  position: absolute;
  right: -16%;
  bottom: -22%;
  height: 88%;
  width: auto;
  opacity: 0;
  transform: translate(28%, 22%);
  transition:
    opacity 0.5s ease,
    transform 0.7s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  pointer-events: none;
  z-index: 0;
  color: rgb(var(--md-sys-color-error));
}

.sv-btn:hover .sv-btn__icon {
  opacity: 0.32;
  transform: translate(0, 0);
}

/* ---------- bubbles ---------- */
.sv-btn__bubble {
  position: absolute;
  bottom: -8px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: rgb(var(--md-sys-color-error-container));
  opacity: 0;
  pointer-events: none;
  z-index: 0;
}

.sv-btn:hover .sv-btn__bubble--a {
  animation:
    bubble-rise  var(--b-dur)  var(--b-delay) ease-out infinite,
    bubble-sway-a var(--b-sway) var(--b-delay) ease-in-out infinite;
}

.sv-btn:hover .sv-btn__bubble--b {
  animation:
    bubble-rise  var(--b-dur)  var(--b-delay) ease-out infinite,
    bubble-sway-b var(--b-sway) var(--b-delay) ease-in-out infinite;
}

.sv-btn:hover .sv-btn__bubble--c {
  animation:
    bubble-rise  var(--b-dur)  var(--b-delay) ease-out infinite,
    bubble-sway-c var(--b-sway) var(--b-delay) ease-in-out infinite;
}

@keyframes bubble-rise {
  0%   { bottom: -8px; opacity: 0; }
  8%   { opacity: 0.6; }
  90%  { opacity: 0.6; }
  100% { bottom: 105%; opacity: 0; }
}

@keyframes bubble-sway-a {
  0%   { transform: translateX(0); }
  25%  { transform: translateX(4px); }
  75%  { transform: translateX(-3px); }
  100% { transform: translateX(0); }
}

@keyframes bubble-sway-b {
  0%   { transform: translateX(0); }
  25%  { transform: translateX(-3px); }
  75%  { transform: translateX(4px); }
  100% { transform: translateX(0); }
}

@keyframes bubble-sway-c {
  0%   { transform: translateX(0); }
  25%  { transform: translateX(3px); }
  75%  { transform: translateX(-4px); }
  100% { transform: translateX(0); }
}

/* ---------- short press: pop ---------- */
@keyframes btn-pop {
  0%   { transform: scale(1); }
  30%  { transform: scale(0.82); }
  55%  { transform: scale(1.08); }
  75%  { transform: scale(0.96); }
  100% { transform: scale(1); }
}

.sv-btn--pop {
  animation: btn-pop 0.36s ease;
}

/* ---------- long press: shake ---------- */
@keyframes btn-shake {
  0%   { transform: translateX(0); }
  12%  { transform: translateX(-3px); }
  25%  { transform: translateX(3px); }
  37%  { transform: translateX(-2.5px); }
  50%  { transform: translateX(2.5px); }
  62%  { transform: translateX(-1.5px); }
  75%  { transform: translateX(1.5px); }
  87%  { transform: translateX(-0.5px); }
  100% { transform: translateX(0); }
}

.sv-btn--shaking {
  animation: btn-shake 0.32s ease-in-out infinite;
}

/* cancelling: pointer left button during long press */
.sv-btn--cancelling {
  filter: brightness(0.5) saturate(0.25);
  transition: filter 0.25s ease;
}

.sv-btn:focus-visible {
  box-shadow: 0 0 0 3px rgb(var(--md-sys-color-error) / .35);
}

.sv-btn:disabled {
  opacity: 0.38;
  cursor: not-allowed;
}

.sv-btn:disabled::before,
.sv-btn:disabled .sv-btn__icon,
.sv-btn:disabled .sv-btn__bubble {
  display: none;
}

/* Sizes */
.sv-btn--sm {
  padding: 6px 18px;
  font-size: 13px;
  border-radius: 999px;
}

.sv-btn--md {
  padding: 10px 26px;
  font-size: 15px;
  border-radius: 999px;
}

.sv-btn--lg {
  padding: 14px 34px;
  font-size: 17px;
  border-radius: 999px;
}

/* ---------- MD3 Error palette (theme variables) ---------- */
.sv-btn--primary {
  color: rgb(var(--md-sys-color-on-error-container));
  background: rgb(var(--md-sys-color-error-container));
  border-color: rgb(var(--md-sys-color-error-container));
}

.sv-btn--primary:hover {
  filter: brightness(.92);
  box-shadow: 0 1px 3px rgb(var(--md-sys-color-shadow) / .12);
}

.sv-btn--primary:active {
  filter: brightness(.85);
  box-shadow: none;
}

/* Outline */
.sv-btn--outline {
  color: rgb(var(--md-sys-color-error));
  background: transparent;
  border-color: rgb(var(--md-sys-color-error));
}

.sv-btn--outline:hover {
  color: rgb(var(--md-sys-color-on-error-container));
  background: rgb(var(--md-sys-color-error-container));
  border-color: rgb(var(--md-sys-color-error-container));
  box-shadow: 0 1px 3px rgb(var(--md-sys-color-shadow) / .1);
}

.sv-btn--outline:active {
  filter: brightness(.92);
  box-shadow: none;
}

/* Ghost */
.sv-btn--ghost {
  color: rgb(var(--md-sys-color-error));
  background: transparent;
  border-color: transparent;
}

.sv-btn--ghost:hover {
  background: rgb(var(--md-sys-color-error) / .08);
}

.sv-btn--ghost:active {
  background: rgb(var(--md-sys-color-error) / .14);
}
</style>

<!-- global (unscoped) warning overlay styles -->
<style>
.warning-enter-active {
  transition: opacity 0.3s ease;
}

.warning-enter-from {
  opacity: 0;
}

.warning-leave-active {
  transition: opacity 0.45s ease;
}

.warning-leave-to {
  opacity: 0;
}

.sv-warning {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9999;
  border: 10px solid transparent;
  border-image: repeating-linear-gradient(
    -45deg,
    #fdd835 0px,
    #fdd835 12px,
    #212121 12px,
    #212121 24px
  ) 10;
}

.sv-warning__hint {
  position: fixed;
  bottom: 36px;
  left: 50%;
  transform: translateX(-50%);
  margin: 0;
  padding: 6px 18px;
  color: #212121;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.6px;
  background: #fdd835;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
  animation: hint-in 0.4s 0.15s ease both;
}

@keyframes hint-in {
  from { opacity: 0; transform: translateX(-50%) translateY(10px); }
  to   { opacity: 1; transform: translateX(-50%) translateY(0); }
}
</style>
