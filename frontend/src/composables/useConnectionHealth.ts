import { ref, onMounted, onUnmounted, type Ref } from 'vue'
import { onConnectionChange } from '../utils/recoveryPoll'
import { onHttpError } from '../utils/request'
import { useSnackbar } from './useSnackbar'

export interface ConnectionHealth {
  state: Ref<'connected' | 'disconnected'>
  browserOnline: Ref<boolean>
}

export function useConnectionHealth(): ConnectionHealth {
  const state = ref<'connected' | 'disconnected'>('connected')
  const browserOnline = ref(navigator.onLine)
  const snackbar = useSnackbar()

  let httpErrorTimer: ReturnType<typeof setTimeout> | null = null

  // ── SSE heartbeat events ──
  onConnectionChange((connected: boolean) => {
    state.value = connected ? 'connected' : 'disconnected'
  })

  // ── HTTP errors ──
  const unregHttp = onHttpError(() => {
    if (httpErrorTimer) return
    snackbar.show('与服务器连接断开', { variant: 'error', duration: 4000 })
    httpErrorTimer = setTimeout(() => {
      httpErrorTimer = null
    }, 10000)
  })

  // ── Browser online/offline ──
  function onOnline(): void {
    browserOnline.value = true
  }
  function onOffline(): void {
    browserOnline.value = false
  }

  onMounted(() => {
    window.addEventListener('online', onOnline)
    window.addEventListener('offline', onOffline)
  })

  onUnmounted(() => {
    window.removeEventListener('online', onOnline)
    window.removeEventListener('offline', onOffline)
    if (httpErrorTimer) clearTimeout(httpErrorTimer)
    unregHttp()
  })

  return { state, browserOnline }
}
