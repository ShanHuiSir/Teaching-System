const STORAGE_KEY = 'cookie_prefs'

export const CATEGORIES = {
  essential: {
    key: 'essential',
    label: '必要 Cookie',
    desc: '用于登录认证和会话保持',
    required: true,
  },
  preferences: {
    key: 'preferences',
    label: '偏好记忆',
    desc: '记住仪表盘面板显示、作业排序与筛选等界面偏好',
    required: false,
  },
  drafts: {
    key: 'drafts',
    label: '草稿保存',
    desc: '自动保存教师批改草稿，避免意外丢失',
    required: false,
  },
}

const CATEGORY_MAP = {
  auth_token: 'essential',
  user_name: 'essential',
  dash_class: 'preferences',
  dash_work: 'preferences',
  dash_score: 'preferences',
  dash_dev: 'preferences',
  dash_tw: 'preferences',
  dash_trend: 'preferences',
  sort_class: 'preferences',
  sort_time: 'preferences',
  sort_completion: 'preferences',
  filter_status: 'preferences',
}

const DRAFT_RE = /^draft_/

export function getCategory(name) {
  if (CATEGORY_MAP[name]) return CATEGORY_MAP[name]
  if (DRAFT_RE.test(name)) return 'drafts'
  return null
}

export function loadPrefs() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch {
    console.warn('[cookiePrefs] 无法解析偏好数据，已重置为默认值')
  }
  return {}
}

export function savePrefs(prefs) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs))
}

export function isAllowed(name) {
  const cat = getCategory(name)
  if (!cat) return true
  const prefs = loadPrefs()
  if (!(cat in prefs)) return true
  return prefs[cat] !== false
}

export function clearCategory(catKey) {
  const names = Object.keys(CATEGORY_MAP).filter(k => CATEGORY_MAP[k] === catKey)
  names.forEach(n => { document.cookie = `${n}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/` })
  if (catKey === 'drafts') {
    // clear all draft_* cookies
    document.cookie.split(';').forEach(c => {
      const name = c.trim().split('=')[0]
      if (DRAFT_RE.test(name)) {
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`
      }
    })
  }
}
