<template>
  <div class="page">
    <AppNotice v-if="notice.visible" :message="notice.message" :type="notice.type" @close="notice.visible = false" />
    <div class="page-header"><h1>作业提交记录</h1></div>
    <div class="card">
      <div class="card__body" style="padding:0;">
        <div v-if="loading" class="empty-state"><div class="spinner" /><div class="empty-state__text" style="margin-top:12px;">加载中...</div></div>
        <div v-else-if="error" class="empty-state"><div class="empty-state__icon">&#9888;</div><div class="empty-state__text">{{ error }}</div></div>
        <div v-else-if="!submissions.length" class="empty-state"><div class="empty-state__icon">&#128196;</div><div class="empty-state__text">暂无作业提交记录</div></div>
        <table v-else class="table">
          <thead><tr><th>学生</th><th>作业标题</th><th>文件名</th><th>类型</th><th>提交时间</th><th style="width:180px;">操作</th></tr></thead>
          <tbody>
            <tr v-for="s in submissions" :key="s.id">
              <td>{{ s.studentName }}</td>
              <td>{{ s.title }}</td>
              <td>{{ s.fileName }}</td>
              <td>{{ s.workType }}</td>
              <td>{{ formatDate(s.submittedAt) }}</td>
              <td class="action-cell">
                <router-link class="btn btn--tonal btn--sm" :to="`/evaluation/${s.id}?studentId=${s.studentId}&studentName=${encodeURIComponent(s.studentName)}&fileName=${encodeURIComponent(s.fileName)}`">评价</router-link>
                <button class="btn btn--text btn--sm" @click="doPreviewFile(s.id, s.fileName, s.contentType)">预览</button>
                <button class="btn btn--text btn--sm" @click="doDownloadFile(s.id, s.fileName)">下载</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
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
import { ref, onMounted } from 'vue';
import { get } from '@/utils/request';
import { useFileActions } from '@/composables/useFileActions';
import FilePreviewDialog from '@/components/FilePreviewDialog.vue';
import AppNotice from '@/components/AppNotice.vue';
import type { WorkSubmission } from '@/types';

const submissions = ref<WorkSubmission[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const notice = ref<{ visible: boolean; message: string; type: 'info' | 'success' | 'warning' | 'error' }>({ visible: false, message: '', type: 'info' });

const { previewVisible, previewContent, previewFileName, previewLoading, previewError, downloadFile, previewFile, closePreview } = useFileActions();
const previewFileId = ref(0);
function doPreviewFile(id: number, fileName: string, contentType?: string) { previewFileId.value = id; previewFile(id, fileName, contentType); }
function doDownloadFile(id: number, fileName: string) { downloadFile(id, fileName); }

function formatDate(d: string) { return (d || '').replace('T', ' ').slice(0, 16); }

onMounted(async () => {
  try { submissions.value = await get<WorkSubmission[]>('/submissions'); }
  catch (e: any) { error.value = e.message || '加载失败'; }
  finally { loading.value = false; }
});
</script>

<style lang="scss" scoped>
.action-cell { display: flex; gap: 4px; flex-wrap: nowrap; }
</style>
