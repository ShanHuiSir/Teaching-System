<template>
  <div class="page">
    <AppNotice v-if="notice.visible" :message="notice.message" :type="notice.type" @close="notice.visible = false" />

    <!-- Step 1: Student & submission info -->
    <div class="card section">
      <div class="card__header"><h2>学生与作业信息</h2></div>
      <div class="card__body">
        <div v-if="!submissionId" class="empty-state"><div class="empty-state__icon">📄</div><div class="empty-state__text">缺少作业提交 ID</div></div>
        <div v-else class="info-grid">
          <div class="info-item"><span class="info-label">学生姓名</span><span class="info-value">{{ studentName }}</span></div>
          <div class="info-item"><span class="info-label">学号</span><span class="info-value">{{ studentNo }}</span></div>
          <div class="info-item"><span class="info-label">班级</span><span class="info-value">{{ className }}</span></div>
          <div class="info-item"><span class="info-label">文件名</span><span class="info-value">{{ fileName }}</span></div>
        </div>
        <div v-if="remark" class="card" style="margin-top:12px; background:#FFF8E1;"><div class="card__body" style="padding:12px 16px;"><strong>学生留言：</strong>{{ remark }}</div></div>
        <el-button v-if="!evalResult || evalResult.status === 0" type="primary" style="margin-top:12px;" :loading="aiLoading" @click="runAI">{{ aiLoading ? 'AI 评价中...' : '执行 AI 评价' }}</el-button>
      </div>
    </div>

    <!-- Step 2: AI Result -->
    <div v-if="evalResult && evalResult.status >= 1" class="card section">
      <div class="card__header"><h2>AI 评价结果</h2><span class="badge badge--ai">🤖 AI 已评价</span></div>
      <div class="card__body">
        <div style="text-align:center;margin-bottom:16px;"><span style="font:700 48px/1.2 $font-family;color:$primary;">{{ evalResult.aiScore ?? '--' }}</span><span style="color:$on-surface-variant;"> 分</span></div>
        <div v-if="evalResult.aiIssues" style="margin-bottom:12px;">
          <strong>问题列表：</strong>
          <ul style="padding-left:20px;margin-top:4px;"><li v-for="(iss, i) in aiIssues" :key="i">{{ iss }}</li></ul>
        </div>
        <div v-if="evalResult.aiComment"><strong>综合评语：</strong><p style="margin-top:4px;">{{ evalResult.aiComment }}</p></div>
      </div>
    </div>

    <!-- Step 3: Teacher Review -->
    <div v-if="evalResult && evalResult.status >= 1" class="card section">
      <div class="card__header"><h2>教师最终评价</h2></div>
      <div class="card__body">
        <el-form label-position="top" @submit.prevent="saveReview">
          <el-form-item label="最终评分（0-100）">
            <el-input-number v-model="teacherScore" :min="0" :max="100" :step="1" />
          </el-form-item>
          <el-form-item label="最终评语">
            <el-input v-model="teacherComment" type="textarea" :rows="3" />
          </el-form-item>
          <el-button type="primary" native-type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存最终评价' }}</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { get, post } from '@/utils/request';
import AppNotice from '@/components/AppNotice.vue';
import type { EvaluationResult, Student, WorkSubmission } from '@/types';

const route = useRoute();
const router = useRouter();

const submissionId = computed(() => Number(route.params.submissionId));
const studentName = ref(route.query.studentName as string || '');
const studentNo = ref('');
const className = ref('');
const fileName = ref(route.query.fileName as string || '');
const remark = ref('');
const evalResult = ref<EvaluationResult | null>(null);
const aiLoading = ref(false);
const saving = ref(false);
const teacherScore = ref<number>(0);
const teacherComment = ref('');
const notice = ref<{ visible: boolean; message: string; type: 'info' | 'success' | 'warning' | 'error' }>({ visible: false, message: '', type: 'info' });

const aiIssues = computed(() => {
  if (!evalResult.value?.aiIssues) return [];
  return evalResult.value.aiIssues.split('\n').filter(Boolean).map(s => s.replace(/^\d+\.\s*/, ''));
});

onMounted(async () => {
  const sid = submissionId.value;
  if (!sid) return;
  // Load student info
  try {
    const studentId = Number(route.query.studentId);
    if (studentId) {
      const students = await get<Student[]>('/students');
      const s = students.find((x: Student) => x.id === studentId);
      if (s) { studentNo.value = s.studentNo; className.value = s.className; }
    }
  } catch { /* ignore */ }
  // Load remark from submission
  try {
    const subs = await get<WorkSubmission[]>('/submissions');
    const sub = subs.find((x: WorkSubmission) => x.id === sid);
    if (sub?.remark) remark.value = sub.remark;
  } catch { /* ignore */ }
  // Load existing evaluation
  try {
    const ev = await get<EvaluationResult>(`/submissions/${sid}/evaluation`);
    evalResult.value = ev;
    if (ev.aiScore != null) teacherScore.value = Number(ev.aiScore);
  } catch { /* not evaluated yet */ }
});

async function runAI() {
  aiLoading.value = true;
  try {
    const r = await post<EvaluationResult>(`/submissions/${submissionId.value}/evaluate`, { studentName: studentName.value, fileName: fileName.value });
    evalResult.value = r;
    if (r.aiScore != null) teacherScore.value = Number(r.aiScore);
    ElMessage.success('AI 评价完成');
  } catch (e: any) { ElMessage.error(e.message); }
  finally { aiLoading.value = false; }
}

async function saveReview() {
  saving.value = true;
  try {
    await post(`/submissions/${submissionId.value}/teacher-review`, { teacherScore: teacherScore.value, teacherComment: teacherComment.value });
    ElMessage.success('最终评价已保存');
    setTimeout(() => router.push('/assignments/completed'), 500);
  } catch (e: any) { ElMessage.error(e.message); }
  finally { saving.value = false; }
}
</script>

<style lang="scss" scoped>
.section { margin-bottom: $space-4; }
.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: $space-3; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font: 500 12px/16px $font-family; color: $on-surface-variant; }
.info-value { font: 400 14px/20px $font-family; color: $on-surface; }
.badge { display: inline-flex; align-items: center; gap: 4px; padding: 2px 12px; border-radius: $shape-full; font: 500 12px/16px $font-family; }
.badge--ai { background: $primary-container; color: $on-primary-container; }
</style>
