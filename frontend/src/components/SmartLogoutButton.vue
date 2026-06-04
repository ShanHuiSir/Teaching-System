<template>
  <button
    class="smart-logout"
    :aria-label="config.label"
    @click="handleClick"
  >
    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
      <path v-if="config.action === 'navigate_to'" d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
      <path v-else d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
    </svg>
    <span>{{ config.label }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { clearAuthCookies } from '@/utils/cookie';

const route = useRoute();
const router = useRouter();

interface ActionConfig {
  label: string;
  action: 'navigate_to' | 'logout';
  target: string;
  clearAuth: boolean;
}

const config = computed<ActionConfig>(() => {
  const name = route.name as string;
  if (name === 'ClassDetail') {
    return { label: '返回班级管理', action: 'navigate_to', target: '/class-selection', clearAuth: false };
  }
  if (name === 'ClassSelection') {
    return { label: '退出系统', action: 'logout', target: '/login', clearAuth: true };
  }
  // Fallback: all other authenticated pages
  return { label: '退出系统', action: 'logout', target: '/login', clearAuth: true };
});

function handleClick() {
  if (config.value.clearAuth) {
    clearAuthCookies();
    localStorage.removeItem('lastAccessedClassId');
  }
  router.replace(config.value.target);
}
</script>

<style lang="scss" scoped>
.smart-logout {
  position: fixed;
  bottom: 20px;
  left: 20px;
  z-index: 300;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1px solid $outline-variant;
  border-radius: $shape-sm;
  background: $surface-bright;
  color: #606266;
  font-size: 14px;
  font-family: $font-family;
  cursor: pointer;
  transition: color .15s, border-color .15s, box-shadow .15s;

  &:hover {
    color: #409EFF;
    border-color: #409EFF;
  }
  &:focus-visible {
    outline: none;
    box-shadow: 0 0 0 3px rgba(64, 158, 255, .3);
  }
}
</style>
