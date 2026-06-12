import http from './request'
import { delCookie, setCookie } from './cookie'

export interface CurrentSession {
  username: string
  expiresInSeconds: number
}

let cachedSession: CurrentSession | null | undefined
let pendingSession: Promise<CurrentSession | null> | null = null

export function setSessionUser(username: string, days = 0.5): void {
  cachedSession = { username, expiresInSeconds: Math.round(days * 86400) }
  setCookie('user_name', username, days)
}

export function clearSessionUser(): void {
  cachedSession = null
  pendingSession = null
  delCookie('user_name')
}

export async function fetchCurrentSession(force = false): Promise<CurrentSession | null> {
  if (!force && cachedSession !== undefined) return cachedSession
  if (!force && pendingSession) return pendingSession

  pendingSession = http.get('/auth/me')
    .then((session: CurrentSession) => {
      if (!session?.username) throw new Error('empty session')
      cachedSession = session
      setCookie('user_name', session.username, 0.5)
      return session
    })
    .catch(() => {
      clearSessionUser()
      return null
    })
    .finally(() => {
      pendingSession = null
    })

  return pendingSession
}
