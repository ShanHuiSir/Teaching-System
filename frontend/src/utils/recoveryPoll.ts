import http from './request'

type RecoverCallback = () => void
type ChangeCallback = (connected: boolean) => void

let timer: ReturnType<typeof setInterval> | null = null
let connected = true
const onRecoverCbs = new Set<RecoverCallback>()
const onChangeCbs = new Set<ChangeCallback>()

function notifyChange(): void {
  onChangeCbs.forEach(fn => fn(connected))
}

function notifyRecover(): void {
  onRecoverCbs.forEach(fn => fn())
  onRecoverCbs.clear()
}

function ensurePolling(): void {
  if (timer) return
  timer = setInterval(async () => {
    try {
      await http.get('/health')
      if (!connected) {
        connected = true
        notifyChange()
        notifyRecover()
      }
    } catch {
      if (connected) {
        connected = false
        notifyChange()
      }
    }
  }, 3000)
}

export function startRecoveryPoll(onRecover?: RecoverCallback): void {
  if (onRecover) onRecoverCbs.add(onRecover)
  ensurePolling()
}

export function onConnectionChange(fn: ChangeCallback): () => void {
  onChangeCbs.add(fn)
  ensurePolling()
  return () => {
    onChangeCbs.delete(fn)
  }
}

export function isConnected(): boolean {
  return connected
}
