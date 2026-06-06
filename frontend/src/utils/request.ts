import axios from 'axios'
import { useSnackbar } from '../composables/useSnackbar'

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const http: any = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

let offlineTimer: ReturnType<typeof setTimeout> | null = null

function notifyOffline(): void {
  if (offlineTimer) return
  useSnackbar().show('与服务器连接断开', { variant: 'error', duration: 4000 })
  offlineTimer = setTimeout(() => { offlineTimer = null }, 10000)
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

export default http
