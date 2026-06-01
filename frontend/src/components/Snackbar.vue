<template>
  <div
    v-if="visible"
    class="snackbar"
    role="alert"
    @mouseenter="pauseTimer"
    @mouseleave="resumeTimer"
  >
    <span class="snackbar__message">{{ message }}</span>
    <button v-if="actionText" class="snackbar__action" @click="onAction">{{ actionText }}</button>
    <button class="snackbar__close" @click="dismiss" aria-label="关闭">&times;</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';

const props = withDefaults(defineProps<{
  message: string;
  actionText?: string;
  duration?: number;
}>(), {
  actionText: '',
  duration: 3000,
});

const emit = defineEmits<{
  close: [];
  action: [];
}>();

const visible = ref(true);
let timer: ReturnType<typeof setTimeout> | null = null;

function dismiss() {
  visible.value = false;
  clearTimer();
  emit('close');
}

function onAction() {
  visible.value = false;
  clearTimer();
  emit('action');
}

function clearTimer() {
  if (timer) { clearTimeout(timer); timer = null; }
}

function pauseTimer() {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
}

function resumeTimer() {
  if (!timer && props.duration > 0) {
    timer = setTimeout(dismiss, props.duration);
  }
}

onMounted(() => {
  if (props.duration > 0) {
    timer = setTimeout(dismiss, props.duration);
  }
});

onBeforeUnmount(() => clearTimer());
</script>

<style lang="scss" scoped>
.snackbar {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 12px;
  width: fit-content;
  max-width: 90%;
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 4px;
  padding: 8px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
  font-size: 14px;
  color: #856404;
}

.snackbar__message {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.snackbar__action {
  flex-shrink: 0;
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  background: rgba(0, 0, 0, .08);
  color: inherit;
  font: 500 12px/16px $font-family;
  cursor: pointer;
  transition: background .15s;

  &:hover {
    background: rgba(0, 0, 0, .14);
  }
}

.snackbar__close {
  flex-shrink: 0;
  margin-left: auto;
  width: 20px; height: 20px;
  border: none; border-radius: 4px;
  background: transparent;
  color: #999;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color .15s;

  &:hover { color: #333; }
}
</style>
