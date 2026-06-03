import request from '@/utils/request'

export interface RoleItem {
  id: number
  roleCode: string
  roleName: string
}

export interface UserManageItem {
  id: number
  username: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  orgId?: number
  orgName?: string
  status: number
  failedLoginCount?: number
  lockedUntil?: string
  roleIds: number[]
  roleCodes: string[]
  roleNames: string[]
}

export interface OrgItem {
  id: number
  parentId: number
  orgName: string
  orgCode: string
  leaderName?: string
  status: number
  createdAt?: string
  updatedAt?: string
  children?: OrgItem[]
}

export interface MenuItem {
  id: number
  parentId: number
  name: string
  path?: string
  component?: string
  icon?: string
  menuType?: string
  permissionCode?: string
  sort?: number
  hidden?: number
  children?: MenuItem[]
}

export interface DictItem {
  id: number
  parentId: number
  dictType: string
  dictLabel: string
  dictValue: string
  status: number
  sort?: number
  remark?: string
  children?: DictItem[]
}

export interface RoleMenuTreeItem extends MenuItem {
  checked: boolean
  children?: RoleMenuTreeItem[]
}

export const managementApi = {
  listUsers: (params: { page: number; size: number; keyword?: string; orgId?: number }) =>
    request.get('/common/user/list', { params }),
  listRoles: () => request.get('/common/user/roles'),
  createUser: (data: any) => request.post('/common/user', data),
  updateUser: (id: number, data: any) => request.put(`/common/user/${id}`, data),
  updateUserStatus: (id: number, status: number) => request.put(`/common/user/${id}/status`, null, { params: { status } }),
  resetUserPassword: (id: number, newPassword: string) => request.put(`/common/user/${id}/password`, { newPassword }),
  deleteUser: (id: number) => request.delete(`/common/user/${id}`),

  roleList: () => request.get('/common/role/list'),
  createRole: (data: any) => request.post('/common/role', data),
  deleteRole: (id: number) => request.delete(`/common/role/${id}`),
  roleMenus: (id: number) => request.get(`/common/role/${id}/menus`),
  updateRoleMenus: (id: number, menuIds: number[]) => request.put(`/common/role/${id}/menus`, menuIds),
  roleUsers: (id: number) => request.get(`/common/role/${id}/users`),
  updateRoleUsers: (id: number, userIds: number[]) => request.put(`/common/role/${id}/users`, userIds),

  menuList: () => request.get('/common/menu/list'),
  menuDetail: (id: number) => request.get(`/common/menu/${id}`),
  createMenu: (data: any) => request.post('/common/menu', data),
  updateMenu: (id: number, data: any) => request.put(`/common/menu/${id}`, data),
  deleteMenu: (id: number) => request.delete(`/common/menu/${id}`),

  dictTree: (dictType?: string) => request.get('/common/dict/tree', { params: { dictType } }),
  dictDetail: (id: number) => request.get(`/common/dict/${id}`),
  createDict: (data: any) => request.post('/common/dict', data),
  updateDict: (id: number, data: any) => request.put(`/common/dict/${id}`, data),
  deleteDict: (id: number) => request.delete(`/common/dict/${id}`),

  orgTree: () => request.get('/common/org/tree'),
  orgOptions: () => request.get('/common/org/options'),
  orgDetail: (id: number) => request.get(`/common/org/${id}`),
  createOrg: (data: any) => request.post('/common/org', data),
  updateOrg: (id: number, data: any) => request.put(`/common/org/${id}`, data),
  deleteOrg: (id: number) => request.delete(`/common/org/${id}`),
}
