<template>
  <div
    class="layout-shell"
    :class="[`layout-${themeStore.layoutMode}`, `sidebar-${themeStore.sidebarStyle}`]"
    :style="{ '--sidebar-width': sidebarWidth }"
  >
    <header class="header">
      <div class="header-brand">
        <button class="site-title" type="button" @click="router.push(homePath)">
          <img class="site-title-icon" :src="siteLogo" alt="logo">
          <div class="site-title-copy">
            <strong>{{ siteName }}</strong>
          </div>
        </button>
      </div>

      <div class="header-center">
        <div class="page-intro">
          <div v-if="themeStore.showBreadcrumb" class="page-breadcrumb">
            <template v-for="(item, index) in breadcrumbTrail" :key="`${item.name}-${index}`">
              <el-icon class="breadcrumb-icon">
                <component :is="item.icon || 'Menu'" />
              </el-icon>
              <span :class="{ current: index === breadcrumbTrail.length - 1 }">{{ item.name }}</span>
              <span v-if="index < breadcrumbTrail.length - 1" class="breadcrumb-separator">&gt;</span>
            </template>
          </div>
          <h1 v-if="!themeStore.showBreadcrumb" class="page-title">{{ pageTitle }}</h1>
        </div>
      </div>

      <nav v-if="themeStore.layoutMode === 'top-mix' && topLevelMenus.length" class="top-nav">
        <button
          v-for="menu in topLevelMenus"
          :key="menu.id"
          class="top-nav-item"
          :class="{ active: currentTopMenuId === menu.id }"
          type="button"
          @click="activateTopMenu(menu)"
        >
          <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
          <span>{{ menu.name }}</span>
        </button>
      </nav>

      <div class="header-right">
        <AppSearchDialog v-if="themeStore.enableSearch" />
        <AppFullscreenToggle v-if="themeStore.enableFullscreen" />
        <ThemeToggle v-if="themeStore.enableThemeToggle" />
        <AppPreferencesDrawer />
        <el-dropdown @command="handleCommand">
          <button class="user-trigger" type="button">
            <el-avatar
              v-if="userInfo?.avatar"
              :src="userInfo.avatar"
              :size="34"
            />
            <el-avatar v-else :size="34" icon="UserFilled" />
            <div class="user-copy">
              <strong>{{ userInfo?.realName || userInfo?.username || 'User' }}</strong>
              <span>{{ userInfo?.email || 'Console Member' }}</span>
            </div>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      </div>
    </header>

    <aside class="sidebar" :class="{ collapsed: isCollapsed, resizing: isSidebarResizing }" :style="{ width: sidebarWidth }">
      <div class="sidebar-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :collapse-transition="false"
          class="sidebar-menu"
          router
          unique-opened
        >
          <template v-for="menu in visibleMenus" :key="menu.id">
            <el-sub-menu v-if="menu.children?.length" :index="String(menu.id)">
              <template #title>
                <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
                <span>{{ menu.name }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.id"
                :index="withConsolePrefix(child.path)"
              >
                <el-icon><component :is="child.icon || 'Menu'" /></el-icon>
                <span>{{ child.name }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="withConsolePrefix(menu.path)">
              <el-icon><component :is="menu.icon || 'Menu'" /></el-icon>
              <span>{{ menu.name }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>

      <div class="sidebar-footer">
        <button class="sidebar-toggle-btn" type="button" :aria-label="isCollapsed ? '展开菜单' : '收起菜单'" @click="toggleSidebar">
          <el-icon>
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </button>
      </div>

      <div
        v-if="!isCollapsed"
        class="sidebar-resizer"
        role="separator"
        aria-orientation="vertical"
        aria-label="调整侧边栏宽度"
        @mousedown="startSidebarResize"
      ></div>
    </aside>

    <section class="workspace">
      <AppTabbar />

      <main class="main-content">
        <div class="content-shell" :class="`content-${themeStore.contentWidth}`">
          <router-view v-slot="{ Component, route: viewRoute }">
            <Transition
              :name="pageTransitionClass"
              mode="out-in"
              appear
            >
              <keep-alive :include="tabbarStore.cachedTabNames">
                <component
                  :is="Component"
                  :key="`${String(viewRoute.name || viewRoute.fullPath)}::${tabbarStore.getRefreshKey(viewRoute.fullPath)}`"
                />
              </keep-alive>
            </Transition>
          </router-view>
        </div>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAccessStore } from '@/stores/access'
import { useSiteStore } from '@/stores/site'
import { useUserStore } from '@/stores/user'
import { useMenuStore, type ConsoleMenu } from '@/stores/menu'
import { useTabbarStore } from '@/stores/tabbar'
import { useThemeStore } from '@/stores/theme'
import AppFullscreenToggle from '@/components/AppFullscreenToggle.vue'
import AppPreferencesDrawer from '@/components/AppPreferencesDrawer.vue'
import AppSearchDialog from '@/components/AppSearchDialog.vue'
import AppTabbar from '@/components/AppTabbar.vue'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { Expand, Fold } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const accessStore = useAccessStore()
const userStore = useUserStore()
const menuStore = useMenuStore()
const tabbarStore = useTabbarStore()
const themeStore = useThemeStore()
const siteStore = useSiteStore()
const userInfo = computed(() => userStore.userInfo)
const homePath = computed(() => accessStore.homePath)

const isCollapsed = ref(false)
const isSidebarResizing = ref(false)
const expandedSidebarWidth = ref(268)
const selectedTopMenuId = ref<number | null>(null)
const SIDEBAR_COLLAPSED_KEY = 'sidebarCollapsed'
const SIDEBAR_WIDTH_KEY = 'sidebarWidth'
const SIDEBAR_COLLAPSED_WIDTH = 76
const SIDEBAR_MIN_WIDTH = 220
const SIDEBAR_MAX_WIDTH = 360
const savedCollapsed = localStorage.getItem(SIDEBAR_COLLAPSED_KEY)
if (savedCollapsed === 'true') {
  isCollapsed.value = true
}
const savedSidebarWidth = Number(localStorage.getItem(SIDEBAR_WIDTH_KEY))
if (!Number.isNaN(savedSidebarWidth) && savedSidebarWidth > 0) {
  expandedSidebarWidth.value = clampSidebarWidth(savedSidebarWidth)
}

const sidebarWidth = computed(() =>
  `${isCollapsed.value ? SIDEBAR_COLLAPSED_WIDTH : expandedSidebarWidth.value}px`,
)
const activeMenu = computed(() => route.path)
const allVisibleMenus = computed(() => filterVisibleMenus(menuStore.menus))
const topLevelMenus = computed(() => allVisibleMenus.value)
const currentTopMenuId = computed(() => {
  if (themeStore.layoutMode !== 'top-mix') {
    return null
  }

  const matched = findMatchedTopMenu(route.path, topLevelMenus.value)
  return matched?.id ?? selectedTopMenuId.value ?? topLevelMenus.value[0]?.id ?? null
})
const visibleMenus = computed(() => {
  if (themeStore.layoutMode !== 'top-mix') {
    return filterSidebarMenus(allVisibleMenus.value)
  }

  const currentTopMenu = topLevelMenus.value.find(menu => menu.id === currentTopMenuId.value)
  if (!currentTopMenu) return []

  const sidebarTopMenu = filterSidebarMenus([currentTopMenu])
  return sidebarTopMenu.length > 0
    ? (sidebarTopMenu[0].children?.length ? sidebarTopMenu[0].children : sidebarTopMenu)
    : []
})
const pageTitle = computed(() => String(route.meta.title || '控制台'))
const pageTransitionClass = computed(() =>
  themeStore.pageTransitionEnabled ? themeStore.pageTransitionName : '',
)
const breadcrumbTrail = computed(() => {
  const matchedMenus = findMenuTrail(route.path, allVisibleMenus.value)
  if (matchedMenus.length > 0) {
    return matchedMenus
  }
  return [{ name: pageTitle.value, icon: 'Menu' }]
})
const siteName = computed(() => siteStore.config.siteName || 'AI Studio')
const siteLogo = computed(() => siteStore.resolvedLogoUrl)

onMounted(async () => {
  if (menuStore.menus.length === 0) {
    await menuStore.fetchMenus()
  }
  await siteStore.fetchSiteConfig()
})

onBeforeUnmount(() => {
  stopSidebarResize()
})

function filterVisibleMenus(menus: ConsoleMenu[]): ConsoleMenu[] {
  return menus
    .filter(menu => menu.hidden !== 1)
    .map(menu => ({
      ...menu,
      children: filterVisibleMenus(menu.children || []),
    }))
}

function filterSidebarMenus(menus: ConsoleMenu[]): ConsoleMenu[] {
  return menus
    .filter(menu => menu.path !== '/settings')
    .map(menu => ({
      ...menu,
      children: filterSidebarMenus(menu.children || []),
    }))
}

function withConsolePrefix(path?: string) {
  if (!path) return homePath.value
  return path.replace(/^\/console(?=\/)/, '') || homePath.value
}

function findMatchedTopMenu(path: string, menus: ConsoleMenu[]) {
  const normalizedPath = withConsolePrefix(path)

  return menus.find((menu) => {
    if (withConsolePrefix(menu.path) === normalizedPath) {
      return true
    }

    return menu.children?.some(child => withConsolePrefix(child.path) === normalizedPath)
  }) || null
}

function findMenuTrail(path: string, menus: ConsoleMenu[]): ConsoleMenu[] {
  const normalizedPath = withConsolePrefix(path)

  for (const menu of menus) {
    if (withConsolePrefix(menu.path) === normalizedPath) {
      return [menu]
    }

    if (menu.children?.length) {
      const childTrail = findMenuTrail(normalizedPath, menu.children)
      if (childTrail.length > 0) {
        return [menu, ...childTrail]
      }
    }
  }

  return []
}

async function activateTopMenu(menu: ConsoleMenu) {
  selectedTopMenuId.value = menu.id
  const nextPath = menu.children?.[0]?.path || menu.path
  await router.push(withConsolePrefix(nextPath))
}

function clampSidebarWidth(width: number) {
  return Math.min(SIDEBAR_MAX_WIDTH, Math.max(SIDEBAR_MIN_WIDTH, Math.round(width)))
}

function persistSidebarWidth(width: number) {
  expandedSidebarWidth.value = clampSidebarWidth(width)
  localStorage.setItem(SIDEBAR_WIDTH_KEY, String(expandedSidebarWidth.value))
}

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem(SIDEBAR_COLLAPSED_KEY, isCollapsed.value.toString())
}

function handleSidebarResize(event: MouseEvent) {
  persistSidebarWidth(event.clientX)
}

function stopSidebarResize() {
  if (!isSidebarResizing.value) return

  isSidebarResizing.value = false
  window.removeEventListener('mousemove', handleSidebarResize)
  window.removeEventListener('mouseup', stopSidebarResize)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

function startSidebarResize(event: MouseEvent) {
  if (window.innerWidth <= 960) return

  event.preventDefault()
  isSidebarResizing.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  handleSidebarResize(event)
  window.addEventListener('mousemove', handleSidebarResize)
  window.addEventListener('mouseup', stopSidebarResize)
}

async function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    menuStore.reset()
    tabbarStore.reset()
    await router.replace('/login')
  } else if (command === 'settings') {
    await router.push('/settings')
  }
}
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  grid-template-rows: auto minmax(0, 1fr);
  height: 100vh;
  background:
    radial-gradient(circle at top right, hsl(var(--primary) / 0.08), transparent 26%),
    hsl(var(--background-deep));
}

.header {
  grid-column: 1 / -1;
  position: sticky;
  top: 0;
  z-index: 10;
  background: hsl(var(--header) / 0.82);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid hsl(var(--border));
  display: grid;
  grid-template-columns: var(--sidebar-width, 268px) minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 42px;
  padding: 0;
}

.sidebar {
  grid-column: 1;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  position: relative;
  min-height: 0;
  height: 100%;
  background:
    linear-gradient(180deg, hsl(var(--sidebar)), hsl(var(--sidebar-deep)));
  border-right: 1px solid hsl(var(--border));
  transition: width 0.3s ease;
  box-shadow: 18px 0 40px hsl(var(--shadow-soft));
}

.sidebar.resizing {
  transition: none;
}

.brand {
  display: none;
}

.sidebar-scroll {
  flex: 1;
  overflow: auto;
  padding: 10px 10px 16px;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.layout-shell.sidebar-light .sidebar {
  background: linear-gradient(180deg, hsl(var(--card)), hsl(var(--card)));
}

.layout-shell.sidebar-semi-dark .sidebar {
  background: linear-gradient(180deg, hsl(220 18% 16%), hsl(220 18% 12%));
}

.layout-shell.sidebar-light .brand-copy strong {
  color: hsl(var(--foreground));
}

.layout-shell.sidebar-light .brand-copy span {
  color: hsl(var(--muted-foreground));
}

.layout-shell.sidebar-light :deep(.sidebar-menu .el-menu-item),
.layout-shell.sidebar-light :deep(.sidebar-menu .el-sub-menu__title) {
  color: hsl(var(--muted-foreground));
}

.layout-shell.sidebar-semi-dark .brand-copy strong,
.layout-shell.sidebar-semi-dark :deep(.sidebar-menu .el-menu-item),
.layout-shell.sidebar-semi-dark :deep(.sidebar-menu .el-sub-menu__title) {
  color: hsl(210 20% 92%);
}

.layout-shell.sidebar-semi-dark .brand-copy span {
  color: hsl(215 16% 72%);
}

:deep(.sidebar-menu .el-menu-item),
:deep(.sidebar-menu .el-sub-menu__title) {
  height: 44px;
  margin-bottom: 6px;
  border-radius: 12px;
  line-height: 44px;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease;
}

:deep(.sidebar-menu .el-menu-item:hover),
:deep(.sidebar-menu .el-sub-menu__title:hover) {
  background: hsl(var(--theme-surface-active));
  color: hsl(var(--foreground));
  transform: translateX(2px);
}

:deep(.sidebar-menu .el-menu-item.is-active) {
  background: var(--theme-primary-subtle);
  color: hsl(var(--primary));
  box-shadow: var(--ai-glow-ring);
}

:deep(.sidebar-menu .el-menu-item .el-icon),
:deep(.sidebar-menu .el-sub-menu__title .el-icon) {
  font-size: 18px;
}

.sidebar-footer {
  display: grid;
  gap: 10px;
  padding: 0 14px 14px;
}

.sidebar-resizer {
  position: absolute;
  top: 0;
  right: -4px;
  width: 8px;
  height: 100%;
  cursor: col-resize;
  z-index: 4;
}

.sidebar-resizer::after {
  content: '';
  position: absolute;
  top: 50%;
  right: 2px;
  width: 2px;
  height: 44px;
  border-radius: 999px;
  background: hsl(var(--border));
  transform: translateY(-50%);
  opacity: 0;
  transition:
    opacity 0.18s ease,
    background-color 0.18s ease;
}

.sidebar:hover .sidebar-resizer::after,
.sidebar.resizing .sidebar-resizer::after,
.sidebar-resizer:hover::after {
  opacity: 1;
  background: hsl(var(--theme-primary) / 0.35);
}

.sidebar-toggle-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
  border: 1px solid hsl(var(--border));
  border-radius: 12px;
  background: hsl(var(--card) / 0.85);
  color: hsl(var(--foreground));
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease;
}

.sidebar-toggle-btn:hover {
  background: hsl(var(--secondary));
  border-color: hsl(var(--primary) / 0.18);
  transform: translateY(-1px);
}

.workspace {
  grid-column: 2;
  grid-row: 2;
  min-width: 0;
  min-height: 0;
  height: 100%;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
}

.header-brand {
  display: flex;
  align-items: center;
  min-width: 0;
  min-height: 42px;
  padding: 6px 14px 6px 18px;
}

.header-center {
  min-width: 0;
  display: grid;
  gap: 4px;
  padding: 6px 0;
}

.layout-shell.layout-inset .header-brand {
  margin: 18px 0 0 18px;
  border: 1px solid hsl(var(--border));
  border-radius: 24px 0 0 0;
}

.layout-shell.layout-inset .header-center {
  margin: 18px 18px 0 0;
  border: 1px solid hsl(var(--border));
  border-left: none;
  border-radius: 0 24px 0 0;
  padding-inline: 18px;
}

.layout-shell.layout-top-mix .header {
  min-height: 48px;
}

.layout-shell.layout-top-mix .header-brand {
  min-height: 48px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.site-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px;
  border: 1px solid transparent;
  border-radius: 12px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease;
}

.site-title:hover {
  background: hsl(var(--secondary));
  border-color: hsl(var(--border));
  transform: translateY(-1px);
}

.site-title-icon {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  object-fit: cover;
  flex: 0 0 auto;
}

.site-title-copy {
  display: flex;
  align-items: center;
  text-align: left;
}

.site-title-copy strong {
  color: hsl(var(--foreground));
  font-size: 14px;
  font-weight: 700;
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 0 8px;
  overflow: auto hidden;
}

.top-nav-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: transparent;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  white-space: nowrap;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.top-nav-item:hover {
  background: hsl(var(--theme-surface-active));
  color: hsl(var(--foreground));
}

.top-nav-item.active {
  background: var(--theme-primary-soft-strong);
  border-color: transparent;
  color: hsl(var(--primary));
  box-shadow: var(--ai-glow-xs);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-right: 18px;
}

.page-intro {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.page-breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: hsl(var(--muted-foreground));
  font-size: 14px;
  font-weight: 600;
}

.breadcrumb-icon {
  margin-right: 2px;
  font-size: 16px;
}

.page-breadcrumb .breadcrumb-separator {
  opacity: 0.55;
}

.page-breadcrumb .current {
  color: hsl(var(--foreground));
  font-weight: 700;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: hsl(var(--foreground));
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px;
  border: none;
  background: hsl(var(--card) / 0.72);
  border-radius: 12px;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.user-trigger:hover {
  background: hsl(var(--theme-surface-active));
  box-shadow: 0 10px 24px hsl(var(--shadow-soft));
  transform: translateY(-1px);
}

.user-trigger:focus-visible {
  outline: none;
  box-shadow:
    0 0 0 2px hsl(var(--theme-primary) / 0.18),
    0 10px 24px hsl(var(--shadow-soft));
}

.user-copy {
  display: grid;
  gap: 2px;
  padding-right: 6px;
  text-align: left;
}

.user-copy strong {
  color: hsl(var(--foreground));
  font-size: 12px;
  font-weight: 600;
}

.user-copy span {
  color: hsl(var(--muted-foreground));
  font-size: 11px;
}

:deep(.el-avatar) {
  background: hsl(var(--primary));
  color: white;
}

:deep(.el-dropdown-menu) {
  background: hsl(var(--card));
  border-color: hsl(var(--border));
}

:deep(.el-dropdown-menu__item) {
  color: hsl(var(--foreground));
}

:deep(.el-dropdown-menu__item:hover) {
  background: hsl(var(--secondary));
  color: hsl(var(--primary));
}

:deep(.header-popover) {
  border-radius: 18px;
  border-color: hsl(var(--border));
  background: hsl(var(--card));
  box-shadow: 0 26px 64px hsl(var(--shadow-strong));
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.mode-card,
.color-card {
  border: 1px solid hsl(var(--border));
  border-radius: 18px;
  background: hsl(var(--panel));
  cursor: pointer;
}

.mode-card {
  display: grid;
  gap: 6px;
  padding: 16px;
  text-align: left;
}

.mode-card span {
  color: hsl(var(--muted-foreground));
  line-height: 1.6;
}

.mode-card.active,
.color-card.active {
  border-color: hsl(var(--primary) / 0.35);
  box-shadow: var(--ai-glow-xs);
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.color-card {
  display: grid;
  justify-items: center;
  gap: 8px;
  padding: 14px 10px;
}

.swatch {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 0.18);
}

.color-card strong {
  font-size: 12px;
}

:global(html[data-show-breadcrumb='false'] .page-breadcrumb) {
  display: none;
}

:global(html[data-header-fixed='false'] .header) {
  position: relative;
}

:global(html[data-header-glass='false'] .header) {
  background: hsl(var(--header));
  backdrop-filter: none;
}

.main-content {
  min-width: 0;
  overflow: auto;
  min-height: 0;
  padding: 24px 26px 26px;
}

.content-shell {
  min-width: 0;
  min-height: 100%;
}

.layout-shell.layout-inset .main-content {
  padding-top: 18px;
}

.layout-shell.layout-inset .content-shell {
  padding: 6px;
}

.content-shell.content-compact {
  max-width: 1080px;
}

.content-shell.content-wide {
  max-width: 1440px;
}

.content-shell.content-full {
  max-width: none;
}

:global(html[data-content-width='compact'] .content-shell.content-compact),
:global(html[data-content-width='wide'] .content-shell.content-wide) {
  margin: 0 auto;
}

:global(html[data-layout-mode='top-mix'] .sidebar) {
  box-shadow: 10px 0 30px hsl(var(--shadow-soft));
}

@media (max-width: 960px) {
  .layout-shell {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto minmax(0, 1fr);
  }

  .header {
    grid-column: 1;
    grid-template-columns: 1fr;
    gap: 12px;
    flex-wrap: wrap;
  }

  .header-brand,
  .header-center,
  .header-right {
    padding: 0;
    border: none;
    margin: 0;
  }

  .sidebar {
    display: none;
  }

  .sidebar-resizer {
    display: none;
  }

  .user-copy span {
    display: none;
  }

  .site-title {
    padding-inline: 0;
  }

  .site-title-copy span {
    display: none;
  }

  .main-content {
    padding: 18px;
  }

  .tabbar-shell,
  .tabbar-history {
    padding-inline: 12px;
  }
}
</style>
