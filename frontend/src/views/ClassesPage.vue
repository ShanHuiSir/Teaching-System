<template>
  <div class="ap">
    <!-- Panel 1: Class List -->
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
          <span>创建班级</span>
        </button>
        <SearchInput v-model="searchQuery" placeholder="搜索班级名称、年级、备注…" />
      </div>

      <ListSkeleton v-if="loading" />
      <div v-else-if="!classes.length" class="ap__empty">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
          <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
        <span>暂无班级，点击上方按钮创建</span>
      </div>
      <EmptyState v-else-if="!filteredClasses.length && searchQuery" text="这里似乎什么都没有" />

      <div class="ap__cards">
        <div
          v-for="c in filteredClasses"
          :key="c.id"
          class="asgn-card"
          :class="{ 'asgn-card--active': activeId === c.id }"
          @click="onSelectClass(c)"
        >
          <div class="asgn-card__header">
            <h3 class="asgn-card__title">{{ c.name }}</h3>
            <span class="asgn-card__type">{{ c.grade || '—' }}</span>
          </div>
          <div class="asgn-card__body">
            <div class="asgn-card__stats">
              <span class="stat-chip" data-tooltip="学生人数">
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
                </svg>
                <span>{{ c.studentCount }}人</span>
              </span>
            </div>
            <div v-if="c.description" class="asgn-card__desc">{{ c.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Panel 2: Class Detail -->
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

          <div class="detail-card__info">
            <h3 class="detail-card__title">{{ active.name }}</h3>
            <span
              v-if="active.grade"
              class="asgn-card__type"
              style="display: inline-flex; vertical-align: middle; margin-left: 8px"
              >{{ active.grade }}</span
            >
          </div>

          <div class="detail-card__grid">
            <div class="info-field">
              <span class="info-field__label">学生总数</span>
              <span class="info-field__value">{{ active.studentCount }}人</span>
            </div>
            <div class="info-field">
              <span class="info-field__label">年级</span>
              <span class="info-field__value">{{ active.grade || '—' }}</span>
            </div>
          </div>

          <div v-if="active.description" class="detail-card__note">
            <div class="detail-card__note-head">备注</div>
            <div class="detail-card__note-body">{{ active.description }}</div>
          </div>

          <!-- Student Roster -->
          <div class="roster">
            <div class="roster__head">
              <div class="roster__heading">
                <span class="roster__title">学生花名册</span>
                <span class="roster__count">{{ filteredRoster.length }}/{{ active.roster.length }}人</span>
              </div>
              <div class="roster__actions">
                <button class="roster__tool" type="button" :disabled="importingRoster" @click="downloadRosterTemplate">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="7 10 12 15 17 10" />
                    <line x1="12" y1="15" x2="12" y2="3" />
                  </svg>
                  <span>模板</span>
                </button>
                <button class="roster__tool roster__tool--primary" type="button" :disabled="importingRoster" @click="chooseRosterFile">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="17 8 12 3 7 8" />
                    <line x1="12" y1="3" x2="12" y2="15" />
                  </svg>
                  <span>{{ importingRoster ? '导入中' : '导入' }}</span>
                </button>
              </div>
            </div>
            <input
              ref="rosterFileInput"
              class="roster__file"
              type="file"
              accept=".xlsx,.xls,.csv"
              @change="onRosterFileChange"
            />
            <div v-if="rosterImportResult" class="roster__result">
              <span>新增 {{ rosterImportResult.created }} 人，更新 {{ rosterImportResult.updated }} 人，跳过 {{ rosterImportResult.skipped }} 行</span>
              <button v-if="rosterImportResult.messages?.length" type="button" @click="showImportMessages = !showImportMessages">
                {{ showImportMessages ? '收起' : '查看' }}
              </button>
            </div>
            <div v-if="showImportMessages && rosterImportResult?.messages?.length" class="roster__messages">
              <div v-for="msg in rosterImportResult.messages" :key="msg" class="roster__message">{{ msg }}</div>
            </div>
            <input
              v-if="active.roster.length"
              v-model="rosterQuery"
              class="roster__search"
              placeholder="搜索姓名或学号…"
            />
            <div v-if="!active.roster.length" class="roster__empty">暂无学生</div>
            <div v-else-if="!filteredRoster.length" class="roster__empty">无匹配学生</div>
            <div v-else class="roster__table">
              <div class="roster__row roster__row--header">
                <span class="roster__cell roster__cell--no">学号</span>
                <span class="roster__cell roster__cell--name">姓名</span>
              </div>
              <div v-for="s in filteredRoster" :key="s.id" class="roster__row">
                <span class="roster__cell roster__cell--no">{{ s.studentNo || '—' }}</span>
                <span class="roster__cell roster__cell--name">{{ s.name || '—' }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
      <PreviewPlaceholder v-else message="选择一个班级查看详情" />
    </div>

    <!-- Panel 3: Create/Edit Form -->
    <div class="ap__panel ap__form" :style="formStyle">
      <div class="form-card">
        <div class="form-card__bar">
          <button class="form-card__back" @click="closeForm">
            <AppIcon name="chevron-left" />
            <span>关闭{{ isCreate ? '创建' : '编辑' }}</span>
          </button>
          <button class="act-btn act-btn--primary" @click="onSave">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
            <span>{{ isCreate ? '创建' : '保存' }}</span>
          </button>
        </div>

        <div class="form-card__fields">
          <div class="form-field">
            <span class="form-field__label">班级名称</span>
            <input v-model="form.name" class="form-field__input" type="text" placeholder="例：软件工程2101" />
          </div>

          <div class="form-field">
            <span class="form-field__label">年级</span>
            <select v-model="form.grade" class="form-field__input">
              <option value="">未指定</option>
              <option value="大一">大一</option>
              <option value="大二">大二</option>
              <option value="大三">大三</option>
              <option value="大四">大四</option>
            </select>
          </div>

          <div class="form-field">
            <span class="form-field__label">备注</span>
            <textarea
              ref="editorRef"
              v-model="form.description"
              class="form-field__input form-field__input--area"
              placeholder="班级描述或备注信息"
              @input="autoResize"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
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
            <p class="modal-card__text">确定要删除班级「{{ deleteModal.name }}」吗？<br />已有学生或作业关联的班级不会被删除。</p>
            <div class="modal-card__btns">
              <button ref="deleteCancelRef" class="modal-card__btn modal-card__btn--cancel" @click="deleteModal.open = false">取消</button>
              <HedgehogButton variant="primary" size="sm" @complete="confirmDelete">确认删除</HedgehogButton>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import {
  ref,
  reactive,
  computed,
  watch,
  watchEffect,
  inject,
  onMounted,
  onActivated,
  onDeactivated,
  nextTick,
} from 'vue'
import http, { retryFetch } from '../utils/request'

import { useNotify } from '../composables/useNotify'
import { MAGIC_BAR_KEY, TRIGGER_RIPPLE_KEY, RIGHT_BUTTONS_KEY } from '../types'
import HedgehogButton from '../components/HedgehogButton.vue'
import EmptyState from '../components/EmptyState.vue'
import SearchInput from '../components/SearchInput.vue'
import PreviewPlaceholder from '../components/PreviewPlaceholder.vue'
import ListSkeleton from '../components/ListSkeleton.vue'
import AppIcon from '../components/AppIcon.vue'
import { fetchVersion, fetchStudents, fetchClasses as storeFetchClasses, students as allStudents, classes as allClasses } from '../stores/data'
// ConfirmDialog reserved for delete modal

const { notify } = useNotify()

const loading = ref(false)
const classes = ref<any[]>([])
const activeId = ref(null)
const editing = ref(false)
const isCreate = ref(true)
const editorRef = ref(null)
const sortKey = ref<string | null>(null)

const active = computed(() => classes.value.find(c => c.id === activeId.value) || null)
const rosterQuery = ref('')
const rosterFileInput = ref<HTMLInputElement | null>(null)
const importingRoster = ref(false)
const showImportMessages = ref(false)
const rosterImportResult = ref<{
  total: number
  created: number
  updated: number
  skipped: number
  messages: string[]
} | null>(null)
const filteredRoster = computed(() => {
  if (!active.value) return []
  const q = rosterQuery.value.trim().toLowerCase()
  if (!q) return active.value.roster
  return active.value.roster.filter((s: any) =>
    (s.name || '').toLowerCase().includes(q) || (s.studentNo || '').toLowerCase().includes(q),
  )
})

const searchQuery = ref('')

const filteredClasses = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  let arr = classes.value
  if (q) {
    arr = arr.filter(
      c =>
        (c.name || '').toLowerCase().includes(q) ||
        (c.grade || '').toLowerCase().includes(q) ||
        (c.description || '').toLowerCase().includes(q) ||
        c.roster.some((s: any) => (s.name || '').toLowerCase().includes(q) || (s.studentNo || '').toLowerCase().includes(q)),
    )
  }
  arr = [...arr]
  if (sortKey.value === 'count') {
    arr.sort((a, b) => b.studentCount - a.studentCount)
  }
  return arr
})

const previewStyle = computed(() => ({
  transform: editing.value ? 'translateX(100%)' : activeId.value ? 'translateX(0)' : 'translateX(100%)',
}))

const formStyle = computed(() => ({
  transform: editing.value ? 'translateX(-100%)' : 'translateX(0)',
}))

const form = reactive({
  id: null,
  name: '',
  grade: '',
  description: '',
})

function resetForm() {
  form.id = null
  form.name = ''
  form.grade = ''
  form.description = ''
}

function onSelectClass(c: any) {
  editing.value = false
  activeId.value = c.id
  rosterImportResult.value = null
  showImportMessages.value = false
}

function chooseRosterFile() {
  rosterFileInput.value?.click()
}

async function onRosterFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !active.value) return

  const formData = new FormData()
  formData.append('file', file)
  importingRoster.value = true
  rosterImportResult.value = null
  showImportMessages.value = false
  try {
    const result = await http.post(`/classes/${active.value.id}/students/import`, formData)
    rosterImportResult.value = result
    await fetchStudents(true)
    await fetchClasses()
    notify({
      type: 'success',
      snackbar: `花名册已导入：新增 ${result.created} 人，更新 ${result.updated} 人`,
      magicbar: `花名册已导入 ${result.total} 人`,
    })
  } catch (e: any) {
    notify({ type: 'error', snackbar: '导入失败：' + (e.message || '网络异常'), magicbar: '导入失败：' + (e.message || '网络异常') })
  } finally {
    importingRoster.value = false
  }
}

function downloadRosterTemplate() {
  const csv = '\uFEFF学号,姓名\n2026001,张三\n2026002,李四\n'
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${active.value?.name || '班级'}-花名册模板.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const DRAFT_PREFIX = 'cp_draft'

function draftKey() {
  return form.id ? `${DRAFT_PREFIX}_${form.id}` : DRAFT_PREFIX
}

function loadDraft() {
  const raw = localStorage.getItem(draftKey())
  if (!raw) return false
  try {
    const d = JSON.parse(raw)
    form.name = d.name || ''
    form.grade = d.grade || ''
    form.description = d.description || ''
    return true
  } catch {
    return false
  }
}

function saveDraft() {
  localStorage.setItem(
    draftKey(),
    JSON.stringify({
      name: form.name,
      grade: form.grade,
      description: form.description,
    }),
  )
}

function clearDraft() {
  localStorage.removeItem(draftKey())
}

function startCreate() {
  isCreate.value = true
  activeId.value = null
  resetForm()
  if (!loadDraft()) resetForm()
  editing.value = true
  nextTick(autoResize)
}

function startEdit(c: any) {
  isCreate.value = false
  resetForm()
  form.id = c.id
  const hasDraft = loadDraft()
  if (!hasDraft) {
    form.name = c.name
    form.grade = c.grade || ''
    form.description = c.description || ''
  }
  editing.value = true
  nextTick(autoResize)
}

function closeForm() {
  editing.value = false
}

watch(editing, (val, old) => {
  if (old && !val) {
    if (localStorage.getItem(draftKey())) {
      notify({ type: 'info', snackbar: '编辑内容已保存至草稿', magicbar: '编辑内容已保存至本地' })
    }
  }
})

watchEffect(() => {
  if (editing.value) saveDraft()
})

function autoResize() {
  const el = editorRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

async function onSave() {
  if (!form.name.trim()) {
    notify({ type: 'error', snackbar: '请输入班级名称' })
    return
  }
  const payload = {
    name: form.name.trim(),
    grade: form.grade || null,
    description: form.description.trim(),
  }

  try {
    const saved = isCreate.value
      ? await http.post('/classes', payload)
      : await http.put(`/classes/${form.id}`, payload)
    clearDraft()
    triggerRipple(window.innerWidth * 0.75, 200)
    notify({ type: 'success', snackbar: isCreate.value ? '班级已创建' : '班级已更新', magicbar: '班级信息已保存' })
    editing.value = false
    activeId.value = saved.id
    await fetchClasses()
  } catch (e: any) {
    notify({ type: 'error', snackbar: '保存失败：' + (e.message || '网络异常'), magicbar: '保存失败：' + (e.message || '网络异常') })
  }
}

const deleteModal = reactive({ open: false, id: null, name: '' })

function onDeleteClick(c: any) {
  deleteModal.id = c.id
  deleteModal.name = c.name
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

async function confirmDelete() {
  deleteModal.open = false
  const c = classes.value.find(x => x.id === deleteModal.id)
  if (!c) return
  try {
    await http.delete(`/classes/${c.id}`)
    if (activeId.value === c.id) activeId.value = null
    classes.value = classes.value.filter(x => x.id !== c.id)
    notify({ type: 'success', snackbar: `「${c.name}」已删除` })
  } catch (e: any) {
    notify({ type: 'error', snackbar: '删除失败：' + (e.message || '网络异常'), magicbar: '删除失败：' + (e.message || '网络异常') })
  }
}

const rightButtons = inject(RIGHT_BUTTONS_KEY, ref([]))

function buildRightButtons() {
  rightButtons.value = [
    {
      key: 'cp-sort-count',
      icon: 'sort-count',
      label: '按人数排序',
      active: sortKey.value === 'count',
      action: () => {
        sortKey.value = sortKey.value === 'count' ? null : 'count'
        buildRightButtons()
      },
    },
  ]
}

// ── Status badge helpers ──
// ── Data fetching ──
async function fetchClasses() {
  loading.value = true
  try {
    await Promise.all([fetchStudents(), storeFetchClasses()])
    const classRows = allClasses.value
    const students = allStudents.value

    // Group students by formal classId first, then className for legacy rows.
    const classMap: Record<string, any[]> = {}
    ;(students || []).forEach((s: any) => {
      const key = s.classId ? `id:${s.classId}` : `name:${s.className || '未分班'}`
      if (!classMap[key]) classMap[key] = []
      classMap[key].push(s)
    })

    classes.value = (classRows || []).map((c: any) => {
      const roster = classMap[`id:${c.id}`] || classMap[`name:${c.name}`] || []
      return {
        ...c,
        studentCount: roster.length,
        roster: roster.map(s => ({
          id: s.id,
          name: s.name || '',
          studentNo: s.studentNo || '',
        })),
      }
    })
  } finally {
    loading.value = false
  }
}

const magicBar = inject(MAGIC_BAR_KEY)!
const triggerRipple = inject(TRIGGER_RIPPLE_KEY)!

watch(active, c => {
  magicBar.sub = c?.name || ''
})

onMounted(() => {
  magicBar.primary = '班级管理'
  magicBar.sub = active.value?.name || ''
  retryFetch(
    () => fetchClasses(),
    (e: any) => notify({ type: 'error', snackbar: '班级列表加载失败：' + (e.message || '网络异常'), magicbar: '加载失败：' + (e.message || '网络异常') }),
  )
})
onActivated(() => {
  magicBar.primary = '班级管理'
  magicBar.sub = active.value?.name || ''
  buildRightButtons()
})
onDeactivated(() => {
  rightButtons.value = []
})
watch(fetchVersion, fetchClasses)
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

/* ── Class Card ── */
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

  &__info {
    display: flex;
    align-items: center;
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
    appearance: none;

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

select.form-field__input {
  cursor: pointer;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23636e6e' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  padding-right: 34px;
}

/* ── Student Roster ── */
.roster {
  background: rgb(var(--md-sys-color-surface-container));
  border-radius: 12px;
  overflow: hidden;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 14px 16px;
  }

  &__heading {
    display: flex;
    align-items: baseline;
    gap: 8px;
    min-width: 0;
  }

  &__title {
    @include font(14px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface));
  }

  &__count {
    @include font(12px, 16px);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &__actions {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  &__tool {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    height: 32px;
    padding: 0 10px;
    border: 1px solid rgb(var(--md-sys-color-outline-variant));
    border-radius: 8px;
    background: rgb(var(--md-sys-color-surface-container-lowest));
    color: rgb(var(--md-sys-color-on-surface));
    cursor: pointer;
    @include font(12px, 16px, 500);
    transition:
      background 0.15s ease,
      border-color 0.15s ease;

    svg {
      width: 14px;
      height: 14px;
      flex-shrink: 0;
    }

    &:hover:not(:disabled) {
      border-color: rgb(var(--md-sys-color-primary));
      background: rgb(var(--md-sys-color-surface-container-high));
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.55;
    }

    &--primary {
      border-color: transparent;
      background: rgb(var(--md-sys-color-primary));
      color: rgb(var(--md-sys-color-on-primary));
    }
  }

  &__file {
    display: none;
  }

  &__result {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin: 0 8px 8px;
    padding: 8px 10px;
    border-radius: 8px;
    background: rgb(var(--md-sys-color-secondary-container));
    color: rgb(var(--md-sys-color-on-secondary-container));

    span {
      @include font(12px, 18px);
    }

    button {
      border: none;
      background: transparent;
      color: inherit;
      cursor: pointer;
      @include font(12px, 18px, 500);
      flex-shrink: 0;
    }
  }

  &__messages {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin: 0 8px 8px;
    padding: 8px 10px;
    border-radius: 8px;
    background: rgb(var(--md-sys-color-surface-container-high));
  }

  &__message {
    @include font(12px, 18px);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }

  &__search {
    width: calc(100% - 16px);
    margin: 0 8px 8px;
    height: 32px;
    padding: 0 12px;
    border: 1px solid rgb(var(--md-sys-color-outline-variant));
    border-radius: 8px;
    background: rgb(var(--md-sys-color-surface-container-lowest));
    color: rgb(var(--md-sys-color-on-surface));
    @include font(12px, 18px);
    outline: none;
    transition: border-color 0.15s ease;
    &::placeholder { color: rgb(var(--md-sys-color-on-surface-variant) / 0.5); }
    &:focus { border-color: rgb(var(--md-sys-color-primary)); }
    &:focus-visible { outline: none; }
  }

  &__empty {
    @include font(13px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    text-align: center;
    padding: 32px 16px;
  }

  &__table {
    display: flex;
    flex-direction: column;
  }

  &__row {
    display: flex;
    align-items: center;
    padding: 0 16px;
    height: 40px;
    transition: background 0.1s ease;

    &:hover {
      background: rgb(var(--md-sys-color-surface-container-high));
    }

    &--header {
      border-bottom: 1px solid rgb(var(--md-sys-color-outline-variant));
      &:hover {
        background: transparent;
      }
    }
  }

  &__cell {
    @include font(13px, 20px);
    color: rgb(var(--md-sys-color-on-surface));

    .roster__row--header & {
      @include font(11px, 16px, 500);
      color: rgb(var(--md-sys-color-on-surface-variant));
      text-transform: uppercase;
    }

    &--no {
      flex: 0 0 120px;
    }
    &--name {
      flex: 1;
    }
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
