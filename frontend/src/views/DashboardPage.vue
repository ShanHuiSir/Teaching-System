<template>
  <div class="dash">
    <h1 class="dash__greeting">{{ greeting }}，{{ teacherName }}老师</h1>

    <div class="dash__cards">
      <div class="stat-card" @click="router.push('/review?filter=pending')">
        <span class="stat-card__title">待复审</span>
        <span class="stat-card__num stat-card__num--primary">{{ stats.aiReviewed }}</span>
        <div class="stat-card__overlay">
          <button class="stat-card__btn">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" /></svg
            ><span>查看</span>
          </button>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/review?filter=none')">
        <span class="stat-card__title">未审批</span>
        <span class="stat-card__num stat-card__num--error">{{ stats.pending }}</span>
        <div class="stat-card__overlay">
          <button class="stat-card__btn">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" /></svg
            ><span>查看</span>
          </button>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/review?filter=all')">
        <span class="stat-card__title">已提交</span>
        <span class="stat-card__num">{{ stats.submitted }}</span>
        <div class="stat-card__overlay">
          <button class="stat-card__btn">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" /></svg
            ><span>查看</span>
          </button>
        </div>
      </div>
      <div class="stat-card" @click="router.push('/classes')">
        <span class="stat-card__title">学生总数</span>
        <span class="stat-card__num stat-card__num--primary">{{ stats.studentCount }}</span>
        <div class="stat-card__overlay">
          <button class="stat-card__btn">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" /></svg
            ><span>查看</span>
          </button>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-card__title">教师均分</span>
        <span class="stat-card__num">{{ stats.avgScore }}</span>
      </div>
    </div>

    <!-- Class-level stats -->
    <section v-if="showClassStats" class="dash__section">
      <h2 class="dash__section-title">班级统计</h2>
      <div class="class-grid">
        <div v-for="c in classStats" :key="c.name" class="class-card">
          <span class="class-card__name">{{ c.name }}</span>
          <div class="class-card__bars">
            <div class="class-card__bar-row">
              <span class="class-card__bar-label">提交率</span>
              <div class="class-card__bar-track">
                <div class="class-card__bar-fill" :style="{ width: c.submitRate + '%' }" />
              </div>
              <span class="class-card__bar-val">{{ c.submitRate }}%</span>
            </div>
            <div class="class-card__bar-row">
              <span class="class-card__bar-label">审批率</span>
              <div class="class-card__bar-track">
                <div class="class-card__bar-fill class-card__bar-fill--alt" :style="{ width: c.reviewRate + '%' }" />
              </div>
              <span class="class-card__bar-val">{{ c.reviewRate }}%</span>
            </div>
          </div>
          <span class="class-card__avg">平均分 {{ c.avgScore }}</span>
        </div>
      </div>
    </section>

    <!-- Work type stats -->
    <section v-if="showWorkType" class="dash__section">
      <h2 class="dash__section-title">作业类型排行</h2>
      <div class="work-grid">
        <div v-for="(w, i) in workTypeStats" :key="w.type" class="work-row">
          <span class="work-row__rank">#{{ i + 1 }}</span>
          <span class="work-row__type">{{ w.type }}</span>
          <span class="work-row__count">{{ w.count }} 份</span>
          <span class="work-row__avg">均分 {{ w.avgScore }}</span>
        </div>
      </div>
    </section>

    <!-- Score Distribution -->
    <section v-if="showScoreDist" class="dash__section">
      <div class="dash__section-head">
        <h2 class="dash__section-title">分数段分布</h2>
        <div class="toggle-group">
          <button class="toggle-btn" :class="{ 'toggle-btn--active': chartMode === 'bar' }" @click="chartMode = 'bar'">
            直方图
          </button>
          <button class="toggle-btn" :class="{ 'toggle-btn--active': chartMode === 'pie' }" @click="chartMode = 'pie'">
            饼图
          </button>
        </div>
      </div>
      <div v-if="chartMode === 'bar'" class="histogram">
        <div v-for="b in scoreBuckets" :key="b.label" class="histogram__bar">
          <span class="histogram__count">{{ b.count }}</span>
          <div class="histogram__fill" :style="{ height: maxBucket ? (b.count / maxBucket) * 160 + 'px' : '0' }" />
          <span class="histogram__label">{{ b.label }}</span>
        </div>
      </div>
      <div v-else class="pie-chart">
        <div class="pie-chart__circle" :style="{ background: pieGradient }" />
        <div class="pie-legend">
          <span v-for="b in scoreBuckets" :key="b.label" class="pie-legend__item">
            <i class="pie-legend__dot" :style="{ background: bucketColor(b.label) }" />{{ b.label }} {{ b.count }}
          </span>
        </div>
      </div>
    </section>

    <!-- AI vs Teacher deviation -->
    <section v-if="showDeviation" class="dash__section">
      <h2 class="dash__section-title">AI vs 教师评分偏差</h2>
      <div v-if="deviations.length" class="dev-grid">
        <div v-for="d in deviations" :key="d.title" class="dev-item">
          <span class="dev-item__title">{{ d.title }}</span>
          <div class="dev-item__scores">
            <span class="dev-item__score dev-item__score--ai">AI {{ d.aiScore }}</span>
            <span
              class="dev-item__diff"
              :class="{ 'dev-item__diff--up': d.diff > 0, 'dev-item__diff--down': d.diff < 0 }"
              >{{ d.diff > 0 ? '+' : '' }}{{ d.diff }}</span
            >
            <span class="dev-item__score dev-item__score--teacher">教师 {{ d.teacherScore }}</span>
          </div>
        </div>
      </div>
      <p v-else class="dash__empty">暂无已完成的评价数据</p>
    </section>

    <!-- Teacher Work Data -->
    <section v-if="showTeacherWork" class="dash__section">
      <h2 class="dash__section-title">教师工作数据</h2>

      <div class="twd-grid">
        <!-- Review trend -->
        <div class="twd-card">
          <h3 class="twd-card__title">审批量趋势</h3>
          <div v-if="trendData.length" class="trend-bars">
            <div v-for="t in trendData" :key="t.date" class="trend-bar">
              <span class="trend-bar__count">{{ t.count }}</span>
              <div class="trend-bar__fill" :style="{ height: trendMax ? (t.count / trendMax) * 120 + 'px' : '0' }" />
              <span class="trend-bar__date">{{ t.date }}</span>
            </div>
          </div>
          <p v-else class="dash__empty">暂无数据</p>
        </div>

        <!-- Score comparison -->
        <div class="twd-card">
          <h3 class="twd-card__title">评分分布对比</h3>
          <div v-if="scoreCompare.length" class="compare-chart">
            <div v-for="s in scoreCompare" :key="s.label" class="compare-row">
              <span class="compare-row__label">{{ s.label }}</span>
              <div class="compare-row__bars">
                <div class="compare-row__bar-wrap">
                  <div
                    class="compare-row__bar compare-row__bar--ai"
                    :style="{ width: cmpMax ? (s.ai / cmpMax) * 100 + '%' : '0%' }"
                  />
                  <span class="compare-row__val">{{ s.ai }}</span>
                </div>
                <div class="compare-row__bar-wrap">
                  <div
                    class="compare-row__bar compare-row__bar--teacher"
                    :style="{ width: cmpMax ? (s.teacher / cmpMax) * 100 + '%' : '0%' }"
                  />
                  <span class="compare-row__val">{{ s.teacher }}</span>
                </div>
              </div>
            </div>
            <div class="compare-legend">
              <span class="compare-legend__item"><i class="compare-legend__dot compare-legend__dot--ai" />AI 评分</span>
              <span class="compare-legend__item"
                ><i class="compare-legend__dot compare-legend__dot--teacher" />教师评分</span
              >
            </div>
          </div>
          <p v-else class="dash__empty">暂无数据</p>
        </div>
      </div>
    </section>

    <!-- Time Trends -->
    <section v-if="showTrends" class="dash__section">
      <h2 class="dash__section-title">时间趋势分析</h2>
      <div class="twd-grid">
        <div class="twd-card">
          <h3 class="twd-card__title">提交量趋势</h3>
          <div v-if="subTrendData.length" class="trend-bars">
            <div v-for="t in subTrendData" :key="t.date" class="trend-bar">
              <span class="trend-bar__count">{{ t.count }}</span>
              <div
                class="trend-bar__fill trend-bar__fill--sub"
                :style="{ height: subTrendMax ? (t.count / subTrendMax) * 120 + 'px' : '0' }"
              />
              <span class="trend-bar__date">{{ t.date }}</span>
            </div>
          </div>
          <p v-else class="dash__empty">暂无数据</p>
        </div>

        <div class="twd-card">
          <h3 class="twd-card__title">审批效率（提交→确认 · 天）</h3>
          <div v-if="efficiencyData.length" class="trend-bars">
            <div v-for="t in efficiencyData" :key="t.date" class="trend-bar">
              <span class="trend-bar__count">{{ t.days }}d</span>
              <div
                class="trend-bar__fill trend-bar__fill--eff"
                :style="{ height: effMax ? (t.days / effMax) * 120 + 'px' : '0' }"
              />
              <span class="trend-bar__date">{{ t.date }}</span>
            </div>
          </div>
          <p v-else class="dash__empty">暂无数据</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onActivated, onDeactivated, inject, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCookie, setCookie } from '../utils/cookie'
import { startRecoveryPoll } from '../utils/recoveryPoll'
import { useSnackbar } from '../composables/useSnackbar'
import http from '../utils/request'
import { MAGIC_BAR_KEY, SHOW_GREETING_KEY, REFRESH_TICK_KEY, RIGHT_BUTTONS_KEY } from '../types'

const router = useRouter()
const snackbar = useSnackbar()

const teacherName = computed(() => getCookie('user_name') || 'teacher')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const stats = ref({ pending: 0, aiReviewed: 0, submitted: 0, studentCount: 0, avgScore: '—' })
const classStats = ref([])
const workTypeStats = ref([])
const scoreBuckets = ref([
  { label: '0-59', count: 0 },
  { label: '60-69', count: 0 },
  { label: '70-79', count: 0 },
  { label: '80-89', count: 0 },
  { label: '90-100', count: 0 },
])
const deviations = ref([])
const trendData = ref([])
const scoreCompare = ref([])
const subTrendData = ref([])
const efficiencyData = ref([])
const chartMode = ref('bar')

// Section visibility
const showClassStats = ref(getCookie('dash_class') !== '0')
const showWorkType = ref(getCookie('dash_work') !== '0')
const showScoreDist = ref(getCookie('dash_score') !== '0')
const showDeviation = ref(getCookie('dash_dev') !== '0')
const showTeacherWork = ref(getCookie('dash_tw') !== '0')
const showTrends = ref(getCookie('dash_trend') !== '0')

function saveVis() {
  setCookie('dash_class', showClassStats.value ? '1' : '0', 30)
  setCookie('dash_work', showWorkType.value ? '1' : '0', 30)
  setCookie('dash_score', showScoreDist.value ? '1' : '0', 30)
  setCookie('dash_dev', showDeviation.value ? '1' : '0', 30)
  setCookie('dash_tw', showTeacherWork.value ? '1' : '0', 30)
  setCookie('dash_trend', showTrends.value ? '1' : '0', 30)
}

function toggleVis(ref) {
  ref.value = !ref.value
  saveVis()
  buildRightButtons()
}

const rightButtons = inject(RIGHT_BUTTONS_KEY, ref([]))

function buildRightButtons() {
  rightButtons.value = [
    {
      key: 'dash-class',
      icon: 'dash-class',
      label: '班级统计',
      active: showClassStats.value,
      action: () => toggleVis(showClassStats),
    },
    {
      key: 'dash-work',
      icon: 'dash-work',
      label: '作业类型排行',
      active: showWorkType.value,
      action: () => toggleVis(showWorkType),
    },
    {
      key: 'dash-score',
      icon: 'dash-score',
      label: '分数段分布',
      active: showScoreDist.value,
      action: () => toggleVis(showScoreDist),
    },
    {
      key: 'dash-dev',
      icon: 'dash-dev',
      label: '评分偏差',
      active: showDeviation.value,
      action: () => toggleVis(showDeviation),
    },
    {
      key: 'dash-tw',
      icon: 'dash-tw',
      label: '教师工作数据',
      active: showTeacherWork.value,
      action: () => toggleVis(showTeacherWork),
    },
    {
      key: 'dash-trend',
      icon: 'dash-trend',
      label: '时间趋势分析',
      active: showTrends.value,
      action: () => toggleVis(showTrends),
    },
  ]
}

const maxBucket = computed(() => Math.max(...scoreBuckets.value.map(b => b.count), 1))
const trendMax = computed(() => Math.max(...trendData.value.map(t => t.count), 1))
const cmpMax = computed(() => Math.max(...scoreCompare.value.flatMap(s => [s.ai, s.teacher]), 1))
const subTrendMax = computed(() => Math.max(...subTrendData.value.map(t => t.count), 1))
const effMax = computed(() => Math.max(...efficiencyData.value.map(t => t.days), 1))

const COLORS = ['#1A56DB', '#0D9488', '#F59E0B', '#DC2626', '#7C3AED']

function bucketColor(label) {
  const map = { '0-59': COLORS[3], '60-69': COLORS[2], '70-79': COLORS[4], '80-89': COLORS[1], '90-100': COLORS[0] }
  return map[label] || '#999'
}

const pieGradient = computed(() => {
  const total = scoreBuckets.value.reduce((s, b) => s + b.count, 0) || 1
  if (total === 0) return 'rgb(var(--md-sys-color-surface-container-high))'
  let acc = 0
  const stops = scoreBuckets.value
    .filter(b => b.count > 0)
    .map(b => {
      const start = (acc / total) * 360
      acc += b.count
      const end = (acc / total) * 360
      return `${bucketColor(b.label)} ${start}deg ${end}deg`
    })
  return `conic-gradient(${stops.join(', ')})`
})

const refreshTick = inject(REFRESH_TICK_KEY, ref(0))

async function fetchAll() {
  try {
    const [summary, subs, evals, students] = await Promise.all([
      http.get('/statistics/summary'),
      http.get('/submissions'),
      http.get('/evaluations'),
      http.get('/students'),
    ])
    stats.value = {
      aiReviewed: summary.aiEvaluatedCount - summary.teacherConfirmedCount,
      pending: summary.submissionCount - summary.aiEvaluatedCount,
      submitted: summary.submissionCount,
      studentCount: summary.studentCount,
      avgScore: summary.averageTeacherScore != null ? Number(summary.averageTeacherScore).toFixed(1) : '—',
    }
    magicBar.count = stats.value.aiReviewed || 0

    const evalMap = {}
    ;(evals || []).forEach(e => {
      evalMap[e.submissionId] = e
    })
    const studentMap = {}
    ;(students || []).forEach(s => {
      studentMap[s.id] = s
    })

    // Class stats
    const classMap: Record<string, any> = {}
    ;(students || []).forEach((s: any) => {
      const cls = s.className || '未分班'
      if (!classMap[cls]) classMap[cls] = { count: 0, submitted: 0, reviewed: 0, scores: [] }
      classMap[cls].count++
    })
    ;(subs || []).forEach(s => {
      const st = studentMap[s.studentId]
      const cls = st?.className || '未分班'
      if (!classMap[cls]) classMap[cls] = { count: 0, submitted: 0, reviewed: 0, scores: [] }
      classMap[cls].submitted++
      const ev = evalMap[s.id]
      if (ev) {
        if (ev.status >= 2) classMap[cls].reviewed++
        if (ev.teacherScore != null) classMap[cls].scores.push(ev.teacherScore)
      }
    })
    classStats.value = Object.entries(classMap).map(([name, d]) => ({
      name,
      submitRate: d.count ? Math.round((d.submitted / d.count) * 100) : 0,
      reviewRate: d.submitted ? Math.round((d.reviewed / d.submitted) * 100) : 0,
      avgScore: d.scores.length ? (d.scores.reduce((a, b) => a + b, 0) / d.scores.length).toFixed(1) : '—',
    }))

    // Work type stats
    const workMap: Record<string, any> = {}
    ;(subs || []).forEach((s: any) => {
      const type = s.workType || '其他'
      if (!workMap[type]) workMap[type] = { count: 0, scores: [] }
      workMap[type].count++
      const ev = evalMap[s.id]
      if (ev?.teacherScore != null) workMap[type].scores.push(ev.teacherScore)
    })
    workTypeStats.value = Object.entries(workMap)
      .map(([type, d]) => ({
        type,
        count: d.count,
        avgScore: d.scores.length ? (d.scores.reduce((a, b) => a + b, 0) / d.scores.length).toFixed(1) : '—',
      }))
      .sort((a, b) => b.count - a.count)

    // Score distribution (teacherScore)
    const buckets = [0, 0, 0, 0, 0]
    ;(evals || []).forEach(e => {
      const s = e.teacherScore
      if (s == null) return
      if (s < 60) buckets[0]++
      else if (s < 70) buckets[1]++
      else if (s < 80) buckets[2]++
      else if (s < 90) buckets[3]++
      else buckets[4]++
    })
    scoreBuckets.value = [
      { label: '0-59', count: buckets[0] },
      { label: '60-69', count: buckets[1] },
      { label: '70-79', count: buckets[2] },
      { label: '80-89', count: buckets[3] },
      { label: '90-100', count: buckets[4] },
    ]

    // AI vs Teacher deviations
    const devs = []
    ;(subs || []).forEach(s => {
      const ev = evalMap[s.id]
      if (ev?.aiScore != null && ev?.teacherScore != null) {
        devs.push({
          title: s.title || s.fileName || '未命名',
          aiScore: ev.aiScore,
          teacherScore: ev.teacherScore,
          diff: ev.teacherScore - ev.aiScore,
        })
      }
    })
    deviations.value = devs.slice(0, 10)

    // Teacher review trend — group by date
    const trendMap = {}
    ;(evals || []).forEach(e => {
      if (e.status >= 2 && e.createdAt) {
        const d = e.createdAt.slice(0, 10)
        trendMap[d] = (trendMap[d] || 0) + 1
      }
    })
    const trendKeys = Object.keys(trendMap).sort()
    trendData.value = trendKeys.slice(-14).map(d => ({ date: d.slice(5), count: trendMap[d] }))

    // Submission volume trend
    const subMap = {}
    ;(subs || []).forEach(s => {
      if (s.submittedAt) {
        const d = s.submittedAt.slice(0, 10)
        subMap[d] = (subMap[d] || 0) + 1
      }
    })
    const subKeys = Object.keys(subMap).sort()
    subTrendData.value = subKeys.slice(-14).map(d => ({ date: d.slice(5), count: subMap[d] }))

    // Teacher review efficiency — avg days from submission to teacher confirmation
    const effMap = {}
    const effCountMap = {}
    ;(subs || []).forEach(s => {
      const ev = evalMap[s.id]
      if (ev?.status >= 2 && s.submittedAt) {
        const subDate = new Date(s.submittedAt)
        const reviewDate: any = ev.updatedAt ? new Date(ev.updatedAt) : new Date()
        const days = Math.round((reviewDate - (subDate as any)) / 864e5)
        const d = s.submittedAt.slice(0, 10)
        effMap[d] = (effMap[d] || 0) + days
        effCountMap[d] = (effCountMap[d] || 0) + 1
      }
    })
    const effKeys = Object.keys(effMap).sort()
    efficiencyData.value = effKeys.slice(-14).map(d => ({
      date: d.slice(5),
      days: Math.round(effMap[d] / effCountMap[d]),
    }))

    // Score comparison — AI vs Teacher in same buckets
    const cmp = [
      { label: '0-59', ai: 0, teacher: 0 },
      { label: '60-69', ai: 0, teacher: 0 },
      { label: '70-79', ai: 0, teacher: 0 },
      { label: '80-89', ai: 0, teacher: 0 },
      { label: '90-100', ai: 0, teacher: 0 },
    ]
    ;(evals || []).forEach(e => {
      const bucket = s => (s < 60 ? 0 : s < 70 ? 1 : s < 80 ? 2 : s < 90 ? 3 : 4)
      if (e.aiScore != null) cmp[bucket(e.aiScore)].ai++
      if (e.teacherScore != null) cmp[bucket(e.teacherScore)].teacher++
    })
    scoreCompare.value = cmp
  } catch (e) {
    snackbar.show('数据加载失败：' + (e.message || '网络异常'), { variant: 'error' })
    startRecoveryPoll(() => {
      snackbar.show('已恢复连接', { variant: 'info' })
      fetchAll()
    })
  }
}

const magicBar = inject(MAGIC_BAR_KEY)!
const showGreeting = inject(SHOW_GREETING_KEY)!

onMounted(async () => {
  magicBar.primary = '仪表盘'
  magicBar.sub = ''
  await fetchAll()
  showGreeting('仪表盘')
})
onActivated(() => {
  magicBar.primary = '仪表盘'
  magicBar.sub = ''
  buildRightButtons()
})
onDeactivated(() => {
  rightButtons.value = []
})
watch(refreshTick, fetchAll)
</script>

<style lang="scss" scoped>
.dash {
  &__greeting {
    @include font(24px, 32px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    letter-spacing: 0.02em;
  }

  &__cards {
    display: flex;
    flex-wrap: wrap;
    gap: 24px;
    margin-top: 32px;
  }

  &__section {
    margin-top: 40px;

    &-title {
      @include font(18px, 26px, 500);
      color: rgb(var(--md-sys-color-on-surface));
      margin-bottom: 16px;
    }

    &-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }
  }

  &__empty {
    @include font(14px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }
}

.stat-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 180px;
  height: 180px;
  border-radius: 24px;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  overflow: hidden;

  &:hover .stat-card__overlay {
    transform: translateY(0);
  }

  &__overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0 0 0 / 0.06);
    transform: translateY(100%);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    height: 38px;
    padding: 0 20px;
    border: 1px solid rgb(var(--md-sys-color-outline));
    border-radius: 20px;
    background: rgb(var(--md-sys-color-surface-container-lowest));
    color: rgb(var(--md-sys-color-on-surface));
    cursor: pointer;
    transition: background 0.15s ease;
    svg {
      width: 18px;
      height: 18px;
    }
    span {
      @include font(13px, 20px, 500);
    }
    &:hover {
      background: rgb(var(--md-sys-color-surface-container-high));
    }
  }

  &__title {
    @include font(14px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }
  &__num {
    @include font(48px, 56px, 700);
    color: rgb(var(--md-sys-color-on-surface));
  }
  &__num--error {
    color: rgb(var(--md-sys-color-error));
  }
  &__num--primary {
    color: rgb(var(--md-sys-color-primary));
  }
}

/* ── Class stats ── */
.class-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.class-card {
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 16px;
  padding: 20px;

  &__name {
    @include font(15px, 22px, 500);
    color: rgb(var(--md-sys-color-on-surface));
  }
  &__bars {
    margin: 14px 0 10px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__bar-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  &__bar-label {
    @include font(12px, 16px);
    color: rgb(var(--md-sys-color-on-surface-variant));
    width: 42px;
    flex-shrink: 0;
  }
  &__bar-track {
    flex: 1;
    height: 8px;
    border-radius: 4px;
    background: rgb(var(--md-sys-color-surface-container-high));
    overflow: hidden;
  }
  &__bar-fill {
    height: 100%;
    border-radius: 4px;
    background: rgb(var(--md-sys-color-primary));
    transition: width 0.5s ease;
  }
  &__bar-fill--alt {
    background: rgb(var(--md-sys-color-tertiary));
  }
  &__bar-val {
    @include font(12px, 16px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    width: 36px;
    text-align: right;
  }
  &__avg {
    @include font(13px, 20px, 500);
    color: rgb(var(--md-sys-color-primary));
  }
}

/* ── Work type ── */
.work-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 520px;
}

.work-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 10px;

  &__rank {
    @include font(16px, 22px, 600);
    color: rgb(var(--md-sys-color-primary));
    width: 32px;
  }
  &__type {
    flex: 1;
    @include font(14px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface));
  }
  &__count {
    @include font(13px, 20px);
    color: rgb(var(--md-sys-color-on-surface-variant));
  }
  &__avg {
    @include font(13px, 20px, 500);
    color: rgb(var(--md-sys-color-primary));
  }
}

/* ── Toggle ── */
.toggle-group {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgb(var(--md-sys-color-outline));
}

.toggle-btn {
  padding: 6px 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  @include font(13px, 20px, 500);
  color: rgb(var(--md-sys-color-on-surface-variant));
  transition: background 0.15s ease;
  &:not(:last-child) {
    border-right: 1px solid rgb(var(--md-sys-color-outline));
  }
  &--active {
    background: rgb(var(--md-sys-color-secondary-container));
    color: rgb(var(--md-sys-color-on-secondary-container));
  }
}

/* ── Histogram ── */
.histogram {
  display: flex;
  gap: 24px;
  align-items: flex-end;
  height: 220px;
  padding: 0 4px;
}

.histogram__bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
}
.histogram__count {
  @include font(13px, 18px, 600);
  color: rgb(var(--md-sys-color-on-surface));
}
.histogram__fill {
  width: 100%;
  max-width: 56px;
  border-radius: 6px 6px 0 0;
  background: rgb(var(--md-sys-color-primary));
  transition: height 0.5s ease;
  min-height: 4px;
}
.histogram__label {
  @include font(12px, 16px);
  color: rgb(var(--md-sys-color-on-surface-variant));
}

/* ── Pie chart ── */
.pie-chart {
  display: flex;
  align-items: center;
  gap: 32px;
}

.pie-chart__circle {
  width: 180px;
  height: 180px;
  flex-shrink: 0;
  border-radius: 50%;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pie-legend__item {
  display: flex;
  align-items: center;
  gap: 8px;
  @include font(13px, 20px);
  color: rgb(var(--md-sys-color-on-surface-variant));
}
.pie-legend__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* ── Deviation ── */
.dev-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 600px;
}

.dev-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 10px;

  &__title {
    @include font(14px, 20px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    flex: 1;
  }
  &__scores {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__score {
    @include font(14px, 20px, 600);
  }
  &__score--ai {
    color: rgb(var(--md-sys-color-tertiary));
  }
  &__score--teacher {
    color: rgb(var(--md-sys-color-primary));
  }

  &__diff {
    @include font(13px, 20px, 500);
    min-width: 40px;
    text-align: center;
    &--up {
      color: rgb(var(--md-sys-color-error));
    }
    &--down {
      color: rgb(var(--md-sys-color-tertiary));
    }
  }
}

/* ── Teacher work data ── */
.twd-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.twd-card {
  background: rgb(var(--md-sys-color-surface-container-lowest));
  border-radius: 16px;
  padding: 20px;
  &__title {
    @include font(15px, 22px, 500);
    color: rgb(var(--md-sys-color-on-surface));
    margin-bottom: 16px;
  }
}

.trend-bars {
  display: flex;
  gap: 6px;
  align-items: flex-end;
  height: 164px;
  padding: 0 4px;
}
.trend-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
}
.trend-bar__count {
  @include font(11px, 14px, 600);
  color: rgb(var(--md-sys-color-on-surface));
}
.trend-bar__fill {
  width: 100%;
  max-width: 32px;
  border-radius: 4px 4px 0 0;
  background: rgb(var(--md-sys-color-primary));
  transition: height 0.5s ease;
  min-height: 4px;
}
.trend-bar__fill--sub {
  background: rgb(var(--md-sys-color-tertiary));
}
.trend-bar__fill--eff {
  background: rgb(var(--md-sys-color-secondary));
}
.trend-bar__date {
  @include font(10px, 14px);
  color: rgb(var(--md-sys-color-on-surface-variant));
  white-space: nowrap;
}

.compare-chart {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.compare-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.compare-row__label {
  @include font(12px, 16px);
  color: rgb(var(--md-sys-color-on-surface-variant));
  width: 44px;
  flex-shrink: 0;
}
.compare-row__bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.compare-row__bar-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 18px;
}
.compare-row__bar {
  height: 14px;
  border-radius: 3px;
  transition: width 0.5s ease;
}
.compare-row__bar--ai {
  background: rgb(var(--md-sys-color-tertiary));
}
.compare-row__bar--teacher {
  background: rgb(var(--md-sys-color-primary));
}
.compare-row__val {
  @include font(11px, 14px, 500);
  color: rgb(var(--md-sys-color-on-surface));
  width: 24px;
  text-align: right;
}

.compare-legend {
  display: flex;
  gap: 20px;
  margin-top: 12px;
}
.compare-legend__item {
  display: flex;
  align-items: center;
  gap: 6px;
  @include font(12px, 16px);
  color: rgb(var(--md-sys-color-on-surface-variant));
}
.compare-legend__dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.compare-legend__dot--ai {
  background: rgb(var(--md-sys-color-tertiary));
}
.compare-legend__dot--teacher {
  background: rgb(var(--md-sys-color-primary));
}
</style>
