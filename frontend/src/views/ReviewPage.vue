<template>
  <div class="review" :class="{ 'is-review': reviewMode }">
    <!-- Panel 1: Assignment List -->
    <div class="review__panel review__list">
      <!-- Tab bar + Search -->
      <div class="tab-card">
        <div class="tab-bar">
          <div v-if="assignmentsTabRef && submissionsTabRef" class="tab-bar__indicator" :style="tabIndicatorStyle" />
          <button
            ref="assignmentsTabRef"
            class="tab-bar__btn"
            :class="{ 'tab-bar__btn--active': activeTab === 'assignments' }"
            @click="activeTab = 'assignments'"
          >
            <svg
              class="tab-bar__icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="12" y1="18" x2="12" y2="12" />
              <line x1="9" y1="15" x2="12" y2="12" />
              <line x1="15" y1="15" x2="12" y2="12" />
            </svg>
            <span>发布的作业</span>
          </button>
          <button
            ref="submissionsTabRef"
            class="tab-bar__btn"
            :class="{ 'tab-bar__btn--active': activeTab === 'submissions' }"
            @click="activeTab = 'submissions'"
          >
            <svg
              class="tab-bar__icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="22 12 16 12 14 15 10 15 8 12 2 12" />
              <path
                d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"
              />
            </svg>
            <span>提交的作业</span>
          </button>
        </div>
        <SearchInput v-model="searchQuery" placeholder="搜索学生、学号、文件名、备注…" />
      </div>

      <!-- Published Assignments (tab 1) -->
      <!-- TODO: Replace workType grouping with real assignment data from GET /api/assignments -->
      <div v-show="activeTab === 'assignments'" class="semester-card">
        <h3 class="semester-card__title">全部作业类型</h3>
        <div class="semester-card__items">
          <!-- "全部作业" — shows all submissions -->
          <div
            class="assign-item"
            :class="{ 'assign-item--active': !selectedWorkType }"
            @click="onSelectWorkType(null)"
          >
            <div class="assign-item__top">
              <span class="assign-item__student">全部作业</span>
              <span class="assign-item__badge assign-item__badge--confirmed">{{ totalSubmissionCount }} 份</span>
            </div>
            <span class="assign-item__file-name" style="color: rgb(var(--md-sys-color-on-surface-variant))"
              >查看所有提交</span
            >
          </div>

          <div
            v-for="wt in workTypes"
            :key="wt.type"
            class="assign-item"
            :class="{ 'assign-item--active': selectedWorkType === wt.type }"
            @click="onSelectWorkType(wt.type)"
          >
            <div class="assign-item__top">
              <span class="assign-item__student">{{ wt.type }}</span>
              <span class="assign-item__badge assign-item__badge--ai">{{ wt.submittedCount }} 份</span>
            </div>
            <div class="assign-item__stats">
              <span class="stat-chip" data-tooltip="受理班级">
                <svg
                  class="stat-chip__icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
                <span>{{ wt.className }}</span>
              </span>
            </div>
            <div class="assign-item__stats assign-item__stats--row">
              <span class="stat-chip" data-tooltip="提交进度">
                <svg
                  class="stat-chip__icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                </svg>
                <span>{{ wt.submittedCount }}/{{ wt.totalStudents }} · {{ wt.submitRate }}%</span>
              </span>
              <span class="stat-chip" data-tooltip="审批进度">
                <svg
                  class="stat-chip__icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <polyline points="9 11 12 14 22 4" />
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
                </svg>
                <span>{{ wt.reviewProgress }}%</span>
              </span>
              <span class="stat-chip" data-tooltip="发布日期">
                <svg
                  class="stat-chip__icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <rect x="3" y="4" width="18" height="18" rx="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                <span><!-- TODO: real createdAt from API -->{{ wt.createdAt }}</span>
              </span>
              <span class="stat-chip stat-chip--push" data-tooltip="截止日期">
                <svg
                  class="stat-chip__icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
                <span><!-- TODO: real dueDate from API -->截止 {{ wt.dueDate }}</span>
              </span>
            </div>
          </div>

          <div v-if="!workTypes.length && !searchQuery" class="assign-item" style="opacity: 0.5; cursor: default">
            <div class="assign-item__top"><span class="assign-item__student">暂无数据</span></div>
          </div>
          <EmptyState v-else-if="!workTypes.length && searchQuery" text="这里似乎什么都没有" />
        </div>
      </div>

      <!-- Submissions (tab 2) -->
      <div v-show="activeTab === 'submissions'">
        <ListSkeleton v-if="loading" />
        <div v-for="sem in semesters" :key="sem.name" class="semester-card">
          <h3 class="semester-card__title">{{ sem.name }}</h3>
          <div class="semester-card__items">
            <div
              v-for="item in sem.assignments"
              :key="item.id"
              class="assign-item"
              :class="{ 'assign-item--active': activeId === item.id }"
              @click="selectItem(item)"
            >
              <div class="assign-item__top">
                <span class="assign-item__student">{{ item.studentName }}</span>
                <span class="assign-item__badge" :class="`assign-item__badge--${item.badgeType}`">
                  {{ item.badgeText }}
                </span>
              </div>
              <div class="assign-item__file">
                <svg
                  class="assign-item__file-icon"
                  :viewBox="iconViewBox(item.fileType)"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path
                    v-for="(p, i) in iconPaths(item.fileType)"
                    :key="i"
                    :d="p.d"
                    :fill="p.fill"
                    :fill-rule="p.fillRule"
                    :stroke-dasharray="p.strokeDasharray"
                  />
                </svg>
                <span class="assign-item__file-name">{{ item.fileName }}</span>
              </div>
              <span class="assign-item__time">{{ item.submitTime }}</span>
            </div>
          </div>
        </div>
        <div v-if="!semesters.length && (searchQuery || hasActiveFilter)" class="semester-card">
          <EmptyState text="这里似乎什么都没有" />
        </div>
      </div>
    </div>

    <!-- Panel 2: Detail Card -->
    <div class="review__panel review__preview">
      <template v-if="active">
        <div class="detail-card">
          <!-- Top action bar -->
          <div class="detail-card__actions">
            <div class="detail-card__actions-left">
              <button class="act-btn act-btn--review" :disabled="reviewMode" @click="onReview">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                <span>批改</span>
              </button>
              <div class="ai-btn-wrap">
                <button class="act-btn act-btn--ai" :disabled="aiLoading" @click="onAiEval">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path
                      d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5"
                    />
                    <path
                      d="M18 2l.9 2.1 2.1.9-2.1.9-.9 2.1-.9-2.1-2.1-.9 2.1-.9.9-2.1z"
                      fill="currentColor"
                      stroke="none"
                    />
                  </svg>
                  <span>{{ aiLoading ? '评价中...' : 'AI评价' }}</span>
                </button>
                <span v-if="activeEval && activeEval.status >= 1" class="ai-btn-wrap__hint">重新评价？</span>
              </div>
            </div>
            <button class="act-btn act-btn--reject" @click="onReject">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
              <span>打回</span>
            </button>
          </div>

          <!-- Info inner card -->
          <div class="info-card">
            <div class="info-card__grid">
              <div class="info-field">
                <span class="info-field__label">姓名</span>
                <span class="info-field__value">{{ active.studentName }}</span>
              </div>
              <div class="info-field">
                <span class="info-field__label">班级</span>
                <span class="info-field__value">{{ active.className || '—' }}</span>
              </div>
              <div class="info-field">
                <span class="info-field__label">学号</span>
                <span class="info-field__value">{{ active.studentNo || '—' }}</span>
              </div>
            </div>

            <div class="info-card__row">
              <span class="info-field__label">作业类型</span>
              <span class="info-field__value">{{ active.workType || '—' }}</span>
            </div>

            <!-- Student note: split-card component -->
            <div class="note-block">
              <div class="note-block__head">学生备注</div>
              <div class="note-block__body">{{ active.remark || '无备注' }}</div>
            </div>
          </div>

          <!-- AI Evaluation -->
          <div v-if="activeEval && activeEval.status >= 1" class="eval-card">
            <div class="eval-card__field">
              <span class="eval-card__label">AI 评分</span>
              <span class="eval-card__score">{{ activeEval.aiScore ?? '—' }}</span>
            </div>
            <hr class="eval-card__sep" />
            <div class="eval-card__field">
              <h4 class="eval-card__label eval-card__label--title">发现的问题</h4>
              <p class="eval-card__text">{{ activeEval.aiIssues || '无' }}</p>
            </div>
            <hr class="eval-card__sep" />
            <div class="eval-card__field">
              <h4 class="eval-card__label eval-card__label--title">综合评语</h4>
              <p class="eval-card__text">{{ activeEval.aiComment || '无' }}</p>
            </div>
          </div>

          <!-- Attachments -->
          <div v-if="active.fileName" class="attach-list">
            <div class="attach-item">
              <svg
                class="attach-item__icon"
                :viewBox="iconViewBox(active.fileType)"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  v-for="(p, i) in iconPaths(active.fileType)"
                  :key="i"
                  :d="p.d"
                  :fill="p.fill"
                  :fill-rule="p.fillRule"
                  :stroke-dasharray="p.strokeDasharray"
                />
              </svg>
              <div class="attach-item__info">
                <span class="attach-item__name">{{ active.fileName }}</span>
                <span class="attach-item__size">100 KB</span>
              </div>
              <div class="attach-item__btns">
                <button class="ghost-btn">预览</button>
                <button class="ghost-btn">下载</button>
              </div>
            </div>
          </div>

          <!-- Saved Draft -->
          <div v-if="draft" class="eval-card eval-card--draft">
            <div class="eval-card__field">
              <span class="eval-card__label">保存的评价</span>
              <span class="eval-card__score">{{ draft.score }}</span>
            </div>
            <hr class="eval-card__sep" />
            <div class="eval-card__field">
              <h4 class="eval-card__label eval-card__label--title">评语草稿</h4>
              <p class="eval-card__text">{{ draft.comment || '无' }}</p>
            </div>
          </div>

          <!-- Teacher Review -->
          <div v-if="activeEval && activeEval.status >= 2" class="eval-card">
            <div class="eval-card__field">
              <span class="eval-card__label">教师评分</span>
              <span class="eval-card__score">{{ activeEval.teacherScore ?? '—' }}</span>
            </div>
            <hr class="eval-card__sep" />
            <div class="eval-card__field">
              <h4 class="eval-card__label eval-card__label--title">教师评语</h4>
              <p class="eval-card__text">{{ activeEval.teacherComment || '无' }}</p>
            </div>
          </div>
        </div>
      </template>

      <PreviewPlaceholder v-else message="选择一份作业以预览" />
    </div>

    <!-- Panel 3: Review Form -->
    <div class="review__panel review__form">
      <div class="form-card">
        <div class="form-card__bar">
          <div class="form-card__bar-left">
            <button class="form-card__back" @click="reviewMode = false">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M15 18l-6-6 6-6" />
              </svg>
              <span>关闭批改</span>
            </button>
            <button
              class="act-btn act-btn--ai"
              :disabled="aiLoading || !activeEval || activeEval.status < 1"
              @click="applyAiEval"
            >
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5"
                />
                <path
                  d="M18 2l.9 2.1 2.1.9-2.1.9-.9 2.1-.9-2.1-2.1-.9 2.1-.9.9-2.1z"
                  fill="currentColor"
                  stroke="none"
                />
              </svg>
              <span>应用AI评价</span>
            </button>
          </div>
          <button
            ref="submitBtnRef"
            class="act-btn act-btn--review"
            :disabled="submitting || !teacherComment.trim()"
            @click="submitReview"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
            <span>{{ submitting ? '提交中...' : '提交批改' }}</span>
          </button>
        </div>

        <div class="score-box">
          <button class="score-box__btn" @click="adjustScore(-5)">-5</button>
          <button class="score-box__btn" @click="adjustScore(-1)">-1</button>
          <button class="score-box__btn" @click="adjustScore(-0.5)">-.5</button>
          <input
            v-model.number="teacherScore"
            class="score-box__input"
            type="number"
            min="0"
            max="100"
            step="0.5"
            @change="clampScore"
          />
          <button class="score-box__btn" @click="adjustScore(0.5)">+.5</button>
          <button class="score-box__btn" @click="adjustScore(1)">+1</button>
          <button class="score-box__btn" @click="adjustScore(5)">+5</button>
        </div>

        <textarea
          ref="editorRef"
          v-model="teacherComment"
          class="form-card__editor"
          placeholder="请输入评语..."
          rows="4"
          @input="autoResize"
        ></textarea>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  ref,
  computed,
  onMounted,
  onActivated,
  onDeactivated,
  nextTick,
  inject,
  watch,
  watchEffect,
} from 'vue'
import { useRoute } from 'vue-router'
import http, { retryFetch } from '../utils/request'
import { useSnackbar } from '../composables/useSnackbar'
import { getCookie, setCookie } from '../utils/cookie'
import { detectFileType, FILE_ICONS } from '../utils/fileIcons'
import { MAGIC_BAR_KEY, TRIGGER_RIPPLE_KEY, REFRESH_TICK_KEY, RIGHT_BUTTONS_KEY } from '../types'
import EmptyState from '../components/EmptyState.vue'
import SearchInput from '../components/SearchInput.vue'
import PreviewPlaceholder from '../components/PreviewPlaceholder.vue'
import ListSkeleton from '../components/ListSkeleton.vue'

const route = useRoute()
const snackbar = useSnackbar()

function iconPaths(type) {
  const raw = FILE_ICONS[type]?.paths || FILE_ICONS.text.paths
  return raw.map(p => (typeof p === 'string' ? { d: p } : { ...p }))
}

function iconViewBox(type) {
  return FILE_ICONS[type]?.viewBox || '0 0 24 24'
}

const activeTab = ref('assignments')

const assignmentsTabRef = ref(null)
const submissionsTabRef = ref(null)

const tabIndicatorStyle = computed(() => {
  const el = activeTab.value === 'assignments' ? assignmentsTabRef.value : submissionsTabRef.value
  if (!el) return { left: '0px', width: '0px' }
  return {
    left: el.offsetLeft + 'px',
    width: el.offsetWidth + 'px',
  }
})

const selectedWorkType = ref(null)
const activeId = ref(null)
const semesters = ref<any[]>([])
const submissionsRaw = ref<any[]>([])
const evalMap = ref<Record<string, any>>({})
const studentsAll = ref<any[]>([])
const searchQuery = ref('')

const filteredSubmissions = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return submissionsRaw.value

  const studentMap = {}
  studentsAll.value.forEach(s => {
    studentMap[s.id] = s
  })

  return submissionsRaw.value.filter(s => {
    const st = studentMap[s.studentId]
    return (
      (s.studentName || '').toLowerCase().includes(q) ||
      (s.studentNo || '').toLowerCase().includes(q) ||
      (s.workType || '').toLowerCase().includes(q) ||
      (s.fileName || '').toLowerCase().includes(q) ||
      (s.remark || '').toLowerCase().includes(q) ||
      (s.className || '').toLowerCase().includes(q) ||
      (st?.name || '').toLowerCase().includes(q) ||
      (st?.studentNo || '').toLowerCase().includes(q)
    )
  })
})

const active = computed(() => {
  return submissionsRaw.value.find(s => s.id === activeId.value) || null
})

const activeEval = computed(() => {
  return activeId.value ? evalMap.value[activeId.value] : null
})

const draftStamp = ref(0)

const draft = computed(() => {
  void draftStamp.value
  if (!activeId.value) return null
  const raw = getCookie(`draft_${activeId.value}`)
  if (!raw) return null
  // Clear draft if teacher already confirmed
  const ev = activeEval.value
  if (ev && ev.status >= 2) {
    setCookie(`draft_${activeId.value}`, '', -1)
    return null
  }
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
})

function formatTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const totalSubmissionCount = computed(() => filteredSubmissions.value.length)

const workTypes = computed(() => {
  const classStudentCounts: Record<string, number> = {}
  studentsAll.value.forEach((s: any) => {
    const cls = s.className || '未分班'
    classStudentCounts[cls] = (classStudentCounts[cls] || 0) + 1
  })

  const map: Record<string, any> = {}
  filteredSubmissions.value.forEach((s: any) => {
    const type = s.workType || '其他'
    if (!map[type]) map[type] = { type, count: 0, reviewed: 0, classes: new Set() }
    map[type].count++
    if (s.className) map[type].classes.add(s.className)
    const ev = evalMap.value[s.id]
    if (ev && ev.status >= 2) map[type].reviewed++
  })

  return Object.values(map)
    .map(w => {
      const classList = [...w.classes]
      const total = classList.reduce((sum, cls) => sum + (classStudentCounts[cls] || 0), 0)
      return {
        type: w.type,
        submittedCount: w.count,
        reviewedCount: w.reviewed,
        totalStudents: total,
        className: classList.join('、') || '—',
        submitRate: total ? Math.round((w.count / total) * 100) : 0,
        reviewProgress: w.count ? Math.round((w.reviewed / w.count) * 100) : 0,
        // TODO: replace with real data from GET /api/assignments
        createdAt: '—',
        dueDate: '—',
      }
    })
    .sort((a, b) => b.submittedCount - a.submittedCount)
})

const magicBar = inject(MAGIC_BAR_KEY)!
const triggerRipple = inject(TRIGGER_RIPPLE_KEY)!

function updateMagicTrail() {
  const parts = []
  parts.push(selectedWorkType.value || '全部作业')
  const a = active.value
  if (a) parts.push(a.studentName)
  magicBar.sub = parts.join(' · ')
}

function onSelectWorkType(type) {
  selectedWorkType.value = type
  activeTab.value = 'submissions'
  activeId.value = null
  updateMagicTrail()
  rebuildSemesters()
}

function selectItem(item) {
  activeId.value = item.id
  updateMagicTrail()
}

const loading = ref(true)
const reviewMode = ref(false)
const aiLoading = ref(false)
const submitting = ref(false)
const teacherScore = ref(0)
const teacherComment = ref('')
const editorRef = ref(null)

async function onReview() {
  if (!active.value) return
  const ev = activeEval.value
  const draftKey = `draft_${active.value.id}`
  const draft = getCookie(draftKey)
  if (draft) {
    try {
      const d = JSON.parse(draft)
      teacherScore.value = d.score ?? ev?.teacherScore ?? ev?.aiScore ?? 0
      teacherComment.value = d.comment ?? ev?.teacherComment ?? ev?.aiComment ?? ''
    } catch {
      snackbar.show('草稿数据损坏，已重置', { variant: 'warning' })
    }
  } else {
    teacherScore.value = ev?.teacherScore ?? ev?.aiScore ?? 0
    teacherComment.value = ev?.teacherComment ?? ev?.aiComment ?? ''
  }
  reviewMode.value = true
  await nextTick()
  autoResize()
}

// Notify on close without submit
watch(reviewMode, (val, old) => {
  if (old && !val && active.value) {
    draftStamp.value++
    const key = `draft_${active.value.id}`
    if (getCookie(key) && !(activeEval.value?.status >= 2)) {
      magicBar.status = '批改草稿已保存至本地'
      magicBar.statusType = 'info'
      setTimeout(() => {
        if (magicBar.status === '批改草稿已保存至本地') magicBar.status = ''
      }, 2500)
      snackbar.show(`${active.value.studentName} 的 ${active.value.workType || '作业'} 批改已保存`, {
        variant: 'info',
        duration: 2500,
      })
    }
  }
})

// Auto-save draft (skip if teacher already confirmed)
watchEffect(() => {
  if (!reviewMode.value || !active.value) return
  if (activeEval.value?.status >= 2) return
  const draftKey = `draft_${active.value.id}`
  const draft = JSON.stringify({ score: teacherScore.value, comment: teacherComment.value })
  setCookie(draftKey, draft, 7)
})

function clampScore() {
  if (teacherScore.value < 0) teacherScore.value = 0
  if (teacherScore.value > 100) teacherScore.value = 100
}

function adjustScore(delta) {
  teacherScore.value = Math.round((teacherScore.value + delta) * 10) / 10
  clampScore()
}

function autoResize() {
  const el = editorRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

function applyAiEval() {
  if (!activeEval.value) return
  const parts = []
  if (activeEval.value.aiIssues) parts.push(activeEval.value.aiIssues)
  if (activeEval.value.aiComment) parts.push(activeEval.value.aiComment)
  teacherComment.value = parts.join('\n')
}

const submitBtnRef = ref(null)

async function submitReview() {
  if (!active.value || submitting.value) return
  submitting.value = true
  try {
    await http.post(`/submissions/${active.value.id}/teacher-review`, {
      teacherScore: teacherScore.value,
      teacherComment: teacherComment.value,
    })
    // Close form immediately — data refresh happens after
    reviewMode.value = false
    setCookie(`draft_${active.value.id}`, '', -1)
    // Ripple as success celebration
    const btn = submitBtnRef.value
    if (btn) {
      const r = btn.getBoundingClientRect()
      triggerRipple(r.left + r.width / 2, r.top + r.height / 2)
    } else {
      triggerRipple()
    }
    snackbar.show('批改已提交', { variant: 'info' })
    // Background refresh
    const evals = await http.get('/evaluations')
    const em = {}
    ;(evals || []).forEach(e => {
      em[e.submissionId] = e
    })
    evalMap.value = em
    rebuildSemesters()
  } catch (e) {
    const saved = getCookie(`draft_${active.value.id}`)
    if (saved) {
      magicBar.status = '提交失败，已保存至本地草稿'
      magicBar.statusType = 'info'
      setTimeout(() => {
        if (magicBar.status === '提交失败，已保存至本地草稿') magicBar.status = ''
      }, 3000)
      snackbar.show('提交失败，评分已保存在本地草稿', { variant: 'error' })
    } else {
      snackbar.show('提交批改失败：' + (e.message || '网络异常'), { variant: 'error' })
    }
  } finally {
    submitting.value = false
  }
}

async function onAiEval() {
  if (!active.value || aiLoading.value) return
  aiLoading.value = true
  magicBar.status = '正在等待 AI 评价…'
  magicBar.statusType = 'loading'
  try {
    await http.post(`/submissions/${active.value.id}/evaluate`, {
      studentName: active.value.studentName,
      fileName: active.value.fileName,
    })
    // Refresh evaluations
    const evals = await http.get('/evaluations')
    const em = {}
    ;(evals || []).forEach(e => {
      em[e.submissionId] = e
    })
    evalMap.value = em
    rebuildSemesters()
    magicBar.status = 'AI 评价已完成'
    magicBar.statusType = 'success'
    setTimeout(() => {
      if (magicBar.status === 'AI 评价已完成') magicBar.status = ''
    }, 2500)
  } catch (e) {
    snackbar.show('AI评价失败：' + (e.message || '网络异常'), { variant: 'error' })
    magicBar.status = ''
  } finally {
    aiLoading.value = false
  }
}

function onReject() {
  /* TODO */
}

const refreshTick = inject(REFRESH_TICK_KEY, ref(0))
const rightButtons = inject(RIGHT_BUTTONS_KEY, ref([]))

const sortClass = ref(getCookie('sort_class') === '1')
const sortTime = ref(getCookie('sort_time') === '1')
const sortCompletion = ref(getCookie('sort_completion') === '1')
const filterStatus = ref<string>(
  'filter' in route.query ? (route.query.filter as string) || 'all' : getCookie('filter_status') || 'all',
)

const hasActiveFilter = computed(() => {
  return filterStatus.value !== 'all' || selectedWorkType.value !== null
})

function rebuildSemesters() {
  const em = evalMap.value

  function makeItem(s) {
    const ev = em[s.id]
    const hasAi = ev && ev.status >= 1
    const confirmed = ev && ev.status >= 2
    return {
      id: s.id,
      studentName: s.studentName || '未知',
      fileType: s.fileType,
      fileName: s.fileName || '未命名',
      badgeType: confirmed ? 'confirmed' : hasAi ? 'ai' : 'none',
      badgeText: confirmed ? '已评价' : hasAi ? '仅AI评价' : '无评价',
      submitTime: formatTime(s.submittedAt),
      className: s.className || '',
      workType: s.workType,
    }
  }

  const all = filteredSubmissions.value.map(makeItem)

  // Apply sort: completion first (confirmed below), then time, then class
  // When combined: sort by all active criteria
  if (sortTime.value) all.sort((a, b) => a.submitTime.localeCompare(b.submitTime))
  if (sortCompletion.value) {
    all.sort((a, b) => {
      const rank = { ai: 0, none: 1, confirmed: 2 }
      return (rank[a.badgeType] ?? 1) - (rank[b.badgeType] ?? 1)
    })
  }

  // Apply filter (single-select radio)
  let showUnsub = true
  let filtered = all
  if (filterStatus.value === 'pending') {
    filtered = all.filter(it => it.badgeType === 'ai')
    showUnsub = false
  } else if (filterStatus.value === 'none') {
    filtered = all.filter(it => it.badgeType === 'none')
    showUnsub = false
  } else if (filterStatus.value === 'unsub') {
    filtered = []
  }

  // Filter by selected workType (from assignments tab)
  if (selectedWorkType.value) {
    filtered = filtered.filter(it => it.workType === selectedWorkType.value)
  }

  // Find unsubmitted students
  const submittedIds = new Set(submissionsRaw.value.map(s => s.studentId))
  const sq = searchQuery.value.trim().toLowerCase()
  const unsubmitted = (studentsAll.value || [])
    .filter(s => {
      if (submittedIds.has(s.id)) return false
      if (sq) {
        return (
          (s.name || '').toLowerCase().includes(sq) ||
          (s.studentNo || '').toLowerCase().includes(sq) ||
          (s.className || '').toLowerCase().includes(sq)
        )
      }
      return true
    })
    .map(s => ({
      id: `u-${s.id}`,
      studentName: s.name || '未知',
      fileType: 'text',
      fileName: '—',
      badgeType: 'unsub',
      badgeText: '未提交',
      submitTime: '—',
      className: s.className || '',
    }))

  if (sortClass.value) {
    // Group by status, then by class
    const groups = []
    const pending = filtered.filter(it => it.badgeType !== 'confirmed')
    const reviewed = filtered.filter(it => it.badgeType === 'confirmed')

    function groupByClass(items, prefix) {
      const map = {}
      items.forEach(it => {
        const cls = it.className || '未分班'
        if (!map[cls]) map[cls] = []
        map[cls].push(it)
      })
      Object.entries(map).forEach(([cls, its]) => {
        groups.push({ name: `${prefix} · ${cls}`, assignments: its })
      })
    }

    if (pending.length) groupByClass(pending, '待审批作业')
    if (reviewed.length) groupByClass(reviewed, '已评价')
    if (showUnsub && unsubmitted.length) groups.push({ name: '未提交', assignments: unsubmitted })
    semesters.value = groups
  } else {
    const pending = filtered.filter(it => it.badgeType !== 'confirmed')
    const reviewed = filtered.filter(it => it.badgeType === 'confirmed')
    const groups = []
    if (pending.length) groups.push({ name: '待审批作业', assignments: pending })
    if (reviewed.length) groups.push({ name: '已评价', assignments: reviewed })
    if (showUnsub && unsubmitted.length) groups.push({ name: '未提交', assignments: unsubmitted })
    semesters.value = groups
  }
}

function buildRightButtons() {
  setCookie('sort_class', sortClass.value ? '1' : '0', 30)
  setCookie('sort_time', sortTime.value ? '1' : '0', 30)
  setCookie('sort_completion', sortCompletion.value ? '1' : '0', 30)
  setCookie('filter_status', filterStatus.value, 30)

  function setFilter(val) {
    filterStatus.value = filterStatus.value === val ? 'all' : val
    rebuildSemesters()
    buildRightButtons()
  }

  const btns = [
    {
      key: 'sort-class',
      icon: 'sort-class',
      label: '按班级排序',
      active: sortClass.value,
      action: () => {
        sortClass.value = !sortClass.value
        rebuildSemesters()
        buildRightButtons()
      },
    },
    {
      key: 'sort-time',
      icon: 'sort-time',
      label: '按提交时间排序',
      active: sortTime.value,
      action: () => {
        sortTime.value = !sortTime.value
        rebuildSemesters()
        buildRightButtons()
      },
    },
    {
      key: 'sort-completion',
      icon: 'sort-completion',
      label: '按完成度排序',
      active: sortCompletion.value,
      action: () => {
        sortCompletion.value = !sortCompletion.value
        rebuildSemesters()
        buildRightButtons()
      },
    },
    { key: 'divider-1', divider: true, gap: true },
    {
      key: 'filter-pending',
      icon: 'filter-pending',
      label: '待复审',
      active: filterStatus.value === 'pending',
      action: () => setFilter('pending'),
    },
    {
      key: 'filter-none',
      icon: 'filter-none',
      label: '未评价',
      active: filterStatus.value === 'none',
      action: () => setFilter('none'),
    },
    {
      key: 'filter-unsub',
      icon: 'filter-unsub',
      label: '未提交',
      active: filterStatus.value === 'unsub',
      action: () => setFilter('unsub'),
    },
  ]
  rightButtons.value = btns
}

async function fetchSubmissions() {
  try {
    const [subs, evals, students] = await Promise.all([
      http.get('/submissions'),
      http.get('/evaluations'),
      http.get('/students'),
    ])
    const em = {}
    ;(evals || []).forEach(e => {
      em[e.submissionId] = e
    })
    evalMap.value = em
    studentsAll.value = students || []
    const studentMap = {}
    studentsAll.value.forEach(s => {
      studentMap[s.id] = s
    })

    // Store full raw data for right panel
    submissionsRaw.value = (subs || []).map(s => {
      const st = studentMap[s.studentId] || {}
      return {
        ...s,
        fileType: detectFileType(s.fileName),
        studentNo: st.studentNo || '',
        className: st.className || '',
      }
    })

    rebuildSemesters()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  retryFetch(
    () => fetchSubmissions(),
    (e: any) => snackbar.show('作业列表加载失败：' + (e.message || '网络异常'), { variant: 'error' }),
  )
})
onMounted(() => {
  magicBar.primary = '作业审批'
  updateMagicTrail()
})
onActivated(() => {
  magicBar.primary = '作业审批'
  updateMagicTrail()
  buildRightButtons()
})
onDeactivated(() => {
  rightButtons.value = []
})
watch(refreshTick, fetchSubmissions)
watch(filteredSubmissions, () => {
  rebuildSemesters()
})
</script>

<style lang="scss" scoped>
.review {
  display: flex;
  height: 100%;
  overflow: hidden;

  &__panel {
    flex: 0 0 50%;
    overflow-y: auto;
    min-width: 0;
    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &__list {
    padding-right: 12px;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__assignments {
    padding-right: 12px;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__preview {
    padding: 0 12px;
  }

  &__form {
    padding-left: 12px;
  }

  /* ── Tab card ── */
  .tab-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    background: rgb(var(--md-sys-color-surface-container-lowest));
    border-radius: 16px;
    padding: 12px 16px;
  }

  /* ── Tab bar ── */
  .tab-bar {
    position: relative;
    display: inline-flex;
    background: rgb(var(--md-sys-color-surface-container));
    border-radius: 12px;
    padding: 4px;
    gap: 2px;

    &__indicator {
      position: absolute;
      top: 4px;
      height: 36px;
      border-radius: 10px;
      background: rgb(var(--md-sys-color-secondary-container));
      transition:
        left 0.35s cubic-bezier(0.4, 0, 0.2, 1),
        width 0.35s cubic-bezier(0.4, 0, 0.2, 1);
      pointer-events: none;
    }

    &__btn {
      position: relative;
      z-index: 1;
      display: inline-flex;
      align-items: center;
      gap: 7px;
      height: 36px;
      padding: 0 14px;
      border: none;
      border-radius: 10px;
      background: transparent;
      color: rgb(var(--md-sys-color-on-surface-variant));
      cursor: pointer;
      @include font(13px, 20px, 500);
      white-space: nowrap;
      transition: color 0.35s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        background: rgb(var(--md-sys-color-on-surface-variant) / 0.08);
      }

      &--active {
        color: rgb(var(--md-sys-color-on-secondary-container));
      }
    }

    &__icon {
      width: 18px;
      height: 18px;
      flex-shrink: 0;
    }
  }

  /* ── Sliding states ── */
  &.is-review .review__panel {
    transform: translateX(-100%);
  }
}

/* ── Semester Card ── */
.semester-card {
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 16px;
  padding: 16px;

  &__title {
    @include font(15px, 22px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    margin-bottom: 12px;
  }

  &__items {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

/* ── Assignment Item ── */
.assign-item {
  position: relative;
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.15s ease;
  display: flex;
  flex-direction: column;
  gap: 8px;

  &:hover {
    background: rgb(var(--md-sys-color-surface-container-high));
  }

  &--active {
    background: rgb(var(--md-sys-color-secondary-container));
  }

  &__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__student {
    @include font(15px, 22px, 500);
    color: rgb(var(--md-sys-color-on-surface));
  }

  &__badge {
    @include font(11px, 16px, 500);
    padding: 2px 10px;
    border-radius: 10px;
    flex-shrink: 0;

    &--ai {
      background: rgb(var(--md-sys-color-secondary-container));
      color: rgb(var(--md-sys-color-on-secondary-container));
    }

    &--none {
      background: rgb(var(--md-sys-color-surface-container-highest));
      color: rgb(var(--md-sys-color-on-surface-variant));
    }

    &--confirmed {
      background: rgb(var(--md-sys-color-primary-container));
      color: rgb(var(--md-sys-color-on-primary-container));
    }

    &--unsub {
      background: rgb(var(--md-sys-color-surface-container-highest));
      color: rgb(var(--md-sys-color-on-surface-variant));
    }
  }

  &__file {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__file-icon {
    width: 16px;
    height: 16px;
    color: rgb(var(--md-sys-color-on-surface-variant));
    flex-shrink: 0;
  }

  &__file-name {
    @include font(13px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__stats {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px 12px;

    &--row {
      justify-content: flex-start;
    }
  }

  &__time {
    @include font(12px, 16px);
    color: rgb(var(--md-sys-color-outline));
    align-self: flex-end;
  }
}

/* ── Stat Chips ── */
.stat-chip {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;

  &__icon {
    width: 14px;
    height: 14px;
    color: rgb(var(--md-sys-color-on-surface-variant));
    flex-shrink: 0;
  }

  span {
    @include font(12px, 16px);
    color: rgb(var(--md-sys-color-on-surface));
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &--push {
    margin-left: auto;
  }

  &:hover::after {
    content: attr(data-tooltip);
    position: absolute;
    top: calc(100% + 6px);
    left: 50%;
    transform: translateX(-50%);
    padding: 3px 10px;
    border-radius: 4px;
    background: rgb(var(--md-sys-color-inverse-surface));
    color: rgb(var(--md-sys-color-inverse-on-surface));
    @include font(11px, 16px, 500);
    white-space: nowrap;
    z-index: 10;
    pointer-events: none;
  }
}

/* ════ Right: Detail Card ════ */
.detail-card {
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ── Action buttons ── */
.detail-card__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;

  &-left {
    display: flex;
    gap: 8px;
  }
}

.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition:
    background 0.15s ease,
    opacity 0.15s ease;

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  svg {
    width: 16px;
    height: 16px;
  }

  span {
    @include font(13px, 20px, 500);
  }

  &--review {
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    &:hover {
      filter: brightness(0.9);
    }
  }

  &--ai {
    background: rgb(var(--md-sys-color-secondary-container));
    color: rgb(var(--md-sys-color-on-secondary-container));
    &:hover {
      filter: brightness(0.95);
    }
  }

  &--reject {
    background: transparent;
    color: rgb(var(--md-sys-color-error));
    border: 1px solid rgb(var(--md-sys-color-error));
    &:hover {
      background: rgb(var(--md-sys-color-error) / 0.08);
    }
  }
}

/* ── AI button hint ── */
.ai-btn-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;

  .act-btn--ai {
    position: relative;
    z-index: 1;
  }

  &__hint {
    position: absolute;
    left: calc(100% - 12px);
    top: 0;
    height: 100%;
    z-index: 0;
    display: flex;
    align-items: center;
    padding: 0 14px;
    border-radius: 10px;
    background: rgb(var(--md-sys-color-secondary-container));
    color: rgb(var(--md-sys-color-on-secondary-container));
    @include font(12px, 18px, 500);
    white-space: nowrap;
    pointer-events: none;
    opacity: 0;
    transform: translateX(-12px);
    transition:
      opacity 0.2s ease,
      transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover &__hint {
    opacity: 1;
    transform: translateX(0);
  }
}

/* ── Info inner card ── */
.info-card {
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 12px;
  padding: 16px;
}

.info-card__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.info-card__row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.info-field {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &__label {
    @include font(12px, 16px, 500);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &__value {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface));
  }
}

/* ── Student note: split-card ── */
.note-block {
  &__head {
    @include font(13px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface-variant));
    background: rgb(var(--md-sys-color-surface-container-high));
    padding: 10px 14px;
    border-radius: 10px 10px 0 0;
  }

  &__body {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface));
    background: rgb(var(--md-sys-color-surface-container-high));
    padding: 10px 14px;
    border-radius: 0 0 10px 10px;
    margin-top: 1px;
  }
}

/* ── AI Evaluation Card ── */
.eval-card {
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;

  &__field {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__label {
    @include font(12px, 16px, 500);
    color: rgb(var(--md-sys-color-on-surface-variant));

    &--title {
      @include font(15px, 22px, 500);
      color: rgb(var(--md-sys-color-on-surface));
    }
  }

  &__score {
    @include font(32px, 40px, 700);
    color: rgb(var(--md-sys-color-primary));
  }

  &__text {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface));
    margin: 0;
    white-space: pre-wrap;
  }

  &__sep {
    border: none;
    height: 1px;
    background: rgb(var(--md-sys-color-outline-variant));
    margin: 0;
  }

  &--draft {
    border: 1px dashed rgb(var(--md-sys-color-outline));
    background: rgb(var(--md-sys-color-tertiary-container));
    .eval-card__label {
      color: rgb(var(--md-sys-color-on-tertiary-container));
    }
    .eval-card__score {
      color: rgb(var(--md-sys-color-tertiary));
    }
  }
}

/* ── Attachment list ── */
.attach-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attach-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 12px;

  &__icon {
    width: 40px;
    height: 40px;
    color: rgb(var(--md-sys-color-on-surface-variant));
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    display: block;
    @include font(14px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__size {
    display: block;
    @include font(12px, 16px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    margin-top: 2px;
  }

  &__btns {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
}

.ghost-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: rgb(var(--md-sys-color-primary));
  cursor: pointer;
  @include font(13px, 20px, 500);
  transition: background 0.15s ease;

  &:hover {
    background: rgb(var(--md-sys-color-primary) / 0.08);
  }
}

/* ── Review Form Card ── */
.form-card {
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;

  &__bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  &__bar-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__back {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 36px;
    padding: 0 14px;
    border: 1px solid rgb(var(--md-sys-color-outline));
    border-radius: 10px;
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface));
    cursor: pointer;
    @include font(13px, 20px, 500);
    transition: background 0.15s ease;

    svg {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
    }

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }
  }

  &__editor {
    min-height: 120px;
    padding: 14px;
    border: 1px solid rgb(var(--md-sys-color-outline-variant));
    border-radius: 10px;
    background: rgb(var(--md-sys-color-surface-container));
    color: rgb(var(--md-sys-color-on-surface));
    @include font(14px, 22px);
    outline: none;
    resize: none;
    overflow-y: hidden;

    &::placeholder {
      color: rgb(var(--md-sys-color-on-surface-variant) / 0.6);
    }

    &:focus {
      border-color: rgb(var(--md-sys-color-primary));
    }
  }
}

/* ── Score Box ── */
.score-box {
  display: flex;
  align-items: center;

  &__btn {
    width: 44px;
    height: 44px;
    border: 1px solid rgb(var(--md-sys-color-outline));
    background: rgb(var(--md-sys-color-surface-container));
    color: rgb(var(--md-sys-color-on-surface));
    cursor: pointer;
    @include font(16px, 20px, 500);
    transition: background 0.15s ease;
    margin-left: -1px;

    &:first-child {
      border-radius: 10px 0 0 10px;
      margin-left: 0;
    }

    &:last-child {
      border-radius: 0 10px 10px 0;
    }

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }
  }

  &__input {
    width: 72px;
    height: 44px;
    text-align: center;
    border: 1px solid rgb(var(--md-sys-color-outline));
    background: rgb(var(--md-sys-color-surface-container));
    color: rgb(var(--md-sys-color-on-surface));
    @include font(20px, 24px, 600);
    outline: none;
    margin-left: -1px;
    border-radius: 0;

    &:focus {
      border-color: rgb(var(--md-sys-color-primary));
      position: relative;
      z-index: 1;
    }

    &::-webkit-outer-spin-button,
    &::-webkit-inner-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }
    -moz-appearance: textfield;
  }
}
</style>
