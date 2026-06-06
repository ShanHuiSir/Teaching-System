import { ref } from 'vue'

const message = ref('')
const visible = ref(false)
const variant = ref('info')
const actionLabel = ref('')
const actionCb = ref(null)
let timer = null

export function useSnackbar() {
  function show(msg, opts = {}) {
    clearTimeout(timer)
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

  function dismiss() {
    clearTimeout(timer)
    visible.value = false
    actionCb.value = null
  }

  function doAction() {
    const cb = actionCb.value
    dismiss()
    if (cb) cb()
  }

  return { message, visible, variant, actionLabel, show, dismiss, doAction }
}
