import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export type Theme = 'light' | 'dark'
export type ContentWidth = 'compact' | 'full' | 'wide'
export type LayoutMode = 'classic' | 'inset' | 'top-mix'
export type SidebarStyle = 'dark' | 'light' | 'semi-dark'
export type PageTransitionName = 'fade' | 'fade-slide' | 'fade-up' | 'fade-down'
export type ThemeColor =
  | 'default'
  | 'deep-blue'
  | 'green'
  | 'orange'
  | 'pink'
  | 'rose'
  | 'sky-blue'
  | 'slate'
  | 'violet'

interface ThemePalette {
  hex: string
  hsl: string
}

const themePalettes: Record<ThemeColor, ThemePalette> = {
  default: { hex: '#1677ff', hsl: '212 100% 45%' },
  'deep-blue': { hex: '#2563eb', hsl: '221.2 83.2% 53.3%' },
  green: { hex: '#16a34a', hsl: '142.1 76.2% 36.3%' },
  orange: { hex: '#ea580c', hsl: '24.6 95% 53.1%' },
  pink: { hex: '#db2777', hsl: '346.8 77.2% 49.8%' },
  rose: { hex: '#e11d48', hsl: '346.8 77.2% 49.8%' },
  'sky-blue': { hex: '#3b82f6', hsl: '221.2 83.2% 53.3%' },
  slate: { hex: '#475569', hsl: '222.2 84% 4.9%' },
  violet: { hex: '#7c3aed', hsl: '262.1 83.3% 57.8%' },
}

interface ThemePreferences {
  contentWidth: ContentWidth
  enableFullscreen: boolean
  enableSearch: boolean
  enableThemeToggle: boolean
  headerFixed: boolean
  headerGlass: boolean
  layoutMode: LayoutMode
  pageTransitionEnabled: boolean
  pageTransitionName: PageTransitionName
  showBreadcrumb: boolean
  sidebarStyle: SidebarStyle
  theme: Theme
  themeColor: ThemeColor
}

const STORAGE_KEY = 'consolePreferences'

const defaults: ThemePreferences = {
  contentWidth: 'full',
  enableFullscreen: true,
  enableSearch: true,
  enableThemeToggle: true,
  headerFixed: true,
  headerGlass: true,
  layoutMode: 'classic',
  pageTransitionEnabled: true,
  pageTransitionName: 'fade-slide',
  showBreadcrumb: true,
  sidebarStyle: 'dark',
  theme: 'light',
  themeColor: 'default',
}

function loadPreferences(): ThemePreferences {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return { ...defaults }

  try {
    const parsed = JSON.parse(raw)
    return {
      ...defaults,
      ...parsed,
      contentWidth: parsed.contentWidth || defaults.contentWidth,
    }
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return { ...defaults }
  }
}

function hexToRgb(hex: string) {
  const normalized = hex.replace('#', '')
  const full = normalized.length === 3
    ? normalized
      .split('')
      .map((value) => `${value}${value}`)
      .join('')
    : normalized
  const value = Number.parseInt(full, 16)

  return {
    r: (value >> 16) & 255,
    g: (value >> 8) & 255,
    b: value & 255,
  }
}

function clampChannel(value: number) {
  return Math.min(255, Math.max(0, Math.round(value)))
}

function mixHex(baseHex: string, targetHex: string, ratio: number) {
  const base = hexToRgb(baseHex)
  const target = hexToRgb(targetHex)
  const mixed = {
    b: clampChannel(base.b + (target.b - base.b) * ratio),
    g: clampChannel(base.g + (target.g - base.g) * ratio),
    r: clampChannel(base.r + (target.r - base.r) * ratio),
  }

  return `rgb(${mixed.r}, ${mixed.g}, ${mixed.b})`
}

export const useThemeStore = defineStore('theme', () => {
  const saved = loadPreferences()

  const theme = ref<Theme>(saved.theme)
  const themeColor = ref<ThemeColor>(saved.themeColor)
  const layoutMode = ref<LayoutMode>(saved.layoutMode)
  const sidebarStyle = ref<SidebarStyle>(saved.sidebarStyle)
  const contentWidth = ref<ContentWidth>(saved.contentWidth)
  const showBreadcrumb = ref(saved.showBreadcrumb)
  const headerFixed = ref(saved.headerFixed)
  const headerGlass = ref(saved.headerGlass)
  const enableSearch = ref(saved.enableSearch)
  const enableFullscreen = ref(saved.enableFullscreen)
  const enableThemeToggle = ref(saved.enableThemeToggle)
  const pageTransitionEnabled = ref(saved.pageTransitionEnabled)
  const pageTransitionName = ref<PageTransitionName>(saved.pageTransitionName)

  const preferences = computed<ThemePreferences>(() => ({
    contentWidth: contentWidth.value,
    enableFullscreen: enableFullscreen.value,
    enableSearch: enableSearch.value,
    enableThemeToggle: enableThemeToggle.value,
    headerFixed: headerFixed.value,
    headerGlass: headerGlass.value,
    layoutMode: layoutMode.value,
    pageTransitionEnabled: pageTransitionEnabled.value,
    pageTransitionName: pageTransitionName.value,
    showBreadcrumb: showBreadcrumb.value,
    sidebarStyle: sidebarStyle.value,
    theme: theme.value,
    themeColor: themeColor.value,
  }))

  function syncDom() {
    const root = document.documentElement
    const palette = themePalettes[themeColor.value]
    root.classList.toggle('dark', theme.value === 'dark')
    root.classList.toggle('light', theme.value !== 'dark')
    root.dataset.theme = themeColor.value
    root.dataset.layoutMode = layoutMode.value
    root.dataset.sidebarStyle = sidebarStyle.value
    root.dataset.contentWidth = contentWidth.value
    root.dataset.showBreadcrumb = String(showBreadcrumb.value)
    root.dataset.headerFixed = String(headerFixed.value)
    root.dataset.headerGlass = String(headerGlass.value)
    const rgb = hexToRgb(palette.hex)
    root.style.setProperty('--primary', palette.hsl)
    root.style.setProperty('--theme-primary', palette.hsl)
    root.style.setProperty('--el-color-primary', palette.hex)
    root.style.setProperty('--el-color-primary-rgb', `${rgb.r}, ${rgb.g}, ${rgb.b}`)
    root.style.setProperty('--el-color-primary-light-3', mixHex(palette.hex, '#ffffff', 0.3))
    root.style.setProperty('--el-color-primary-light-5', mixHex(palette.hex, '#ffffff', 0.5))
    root.style.setProperty('--el-color-primary-light-7', mixHex(palette.hex, '#ffffff', 0.7))
    root.style.setProperty('--el-color-primary-light-8', mixHex(palette.hex, '#ffffff', 0.78))
    root.style.setProperty('--el-color-primary-light-9', mixHex(palette.hex, '#ffffff', 0.88))
    root.style.setProperty('--el-color-primary-dark-2', mixHex(palette.hex, '#000000', 0.18))
  }

  function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences.value))
    syncDom()
  }

  function setTheme(value: Theme) {
    theme.value = value
    persist()
  }

  function toggleTheme() {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  function setThemeColor(value: ThemeColor) {
    themeColor.value = value
    persist()
  }

  function setLayoutMode(value: LayoutMode) {
    layoutMode.value = value
    persist()
  }

  function setPageTransitionEnabled(value: boolean) {
    pageTransitionEnabled.value = value
    persist()
  }

  function setPageTransitionName(value: PageTransitionName) {
    pageTransitionName.value = value
    persist()
  }

  function setSidebarStyle(value: SidebarStyle) {
    sidebarStyle.value = value
    persist()
  }

  function setContentWidth(value: ContentWidth) {
    contentWidth.value = value
    persist()
  }

  function setShowBreadcrumb(value: boolean) {
    showBreadcrumb.value = value
    persist()
  }

  function setHeaderFixed(value: boolean) {
    headerFixed.value = value
    persist()
  }

  function setHeaderGlass(value: boolean) {
    headerGlass.value = value
    persist()
  }

  function setToolVisible(
    key: 'enableFullscreen' | 'enableSearch' | 'enableThemeToggle',
    value: boolean,
  ) {
    if (key === 'enableFullscreen') enableFullscreen.value = value
    if (key === 'enableSearch') enableSearch.value = value
    if (key === 'enableThemeToggle') enableThemeToggle.value = value
    persist()
  }

  function resetPreferences() {
    theme.value = defaults.theme
    themeColor.value = defaults.themeColor
    layoutMode.value = defaults.layoutMode
    sidebarStyle.value = defaults.sidebarStyle
    contentWidth.value = defaults.contentWidth
    showBreadcrumb.value = defaults.showBreadcrumb
    headerFixed.value = defaults.headerFixed
    headerGlass.value = defaults.headerGlass
    enableSearch.value = defaults.enableSearch
    enableFullscreen.value = defaults.enableFullscreen
    enableThemeToggle.value = defaults.enableThemeToggle
    pageTransitionEnabled.value = defaults.pageTransitionEnabled
    pageTransitionName.value = defaults.pageTransitionName
    persist()
  }

  function initTheme() {
    syncDom()
  }

  return {
    enableFullscreen,
    enableSearch,
    enableThemeToggle,
    headerFixed,
    headerGlass,
    initTheme,
    contentWidth,
    layoutMode,
    pageTransitionEnabled,
    pageTransitionName,
    preferences,
    resetPreferences,
    setContentWidth,
    setHeaderFixed,
    setHeaderGlass,
    setLayoutMode,
    setPageTransitionEnabled,
    setPageTransitionName,
    setShowBreadcrumb,
    setSidebarStyle,
    setTheme,
    setThemeColor,
    setToolVisible,
    showBreadcrumb,
    sidebarStyle,
    theme,
    themeColor,
    toggleTheme,
  }
})
