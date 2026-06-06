import { ref, watch } from 'vue'

const STORAGE_KEY = 'theme_mode' // 'light' | 'dark' | 'auto'
const DARK_CLASS = 'dark'

const mode = ref(localStorage.getItem(STORAGE_KEY) || 'auto')
const isDark = ref(false)

function apply() {
  const preferDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const dark = mode.value === 'dark' || (mode.value === 'auto' && preferDark)
  isDark.value = dark
  document.documentElement.classList.toggle(DARK_CLASS, dark)
}

// Initialize
apply()

// Watch browser preference changes
const mql = window.matchMedia('(prefers-color-scheme: dark)')
mql.addEventListener('change', () => {
  if (mode.value === 'auto') apply()
})

// Watch manual changes
watch(mode, (val) => {
  localStorage.setItem(STORAGE_KEY, val)
  apply()
})

export function useTheme() {
  function toggle() {
    mode.value = isDark.value ? 'light' : 'dark'
  }

  function setAuto() {
    mode.value = 'auto'
  }

  return { mode, isDark, toggle, setAuto }
}
