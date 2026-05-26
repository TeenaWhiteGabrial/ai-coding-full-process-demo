import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export interface ConsoleMenu {
  id: number
  parentId: number
  name: string
  path: string
  component?: string
  icon?: string
  sort?: number
  hidden?: number
  children?: ConsoleMenu[]
}

function normalizeMenu(item: any): ConsoleMenu {
  return {
    id: item.id,
    parentId: item.parent_id ?? item.parentId ?? 0,
    name: item.name ?? '',
    path: item.path ?? '',
    component: item.component ?? '',
    icon: item.icon ?? '',
    sort: item.sort ?? 0,
    hidden: item.hidden ?? 0,
    children: Array.isArray(item.children) ? item.children.map(normalizeMenu) : [],
  }
}

export const useMenuStore = defineStore('menu', () => {
  const menus = ref<ConsoleMenu[]>([])
  const loaded = ref(false)

  async function fetchMenus(force = false) {
    if (loaded.value && !force) {
      return menus.value
    }
    const res = await request.get('/common/menu/tree') as any
    menus.value = (res.data || []).map(normalizeMenu)
    loaded.value = true
    return menus.value
  }

  function reset() {
    menus.value = []
    loaded.value = false
  }

  return { menus, loaded, fetchMenus, reset }
})
