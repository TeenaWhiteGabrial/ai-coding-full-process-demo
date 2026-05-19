import request from '@/utils/request'

export const authApi = {
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    request.post('/common/auth/change-password', data, { headers: { 'Content-Type': 'application/json' } }),
  updateProfile: (data: { realName?: string; email?: string; avatar?: string }) =>
    request.post('/common/auth/update-profile', data, { headers: { 'Content-Type': 'application/json' } }),
  getUserInfo: () => request.get('/common/auth/user-info'),
}
