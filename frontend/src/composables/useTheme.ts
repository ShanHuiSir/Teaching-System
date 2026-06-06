import { ref, watch, type Ref } from 'vue'

type ThemeMode = 'light' | 'dark' | 'auto'

export interface ThemeApi {
  mode: Ref<ThemeMode>
  isDark: Ref<boolean>
  toggle: () => void
  setAuto: () => void
}

const STORAGE_KEY = 'theme_mode'
const DARK_CLASS = 'dark'

const mode = ref<ThemeMode>((localStorage.getItem(STORAGE_KEY) as ThemeMode) || 'auto')
const isDark = ref(false)

function apply(): void {
  const preferDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const dark = mode.value === 'dark' || (mode.value === 'auto' && preferDark)
  isDark.value = dark
  document.documentElement.classList.toggle(DARK_CLASS, dark)
}

apply()

const mql = window.matchMedia('(prefers-color-scheme: dark)')
mql.addEventListener('change', () => {
  if (mode.value === 'auto') apply()
})

watch(mode, (val: ThemeMode) => {
  localStorage.setItem(STORAGE_KEY, val)
  apply()
})

export function useTheme(): ThemeApi {
  function toggle(): void {
    mode.value = isDark.value ? 'light' : 'dark'
  }

  function setAuto(): void {
    mode.value = 'auto'
  }

  return { mode, isDark, toggle, setAuto }
}
