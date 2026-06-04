<template>
  <div class="page submit-page">
    <AppNotice
      v-if="notice.visible"
      :message="notice.message"
      :type="notice.type"
      @close="notice.visible = false"
    />
    <div class="page-header">
      <h1>作业提交</h1>
    </div>

    <div class="card submit-card">
      <div class="card__header"><h2>提交新作业</h2></div>
      <div class="card__body">
        <el-form :model="form" label-position="top" class="form-grid form-grid--2" @submit.prevent="handleSubmit">
          <el-form-item label="学生" required>
            <el-select v-model="form.studentId" placeholder="请选择学生" style="width:100%;">
              <el-option
                v-for="s in students"
                :key="s.id"
                :label="`${s.studentNo} - ${s.name}`"
                :value="s.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="作业类型">
            <el-select v-model="form.workType" style="width:100%;">
              <el-option v-for="t in workTypes" :key="t" :label="t" :value="t" />
            </el-select>
          </el-form-item>

          <el-form-item label="作业标题" class="form-full">
            <el-input v-model="form.title" placeholder="如：第二阶段实训报告" />
          </el-form-item>

          <el-form-item label="备注" class="form-full">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可填写作业说明" />
          </el-form-item>

          <!-- Upload area -->
          <div class="form-full">
            <div class="dropzone" :class="dropzoneClass">
              <template v-if="status === 'uploading'">
                <el-progress :percentage="progress" :stroke-width="6" style="max-width:300px;margin:0 auto 12px;" />
                <div class="dropzone__text">上传中...</div>
              </template>
              <template v-else>
                <div class="dropzone__icon">{{ statusIcon }}</div>
                <div class="dropzone__text" :style="{ color: statusColor }">{{ statusText }}</div>
                <div v-if="file && status === 'idle'" class="dropzone__fileinfo">
                  已选择：{{ file.name }} ({{ (file.size / 1024).toFixed(1) }} KB)
                </div>
                <el-upload :auto-upload="false" :show-file-list="false" :on-change="onFileChange" accept="*">
                  <el-button type="primary" style="margin-top:8px;">选择文件</el-button>
                </el-upload>
              </template>
            </div>
          </div>

          <div class="form-full">
            <el-button type="primary" size="large" :disabled="status === 'uploading' || !file" style="width:100%;" @click="handleSubmit">
              {{ status === 'uploading' ? '上传中...' : '提交作业' }}
            </el-button>
          </div>
        </el-form>

        <div v-if="uploadedSubmission" class="upload-result">
          <div class="upload-result__title">提交结果</div>
          <div class="upload-result__grid">
            <span>文件名</span><strong>{{ uploadedSubmission.fileName }}</strong>
            <span>文件大小</span><strong>{{ formatFileSize(uploadedSubmission.fileSize) }}</strong>
            <span>保存路径</span><strong>{{ uploadedSubmission.filePath || '未返回' }}</strong>
            <span>预处理状态</span><strong :class="preprocessClass">{{ preprocessText }}</strong>
          </div>
          <div v-if="uploadedSubmission.preprocessMessage" class="upload-result__message">
            {{ uploadedSubmission.preprocessMessage }}
          </div>
          <ul v-if="preprocessWarnings.length" class="upload-result__warnings">
            <li v-for="(warning, index) in preprocessWarnings" :key="index">{{ warning }}</li>
          </ul>
        </div>
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
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { UploadFile } from 'element-plus';
import { get, post } from '@/utils/request';
import AppNotice from '@/components/AppNotice.vue';
import Snackbar from '@/components/Snackbar.vue';
import type { Student, WorkSubmission } from '@/types';

const workTypes = ['代码压缩包', '实验报告', '截图材料', '其他'];

const students = ref<Student[]>([]);
const snackbar = ref<{ visible: boolean; message: string; actionText?: string; duration?: number }>({
  visible: false, message: '', actionText: '', duration: 3000,
});

function handleSnackbarAction() {
  snackbar.value.visible = false;
}
const notice = ref<{ visible: boolean; message: string; type: 'info' | 'success' | 'warning' | 'error' }>({
  visible: false, message: '', type: 'info',
});
const form = reactive({ studentId: '', title: '', workType: '代码压缩包', remark: '' });
const file = ref<File | null>(null);
const uploadedSubmission = ref<WorkSubmission | null>(null);
const status = ref<'idle' | 'uploading' | 'success' | 'error'>('idle');
const errorMsg = ref('');
const progress = ref(0);

const statusIcon = computed(() => status.value === 'success' ? '✅' : status.value === 'error' ? '❌' : '📄');
const statusColor = computed(() => status.value === 'success' ? '#16A34A' : status.value === 'error' ? '#DC2626' : '');
const statusText = computed(() => {
  if (status.value === 'success') return '上传成功';
  if (status.value === 'error') return `上传失败：${errorMsg.value}`;
  return '请上传作业文件';
});
const dropzoneClass = computed(() => ({
  'dropzone--error': status.value === 'error',
  'dropzone--success': status.value === 'success',
}));
const preprocessText = computed(() => {
  const current = uploadedSubmission.value;
  if (!current) return '';
  if (current.preprocessStatus === 'SUCCESS') return '预处理成功';
  if (current.preprocessStatus === 'FAILED') return '预处理失败';
  if (current.preprocessStatus === 'SKIPPED') return 'Py 预处理未启用';
  return current.preprocessStatus || '未返回';
});
const preprocessClass = computed(() => ({
  'text-success': uploadedSubmission.value?.preprocessStatus === 'SUCCESS',
  'text-warning': uploadedSubmission.value?.preprocessStatus === 'SKIPPED',
  'text-error': uploadedSubmission.value?.preprocessStatus === 'FAILED',
}));
const preprocessWarnings = computed(() => {
  const raw = uploadedSubmission.value?.preprocessResult;
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    return [
      ...(Array.isArray(parsed.renderWarnings) ? parsed.renderWarnings : []),
      ...(Array.isArray(parsed.warnings) ? parsed.warnings : []),
    ].filter(Boolean);
  } catch {
    return [];
  }
});

onMounted(async () => {
  try { students.value = await get<Student[]>('/students'); } catch { /* ignore */ }
});

function onFileChange(uploadFile: UploadFile) {
  file.value = uploadFile.raw || null;
  status.value = 'idle';
  errorMsg.value = '';
  uploadedSubmission.value = null;
}

function formatFileSize(size?: number) {
  if (!size) return '未返回';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

async function handleSubmit() {
  if (!file.value) { errorMsg.value = '请选择要上传的作业文件'; return; }
  if (!form.studentId) { errorMsg.value = '请选择学生'; return; }

  status.value = 'uploading';
  errorMsg.value = '';
  progress.value = 0;

  const timer = setInterval(() => { progress.value = Math.min(progress.value + 15, 85); }, 200);

  try {
    const fd = new FormData();
    fd.append('studentId', form.studentId);
    fd.append('title', form.title || '未命名作业');
    fd.append('workType', form.workType);
    if (form.remark) fd.append('remark', form.remark);
    fd.append('file', file.value);

    const result = await post<WorkSubmission>('/submissions/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    clearInterval(timer);
    progress.value = 100;
    status.value = 'success';
    uploadedSubmission.value = result;
    ElMessage.success('上传成功');
    form.studentId = ''; form.title = ''; form.remark = '';
    file.value = null;
  } catch (e: any) {
    clearInterval(timer);
    progress.value = 0;
    status.value = 'error';
    errorMsg.value = e.message || '上传失败';
  }
}
</script>

<style lang="scss" scoped>
.submit-page .page-header,
.submit-card {
  width: 100%;
  max-width: 640px;
  margin-left: auto;
  margin-right: auto;
}

.submit-card { margin-bottom: $space-6; }
.form-full { grid-column: 1 / -1; }
.text-success { color: $success; }
.text-warning { color: #B7791F; }
.text-error { color: $error; }

.dropzone {
  border: 2px dashed $outline-variant;
  border-radius: var(--shape-md);
  padding: 32px 24px;
  text-align: center;
  transition: border-color .15s;

  &--error { border-color: $error; }
  &--success { border-color: $success; }

  &__icon { font-size: 36px; margin-bottom: 8px; }
  &__text {
    margin-bottom: 8px;
    font: 400 14px/20px $font-family;
    color: $on-surface-variant;
  }
  &__fileinfo { color: $on-surface; font-size: 13px; margin-bottom: 8px; }
}

.upload-result {
  margin-top: $space-4;
  padding: $space-4;
  border: 1px solid $outline-variant;
  border-radius: $shape-md;
  background: #FAFDF8;

  &__title {
    margin-bottom: $space-3;
    font: 600 16px/24px $font-family;
    color: $on-surface;
  }

  &__grid {
    display: grid;
    grid-template-columns: 88px minmax(0, 1fr);
    gap: 8px 12px;
    font: 400 13px/20px $font-family;

    span { color: $on-surface-variant; }
    strong {
      min-width: 0;
      overflow-wrap: anywhere;
      color: $on-surface;
    }
  }

  &__message {
    margin-top: $space-3;
    color: $on-surface-variant;
    font: 400 13px/20px $font-family;
  }

  &__warnings {
    margin: $space-3 0 0;
    padding-left: 18px;
    color: #92400E;
    font: 400 13px/20px $font-family;
  }
}
</style>
