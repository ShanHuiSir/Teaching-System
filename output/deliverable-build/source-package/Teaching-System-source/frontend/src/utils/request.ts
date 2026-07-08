import axios from 'axios'


const http: any = axios.create({
  baseURL: '/api',
  timeout: 120000,
  withCredentials: true,
  headers: {
    'X-Requested-With': 'XMLHttpRequest',
  },
})

const httpErrorCbs = new Set<() => void>()

export function onHttpError(fn: () => void): () => void {
  httpErrorCbs.add(fn)
  return () => httpErrorCbs.delete(fn)
}

function notifyHttpError(): void {
  httpErrorCbs.forEach(fn => fn())
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
      const axiosErr = err as { response?: { status?: number; data?: { message?: string } } }
      // 提取后端返回的错误消息，替换 Axios 默认的 "Request failed with status code XXX"
      const backendMsg = axiosErr.response?.data?.message
      if (backendMsg) {
        return Promise.reject(new Error(backendMsg))
      }
      if (!axiosErr.response || axiosErr.response.status! >= 500) {
        notifyHttpError()
      }
    } else {
      notifyHttpError()
    }
    return Promise.reject(err)
  },
)

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
