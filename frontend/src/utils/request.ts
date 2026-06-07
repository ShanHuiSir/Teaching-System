import axios from 'axios'
import { useSnackbar } from '../composables/useSnackbar'

 
const http: any = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

let offlineTimer: ReturnType<typeof setTimeout> | null = null

function notifyOffline(): void {
  if (offlineTimer) return
  useSnackbar().show('与服务器连接断开', { variant: 'error', duration: 4000 })
  offlineTimer = setTimeout(() => {
    offlineTimer = null
  }, 10000)
}

http.interceptors.response.use(
  (res: any) => {
    const body = res.data as Record<string, unknown> | undefined
    if (body && typeof body.code === 'number') {
      if (body.code === 200) return body.data
      return Promise.reject(new Error((body.message as string) || '请求失败'))
    }
    return body
  },
  (err: unknown) => {
    if (err && typeof err === 'object' && 'response' in err) {
      const axiosErr = err as { response?: { status?: number } }
      if (!axiosErr.response || axiosErr.response.status! >= 500) {
        notifyOffline()
      }
    } else {
      notifyOffline()
    }
    return Promise.reject(err)
  },
)

if (typeof window !== 'undefined') {
  window.addEventListener('offline', () => {
    notifyOffline()
  })
}

/**
 * Retry a fetch function with exponential backoff.
 * `onError(err, attempt, delay)` called on each failure.
 * `onSuccess()` called when fn finally succeeds.
 * Returns a stop function to cancel pending retries.
 */
export function retryFetch(
  fn: () => Promise<void>,
  onError: (err: unknown, attempt: number, delay: number) => void,
): () => void {
  let cancelled = false
  let attempt = 0

  async function run(): Promise<void> {
    if (cancelled) return
    try {
      await fn()
    } catch (e) {
      if (cancelled) return
      attempt++
      const delay = Math.min(1000 * Math.pow(2, attempt - 1), 15000)
      onError(e, attempt, delay)
      setTimeout(() => run(), delay)
    }
  }

  run()
  return () => {
    cancelled = true
  }
}

export default http
