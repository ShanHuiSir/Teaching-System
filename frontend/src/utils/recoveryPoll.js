import http from './request.js'

let timer = null
let connected = true
const onRecoverCbs = new Set()
const onChangeCbs = new Set()

function notifyChange() {
  onChangeCbs.forEach(fn => fn(connected))
}

function notifyRecover() {
  onRecoverCbs.forEach(fn => fn())
  onRecoverCbs.clear()
}

function ensurePolling() {
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

// ── Public API ──

/** Start recovery poll. Called by pages after fetch failure. Stops callback after first recovery. */
export function startRecoveryPoll(onRecover) {
  if (onRecover) onRecoverCbs.add(onRecover)
  ensurePolling()
}

/** Register a persistent callback for connection state changes. Returns unsubscribe function. */
export function onConnectionChange(fn) {
  onChangeCbs.add(fn)
  ensurePolling()
  return () => onChangeCbs.delete(fn)
}

/** Current connection state */
export function isConnected() {
  return connected
}
