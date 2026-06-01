<template>
  <div class="page">
    <AppNotice v-if="notice.visible" :message="notice.message" :type="notice.type" @close="notice.visible = false" />
    <div class="page-header"><h1>导出成绩</h1></div>

    <!-- Stats -->
    <div class="stats-grid" v-if="!statsLoading">
      <div class="card stat-card"><div class="stat-value">{{ stats.studentCount }}</div><div class="stat-label">学生总数</div></div>
      <div class="card stat-card"><div class="stat-value">{{ stats.submissionCount }}</div><div class="stat-label">作业提交数</div></div>
      <div class="card stat-card"><div class="stat-value">{{ stats.aiEvaluatedCount }}</div><div class="stat-label">AI 已评价</div></div>
      <div class="card stat-card"><div class="stat-value">{{ stats.teacherConfirmedCount }}</div><div class="stat-label">教师已确认</div></div>
      <div class="card stat-card"><div class="stat-value">{{ stats.avgTeacherScore ?? '--' }}</div><div class="stat-label">教师平均分</div></div>
    </div>
    <div v-else class="empty-state"><div class="spinner" /></div>

    <div class="card" style="margin-top:16px;">
      <div class="card__header"><h2>数据导出</h2></div>
      <div class="card__body" style="text-align:center;">
        <el-button type="primary" size="large" :loading="exporting" @click="doExport">
          {{ exporting ? '导出中...' : '导出 Excel' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { get } from '@/utils/request';
import AppNotice from '@/components/AppNotice.vue';

const stats = reactive({ studentCount: 0, submissionCount: 0, aiEvaluatedCount: 0, teacherConfirmedCount: 0, avgTeacherScore: null as number | null });
const statsLoading = ref(true);
const exporting = ref(false);
const notice = ref<{ visible: boolean; message: string; type: 'info' | 'success' | 'warning' | 'error' }>({ visible: false, message: '', type: 'info' });

onMounted(async () => {
  try {
    const data = await get<typeof stats>('/statistics/summary');
    Object.assign(stats, data);
  } catch { /* ignore */ }
  finally { statsLoading.value = false; }
});

async function doExport() {
  exporting.value = true;
  try {
    const resp = await fetch('/api/export/excel', { method: 'POST' });
    if (!resp.ok) throw new Error('导出失败');
    const blob = await resp.blob();
    const disposition = resp.headers.get('content-disposition') || '';
    const match = disposition.match(/filename\*=UTF-8''(.+)/);
    const filename = match ? decodeURIComponent(match[1]) : 'export.xlsx';
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename;
    document.body.appendChild(a); a.click();
    document.body.removeChild(a); URL.revokeObjectURL(url);
    ElMessage.success('导出成功');
  } catch (e: any) { ElMessage.error(e.message); }
  finally { exporting.value = false; }
}
</script>

<style lang="scss" scoped>
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: $space-4; margin-bottom: $space-4; }
.stat-card { text-align: center; padding: 20px 12px; }
.stat-value { font: 700 32px/1.2 $font-family; color: $primary; }
.stat-label { font: 400 13px/20px $font-family; color: $on-surface-variant; margin-top: 4px; }
</style>
