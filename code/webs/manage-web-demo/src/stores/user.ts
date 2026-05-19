import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import JSEncrypt from 'jsencrypt'

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
  const userInfo = ref<UserInfo | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  function persistUserInfo(data: UserInfo | null) {
    userInfo.value = data
    if (data) {
      localStorage.setItem('userInfo', JSON.stringify(data))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  async function login(username: string, password: string) {
    const keyRes = await request.get('/common/auth/public-key') as any
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(keyRes.data)
    const encryptedPassword = encryptor.encrypt(password)
    if (!encryptedPassword) throw new Error('Password encryption failed')

    const tokenRes = await request.post('/common/auth/token', { username, password: encryptedPassword }) as any
    if (tokenRes.code !== 200) throw new Error(tokenRes.message || 'Login failed')

    const token = tokenRes.data.token
    localStorage.setItem('token', token)

    const userRes = await request.get('/common/auth/user-info') as any
    if (userRes.code !== 200) throw new Error(userRes.message || 'Failed to load user info')

    const data = {
      ...userRes.data,
      userId: userRes.data?.userId ?? userRes.data?.user_id,
      realName: userRes.data?.realName ?? userRes.data?.real_name,
    }
    data.token = token
    persistUserInfo(data)
  }

  async function refreshUserInfo() {
    const token = localStorage.getItem('token') || ''
    const userRes = await request.get('/common/auth/user-info') as any
    if (userRes.code !== 200) throw new Error(userRes.message || 'Failed to load user info')
    const data = {
      ...userRes.data,
      userId: userRes.data?.userId ?? userRes.data?.user_id,
      realName: userRes.data?.realName ?? userRes.data?.real_name,
      token,
    }
    persistUserInfo(data)
  }

  function logout() {
    persistUserInfo(null)
    localStorage.removeItem('token')
  }

  return { userInfo, login, refreshUserInfo, logout }
})
