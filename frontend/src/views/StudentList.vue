<template>
  <div class="page">
    <div class="page-header">
      <h1>学生管理</h1>
      <div class="page-header__actions">
        <el-button type="primary" @click="openAddDialog">新增学生</el-button>
        <el-button @click="resetDemoData" :loading="resetting">重置数据</el-button>
      </div>
    </div>

    <div class="card">
      <div class="card__header">
        <h2>学生列表</h2>
        <el-input
          v-model="keyword"
          placeholder="搜索学号或姓名..."
          style="width: 220px;"
          clearable
          @input="doSearch"
          @keydown.enter="doSearch"
        />
        <span class="count-text">共 {{ totalElements }} 人</span>
      </div>
      <div class="card__body" style="padding: 0;">
        <div v-if="loading" class="empty-state">
          <div class="spinner" />
          <div class="empty-state__text" style="margin-top:12px;">加载中...</div>
        </div>
        <div v-else-if="error" class="empty-state">
          <div class="empty-state__icon">&#9888;</div>
          <div class="empty-state__text">{{ error }}</div>
          <el-button style="margin-top:12px;" @click="fetchStudents(page)">重试</el-button>
        </div>
        <div v-else-if="!students.length" class="empty-state">
          <div class="empty-state__icon">&#128100;</div>
          <div class="empty-state__text">暂无学生，请先新增</div>
        </div>
        <template v-else>
          <table class="table">
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>班级</th>
                <th style="width:120px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in students" :key="s.id">
                <td class="mono">{{ s.studentNo }}</td>
                <td>{{ s.name }}</td>
                <td><span class="chip">{{ s.className }}</span></td>
                <td>
                  <div class="action-cell">
                    <router-link
                      class="btn btn--tonal btn--sm"
                      :to="`/evaluation/0?studentId=${s.id}&studentName=${encodeURIComponent(s.name)}`"
                    >评价</router-link>
                    <el-popconfirm
                      title="确定删除该学生吗？"
                      confirm-button-text="删除"
                      cancel-button-text="取消"
                      @confirm="deleteStudent(s.id)"
                    >
                      <template #reference>
                        <button class="btn btn--text btn--danger btn--sm">删除</button>
                      </template>
                    </el-popconfirm>
                  </div>
                </td>
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

    <!-- Add Student Dialog -->
    <el-dialog v-model="dialogVisible" title="新增学生" width="420px" @closed="resetForm">
      <el-form :model="addForm" label-position="top" @submit.prevent="handleAdd">
        <el-form-item label="学号" required>
          <el-input v-model="addForm.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="addForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="班级" required>
          <el-input v-model="addForm.className" placeholder="请输入班级" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="handleAdd">确认新增</el-button>
      </template>
    </el-dialog>

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
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { get, post, del } from '@/utils/request';
import Snackbar from '@/components/Snackbar.vue';
import type { Student, StudentPageResponse } from '@/types';

const PAGE_SIZE = 10;

const students = ref<Student[]>([]);
const page = ref(0);
const totalElements = ref(0);
const keyword = ref('');
const loading = ref(false);
const error = ref<string | null>(null);
const dialogVisible = ref(false);
const adding = ref(false);
const resetting = ref(false);
const snackbar = ref<{ visible: boolean; message: string; actionText?: string; duration?: number }>({
  visible: false, message: '', actionText: '', duration: 3000,
});

const addForm = reactive({ studentNo: '', name: '', className: '' });

const pagePlusOne = computed({
  get: () => page.value + 1,
  set: () => {},
});

function handleSnackbarAction() { snackbar.value.visible = false; }

async function fetchStudents(p?: number) {
  loading.value = true;
  error.value = null;
  try {
    const params = new URLSearchParams({ page: String(p ?? page.value), size: String(PAGE_SIZE) });
    if (keyword.value) params.set('keyword', keyword.value);
    const data = await get<StudentPageResponse>(`/students/page?${params}`);
    students.value = data.content || [];
    page.value = data.page;
    totalElements.value = data.totalElements;
  } catch (e: any) {
    error.value = e.message || '加载失败';
    ElMessage.error(error.value ?? '加载失败');
  } finally {
    loading.value = false;
  }
}

let searchTimer: ReturnType<typeof setTimeout> | null = null;
function doSearch() {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = setTimeout(() => fetchStudents(0), 200);
}

function onPageChange(p: number) { fetchStudents(p - 1); }

function openAddDialog() { dialogVisible.value = true; }
function resetForm() { addForm.studentNo = ''; addForm.name = ''; addForm.className = ''; }

async function handleAdd() {
  if (!addForm.studentNo.trim() || !addForm.name.trim() || !addForm.className.trim()) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  adding.value = true;
  try {
    const created = await post<Student>('/students', { ...addForm });
    ElMessage.success('已新增学生：' + created.name);
    dialogVisible.value = false;
    await fetchStudents(page.value);
  } catch (e: any) { ElMessage.error(e.message); }
  finally { adding.value = false; }
}

async function deleteStudent(id: number) {
  try {
    await del(`/students/${id}`);
    ElMessage.success('已删除学生');
    await fetchStudents(page.value);
  } catch (e: any) { ElMessage.error(e.message); }
}

async function resetDemoData() {
  resetting.value = true;
  try {
    await post('/dev/reset-demo-data');
    ElMessage.success('数据已重置');
    await fetchStudents(0);
  } catch (e: any) { ElMessage.error(e.message); }
  finally { resetting.value = false; }
}

onMounted(() => fetchStudents(0));
</script>

<style lang="scss" scoped>
.page-header__actions { display: flex; gap: $space-2; }
.count-text {
  @include font(14px, 20px);
  color: #999;
  flex-shrink: 0;
}
.mono { font-family: monospace; }
.chip {
  display: inline-block;
  padding: 2px 10px;
  border-radius: $shape-full;
  background: $surface-container-low;
  color: $on-surface-variant;
  @include font(12px, 20px);
}
.action-cell {
  display: flex;
  gap: $space-2;
  align-items: center;
}
.btn--danger {
  color: $error !important;
  &:hover { background: $error-container !important; }
}
.pagination-wrap { display: flex; justify-content: center; padding: 16px 0; }
</style>
