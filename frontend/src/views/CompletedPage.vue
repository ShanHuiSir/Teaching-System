<template>
  <div class="page">
    <AppNotice v-if="notice.visible" :message="notice.message" :type="notice.type" @close="notice.visible = false" />
    <div class="page-header"><h1>已完成</h1><span :class="['status-text', error ? 'error-text' : '']">{{ statusMsg }}</span></div>
    <div class="card" style="margin-bottom:16px;">
      <div class="card__header"><h2>提交作业</h2></div>
      <div class="card__body">
        <el-form :model="form" label-position="top" class="form-grid form-grid--2" @submit.prevent="handleSubmit">
          <el-form-item label="学生"><el-select v-model="form.studentId" placeholder="请选择学生" style="width:100%;"><el-option v-for="s in students" :key="s.id" :label="`${s.studentNo} - ${s.name}`" :value="s.id" /></el-select></el-form-item>
          <el-form-item label="作业标题"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="作业文件名"><el-input v-model="form.fileName" /></el-form-item>
          <el-form-item label="作业类型"><el-select v-model="form.workType" style="width:100%;"><el-option v-for="t in workTypes" :key="t" :label="t" :value="t" /></el-select></el-form-item>
          <el-form-item label="备注" class="form-full"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
          <div class="form-full"><el-button type="primary" native-type="submit" :disabled="submitting">{{ submitting ? '保存中...' : '保存作业提交' }}</el-button></div>
        </el-form>
      </div>
    </div>
    <div class="card"><div class="card__body" style="padding:0;">
      <div v-if="loading" class="empty-state"><div class="spinner" /><div class="empty-state__text" style="margin-top:12px;">加载中...</div></div>
      <div v-else-if="!filtered.length" class="empty-state"><div class="empty-state__icon">&#9989;</div><div class="empty-state__text">暂无已完成作业</div></div>
      <table v-else class="table"><thead><tr><th>学生</th><th>作业标题</th><th>文件名</th><th>类型</th><th>教师评分</th><th>提交时间</th><th style="width:180px;">操作</th></tr></thead><tbody><tr v-for="s in filtered" :key="s.id"><td>{{ s.studentName }}</td><td>{{ s.title }}</td><td>{{ s.fileName }}</td><td>{{ s.workType }}</td><td>{{ evalMap[s.id]?.teacherScore ?? '--' }}</td><td>{{ fmt(s.submittedAt) }}</td><td class="action-cell"><router-link class="btn btn--tonal btn--sm" :to="evalLink(s)">查看</router-link><button class="btn btn--text btn--sm" @click="doPreviewFile(s.id, s.fileName, s.contentType)">预览</button><button class="btn btn--text btn--sm" @click="doDownloadFile(s.id, s.fileName)">下载</button></td></tr></tbody></table>
    </div></div>
    <FilePreviewDialog
      v-model="previewVisible"
      :file-name="previewFileName"
      :content="previewContent"
      :loading="previewLoading"
      :error="previewError"
      @closed="closePreview"
      @download="doDownloadFile(previewFileId, previewFileName)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useFileActions } from '@/composables/useFileActions';
import FilePreviewDialog from '@/components/FilePreviewDialog.vue';
import usePolling from '@/composables/usePolling';
import { storeToRefs } from 'pinia';
import { ElMessage } from 'element-plus';
import { get } from '@/utils/request';
import { useSubmissionStore } from '@/stores/submission';
import AppNotice from '@/components/AppNotice.vue';
import type { Student } from '@/types';

const workTypes = ['代码压缩包', '实验报告', '截图材料', '其他'];

const store = useSubmissionStore();
const { loading, evalMap } = storeToRefs(store);

const students = ref<Student[]>([]);
const filtered = computed(() => store.filteredByStatus(2));
const error = ref<string | null>(null);
const statusMsg = ref('加载中...');
const submitting = ref(false);

const form = reactive({ studentId: '', title: '第二天实训作业', fileName: 'student-work.zip', workType: '代码压缩包', remark: '' });
const notice = ref<{ visible: boolean; message: string; type: 'info' | 'success' | 'warning' | 'error' }>({ visible: false, message: '', type: 'info' });

const { previewVisible, previewContent, previewFileName, previewLoading, previewError, downloadFile, previewFile, closePreview } = useFileActions();
const previewFileId = ref(0);
function doPreviewFile(id: number, fileName: string, contentType?: string) { previewFileId.value = id; previewFile(id, fileName, contentType); }
function doDownloadFile(id: number, fileName: string) { downloadFile(id, fileName); }


function fmt(d: string) { return (d || '').replace('T', ' ').slice(0, 16); }
function evalLink(s: { id: number; studentId: number; studentName: string; fileName: string }) {
  return `/evaluation/${s.id}?studentId=${s.studentId}&studentName=${encodeURIComponent(s.studentName)}&fileName=${encodeURIComponent(s.fileName)}`;
}

async function loadData() {
  try {
    await store.fetchAll();
    statusMsg.value = `已加载 ${store.stats.completed} 条`;
  } catch (e: any) { error.value = e.message; ElMessage.error(e.message); }
}

async function handleSubmit() {
  submitting.value = true;
  try {
    await store.addSubmission({
      studentId: Number(form.studentId), title: form.title,
      fileName: form.fileName, workType: form.workType, remark: form.remark,
    });
    ElMessage.success('已保存');
    form.studentId = ''; form.title = '第二天实训作业'; form.remark = '';
    statusMsg.value = `已加载 ${store.stats.completed} 条`;
  } catch (e: any) { ElMessage.error(e.message); }
  finally { submitting.value = false; }
}

onMounted(async () => {
  try { students.value = await get<Student[]>('/students'); } catch { /* ignore */ }
  await loadData();
  usePolling(() => store.fetchAll(), { interval: 5000 });
});
</script>

<style lang="scss" scoped>
.form-full { grid-column: 1 / -1; }
.status-text { font: 400 13px/20px $font-family; color: $on-surface-variant; }
.error-text { color: $error !important; }
.action-cell { display: flex; gap: 4px; flex-wrap: nowrap; }
</style>
