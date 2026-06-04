<template>
  <div class="page">
    <!-- No submission hint -->
    <div v-if="!submissionId" class="empty-state">
      <div class="empty-state__icon">&#128196;</div>
      <div class="empty-state__text">请从「未审批」「AI 已审批」或作业提交记录中选择一个作业进入评价。</div>
    </div>

    <template v-else>
      <!-- Step 1: Student & Submission Info -->
      <div class="card section">
        <div class="card__header">
          <span class="step-marker">1</span>
          <h2>学生与作业信息</h2>
        </div>
        <div class="card__body">
          <div class="info-grid">
            <div class="info-item"><span class="info-label">学生姓名</span><span class="info-value">{{ studentName }}</span></div>
            <div class="info-item"><span class="info-label">学号</span><span class="info-value">{{ studentNo }}</span></div>
            <div class="info-item"><span class="info-label">班级</span><span class="info-value">{{ className }}</span></div>
            <div class="info-item"><span class="info-label">文件名</span><span class="info-value">{{ fileName }}</span></div>
          </div>

          <div v-if="remark" class="remark-card">
            <div class="remark-card__header">学生备注</div>
            <div class="remark-card__body">{{ remark }}</div>
          </div>

          <div v-if="submission" class="preprocess-panel">
            <strong>预处理状态：</strong>
            <span :class="preprocessClass">{{ preprocessText }}</span>
            <span v-if="submission.preprocessMessage">，{{ submission.preprocessMessage }}</span>
            <ul v-if="preprocessWarnings.length">
              <li v-for="(warning, index) in preprocessWarnings" :key="index">{{ warning }}</li>
            </ul>
          </div>

          <div v-if="aiError" class="error-panel">{{ aiError }}</div>
          <el-button
            v-if="!evalResult || evalResult.status === 0"
            type="primary"
            style="margin-top:12px;"
            :loading="aiLoading"
            @click="runAI"
          >{{ aiLoading ? 'AI 评价中...' : '执行 AI 评价' }}</el-button>
        </div>
      </div>

      <!-- Step 2: AI Evaluation Result -->
      <div v-if="evalResult && evalResult.status >= 1" class="card section">
        <div class="card__header">
          <span class="step-marker step-marker--done">2</span>
          <h2>AI 评价结果</h2>
          <span class="badge badge--ai">AI 已评价</span>
        </div>
        <div class="card__body">
          <div class="score-display">
            <span class="score-value">{{ evalResult.aiScore ?? '--' }}</span>
            <span class="score-unit">/ 100 分</span>
          </div>

          <div class="ai-cards">
            <div class="ai-subcard">
              <div class="ai-subcard__header">发现的问题</div>
              <div class="ai-subcard__body">
                <ol v-if="aiIssues.length" class="issue-list">
                  <li v-for="(iss, i) in aiIssues" :key="i">{{ iss }}</li>
                </ol>
                <p v-else class="ai-subcard__empty">暂无</p>
              </div>
            </div>
            <div class="ai-subcard">
              <div class="ai-subcard__header">综合评语</div>
              <div class="ai-subcard__body">
                <p class="ai-comment">{{ evalResult.aiComment || '暂无评语' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 3: Teacher Final Review -->
      <div v-if="evalResult && evalResult.status >= 1" class="card section">
        <div class="card__header">
          <span class="step-marker">3</span>
          <h2>教师复核</h2>
        </div>
        <div class="card__body">
          <el-form label-position="top" @submit.prevent="saveReview">
            <el-form-item label="最终分数（0-100）">
              <el-input-number v-model="teacherScore" :min="0" :max="100" :step="1" />
            </el-form-item>
            <el-form-item label="最终评语">
              <el-input
                v-model="teacherComment"
                type="textarea"
                :rows="3"
                :autosize="{ minRows: 3, maxRows: 8 }"
                placeholder="请输入教师最终评语"
              />
            </el-form-item>
            <el-button type="primary" native-type="submit" size="large" :disabled="saving">
              {{ saving ? '保存中...' : '保存最终评价' }}
            </el-button>
          </el-form>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { get, post } from '@/utils/request';
import type { EvaluationResult, Student, WorkSubmission } from '@/types';

const route = useRoute();
const router = useRouter();

const submissionId = computed(() => Number(route.params.submissionId));
const studentName = ref(route.query.studentName as string || '');
const studentNo = ref('');
const className = ref('');
const fileName = ref(route.query.fileName as string || '');
const remark = ref('');
const submission = ref<WorkSubmission | null>(null);
const evalResult = ref<EvaluationResult | null>(null);
const aiLoading = ref(false);
const aiError = ref('');
const saving = ref(false);
const teacherScore = ref<number>(0);
const teacherComment = ref('');

const aiIssues = computed(() => {
  if (!evalResult.value?.aiIssues) return [];
  return evalResult.value.aiIssues.split('\n').filter(Boolean).map(s => s.replace(/^\d+[\.\、\s]+/, ''));
});
const preprocessText = computed(() => {
  const current = submission.value;
  if (!current) return '';
  if (current.preprocessStatus === 'SUCCESS') return '预处理成功';
  if (current.preprocessStatus === 'FAILED') return '预处理失败';
  if (current.preprocessStatus === 'SKIPPED') return 'Py 预处理未启用';
  return current.preprocessStatus || '未返回';
});
const preprocessClass = computed(() => ({
  'text-success': submission.value?.preprocessStatus === 'SUCCESS',
  'text-warning': submission.value?.preprocessStatus === 'SKIPPED',
  'text-error': submission.value?.preprocessStatus === 'FAILED',
}));
const preprocessWarnings = computed(() => {
  const raw = submission.value?.preprocessResult;
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    return [
      ...(Array.isArray(parsed.renderWarnings) ? parsed.renderWarnings : []),
      ...(Array.isArray(parsed.warnings) ? parsed.warnings : []),
    ].filter(Boolean);
  } catch { return []; }
});

onMounted(async () => {
  const sid = submissionId.value;
  if (!sid) return;
  try {
    const studentId = Number(route.query.studentId);
    if (studentId) {
      const students = await get<Student[]>('/students');
      const s = students.find((x: Student) => x.id === studentId);
      if (s) { studentNo.value = s.studentNo; className.value = s.className; }
    }
  } catch { /* ignore */ }
  try {
    const subs = await get<WorkSubmission[]>('/submissions');
    const sub = subs.find((x: WorkSubmission) => x.id === sid);
    if (sub) {
      submission.value = sub;
      studentName.value = sub.studentName || studentName.value;
      fileName.value = sub.fileName || fileName.value;
      if (sub.remark) remark.value = sub.remark;
    }
  } catch { /* ignore */ }
  try {
    const ev = await get<EvaluationResult>(`/submissions/${sid}/evaluation`);
    evalResult.value = ev;
    if (ev.aiScore != null) teacherScore.value = Number(ev.aiScore);
  } catch { /* not evaluated yet */ }
});

async function runAI() {
  aiLoading.value = true;
  aiError.value = '';
  try {
    const r = await post<EvaluationResult>(`/submissions/${submissionId.value}/evaluate`, {
      studentName: studentName.value,
      fileName: fileName.value,
    });
    evalResult.value = r;
    if (r.aiScore != null) teacherScore.value = Number(r.aiScore);
    ElMessage.success('AI 评价完成');
  } catch (e: any) {
    aiError.value = e.response?.data?.message || e.message || 'AI 评价失败，请稍后重试';
    ElMessage.error(aiError.value);
  } finally {
    aiLoading.value = false;
  }
}

async function saveReview() {
  saving.value = true;
  try {
    await post(`/submissions/${submissionId.value}/teacher-review`, {
      teacherScore: teacherScore.value,
      teacherComment: teacherComment.value,
    });
    ElMessage.success('最终评价已保存');
    setTimeout(() => router.push('/assignments/completed'), 500);
  } catch (e: any) { ElMessage.error(e.message); }
  finally { saving.value = false; }
}
</script>

<style lang="scss" scoped>
.section { margin-bottom: $space-4; }

// Step markers
.step-marker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border-radius: 50%;
  background: $primary;
  color: #fff;
  @include font(14px, 20px, 600);
  flex-shrink: 0;
  &--done { background: $success; }
}

// Info grid
.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: $space-3; }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font: 500 12px/16px $font-family; color: $on-surface-variant; }
.info-value { font: 400 14px/20px $font-family; color: $on-surface; }

// Remark card
.remark-card {
  margin-top: $space-4;
  border: 1px solid $outline-variant;
  border-radius: $shape-md;
  background: $tertiary;
  overflow: hidden;
  &__header {
    padding: $space-2 $space-4;
    background: transparent;
    border-bottom: 1px solid $outline-variant;
    @include font(13px, 20px, 500);
    color: #fff;
  }
  &__body {
    padding: $space-3 $space-4;
    @include font(13px, 20px);
    color: #fff;
    opacity: .9;
  }
}

// Score
.score-display { text-align: center; margin-bottom: $space-5; }
.score-value { font: 700 48px/1.2 $font-family; color: $primary; }
.score-unit { @include font(14px, 20px); color: $on-surface-variant; }

// AI sub-cards
.ai-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-4;
}
.ai-subcard {
  border: 1px solid $outline-variant;
  border-radius: $shape-md;
  background: $surface-container-low;
  overflow: hidden;
  &__header {
    padding: $space-2 $space-4;
    border-bottom: 1px solid $outline-variant;
    @include font(14px, 22px, 500);
    color: $on-surface;
  }
  &__body { padding: $space-3 $space-4; }
  &__empty { color: $on-surface-variant; @include font(13px, 20px); }
}
.issue-list {
  margin: 0;
  padding-left: 20px;
  display: grid;
  gap: $space-2;
  @include font(13px, 20px);
  color: $on-surface-variant;
}
.ai-comment {
  @include font(13px, 22px);
  color: $on-surface-variant;
  line-height: 1.8;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 12px;
  border-radius: $shape-full;
  @include font(12px, 16px, 500);
  margin-left: auto;
}
.badge--ai { background: $primary-container; color: $on-primary-container; }

.text-success { color: $success; }
.text-warning { color: #B7791F; }
.text-error { color: $error; }
.preprocess-panel {
  margin-top: $space-3;
  padding: $space-3;
  border-radius: $shape-md;
  background: #F8FAFC;
  color: $on-surface-variant;
  @include font(13px, 20px);
  ul { margin: 6px 0 0; padding-left: 18px; color: #92400E; }
}
.error-panel {
  margin-top: $space-3;
  padding: $space-3;
  border-radius: $shape-md;
  background: #FEE2E2;
  color: $error;
  @include font(13px, 20px, 500);
}

@media (max-width: 768px) {
  .info-grid { grid-template-columns: 1fr; }
  .ai-cards { grid-template-columns: 1fr; }
}
</style>
