import { ref, type Ref } from 'vue'

export interface SnackbarOptions {
  variant?: 'info' | 'error' | 'warning'
  action?: string
  onAction?: (() => void) | null
  duration?: number
}

export interface SnackbarApi {
  message: Ref<string>
  visible: Ref<boolean>
  variant: Ref<string>
  actionLabel: Ref<string>
  show: (msg: string, opts?: SnackbarOptions) => void
  dismiss: () => void
  doAction: () => void
}

const message = ref('')
const visible = ref(false)
const variant = ref('info')
const actionLabel = ref('')
const actionCb = ref<(() => void) | null>(null)
let timer: ReturnType<typeof setTimeout> | null = null

export function useSnackbar(): SnackbarApi {
  function show(msg: string, opts: SnackbarOptions = {}): void {
    if (timer !== null) clearTimeout(timer)
    message.value = msg
    variant.value = opts.variant || 'info'
    actionLabel.value = opts.action || ''
    actionCb.value = opts.onAction || null
    visible.value = true
    timer = setTimeout(() => {
      visible.value = false
      actionCb.value = null
    }, opts.duration || 3000)
  }

  function dismiss(): void {
    if (timer !== null) clearTimeout(timer)
    visible.value = false
    actionCb.value = null
  }

  function doAction(): void {
    const cb = actionCb.value
    dismiss()
    if (cb) cb()
  }

  return { message, visible, variant, actionLabel, show, dismiss, doAction }
}
