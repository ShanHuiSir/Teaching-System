import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { get, post } from '@/utils/request';
import type { WorkSubmission, EvaluationResult } from '@/types';

export const useSubmissionStore = defineStore('submission', () => {
  // ── State: single source of truth ──
  const list = ref<WorkSubmission[]>([]);
  const evaluations = ref<EvaluationResult[]>([]);
  const loading = ref(false);

  // ── Getters: evaluation lookup map ──
  const evalMap = computed(() => {
    const map: Record<number, EvaluationResult> = {};
    evaluations.value.forEach(e => { map[e.submissionId] = e; });
    return map;
  });

  // ── Unified stats computed: three counts from ONE pass over list ──
  const stats = computed(() => {
    let unapproved = 0;
    let aiReviewed = 0;
    let completed = 0;
    for (const s of list.value) {
      const st = evalMap.value[s.id]?.status ?? 0;
      if (st === 0) unapproved++;
      else if (st === 1) aiReviewed++;
      else if (st === 2) completed++;
    }
    return { unapproved, aiReviewed, completed };
  });

  // Convenience: filter list by status for page display
  const filteredByStatus = (status: number) =>
    list.value.filter(s => (evalMap.value[s.id]?.status ?? 0) === status);

  // ── Actions ──
  async function fetchAll() {
    loading.value = true;
    try {
      const [subs, evals] = await Promise.all([
        get<WorkSubmission[]>('/submissions'),
        get<EvaluationResult[]>('/evaluations'),
      ]);
      list.value = subs;
      evaluations.value = evals;
    } finally {
      loading.value = false;
    }
  }

  /** Submit new homework → refetch list → stats auto-recompute */
  async function addSubmission(payload: {
    studentId: number; title: string; fileName: string; workType: string; remark?: string;
  }) {
    const result = await post<WorkSubmission>('/submissions', payload);
    await fetchAll();
    return result;
  }

  return { list, evaluations, loading, evalMap, stats, filteredByStatus, fetchAll, addSubmission };
});
