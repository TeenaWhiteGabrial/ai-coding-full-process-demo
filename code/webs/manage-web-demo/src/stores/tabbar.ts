import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getConsoleRouteDefinition } from '@/constants/routes'

interface TabItem {
  closable: boolean
  fixed: boolean
  fullPath: string
  keepAlive: boolean
  name: string
  title: string
}

const STORAGE_KEY = 'consoleTabbar'
const FALLBACK_TAB: TabItem = {
  closable: false,
  fixed: true,
  fullPath: '/dashboard',
  keepAlive: true,
  name: 'ConsoleDashboard',
  title: '工作台',
}

function createTabFromPath(path?: string): TabItem {
  const matched = getConsoleRouteDefinition(path)
  if (!matched) return { ...FALLBACK_TAB }

  return {
    closable: false,
    fixed: true,
    fullPath: matched.fullPath,
    keepAlive: true,
    name: matched.name,
    title: matched.title,
  }
}

function loadStoredTabs(defaultTab: TabItem) {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return [defaultTab]

  try {
    const parsed = JSON.parse(raw) as TabItem[]
    if (!Array.isArray(parsed) || parsed.length === 0) {
      return [defaultTab]
    }

    return parsed.map(tab => ({
      ...tab,
      fullPath: tab.fullPath.replace(/^\/console(?=\/)/, ''),
      fixed: Boolean(tab.fixed),
      keepAlive: tab.keepAlive !== false,
    }))
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return [defaultTab]
  }
}

function sortTabsWithFixedFirst(items: TabItem[]) {
  const fixedTabs = items.filter(tab => tab.fixed)
  const normalTabs = items.filter(tab => !tab.fixed)
  return [...fixedTabs, ...normalTabs]
}

export const useTabbarStore = defineStore('tabbar', () => {
  const defaultTab = ref<TabItem>(createTabFromPath())
  const tabs = ref<TabItem[]>(sortTabsWithFixedFirst(loadStoredTabs(defaultTab.value)))
  const refreshKeyMap = ref<Record<string, number>>({})

  const historyTabs = computed(() => [...tabs.value].reverse())
  const cachedTabNames = computed(() =>
    tabs.value
      .filter(tab => tab.keepAlive)
      .map(tab => tab.name),
  )

  function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tabs.value))
  }

  function ensureDefaultTab() {
    if (!tabs.value.find(tab => tab.fullPath === defaultTab.value.fullPath)) {
      tabs.value.unshift({ ...defaultTab.value })
    }
    tabs.value = sortTabsWithFixedFirst(tabs.value)
  }

  function addTab(tab: TabItem) {
    const existing = tabs.value.find(item => item.fullPath === tab.fullPath)
    if (existing) {
      existing.title = tab.title
      existing.name = tab.name
      existing.closable = tab.closable
      existing.keepAlive = tab.keepAlive
      existing.fixed = tab.fixed
      tabs.value = sortTabsWithFixedFirst(tabs.value)
      persist()
      return
    }

    tabs.value.push(tab)
    tabs.value = sortTabsWithFixedFirst(tabs.value)
    persist()
  }

  function removeTab(path: string) {
    tabs.value = tabs.value.filter(tab => tab.fullPath !== path || tab.fixed || !tab.closable)
    ensureDefaultTab()
    persist()
  }

  function removeOtherTabs(path: string) {
    tabs.value = tabs.value.filter(tab => tab.fixed || tab.fullPath === path)
    ensureDefaultTab()
    persist()
  }

  function clearTabs() {
    tabs.value = tabs.value.filter(tab => tab.fixed)
    ensureDefaultTab()
    persist()
  }

  function removeLeftTabs(path: string) {
    const index = tabs.value.findIndex(tab => tab.fullPath === path)
    if (index <= 0) return

    tabs.value = tabs.value.filter((tab, currentIndex) => (
      tab.fixed || currentIndex >= index
    ))
    ensureDefaultTab()
    persist()
  }

  function removeRightTabs(path: string) {
    const index = tabs.value.findIndex(tab => tab.fullPath === path)
    if (index === -1 || index >= tabs.value.length - 1) return

    tabs.value = tabs.value.filter((tab, currentIndex) => (
      tab.fixed || currentIndex <= index
    ))
    ensureDefaultTab()
    persist()
  }

  function hasTab(path: string) {
    return tabs.value.some(tab => tab.fullPath === path)
  }

  function getLastClosableTab(excludePath: string) {
    const candidates = tabs.value.filter(tab => tab.fullPath !== excludePath)
    return candidates[candidates.length - 1] || defaultTab.value
  }

  function toggleFixed(path: string) {
    const target = tabs.value.find(tab => tab.fullPath === path)
    if (!target || target.fullPath === defaultTab.value.fullPath) return
    target.fixed = !target.fixed
    target.closable = !target.fixed
    tabs.value = sortTabsWithFixedFirst(tabs.value)
    persist()
  }

  function setDefaultTab(path?: string) {
    const previousDefaultPath = defaultTab.value.fullPath
    defaultTab.value = createTabFromPath(path)
    tabs.value = tabs.value.filter(tab => (
      tab.fullPath !== previousDefaultPath && tab.fullPath !== defaultTab.value.fullPath
    ))
    ensureDefaultTab()
    persist()
  }

  function refreshTab(path: string) {
    refreshKeyMap.value = {
      ...refreshKeyMap.value,
      [path]: (refreshKeyMap.value[path] || 0) + 1,
    }
  }

  function getRefreshKey(path: string) {
    return refreshKeyMap.value[path] || 0
  }

  function reset() {
    tabs.value = [{ ...defaultTab.value }]
    refreshKeyMap.value = {}
    persist()
  }

  return {
    addTab,
    cachedTabNames,
    clearTabs,
    getLastClosableTab,
    getRefreshKey,
    hasTab,
    historyTabs,
    refreshTab,
    removeLeftTabs,
    removeOtherTabs,
    removeRightTabs,
    removeTab,
    reset,
    setDefaultTab,
    tabs,
    toggleFixed,
  }
})
