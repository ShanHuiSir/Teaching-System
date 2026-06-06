import axios from 'axios'
import { useSnackbar } from '../composables/useSnackbar.js'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

let offlineTimer = null

function notifyOffline() {
  if (offlineTimer) return
  useSnackbar().show('与服务器连接断开', { variant: 'error', duration: 4000 })
  offlineTimer = setTimeout(() => { offlineTimer = null }, 10000)
}

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number') {
      if (body.code === 200) return body.data
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (err) => {
    // No response at all — network layer failure (offline, DNS, etc.)
    if (!err.response) {
      notifyOffline()
    } else if (err.response.status >= 500) {
      // Proxy returns 502/503/504 when backend is down
      notifyOffline()
    }
    return Promise.reject(err)
  },
)

// Also listen for browser online/offline events
if (typeof window !== 'undefined') {
  window.addEventListener('offline', () => {
    notifyOffline()
  })
}

export default http
