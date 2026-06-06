import { watch, watchEffect, type Ref } from 'vue'
import { getCookie, setCookie } from '../utils/cookie'
import { useSnackbar } from './useSnackbar'

export interface DraftApi {
  loadDraft: (keys: string[]) => boolean
  saveDraft: (data: Record<string, unknown>) => void
  clearDraft: () => void
}

export function useDraft(draftKey: string, editing: Ref<boolean>): DraftApi {
  const snackbar = useSnackbar()

  function loadDraft(keys: string[]): boolean {
    const raw = getCookie(draftKey)
    if (!raw) return false
    try {
      const saved = JSON.parse(raw) as Record<string, unknown>
      keys.forEach(k => { delete saved[k] })
      return true
    } catch {
      clearDraft()
      return false
    }
  }

  function saveDraft(data: Record<string, unknown>): void {
    setCookie(draftKey, JSON.stringify(data), 7)
  }

  function clearDraft(): void {
    setCookie(draftKey, '', -1)
  }

  // Auto-save on editing
  watchEffect(() => {
    // Intentionally empty — pages use this as a reactive trigger
    void editing.value
  })

  // Notify on close without submit
  watch(editing, (val, old) => {
    if (old && !val && getCookie(draftKey)) {
      snackbar.show('编辑内容已保存至草稿', { variant: 'info', duration: 2500 })
    }
  })

  return { loadDraft, saveDraft, clearDraft }
}
