import type { InjectionKey, Ref } from 'vue'

export interface MagicBar {
  primary: string
  sub: string
  suffix: string
  suffixType: string
  status: string
  statusType: string
  count: number
}

export interface RightButton {
  key: string
  icon: string
  label: string
  active: boolean
  action: () => void
  divider?: boolean
  gap?: boolean
}

export const MAGIC_BAR_KEY: InjectionKey<MagicBar> = Symbol('magicBar')
export const TRIGGER_RIPPLE_KEY: InjectionKey<(x?: number, y?: number) => void> = Symbol('triggerRipple')
export const REFRESH_TICK_KEY: InjectionKey<Ref<number>> = Symbol('refreshTick')
export const RIGHT_BUTTONS_KEY: InjectionKey<Ref<RightButton[]>> = Symbol('rightButtons')
export const SHOW_GREETING_KEY: InjectionKey<(pagePrimary: string) => void> = Symbol('showGreeting')
