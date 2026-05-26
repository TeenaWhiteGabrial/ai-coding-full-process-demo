import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import JSEncrypt from 'jsencrypt'

import { authApi } from '@/api'
import { useAccessStore } from '@/stores/access'
import {
  clearStoredSession,
  getStoredToken,
  getStoredUserInfo,
  setStoredToken,
  setStoredUserInfo,
} from '@/utils/session'

interface UserInfo {
  userId: number
  username: string
  realName?: string
  roles: string[]
  token: string
  avatar?: string
  email?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(getStoredUserInfo<UserInfo>())

  function persistUserInfo(data: UserInfo | null) {
    userInfo.value = data
    if (data) {
      setStoredUserInfo(data)
    } else {
      clearStoredSession()
    }
  }

  function normalizeUserInfo(data: any, token = getStoredToken()): UserInfo {
    return {
      ...data,
      avatar: data?.avatar,
      email: data?.email,
      realName: data?.realName ?? data?.real_name,
      roles: Array.isArray(data?.roles) ? data.roles : [],
      token,
      userId: data?.userId ?? data?.user_id,
      username: data?.username ?? '',
    }
  }

  async function login(username: string, password: string) {
    const accessStore = useAccessStore()
    const keyRes = await request.get('/common/auth/public-key') as any
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(keyRes.data)
    const encryptedPassword = encryptor.encrypt(password)
    if (!encryptedPassword) throw new Error('Password encryption failed')

    const tokenRes = await request.post('/common/auth/token', { username, password: encryptedPassword }) as any
    if (tokenRes.code !== 200) throw new Error(tokenRes.message || 'Login failed')

    const token = tokenRes.data.token
    setStoredToken(token)
    accessStore.setAccessToken(token)
    await refreshUserInfo()
  }

  async function refreshUserInfo() {
    const userRes = await authApi.getUserInfo() as any
    if (userRes.code !== 200) throw new Error(userRes.message || 'Failed to load user info')
    const data = normalizeUserInfo(userRes.data)
    persistUserInfo(data)
    return data
  }

  function logout() {
    const accessStore = useAccessStore()
    persistUserInfo(null)
    accessStore.resetAccess()
  }

  function setUserInfo(data: UserInfo | null) {
    persistUserInfo(data)
  }

  return { userInfo, login, refreshUserInfo, logout, setUserInfo }
})
