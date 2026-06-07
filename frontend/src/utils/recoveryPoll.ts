// ── SSE heartbeat monitor ──
// Server sends "retry: 5000" → browser reconnects every 5s.
// We just watch: do heartbeats arrive, or did they stop?

let es: EventSource | null = null
let connected = true
let beatTimer: ReturnType<typeof setTimeout> | null = null
const BEAT_GRACE = 15_000 // 15s without a heartbeat → disconnected

const onChangeCbs = new Set<(connected: boolean) => void>()

function emit(val: boolean): void {
  if (connected === val) return
  connected = val
  onChangeCbs.forEach(fn => fn(val))
}

function kickBeatTimer(): void {
  if (beatTimer) clearTimeout(beatTimer)
  beatTimer = setTimeout(() => {
    emit(false)
    beatTimer = null
  }, BEAT_GRACE)
}

function start(): void {
  if (es) return
  es = new EventSource('/api/heartbeat')

  es.addEventListener('heartbeat', () => {
    emit(true)
    kickBeatTimer()
  })

  es.addEventListener('open', () => {
    emit(true)
    kickBeatTimer()
  })

  es.onerror = () => {
    if (es && es.readyState === EventSource.CLOSED) {
      // HTTP error (e.g. 502 from dev proxy) — the browser treats this
      // as fatal and will never auto-reconnect. Recreate the EventSource.
      es.close()
      es = null
      setTimeout(start, 5000)
    }
    // For non-fatal errors (network blip), the browser auto-reconnects
    // every 5s. If heartbeats don't resume, the beat timer fires emit(false).
  }

  kickBeatTimer()
}

// ── Public API ──

export function isConnected(): boolean {
  return connected
}

export function onConnectionChange(fn: (connected: boolean) => void): () => void {
  onChangeCbs.add(fn)
  start()
  return () => onChangeCbs.delete(fn)
}
