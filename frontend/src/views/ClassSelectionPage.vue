<template>
  <div class="page">
    <div class="page-header">
      <h1>选择班级</h1>
      <el-button text @click="handleLogout">退出登录</el-button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="empty-state">
      <div class="spinner" />
      <div class="empty-state__text" style="margin-top:12px;">加载班级列表...</div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="empty-state">
      <div class="empty-state__icon">&#9888;</div>
      <div class="empty-state__text">{{ error }}</div>
      <el-button style="margin-top:12px;" @click="fetchClasses">重试</el-button>
    </div>

    <!-- Empty -->
    <div v-else-if="!classes.length" class="empty-state">
      <div class="empty-state__icon">&#128218;</div>
      <div class="empty-state__text">暂无可访问的班级</div>
      <div style="color:var(--on-surface-variant);font-size:13px;margin-top:4px;">请联系管理员分配班级权限</div>
      <el-button style="margin-top:12px;" @click="fetchClasses">刷新</el-button>
    </div>

    <!-- Class grid -->
    <div v-else class="class-grid">
      <div
        v-for="c in classes"
        :key="c.classId"
        class="class-card"
        role="button"
        tabindex="0"
        :aria-label="`进入${c.className}`"
        @click="selectClass(c)"
        @keydown.enter="selectClass(c)"
        @keydown.space.prevent="selectClass(c)"
      >
        <!-- Cover -->
        <div class="class-card__cover" :style="coverStyle(c)">
          <span v-if="!c.coverImage" class="class-card__cover-icon">&#128218;</span>
          <span v-if="c.unreadCount" class="class-card__badge">{{ c.unreadCount > 99 ? '99+' : c.unreadCount }}</span>
        </div>

        <!-- Info -->
        <div class="class-card__body">
          <div class="class-card__name">{{ c.className }}</div>
          <div class="class-card__meta">
            <span v-if="c.studentCount != null">{{ c.studentCount }} 人</span>
            <span v-if="c.teacherName">{{ c.teacherName }}</span>
          </div>
          <div v-if="c.lastAccessed" class="class-card__last">
            最近访问：{{ fmt(c.lastAccessed) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { get } from '@/utils/request';
import { clearAuthCookies } from '@/utils/cookie';

interface ClassInfo {
  classId: string;
  className: string;
  classCode?: string;
  studentCount?: number;
  teacherName?: string;
  coverImage?: string;
  status: 'active' | 'inactive' | 'archived';
  lastAccessed?: string;
  unreadCount?: number;
}

const router = useRouter();
const classes = ref<ClassInfo[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const COVER_COLORS = ['#DBE4FF', '#FEE2E2', '#DCFCE7', '#FEF3C7', '#E8F2FF', '#CCFBF1'];

function coverStyle(c: ClassInfo) {
  if (c.coverImage) return { backgroundImage: `url(${c.coverImage})`, backgroundSize: 'cover' };
  return { background: COVER_COLORS[Math.abs(hashCode(c.classId)) % COVER_COLORS.length] };
}

function hashCode(s: string): number {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
  return h;
}

function fmt(ts: string) {
  if (!ts) return '';
  const d = new Date(ts);
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
}

function selectClass(c: ClassInfo) {
  try { localStorage.setItem('lastAccessedClassId', c.classId); } catch { /* ignore */ }
  router.push(`/class/${c.classId}/detail`);
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出系统吗？', '退出确认', {
      confirmButtonText: '确认退出', cancelButtonText: '取消', type: 'warning',
    });
  } catch { return; }
  clearAuthCookies();
  router.replace('/login');
}

async function fetchClasses() {
  loading.value = true;
  error.value = null;
  try {
    const data = await get<{ classes: ClassInfo[] }>('/user/classes');
    classes.value = (data?.classes || []).filter(c => c.status !== 'archived');
  } catch (e: any) {
    if (e.response?.status === 401) {
      router.replace('/login');
      return;
    }
    error.value = e.message || '加载失败，请检查网络连接';
  } finally {
    loading.value = false;
  }
}

onMounted(fetchClasses);
</script>

<style lang="scss" scoped>
.class-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.class-card {
  background: $surface-bright;
  border: 1px solid $outline-variant;
  border-radius: $shape-lg;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow .2s ease, transform .2s ease;
  outline: none;

  &:hover, &:focus-visible {
    box-shadow: $elevation-2;
    transform: translateY(-2px);
  }
  &:focus-visible { box-shadow: 0 0 0 3px rgba(26,86,219,.3); }

  &__cover {
    position: relative;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  &__cover-icon {
    font-size: 40px;
    opacity: .6;
  }
  &__badge {
    position: absolute;
    top: 8px; right: 8px;
    min-width: 20px; height: 20px;
    padding: 0 6px;
    border-radius: 10px;
    background: $error;
    color: $on-error;
    font: 500 11px/20px $font-family;
    text-align: center;
  }
  &__body {
    padding: var(--space-3) var(--space-4);
  }
  &__name {
    font: 500 15px/22px $font-family;
    color: $on-surface;
    margin-bottom: 4px;
  }
  &__meta {
    display: flex;
    gap: var(--space-3);
    font: 400 12px/16px $font-family;
    color: $on-surface-variant;
    margin-bottom: 4px;
  }
  &__last {
    font: 400 11px/16px $font-family;
    color: $outline;
  }
}

@media (max-width: 768px) {
  .class-grid { grid-template-columns: 1fr; }
}
</style>
