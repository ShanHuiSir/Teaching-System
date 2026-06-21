<template>
  <div class="ap">
    <!-- Panel 1: Assignment List (always visible) -->
    <div class="ap__panel ap__list">
      <div class="ap__toolbar">
        <button class="ap__create-btn" @click="startCreate">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span>创建作业</span>
        </button>
        <SearchInput v-model="searchQuery" placeholder="搜索作业名称、班级、描述…" />
      </div>

      <ListSkeleton v-if="loading" />
      <div v-else-if="!assignments.length" class="ap__empty">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
        <span>暂无作业，点击上方按钮创建</span>
      </div>
      <EmptyState v-else-if="!filteredAssignments.length && searchQuery" text="这里似乎什么都没有" />

      <div class="ap__cards">
        <div
          v-for="a in filteredAssignments"
          :key="a.id"
          class="asgn-card"
          :class="{ 'asgn-card--active': activeId === a.id }"
          @click="onSelectCard(a)"
        >
          <div class="asgn-card__header">
            <h3 class="asgn-card__title">{{ a.title }}</h3>
            <span class="asgn-card__type">{{ a.workType }}</span>
          </div>

          <div class="asgn-card__body">
            <div class="asgn-card__stats">
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
                <span>{{ formatClassNames(a) }}</span>
              </span>
            </div>

            <div class="asgn-card__stats asgn-card__stats--row">
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
                <span>{{ a.submittedCount }}/{{ a.totalStudents }} · {{ a.submitRate }}%</span>
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
                <span>{{ a.reviewProgress }}%</span>
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
                <span>{{ a.createdAt ? formatDate(a.createdAt) : '—' }}</span>
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
                <span>截止 {{ a.dueDate ? formatDate(a.dueDate) : '—' }}</span>
              </span>
            </div>

            <div v-if="a.description" class="asgn-card__desc">{{ a.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Panel 2: Detail (slides in over right half) -->
    <div class="ap__panel ap__preview" :style="previewStyle">
      <template v-if="active">
        <div class="detail-card">
          <div class="detail-card__bar">
            <div class="detail-card__bar-left">
              <button class="act-btn act-btn--primary" @click="startEdit(active)">
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
                <span>编辑</span>
              </button>
              <button class="act-btn act-btn--outline" :disabled="exporting" @click="onExport(active)">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                  <path fill-rule="evenodd" d="M17,10 L17,8 L18,8 C19.6568542,8 21,9.34314575 21,11 L21,19 C21,20.6568542 19.6568542,22 18,22 L6,22 C4.34314575,22 3,20.6568542 3,19 L3,11 C3,9.34314575 4.34314575,8 6,8 L7,8 L7,10 L6,10 C5.44771525,10 5,10.4477153 5,11 L5,19 C5,19.5522847 5.44771525,20 6,20 L18,20 C18.5522847,20 19,19.5522847 19,19 L19,11 C19,10.4477153 18.5522847,10 18,10 L17,10 Z M10.9551845,5.95272695 L9.78361162,7.11045387 C9.37558579,7.51365754 8.71404521,7.51365754 8.30601937,7.11045387 C7.89799354,6.70725019 7.89799354,6.05352787 8.30601937,5.65032419 L12,2 L15.6939806,5.65032419 C16.1020065,6.05352787 16.1020065,6.70725019 15.6939806,7.11045387 C15.2859548,7.51365754 14.6244142,7.51365754 14.2163884,7.11045387 L13.0448155,5.95272695 L13.0448155,13.9675324 C13.0448155,14.5377485 12.5770357,15 12,15 C11.4229643,15 10.9551845,14.5377485 10.9551845,13.9675324 L10.9551845,5.95272695 Z"/>
                </svg>
                <span>{{ exporting ? '导出中...' : '导出为Excel' }}</span>
              </button>
            </div>
            <button class="act-btn act-btn--danger" @click="onDeleteClick(active)">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
              <span>删除</span>
            </button>
          </div>

          <h3 class="detail-card__title">{{ active.title }}</h3>

          <div class="detail-card__grid">
            <div class="info-field">
              <span class="info-field__label">作业类型</span>
              <span class="info-field__value">{{ active.workType || '—' }}</span>
            </div>
            <div class="info-field">
              <span class="info-field__label">受理班级</span>
              <span class="info-field__value">{{ formatClassNames(active) }}</span>
            </div>
            <div class="info-field">
              <span class="info-field__label">提交进度</span>
              <span class="info-field__value"
                >{{ active.submittedCount }}/{{ active.totalStudents }} · {{ active.submitRate }}%</span
              >
            </div>
            <div class="info-field">
              <span class="info-field__label">审批进度</span>
              <span class="info-field__value">{{ active.reviewProgress }}%</span>
            </div>
            <div class="info-field">
              <span class="info-field__label">发布日期</span>
              <span class="info-field__value">{{ active.createdAt ? formatDate(active.createdAt) : '—' }}</span>
            </div>
            <div class="info-field">
              <span class="info-field__label">截止日期</span>
              <span class="info-field__value">{{ active.dueDate ? formatDate(active.dueDate) : '—' }}</span>
            </div>
          </div>

          <div v-if="active.description" class="detail-card__note">
            <div class="detail-card__note-head">作业说明</div>
            <div class="detail-card__note-body">{{ active.description }}</div>
          </div>
        </div>
      </template>

      <PreviewPlaceholder v-else message="选择一份作业以查看详情" />
    </div>

    <!-- Panel 3: Edit Form (slides in over right half) -->
    <div class="ap__panel ap__form" :style="formStyle">
      <div class="form-card">
        <div class="form-card__bar">
          <button class="form-card__back" @click="editing = false">
            <AppIcon name="chevron-left" />
            <span>关闭{{ isCreate ? '创建' : '编辑' }}</span>
          </button>
          <button class="act-btn act-btn--primary" :disabled="saving" @click="onSave">
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
            <span>{{ saving ? '保存中...' : (isCreate ? '发布' : '保存') }}</span>
          </button>
        </div>

        <div class="form-card__fields">
          <label class="form-field">
            <span class="form-field__label">作业名称</span>
            <input v-model="form.title" class="form-field__input" placeholder="输入作业名称" />
          </label>

          <label class="form-field">
            <span class="form-field__label">作业类型</span>
            <input v-model="form.workType" class="form-field__input" placeholder="如 docx / zip / code" />
          </label>

          <label class="form-field">
            <span class="form-field__label">截止日期</span>
            <input v-model="form.dueDate" class="form-field__input" type="datetime-local" />
          </label>

          <!-- 受理班级 -->
          <div class="form-field">
            <span class="form-field__label">受理班级</span>
            <div class="class-card">
              <div v-if="selectedClasses.length" class="class-card__section">
                <span class="class-card__section-title">已受理</span>
                <div v-for="cls in selectedClasses" :key="cls" class="class-card__row" @click="toggleClass(cls)">
                  <span class="class-card__name">{{ cls }}</span>
                  <span class="class-card__btn class-card__btn--remove">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                      <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                  </span>
                </div>
              </div>

              <hr v-if="selectedClasses.length && unselectedClasses.length" class="class-card__divider" />

              <div v-if="unselectedClasses.length" class="class-card__section">
                <span v-if="selectedClasses.length" class="class-card__section-title">未受理</span>
                <div v-for="cls in unselectedClasses" :key="cls" class="class-card__row" @click="toggleClass(cls)">
                  <span class="class-card__name">{{ cls }}</span>
                  <span class="class-card__btn class-card__btn--add">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                      <line x1="12" y1="5" x2="12" y2="19" />
                      <line x1="5" y1="12" x2="19" y2="12" />
                    </svg>
                  </span>
                </div>
              </div>

              <div v-if="!availableClasses.length" class="class-card__empty">暂无班级数据</div>
            </div>
          </div>

          <label class="form-field">
            <span class="form-field__label">作业说明</span>
            <textarea
              ref="editorRef"
              v-model="form.description"
              class="form-field__input form-field__input--area"
              placeholder="作业要求与说明"
              @input="autoResize"
            ></textarea>
          </label>
        </div>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <Transition name="modal">
      <div v-if="deleteModal.open" class="modal-overlay" @click.self="deleteModal.open = false" @keydown.escape="deleteModal.open = false">
        <div ref="deleteDialogRef" class="modal-card" role="alertdialog" aria-modal="true" @keydown="onDeleteKeydown">
          <svg
            class="modal-card__icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <p class="modal-card__text">确定要删除「{{ deleteModal.title }}」吗？<br />已有提交记录的作业不会被删除。</p>
          <div class="modal-card__btns">
            <button ref="deleteCancelRef" class="modal-card__btn modal-card__btn--cancel" @click="deleteModal.open = false">取消</button>
            <HedgehogButton variant="primary" size="sm" @complete="confirmDelete">确认删除</HedgehogButton>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import {
  ref,
  reactive,
  computed,
  onMounted,
  onActivated,
  onDeactivated,
  nextTick,
  inject,
  watch,
  watchEffect,
} from 'vue'
import http, { retryFetch } from '../utils/request'
import { useNotify } from '../composables/useNotify'
import { getCookie, setCookie } from '../utils/cookie'
import { MAGIC_BAR_KEY, TRIGGER_RIPPLE_KEY, REFRESH_TICK_KEY, RIGHT_BUTTONS_KEY } from '../types'
import HedgehogButton from '../components/HedgehogButton.vue'
import EmptyState from '../components/EmptyState.vue'
import SearchInput from '../components/SearchInput.vue'
import PreviewPlaceholder from '../components/PreviewPlaceholder.vue'
import ListSkeleton from '../components/ListSkeleton.vue'
import AppIcon from '../components/AppIcon.vue'
// ConfirmDialog reserved for delete modal

const { notify } = useNotify()

const loading = ref(false)
const saving = ref(false)
const activeId = ref(null)
const assignments = ref<any[]>([])
const classesAll = ref<any[]>([])
const searchQuery = ref('')
const sortKey = ref<string | null>(null)

const filteredAssignments = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  let arr = assignments.value
  if (q) {
    arr = arr.filter(
      a =>
        (a.title || '').toLowerCase().includes(q) ||
        (a.workType || '').toLowerCase().includes(q) ||
        formatClassNames(a).toLowerCase().includes(q) ||
        (a.description || '').toLowerCase().includes(q),
    )
  }
  arr = [...arr]
  if (sortKey.value === 'time') arr.sort((a: any, b: any) => (b.latestTime || '').localeCompare(a.latestTime || ''))
  if (sortKey.value === 'submitRate') arr.sort((a: any, b: any) => b.submitRate - a.submitRate)
  if (sortKey.value === 'reviewRate') arr.sort((a: any, b: any) => b.reviewProgress - a.reviewProgress)
  return arr
})
const editing = ref(false)
const isCreate = ref(false)
const studentsAll = ref<any[]>([])
const exporting = ref(false)
const editorRef = ref(null)

const active = computed(() => assignments.value.find(a => a.id === activeId.value) || null)

const availableClasses = computed(() => {
  return classesAll.value.map((c: any) => c.name).filter(Boolean).sort()
})

const selectedClasses = computed(() => {
  return availableClasses.value.filter(c => form.classes.includes(c))
})

const unselectedClasses = computed(() => {
  return availableClasses.value.filter(c => !form.classes.includes(c))
})

const previewStyle = computed(() => ({
  transform: editing.value ? 'translateX(100%)' : activeId.value ? 'translateX(0)' : 'translateX(100%)',
}))

const formStyle = computed(() => ({
  transform: editing.value ? 'translateX(-100%)' : 'translateX(0)',
}))

const form = reactive({
  id: null,
  title: '',
  workType: '',
  classes: [],
  description: '',
  dueDate: '',
})

function resetForm() {
  form.id = null
  form.title = ''
  form.workType = ''
  form.classes = []
  form.description = ''
  form.dueDate = ''
}

function onSelectCard(a: any) {
  editing.value = false
  activeId.value = a.id
}

const DRAFT_PREFIX = 'ap_draft'

function draftKey() {
  return form.id ? `${DRAFT_PREFIX}_${form.id}` : DRAFT_PREFIX
}

function loadDraft() {
  const raw = getCookie(draftKey())
  if (!raw) return false
  try {
    const d = JSON.parse(raw)
    form.title = d.title || ''
    form.workType = d.workType || ''
    form.classes = d.classes || []
    form.description = d.description || ''
    form.dueDate = d.dueDate || ''
    return true
  } catch {
    return false
  }
}

function saveDraft() {
  const data = {
    title: form.title,
    workType: form.workType,
    classes: form.classes,
    description: form.description,
    dueDate: form.dueDate,
  }
  setCookie(draftKey(), JSON.stringify(data), 7)
}

function clearDraft() {
  setCookie(draftKey(), '', -1)
}

function startCreate() {
  isCreate.value = true
  activeId.value = null
  resetForm()
  if (!loadDraft()) resetForm()
  editing.value = true
}

function startEdit(a: any) {
  isCreate.value = false
  resetForm()
  form.id = a.id
  const hasDraft = loadDraft()
  if (!hasDraft) {
    form.title = a.title
    form.workType = a.workType || ''
    form.classes = normalizeAssignmentClassNames(a)
    form.description = a.description || ''
    form.dueDate = toDatetimeLocal(a.dueAt || a.dueDate)
  }
  editing.value = true
  nextTick(autoResize)
}

// Notify on close without save
watch(editing, (val, old) => {
  if (old && !val) {
    if (getCookie(draftKey())) {
      notify({ type: 'info', snackbar: '编辑内容已保存至草稿', magicbar: '编辑内容已保存至本地' })
    }
  }
})

// Auto-save draft
watchEffect(() => {
  if (editing.value) {
    saveDraft()
  }
})

function toggleClass(cls: any) {
  const idx = form.classes.indexOf(cls)
  if (idx === -1) {
    form.classes = [...form.classes, cls]
  } else {
    form.classes = form.classes.filter(item => item !== cls)
  }
}

function normalizeAssignmentClassNames(assignment: any) {
  if (!assignment) return []
  if (Array.isArray(assignment.classNames) && assignment.classNames.length) {
    return assignment.classNames.filter(Boolean)
  }
  return assignment.className ? assignment.className.split('、').filter(Boolean) : []
}

function normalizeAssignmentClassIds(assignment: any) {
  if (!assignment) return []
  if (Array.isArray(assignment.classIds) && assignment.classIds.length) {
    return assignment.classIds.filter((id: any) => id != null)
  }
  return assignment.classId ? [assignment.classId] : []
}

function formatClassNames(assignment: any) {
  const names = normalizeAssignmentClassNames(assignment)
  return names.length ? names.join('、') : '全部班级'
}

function formatDate(iso: any) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: any) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function toDatetimeLocal(iso: any) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n: any) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function toApiDateTime(value: any) {
  if (!value) return null
  return value.length === 16 ? `${value}:00` : value
}

function autoResize() {
  const el = editorRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

async function onSave() {
  if (!form.title.trim()) {
    notify({ type: 'error', snackbar: '请输入作业名称' })
    return
  }
  if (!form.workType.trim()) {
    notify({ type: 'error', snackbar: '请输入作业类型' })
    return
  }

  saving.value = true
  const selectedClassItems = form.classes
    .map(name => classesAll.value.find((c: any) => c.name === name))
    .filter(Boolean)
  const payload = {
    title: form.title.trim(),
    workType: form.workType.trim(),
    description: form.description.trim(),
    classId: selectedClassItems[0]?.id || null,
    className: selectedClassItems.length ? null : form.classes[0] || null,
    classIds: selectedClassItems.map((item: any) => item.id),
    classNames: selectedClassItems.length ? [] : form.classes,
    dueAt: toApiDateTime(form.dueDate),
  }

  try {
    const saved = isCreate.value
      ? await http.post('/assignments', payload)
      : await http.put(`/assignments/${form.id}`, payload)
    clearDraft()
    triggerRipple(window.innerWidth * 0.75, 200)
    notify({ type: 'success', snackbar: isCreate.value ? '作业已发布' : '作业已更新', magicbar: '作业信息已保存' })
    editing.value = false
    activeId.value = saved.id
    await fetchAssignments()
  } catch (e: any) {
    notify({ type: 'error', snackbar: '保存失败：' + (e.message || '网络异常'), magicbar: '保存作业时遇到了问题' })
  } finally {
    saving.value = false
  }
}

const deleteModal = reactive({ open: false, id: null, title: '' })

function onDeleteClick(a: any) {
  deleteModal.id = a.id
  deleteModal.title = a.title
  deleteModal.open = true
}

const deleteDialogRef = ref<HTMLElement | null>(null)
const deleteCancelRef = ref<HTMLElement | null>(null)

watch(
  () => deleteModal.open,
  async isOpen => {
    if (isOpen) {
      await nextTick()
      deleteCancelRef.value?.focus()
    }
  },
)

function onDeleteKeydown(e: KeyboardEvent) {
  if (e.key === 'Tab') {
    const dialog = deleteDialogRef.value
    if (!dialog) return
    const focusable = dialog.querySelectorAll<HTMLElement>(
      'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
    )
    if (focusable.length < 2) return
    const first = focusable[0]
    const last = focusable[focusable.length - 1]
    if (e.shiftKey && document.activeElement === first) {
      e.preventDefault()
      last.focus()
    } else if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault()
      first.focus()
    }
  }
}

async function onExport(item: any) {
  if (exporting.value) return
  exporting.value = true
  magicBar.status = '导出可能需要时间，休息一下吧'
  magicBar.statusType = 'info'
  try {
    const blob = await http.post('/export/excel', null, { responseType: 'blob' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `成绩汇总_${item.title}_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    notify({ type: 'success', snackbar: '导出成功', magicbar: '导出完成' })
  } catch (e: any) {
    notify({ type: 'error', snackbar: '导出失败：' + (e.message || '网络异常'), magicbar: '导出成绩时遇到了问题' })
  } finally {
    exporting.value = false
  }
}

function confirmDelete() {
  deleteModal.open = false
  onDelete({ id: deleteModal.id, title: deleteModal.title })
}

async function onDelete(a: any) {
  try {
    await http.delete(`/assignments/${a.id}`)
    if (activeId.value === a.id) activeId.value = null
    assignments.value = assignments.value.filter(x => x.id !== a.id)
    notify({ type: 'success', snackbar: `「${a.title}」已删除` })
  } catch (e: any) {
    notify({ type: 'error', snackbar: '删除失败：' + (e.message || '网络异常'), magicbar: '删除作业时遇到了问题' })
  }
}

const refreshTick = inject(REFRESH_TICK_KEY, ref(0))
const rightButtons = inject(RIGHT_BUTTONS_KEY, ref([]))

function buildRightButtons() {
  rightButtons.value = [
    {
      key: 'sort-time',
      icon: 'sort-time',
      label: '按时间排序',
      active: sortKey.value === 'time',
      action: () => {
        sortKey.value = sortKey.value === 'time' ? null : 'time'
        buildRightButtons()
      },
    },
    {
      key: 'sort-submit-rate',
      icon: 'sort-rate',
      label: '按提交率排序',
      active: sortKey.value === 'submitRate',
      action: () => {
        sortKey.value = sortKey.value === 'submitRate' ? null : 'submitRate'
        buildRightButtons()
      },
    },
    {
      key: 'sort-review-rate',
      icon: 'sort-completion',
      label: '按批改率排序',
      active: sortKey.value === 'reviewRate',
      action: () => {
        sortKey.value = sortKey.value === 'reviewRate' ? null : 'reviewRate'
        buildRightButtons()
      },
    },
  ]
}

async function fetchAssignments() {
  loading.value = true
  try {
    const [assignmentRows, subs, evals, students, classes] = await Promise.all([
      http.get('/assignments'),
      http.get('/submissions'),
      http.get('/evaluations'),
      http.get('/students'),
      http.get('/classes'),
    ])

    studentsAll.value = students || []
    classesAll.value = classes || []

    const evalMap: Record<string, any> = {}
    ;(evals || []).forEach((e: any) => {
      evalMap[e.submissionId] = e
    })

    const classStudentCounts: Record<string, number> = {}
    const classStudentCountsById: Record<string, number> = {}
    ;(students || []).forEach((s: any) => {
      const cls = s.className || '未分班'
      classStudentCounts[cls] = (classStudentCounts[cls] || 0) + 1
      if (s.classId != null) {
        const key = String(s.classId)
        classStudentCountsById[key] = (classStudentCountsById[key] || 0) + 1
      }
    })

    const submissionStats: Record<string, any> = {}
    ;(subs || []).forEach((s: any) => {
      if (!s.assignmentId) return
      const key = String(s.assignmentId)
      if (!submissionStats[key]) submissionStats[key] = { count: 0, reviewed: 0, latestTime: '' }
      submissionStats[key].count++
      const ev = evalMap[s.id]
      if (ev && ev.status >= 2) submissionStats[key].reviewed++
      if (s.submittedAt && s.submittedAt > submissionStats[key].latestTime) {
        submissionStats[key].latestTime = s.submittedAt
      }
    })

    assignments.value = (assignmentRows || [])
      .map((a: any) => {
        const stat = submissionStats[String(a.id)] || { count: 0, reviewed: 0, latestTime: '' }
        const classIds = normalizeAssignmentClassIds(a)
        const classNames = normalizeAssignmentClassNames(a)
        const total = classIds.length
          ? classIds.reduce((sum: any, id: any) => sum + (classStudentCountsById[String(id)] || 0), 0)
          : classNames.length
            ? classNames.reduce((sum: any, name: any) => sum + (classStudentCounts[name] || 0), 0)
            : studentsAll.value.length
        return {
          ...a,
          dueDate: a.dueAt,
          submittedCount: stat.count,
          reviewedCount: stat.reviewed,
          totalStudents: total,
          submitRate: total ? Math.round((stat.count / total) * 100) : 0,
          reviewProgress: stat.count ? Math.round((stat.reviewed / stat.count) * 100) : 0,
          latestTime: stat.latestTime || a.updatedAt || a.createdAt,
        }
      })
      .sort((a: any, b: any) => (b.latestTime || '').localeCompare(a.latestTime || ''))
  } finally {
    loading.value = false
  }
}

const magicBar = inject(MAGIC_BAR_KEY)!
const triggerRipple = inject(TRIGGER_RIPPLE_KEY)!

watch(active, a => {
  magicBar.sub = a?.title || ''
})

onMounted(() => {
  magicBar.primary = '作业管理'
  magicBar.sub = active.value?.title || ''
  retryFetch(
    () => fetchAssignments(),
    (e: any) => notify({ type: 'error', snackbar: '作业列表加载失败：' + (e.message || '网络异常'), magicbar: '加载作业列表时遇到了问题' }),
  )
})
onActivated(() => {
  magicBar.primary = '作业管理'
  magicBar.sub = active.value?.title || ''
  buildRightButtons()
})
onDeactivated(() => {
  rightButtons.value = []
})
watch(refreshTick, fetchAssignments)
</script>

<style lang="scss" scoped>
.ap {
  display: flex;
  height: 100%;
  overflow: hidden;

  &__panel {
    flex: 0 0 50%;
    overflow-y: auto;
    min-width: 0;
  }

  &__list {
    padding: 20px 12px 20px 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    background: rgb(var(--md-sys-color-surface-container-lowest));
    border-radius: 16px;

    &-head {
      display: flex;
      align-items: center;
    }
  }

  &__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    background: rgb(var(--md-sys-color-surface-container-lowest));
    border-radius: 16px;
    padding: 12px 16px;
  }

  &__create-btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 40px;
    padding: 0 20px;
    border: none;
    border-radius: 20px;
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    cursor: pointer;
    @include font(14px, 20px, 500);
    transition: box-shadow 0.15s ease;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      box-shadow: 0 0 16px rgb(var(--md-sys-color-primary) / 0.3);
    }
  }

  &__search {
    position: relative;
    display: flex;
    align-items: center;
    flex: 0 1 280px;
    min-width: 0;

    &-icon {
      position: absolute;
      left: 12px;
      width: 16px;
      height: 16px;
      color: rgb(var(--md-sys-color-on-surface-variant));
      pointer-events: none;
    }

    &-input {
      width: 100%;
      height: 36px;
      padding: 0 36px 0 36px;
      border: 1px solid transparent;
      border-radius: 10px;
      background: rgb(var(--md-sys-color-surface-container));
      color: rgb(var(--md-sys-color-on-surface));
      @include font(13px, 20px);
      outline: none;
      transition:
        border-color 0.2s ease,
        background 0.2s ease;

      &::placeholder {
        color: rgb(var(--md-sys-color-on-surface-variant) / 0.5);
      }

      &:focus {
        border-color: rgb(var(--md-sys-color-primary));
        background: rgb(var(--md-sys-color-surface-container-lowest));
      }
    }

    &-clear {
      position: absolute;
      right: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border: none;
      border-radius: 8px;
      background: transparent;
      color: rgb(var(--md-sys-color-on-surface-variant));
      cursor: pointer;
      transition: background 0.15s ease;

      svg {
        width: 14px;
        height: 14px;
      }

      &:hover {
        background: rgb(var(--md-sys-color-on-surface-variant) / 0.12);
      }
    }
  }

  &__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    padding: 80px 0;
    color: rgb(var(--md-sys-color-outline));

    svg {
      width: 56px;
      height: 56px;
    }
    span {
      @include font(14px, 20px);
    }
  }

  &__cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
}

/* ── Empty Search ── */
.empty-search {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 0;
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 16px;

  &__emoji {
    @include font(28px, 36px);
  }

  &__text {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }
}

.ap {
  &__preview {
    padding-left: 12px;
    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &__form {
    padding-left: 12px;
    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

/* ── Assignment Card ── */
.asgn-card {
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: rgb(var(--md-sys-color-surface-container-high));
  }

  &--active {
    background: rgb(var(--md-sys-color-secondary-container));
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 16px 0 16px;
  }

  &__title {
    @include font(15px, 22px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    min-width: 0;
  }

  &__type {
    @include font(11px, 16px, 500);
    padding: 2px 10px;
    border-radius: 10px;
    background: rgb(var(--md-sys-color-tertiary-container));
    color: rgb(var(--md-sys-color-on-tertiary-container));
    flex-shrink: 0;
  }

  &__body {
    padding: 10px 16px 16px 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
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

  &__desc {
    @include font(13px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    padding: 8px 12px;
    background: rgb(var(--md-sys-color-surface-container-high));
    border-radius: 8px;
    margin-top: 2px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
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

/* ── Detail Card ── */
.detail-card {
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

    &-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  &__title {
    @include font(18px, 26px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: 0.02em;
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  &__note {
    &-head {
      @include font(13px, 20px, 500);
      color: rgb(var(--md-sys-color-on-surface-variant));
      background: rgb(var(--md-sys-color-surface-container-high));
      padding: 10px 14px;
      border-radius: 10px 10px 0 0;
    }
    &-body {
      @include font(14px, 20px);
      color: rgb(var(--md-sys-color-on-surface));
      background: rgb(var(--md-sys-color-surface-container-high));
      padding: 10px 14px;
      border-radius: 0 0 10px 10px;
      margin-top: 1px;
      white-space: pre-wrap;
    }
  }
}

/* ── Action buttons ── */
.act-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  @include font(13px, 20px, 500);
  transition: background 0.15s ease;

  svg {
    width: 16px;
    height: 16px;
  }

  &--primary {
    background: rgb(var(--md-sys-color-primary));
    color: rgb(var(--md-sys-color-on-primary));
    &:hover {
      filter: brightness(0.9);
    }
  }

  &--outline {
    background: transparent;
    color: rgb(var(--md-sys-color-on-surface-variant));
    border: 1px solid rgb(var(--md-sys-color-outline));
    &:hover {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }
  }

  &--danger {
    background: transparent;
    color: rgb(var(--md-sys-color-error));
    border: 1px solid rgb(var(--md-sys-color-error));
    &:hover {
      background: rgb(var(--md-sys-color-error) / 0.08);
    }
  }
}

/* ── Info Field ── */
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

/* ── Form Card ── */
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

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-highest));
    }
  }

  &__fields {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__label {
    @include font(13px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &__input {
    height: 44px;
    padding: 0 14px;
    border: 1px solid rgb(var(--md-sys-color-outline-variant));
    border-radius: 10px;
    background: rgb(var(--md-sys-color-surface-container));
    color: rgb(var(--md-sys-color-on-surface));
    @include font(14px, 20px);
    outline: none;

    &:focus {
      border-color: rgb(var(--md-sys-color-primary));
    }

    &::placeholder {
      color: rgb(var(--md-sys-color-on-surface-variant) / 0.6);
    }

    &--area {
      height: auto;
      min-height: 100px;
      padding: 12px 14px;
      resize: none;
      overflow-y: hidden;
    }
  }
}

/* ── Class Card ── */
.class-card {
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 12px;
  overflow: hidden;

  &__section {
    padding: 8px 0;
  }

  &__section-title {
    @include font(11px, 16px, 500);
    color: rgb(var(--md-sys-color-on-surface-variant));
    padding: 4px 14px;
  }

  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    cursor: pointer;
    transition: background 0.1s ease;

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-high));
    }
  }

  &__name {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface));
  }

  &__btn {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    svg {
      width: 16px;
      height: 16px;
    }

    &--add {
      background: rgb(var(--md-sys-color-primary));
      color: rgb(var(--md-sys-color-on-primary));
    }

    &--remove {
      background: rgb(var(--md-sys-color-error-container));
      color: rgb(var(--md-sys-color-on-error-container));
    }
  }

  &__divider {
    border: none;
    height: 1px;
    background: rgb(var(--md-sys-color-outline-variant));
    margin: 0;
  }

  &__empty {
    @include font(13px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    padding: 16px 14px;
    text-align: center;
  }
}

/* ── Delete Modal ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0 0 0 / 0.4);
  z-index: 200;
}

.modal-card {
  width: 340px;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 28px;
  padding: 32px 28px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  text-align: center;

  &__icon {
    width: 48px;
    height: 48px;
    color: rgb(var(--md-sys-color-error));
  }

  &__text {
    @include font(15px, 24px);
    color: rgb(var(--md-sys-color-on-surface));
  }

  &__btns {
    display: flex;
    gap: 12px;
    width: 100%;
    margin-top: 4px;
  }

  &__btn {
    flex: 1;
    height: 40px;
    border: none;
    border-radius: 20px;
    cursor: pointer;
    @include font(14px, 20px, 500);

    &--cancel {
      background: rgb(var(--md-sys-color-surface-container-high));
      color: rgb(var(--md-sys-color-on-surface));
    }

    &--danger {
      background: rgb(var(--md-sys-color-error-container));
      color: rgb(var(--md-sys-color-on-error-container));
    }
  }
}

.modal-enter-active {
  transition: opacity 0.2s ease;
  .modal-card {
    transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }
}
.modal-leave-active {
  transition: opacity 0.15s ease;
  .modal-card {
    transition: transform 0.15s ease;
  }
}
.modal-enter-from {
  opacity: 0;
  .modal-card {
    transform: scale(0.92) translateY(12px);
  }
}
.modal-leave-to {
  opacity: 0;
  .modal-card {
    transform: scale(0.92) translateY(12px);
  }
}

/* ── Preview Placeholder ── */
.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: rgb(var(--md-sys-color-outline));

  svg {
    width: 56px;
    height: 56px;
  }
  span {
    @include font(14px, 20px);
  }
}
</style>
