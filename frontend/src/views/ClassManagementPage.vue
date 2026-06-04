<template>
  <div class="page">
    <div class="page-header">
      <h1>班级管理</h1>
      <span class="page-header__count" v-if="!loading">{{ classes.length }} 个班级</span>
    </div>

    <div v-if="loading" class="empty-state">
      <div class="spinner" />
      <div class="empty-state__text" style="margin-top:12px;">加载中...</div>
    </div>

    <div v-else-if="!classes.length" class="empty-state">
      <div class="empty-state__icon">&#128218;</div>
      <div class="empty-state__text">暂无班级数据</div>
    </div>

    <div v-else class="class-grid">
      <div
        v-for="(cls, idx) in classes"
        :key="cls.name"
        class="class-card"
        :style="{ '--accent': accents[idx % accents.length], '--accent-bg': accentBgs[idx % accentBgs.length] }"
      >
        <!-- Left: class identity -->
        <div class="class-card__left">
          <div class="class-card__avatar">
            <span>{{ cls.name.charAt(0) }}</span>
          </div>
          <div class="class-card__info">
            <h3 class="class-card__name">{{ cls.name }}</h3>
            <span class="class-card__count">{{ cls.studentCount }} 名学生</span>
          </div>
        </div>

        <!-- Right: task stats -->
        <div class="class-card__right">
          <div class="stat stat--pending">
            <span class="stat__dot" />
            <span class="stat__label">未审批</span>
            <span class="stat__value">{{ cls.unapproved }}</span>
          </div>
          <div class="stat stat--ai">
            <span class="stat__dot" />
            <span class="stat__label">AI 已审批</span>
            <span class="stat__value">{{ cls.aiReviewed }}</span>
          </div>
          <div class="stat stat--done">
            <span class="stat__dot" />
            <span class="stat__label">已完成</span>
            <span class="stat__value">{{ cls.completed }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { get } from '@/utils/request';
import type { Student, WorkSubmission, EvaluationResult } from '@/types';

interface ClassStat {
  name: string;
  studentCount: number;
  unapproved: number;
  aiReviewed: number;
  completed: number;
}

const loading = ref(true);
const classes = ref<ClassStat[]>([]);

const accents = ['#1A56DB', '#0D9488', '#7C3AED', '#DB2777', '#EA580C', '#2563EB'];
const accentBgs = ['rgba(26,86,219,.08)', 'rgba(13,148,136,.08)', 'rgba(124,58,237,.08)', 'rgba(219,39,119,.08)', 'rgba(234,88,12,.08)', 'rgba(37,99,235,.08)'];

onMounted(async () => {
  try {
    const [students, submissions, evaluations] = await Promise.all([
      get<Student[]>('/students'),
      get<WorkSubmission[]>('/submissions'),
      get<EvaluationResult[]>('/evaluations'),
    ]);

    // Build eval lookup: submissionId → status
    const evalMap: Record<number, number> = {};
    evaluations.forEach(e => { evalMap[e.submissionId] = e.status; });

    // Build student → submissions map
    const studentSubs: Record<number, WorkSubmission[]> = {};
    submissions.forEach(s => {
      if (!studentSubs[s.studentId]) studentSubs[s.studentId] = [];
      studentSubs[s.studentId].push(s);
    });

    // Group by class & compute stats
    const classMap: Record<string, ClassStat> = {};
    students.forEach(st => {
      if (!classMap[st.className]) {
        classMap[st.className] = { name: st.className, studentCount: 0, unapproved: 0, aiReviewed: 0, completed: 0 };
      }
      const cls = classMap[st.className];
      cls.studentCount++;

      const subs = studentSubs[st.id] || [];
      subs.forEach(s => {
        const status = evalMap[s.id] ?? 0;
        if (status === 0) cls.unapproved++;
        else if (status === 1) cls.aiReviewed++;
        else if (status === 2) cls.completed++;
      });
    });

    classes.value = Object.values(classMap);
  } finally {
    loading.value = false;
  }
});
</script>

<style lang="scss" scoped>
.page-header__count {
  @include font(13px, 20px);
  color: $on-surface-variant;
}

.class-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: $space-5;
}

.class-card {
  display: flex;
  align-items: stretch;
  background: $surface-bright;
  border: 1px solid $outline-variant;
  border-radius: $shape-lg;
  box-shadow: $elevation-2;
  overflow: hidden;
  transition: transform .2s ease, box-shadow .2s ease;
  cursor: default;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $elevation-3;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $space-4;
    padding: $space-5 $space-6;
    background: var(--accent-bg);
    border-right: 1px solid $outline-variant;
    min-width: 0;
  }

  &__avatar {
    width: 44px; height: 44px;
    border-radius: $shape-md;
    background: var(--accent);
    color: #fff;
    display: grid;
    place-items: center;
    flex-shrink: 0;
    span {
      @include font(18px, 24px, 600);
    }
  }

  &__info {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__name {
    @include font(15px, 22px, 500);
    color: $on-surface;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__count {
    @include font(12px, 18px);
    color: $on-surface-variant;
  }

  &__right {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: $space-2;
    padding: $space-4 $space-5;
    min-width: 0;
  }
}

// Stat indicators
.stat {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-2 $space-3;
  border-radius: $shape-sm;
  transition: background .15s ease;

  &__dot {
    width: 6px; height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }
  &__label {
    @include font(12px, 18px);
    color: $on-surface-variant;
    flex: 1;
  }
  &__value {
    @include font(14px, 20px, 600);
    padding: 0 6px;
    min-width: 24px;
    text-align: center;
  }

  &--pending {
    .stat__dot { background: #DC2626; }
    .stat__value { color: #DC2626; }
    &:hover { background: rgba(220,38,38,.04); }
  }
  &--ai {
    .stat__dot { background: #2563EB; }
    .stat__value { color: #2563EB; }
    &:hover { background: rgba(37,99,235,.04); }
  }
  &--done {
    .stat__dot { background: #16A34A; }
    .stat__value { color: #16A34A; }
    &:hover { background: rgba(22,163,74,.04); }
  }
}

@media (max-width: 768px) {
  .class-grid {
    grid-template-columns: 1fr;
  }
  .class-card {
    flex-direction: column;
    &__left {
      border-right: none;
      border-bottom: 1px solid $outline-variant;
    }
  }
}
</style>
