import { inject } from 'vue'
import { MAGIC_BAR_KEY } from '../types'
import { useSnackbar } from './useSnackbar'
import type { MagicBar } from '../types'

export interface NotifyOptions {
  type: 'success' | 'error' | 'info' | 'warning'
  snackbar: string
  magicbar?: string
  snackbarDuration?: number
  magicbarDuration?: number
}

const VARIANT_MAP: Record<string, 'info' | 'error' | 'warning'> = {
  success: 'info',
  error: 'error',
  info: 'info',
  warning: 'warning',
}

export function useNotify() {
  const snackbar = useSnackbar()
  const magicBar = inject<MagicBar | null>(MAGIC_BAR_KEY, null)

  let clearTimer: ReturnType<typeof setTimeout> | null = null

  function notify(opts: NotifyOptions) {
    snackbar.show(opts.snackbar, {
      variant: VARIANT_MAP[opts.type] || 'info',
      duration: opts.snackbarDuration,
    })

    if (!magicBar) return

    const message = opts.magicbar ?? opts.snackbar
    magicBar.status = message
    magicBar.statusType = opts.type === 'error' ? 'error' : opts.type === 'success' ? 'success' : 'info'

    if (clearTimer) clearTimeout(clearTimer)
    clearTimer = setTimeout(() => {
      magicBar.status = ''
      clearTimer = null
    }, opts.magicbarDuration ?? 2500)
  }

  return { notify }
}
