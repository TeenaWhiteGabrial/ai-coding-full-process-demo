const TOKEN_STORAGE_KEY = 'token'
const USER_INFO_STORAGE_KEY = 'userInfo'

function getStoredToken() {
  return localStorage.getItem(TOKEN_STORAGE_KEY) || ''
}

function setStoredToken(token: string) {
  localStorage.setItem(TOKEN_STORAGE_KEY, token)
}

function clearStoredToken() {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
}

function getStoredUserInfo<T>() {
  const raw = localStorage.getItem(USER_INFO_STORAGE_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw) as T
  } catch {
    localStorage.removeItem(USER_INFO_STORAGE_KEY)
    return null
  }
}

function setStoredUserInfo<T>(value: T) {
  localStorage.setItem(USER_INFO_STORAGE_KEY, JSON.stringify(value))
}

function clearStoredUserInfo() {
  localStorage.removeItem(USER_INFO_STORAGE_KEY)
}

function clearStoredSession() {
  clearStoredToken()
  clearStoredUserInfo()
}

export {
  TOKEN_STORAGE_KEY,
  USER_INFO_STORAGE_KEY,
  clearStoredSession,
  clearStoredToken,
  clearStoredUserInfo,
  getStoredToken,
  getStoredUserInfo,
  setStoredToken,
  setStoredUserInfo,
}
