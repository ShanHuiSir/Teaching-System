<template>
  <div class="page">
    <AppNotice
      v-if="notice.visible"
      :message="notice.message"
      :type="notice.type"
      @close="notice.visible = false"
    />
    <div class="page-header">
      <h1>学生管理</h1>
      <div class="search-row">
        <el-input
          v-model="keyword"
          placeholder="搜索学号或姓名..."
          style="width: 220px;"
          clearable
          @keydown.enter="doSearch"
        />
        <el-button type="primary" @click="doSearch">搜索</el-button>
      </div>
    </div>

    <div class="card">
      <div class="card__header">
        <h2>学生列表</h2>
        <span class="count-badge">共 {{ totalElements }} 人</span>
      </div>
      <div class="card__body" style="padding: 0;">
        <!-- Loading -->
        <div v-if="loading" class="empty-state">
          <div class="spinner" />
          <div class="empty-state__text" style="margin-top:12px;">加载中...</div>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="empty-state">
          <div class="empty-state__icon">&#9888;</div>
          <div class="empty-state__text">{{ error }}</div>
          <el-button style="margin-top:12px;" @click="fetchStudents(page)">重试</el-button>
        </div>

        <!-- Empty -->
        <div v-else-if="!students.length" class="empty-state">
          <div class="empty-state__icon">&#128203;</div>
          <div class="empty-state__text">暂无学生数据</div>
        </div>

        <!-- Data -->
        <template v-else>
          <table class="table">
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>班级</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in students" :key="s.id">
                <td class="mono">{{ s.studentNo }}</td>
                <td>{{ s.name }}</td>
                <td>{{ s.className }}</td>
              </tr>
            </tbody>
          </table>
          <div class="pagination-wrap">
            <el-pagination
              v-model:current-page="pagePlusOne"
              :page-size="PAGE_SIZE"
              :total="totalElements"
              layout="prev, pager, next, total"
              @current-change="onPageChange"
            />
          </div>
        </template>
      </div>
    </div>
    <Snackbar
      v-if="snackbar.visible"
      :message="snackbar.message"
      :action-text="snackbar.actionText"
      :duration="snackbar.duration"
      @close="snackbar.visible = false"
      @action="handleSnackbarAction"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { get } from '@/utils/request';
import AppNotice from '@/components/AppNotice.vue';
import Snackbar from '@/components/Snackbar.vue';
import type { Student, StudentPageResponse } from '@/types';

const PAGE_SIZE = 10;

const students = ref<Student[]>([]);
const page = ref(0);
const totalPages = ref(0);
const totalElements = ref(0);
const keyword = ref('');
const loading = ref(false);
const error = ref<string | null>(null);
const notice = ref<{ visible: boolean; message: string; type: 'info' | 'success' | 'warning' | 'error' }>({
  visible: false, message: '', type: 'info',
});
const snackbar = ref<{ visible: boolean; message: string; actionText?: string; duration?: number }>({
  visible: false, message: '', actionText: '', duration: 3000,
});

function handleSnackbarAction() {
  snackbar.value.visible = false;
}

const pagePlusOne = computed({
  get: () => page.value + 1,
  set: () => {},
});

async function fetchStudents(p?: number) {
  loading.value = true;
  error.value = null;
  try {
    const params = new URLSearchParams({ page: String(p ?? page.value), size: String(PAGE_SIZE) });
    if (keyword.value) params.set('keyword', keyword.value);
    const data = await get<StudentPageResponse>(`/students/page?${params}`);
    students.value = data.content || [];
    page.value = data.page;
    totalPages.value = data.totalPages;
    totalElements.value = data.totalElements;
  } catch (e: any) {
    error.value = e.message || '加载失败';
    ElMessage.error(error.value ?? '加载失败');
  } finally {
    loading.value = false;
  }
}

function doSearch() { fetchStudents(0); }
function onPageChange(p: number) { fetchStudents(p - 1); }

onMounted(() => fetchStudents(0));
</script>

<style lang="scss" scoped>
.search-row { display: flex; gap: 8px; align-items: center; }
.count-badge {
  font: 400 12px/16px $font-family;
  color: $on-surface-variant;
}
.mono { font-family: monospace; }
.pagination-wrap { display: flex; justify-content: center; padding: 16px 0; }
</style>
