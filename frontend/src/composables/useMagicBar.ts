import { ref, reactive, computed, watch, onMounted, onUnmounted, type Ref, type ComputedRef } from 'vue'
import { useSnackbar } from './useSnackbar'
import { useConnectionHealth } from './useConnectionHealth'
import type { MagicBar } from '../types'

export interface MagicBarApi {
  magicBar: MagicBar
  greeting: Ref<string>
  ripple: Ref<number>
  rippleLeft: Ref<string>
  rippleTop: Ref<string>
  magicKey: ComputedRef<string>
  triggerRipple: (cx?: number, cy?: number) => void
  showGreeting: (pagePrimary: string) => void
}

const POEMS = [
  '学而不厌，诲人不倦',
  '随风潜入夜，润物细无声',
  '桃李不言，下自成蹊',
  '落红不是无情物，化作春泥更护花',
  '令公桃李满天下，何用堂前更种花',
  '新竹高于旧竹枝，全凭老干为扶持',
]

export function useMagicBar(teacherName: Ref<string>): MagicBarApi {
  const snackbar = useSnackbar()
  const { state: connState, browserOnline } = useConnectionHealth()

  // ── Core state ──
  const magicBar = reactive<MagicBar>({
    primary: '仪表盘',
    sub: '',
    status: '',
    statusType: '',
    count: 0,
    suffix: '',
    suffixType: '',
  })

  // ── Ripple ──
  const ripple = ref(0)
  const rippleLeft = ref('50%')
  const rippleTop = ref('50%')

  function triggerRipple(cx?: number, cy?: number): void {
    const bar = document.querySelector('.top-bar')
    const r = bar ? bar.getBoundingClientRect() : { left: 0, top: 0 }
    rippleLeft.value = (cx ?? window.innerWidth / 2) - r.left + 'px'
    rippleTop.value = (cy ?? 32) - r.top + 'px'
    ripple.value++
  }

  // ── Greeting ──
  const greeting = ref('')
  const magicKey = computed(
    () => `${magicBar.primary}|${magicBar.sub}|${magicBar.status}|${magicBar.suffix}|${greeting.value}`,
  )

  function pickGreeting(): string {
    const h = new Date().getHours()
    const name = teacherName.value
    const timeGreet =
      h < 6 ? '夜深了' : h < 9 ? '早上好' : h < 12 ? '上午好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
    const poem = POEMS[Math.floor(Math.random() * POEMS.length)]
    const count = magicBar.count
    const countPart = count > 0 ? `今天还有 ${count} 份作业待批改。` : ''
    return Math.random() < 0.5
      ? `${timeGreet}，${name}老师！${countPart}`
      : `「${poem}」 ${timeGreet}，${name}老师！${countPart}`
  }

  let greetTimer: ReturnType<typeof setTimeout> | null = null
  let greetDismissTimer: ReturnType<typeof setTimeout> | null = null

  function showGreeting(pagePrimary: string): void {
    clearTimeout(greetTimer!)
    clearTimeout(greetDismissTimer!)
    greetTimer = setTimeout(() => {
      magicBar.primary = pagePrimary
      greeting.value = pickGreeting()
      greetDismissTimer = setTimeout(() => {
        greeting.value = ''
      }, 5000)
    }, 300)
  }

  // ── Rest reminder ──
  let restTimer: ReturnType<typeof setTimeout> | null = null
  let restClearTimer: ReturnType<typeof setTimeout> | null = null
  const REST_INTERVAL = 60 * 60 * 1000

  function resetRestTimer(): void {
    clearTimeout(restTimer!)
    clearTimeout(restClearTimer!)
    restTimer = setTimeout(() => {
      magicBar.status = '已经连续工作一段时间了，起来活动一下吧'
      magicBar.statusType = 'info'
      restClearTimer = setTimeout(() => {
        magicBar.status = ''
        restClearTimer = null
        resetRestTimer()
      }, 6000)
    }, REST_INTERVAL)
  }

  // ── Connection health → MagicBar UI ──
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectedClearTimer: ReturnType<typeof setTimeout> | null = null
  let prevState = connState.value

  function hasDraftsEnabled(): boolean {
    try {
      const prefs = JSON.parse(localStorage.getItem('cookie_prefs') || '{}')
      return prefs.drafts !== false
    } catch {
      return true
    }
  }

  watch(
    () => connState.value,
    (cur) => {
      if (cur === 'disconnected' && prevState === 'connected') {
        const draftMsg = hasDraftsEnabled() ? ' · 修改已保存在本地' : ''
        magicBar.status = `啊！与服务器断连了${draftMsg}`
        magicBar.statusType = 'info'
        clearTimeout(reconnectTimer!)
        reconnectTimer = setTimeout(() => {
          magicBar.status = ''
          magicBar.suffix = browserOnline.value ? '积极重连中' : '离线'
          magicBar.suffixType = browserOnline.value ? 'reconnecting' : 'offline'
        }, 4000)
      }
      if (cur === 'connected' && prevState === 'disconnected') {
        clearTimeout(reconnectTimer!)
        clearTimeout(reconnectedClearTimer!)
        magicBar.status = ''
        magicBar.suffix = '服务器已恢复连接'
        magicBar.suffixType = 'reconnected'
        snackbar.show('服务器已恢复连接', { variant: 'info', duration: 2500 })
        reconnectedClearTimer = setTimeout(() => {
          if (magicBar.suffix === '服务器已恢复连接') magicBar.suffix = ''
        }, 3000)
      }
      prevState = cur
    },
  )

  // Also update suffix when browser online status changes while disconnected
  watch(
    () => browserOnline.value,
    (online) => {
      if (connState.value === 'disconnected' && !magicBar.status) {
        magicBar.suffix = online ? '积极重连中' : '离线'
        magicBar.suffixType = online ? 'reconnecting' : 'offline'
      }
    },
  )

  // ── Lifecycle ──
  onMounted(() => {
    resetRestTimer()
    document.addEventListener('click', resetRestTimer)
    document.addEventListener('keydown', resetRestTimer)
  })

  onUnmounted(() => {
    clearTimeout(restTimer!)
    clearTimeout(restClearTimer!)
    clearTimeout(greetTimer!)
    clearTimeout(greetDismissTimer!)
    clearTimeout(reconnectTimer!)
    clearTimeout(reconnectedClearTimer!)
    document.removeEventListener('click', resetRestTimer)
    document.removeEventListener('keydown', resetRestTimer)
  })

  return {
    magicBar,
    greeting,
    ripple,
    rippleLeft,
    rippleTop,
    magicKey,
    triggerRipple,
    showGreeting,
  }
}
