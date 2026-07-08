import { ref } from 'vue'
import http from '../utils/request'

/* ── 共享原始数据 ── */
export const students = ref<any[]>([])
export const classes = ref<any[]>([])
export const assignments = ref<any[]>([])
export const submissions = ref<any[]>([])
export const evaluations = ref<any[]>([])

/* ── 刷新版本号，页面 watch 此值来响应全局刷新 ── */
export const fetchVersion = ref(0)

/* ── 缓存标记 ── */
let _s = false, _c = false, _a = false, _u = false, _e = false

export async function fetchStudents(force = false) {
  if (!_s || force) { students.value = (await http.get('/students')) || []; _s = true }
  return students.value
}

export async function fetchClasses(force = false) {
  if (!_c || force) { classes.value = (await http.get('/classes')) || []; _c = true }
  return classes.value
}

export async function fetchAssignments(force = false) {
  if (!_a || force) { assignments.value = (await http.get('/assignments')) || []; _a = true }
  return assignments.value
}

export async function fetchSubmissions(force = false) {
  if (!_u || force) { submissions.value = (await http.get('/submissions')) || []; _u = true }
  return submissions.value
}

export async function fetchEvaluations(force = false) {
  if (!_e || force) { evaluations.value = (await http.get('/evaluations')) || []; _e = true }
  return evaluations.value
}

/** 首次加载：并行拉取全部数据（已缓存则跳过） */
export async function fetchAll() {
  await Promise.all([
    fetchStudents(),
    fetchClasses(),
    fetchAssignments(),
    fetchSubmissions(),
    fetchEvaluations(),
  ])
}

/** 强制刷新全部数据，递增版本号通知所有页面 */
export async function refreshAll() {
  await Promise.all([
    fetchStudents(true),
    fetchClasses(true),
    fetchAssignments(true),
    fetchSubmissions(true),
    fetchEvaluations(true),
  ])
  fetchVersion.value++
}
