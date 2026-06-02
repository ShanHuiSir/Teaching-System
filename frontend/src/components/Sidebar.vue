<template>
  <aside class="sidebar" :class="{ 'sidebar--collapsed': collapsed }">
    <div class="sidebar__header">
      <span v-if="!collapsed" class="sidebar__brand">教学评价系统</span>
      <button class="sidebar__toggle" @click="toggle">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
          <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
        </svg>
      </button>
    </div>

    <nav class="sidebar__nav">
      <!-- Status group -->
      <div class="status-group">
        <span v-if="!collapsed" class="status-group__hint">作业状态</span>
        <router-link
          to="/assignments/pending"
          class="nav-item nav-item--pending"
          :class="{ active: isActive('pending') }"
        >
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M17 12c-2.8 0-5 2.2-5 5s2.2 5 5 5 5-2.2 5-5-2.2-5-5-5zm1 3.8h-1.2v-2.5h-1.2v3.7h2.4v-1.2zM14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h4v-2H6V4h7v5h5v3h2V8l-6-6z"/></svg>
          <span>未审批</span>
          <span class="nav-item__badge">{{ stats.unapproved }}</span>
        </router-link>
        <router-link
          to="/assignments/ai-reviewed"
          class="nav-item nav-item--ai"
          :class="{ active: isActive('ai-reviewed') }"
        >
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M9 11.75c-.69 0-1.25.56-1.25 1.25s.56 1.25 1.25 1.25 1.25-.56 1.25-1.25-.56-1.25-1.25-1.25zm6 0c-.69 0-1.25.56-1.25 1.25s.56 1.25 1.25 1.25 1.25-.56 1.25-1.25-.56-1.25-1.25-1.25zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-12.5c-2.67 0-4.85 2.03-4.85 4.5h1.8c0-1.66 1.34-3 3.05-3 1.66 0 3.05 1.32 3.05 3h1.8c0-2.47-2.18-4.5-4.85-4.5z"/></svg>
          <span>AI 已审批</span>
          <span class="nav-item__badge">{{ stats.aiReviewed }}</span>
        </router-link>
        <router-link
          to="/assignments/completed"
          class="nav-item nav-item--done"
          :class="{ active: isActive('completed') }"
        >
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
          <span>已完成</span>
          <span class="nav-item__badge">{{ stats.completed }}</span>
        </router-link>
      </div>

      <div class="sidebar-divider" />

      <!-- Functional nav -->
      <router-link to="/students" class="nav-item nav-item--func" :class="{ active: isActive('students') }">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
        <span>学生管理</span>
      </router-link>
      <router-link to="/submit" class="nav-item nav-item--func" :class="{ active: isActive('submit') }">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M9 16h6v-6h4l-7-7-7 7h4v6zm-4 2h14v2H5v-2z"/></svg>
        <span>作业提交</span>
      </router-link>
      <router-link to="/export" class="nav-item nav-item--func" :class="{ active: isActive('export') }">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
        <span>导出成绩</span>
      </router-link>
    </nav>

    <div class="sidebar__actions">
      <button class="sidebar__logout" @click="handleLogout">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/></svg>
        <span>退出</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useRoute, useRouter } from 'vue-router';
import { useSubmissionStore } from '@/stores/submission';

const route = useRoute();
const router = useRouter();
const collapsed = ref(false);

const store = useSubmissionStore();
const { stats } = storeToRefs(store);

function isActive(name: string) {
  return route.path.includes(name);
}

function toggle() {
  collapsed.value = !collapsed.value;
  document.body.classList.toggle('has-sidebar--collapsed', collapsed.value);
}

function handleLogout() {
  router.push('/');
}

onMounted(() => {
  store.fetchAll();
});
</script>

<style lang="scss" scoped>
.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 240px;
  display: flex;
  flex-direction: column;
  background: $surface-bright;
  border-right: 1px solid $outline-variant;
  z-index: 200;
  transition: width .25s ease;
  overflow: hidden;

  &--collapsed { width: 64px; }
}

.sidebar__header {
  display: flex;
  align-items: center;
  gap: $space-3;
  height: 56px;
  padding: 0 $space-3 0 $space-4;
  border-bottom: 1px solid $outline-variant;
  flex-shrink: 0;
}

.sidebar__brand {
  font: 500 16px/24px $font-family;
  color: $on-surface;
  white-space: nowrap;
}

.sidebar__toggle {
  width: 36px; height: 36px;
  margin-left: auto;
  border: none;
  border-radius: $shape-full;
  background: transparent;
  color: $on-surface-variant;
  cursor: pointer;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  &:hover { background: $surface-container-highest; }
}

.sidebar__nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $space-1;
  padding: $space-3;
  overflow-y: auto;
}

.status-group {
  background: #FAFBFC;
  border-top: 1px solid #E0E0E0;
  border-bottom: 1px solid #E0E0E0;
  margin: 8px 0;
  padding: 8px 0;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}
.status-group__hint {
  font: 500 14px/1.5 $font-family;
  color: #757575;
  padding: 0 16px 10px;
  margin: 0 8px 8px;
  border-bottom: 1px solid #E0E0E0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  height: 48px;
  padding: 0 $space-4;
  border-radius: 0 $shape-full $shape-full 0;
  font: 500 14px/20px $font-family;
  color: #6B7280;
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  flex-shrink: 0;
  transition: all .25s ease;

  svg { width: 20px; height: 20px; flex-shrink: 0; }

  &--pending {
    height: 36px; padding: 0 16px;
    border-radius: 20px; color: #C62828; background: transparent;
    &:hover { background: rgba(198, 40, 40, .06); }
    &.active { background: #FFE8E8; color: #C62828; }
  }
  &--ai {
    height: 36px; padding: 0 16px;
    border-radius: 20px; color: #1976D2; background: transparent;
    &:hover { background: rgba(25, 118, 210, .06); }
    &.active { background: #E8F2FF; color: #1976D2; }
  }
  &--done {
    height: 36px; padding: 0 16px;
    border-radius: 20px; color: #2E7D32; background: transparent;
    &:hover { background: rgba(46, 125, 50, .06); }
    &.active { background: #E8F5E9; color: #2E7D32; }
  }
  &--func {
    background: #F8FAFD; color: #333;
    border-radius: 16px; border: 1px solid #E0E4EB;
    height: 38px; padding: 0 16px; gap: 8px;
    &:hover { background: #F0F5FF; color: #1976D2; }
    &.active { background: #E3F2FD; color: #1976D2; border-color: #1976D2; }
  }

  &__badge {
    margin-left: auto;
    font: 500 12px/16px $font-family;
  }
}

.sidebar-divider {
  height: 1px;
  margin: $space-2 $space-3;
  background: #e5e7eb;
  flex-shrink: 0;
}

.sidebar__actions {
  display: flex;
  flex-direction: column;
  gap: $space-2;
  padding: $space-3;
  border-top: 1px solid $outline-variant;
}

.sidebar__logout {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 16px;
  border: 1px solid $outline;
  border-radius: $shape-full;
  background: transparent;
  color: $on-surface-variant;
  font: 500 14px/20px $font-family;
  cursor: pointer;
  transition: background .15s;
  &:hover { background: $surface-container-low; }
}

// Collapsed
.sidebar--collapsed {
  .sidebar__brand { display: none; }
  .sidebar__toggle { margin-left: 0; }
  .status-group { padding: 4px; gap: 4px; border-radius: 4px; }
  .status-group__hint { display: none; }
  .nav-item span, .nav-item__badge, .sidebar__logout span { display: none; }
  .nav-item {
    justify-content: center; padding: 0;
    width: 36px; height: 36px; border-radius: 50%; margin: 0 auto;
  }
  .nav-item--func { border: none; }
  .sidebar__logout { justify-content: center; padding: 0; width: 36px; }
}
</style>
