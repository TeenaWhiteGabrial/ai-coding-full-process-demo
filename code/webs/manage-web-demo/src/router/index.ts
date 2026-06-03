import type { RouteRecordRaw } from 'vue-router'

import { createRouter, createWebHashHistory } from 'vue-router'

import {
  CONSOLE_BASE_PATH,
  FORBIDDEN_PATH,
  LOGIN_PATH,
  NOT_FOUND_PATH,
  createConsoleRouteRecord,
  knownConsolePaths,
  normalizeConsolePath,
  resolveConsoleRoute,
} from '@/constants/routes'
import Layout from '@/layout/index.vue'
import { useAccessStore } from '@/stores/access'
import { useMenuStore, type ConsoleMenu } from '@/stores/menu'
import { useTabbarStore } from '@/stores/tabbar'
import { useUserStore } from '@/stores/user'
import { getStoredToken } from '@/utils/session'
import Login from '@/views/login/index.vue'
import Forbidden from '@/views/system/Forbidden.vue'
import NotFound from '@/views/system/NotFound.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: LOGIN_PATH,
      name: 'ConsoleLogin',
      component: Login,
      meta: { public: true, title: '登录' },
    },
    {
      path: FORBIDDEN_PATH,
      name: 'ConsoleForbidden',
      component: Forbidden,
      meta: { public: true, title: '无权限访问' },
    },
    {
      path: CONSOLE_BASE_PATH,
      name: 'ConsoleRoot',
      component: Layout,
      meta: { requiresAuth: true },
    },
    {
      path: NOT_FOUND_PATH,
      name: 'ConsoleNotFound',
      component: NotFound,
      meta: { public: true, title: '页面不存在' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'ConsoleRouteFallback',
      component: NotFound,
      meta: { public: true, title: '页面不存在' },
    },
  ],
})

function flattenMenus(menus: ConsoleMenu[]) {
  const bucket: ConsoleMenu[] = []

  const walk = (items: ConsoleMenu[]) => {
    for (const item of items) {
      bucket.push(item)
      if (item.children?.length) {
        walk(item.children)
      }
    }
  }

  walk(menus)
  return bucket
}

function buildAccessibleRoutes(menus: ConsoleMenu[]) {
  const routes: RouteRecordRaw[] = []
  const visitedNames = new Set<string>()

  for (const item of flattenMenus(menus)) {
    const matched = resolveConsoleRoute({
      component: item.component,
      path: item.path,
    })

    if (!matched || visitedNames.has(matched.name)) {
      continue
    }

    visitedNames.add(matched.name)
    routes.push(createConsoleRouteRecord(matched, item.name))
  }

  const settingsMatched = resolveConsoleRoute({ path: '/settings' })
  if (settingsMatched && !visitedNames.has(settingsMatched.name)) {
    routes.push(createConsoleRouteRecord(settingsMatched))
  }

  return routes
}

async function ensureAccessState() {
  const accessStore = useAccessStore()
  const userStore = useUserStore()
  const menuStore = useMenuStore()
  const tabbarStore = useTabbarStore()

  if (accessStore.accessReady) {
    return
  }

  if (!userStore.userInfo) {
    await userStore.refreshUserInfo()
  }

  const menus = await menuStore.fetchMenus()
  const routes = buildAccessibleRoutes(menus)
  const firstPath = String(routes[0]?.meta?.pathKey || '')

  for (const route of routes) {
    if (!router.hasRoute(String(route.name))) {
      router.addRoute('ConsoleRoot', route)
    }
  }

  accessStore.setAccessibleRoutes(routes)
  tabbarStore.setDefaultTab(firstPath)
  accessStore.markAccessReady(true)
}

router.beforeEach(async (to) => {
  const token = getStoredToken()
  const accessStore = useAccessStore()
  const menuStore = useMenuStore()
  const userStore = useUserStore()
  const isPublicRoute = Boolean(to.meta.public)
  const normalizedTargetPath = normalizeConsolePath(to.path)
  const isFallbackRoute = to.matched.some(record => record.path === '/:pathMatch(.*)*')

  if (normalizedTargetPath && normalizedTargetPath !== to.path) {
    return {
      path: normalizedTargetPath,
      query: to.query,
      hash: to.hash,
      replace: true,
    }
  }

  if (!token) {
    accessStore.resetAccess()
    menuStore.reset()
    userStore.setUserInfo(null)

    if (!isPublicRoute) {
      return {
        path: LOGIN_PATH,
        query:
          to.fullPath === accessStore.homePath
            ? {}
            : { redirect: encodeURIComponent(to.fullPath) },
        replace: true,
      }
    }

    return true
  }

  try {
    await ensureAccessState()
  } catch {
    accessStore.resetAccess()
    menuStore.reset()
    userStore.setUserInfo(null)
    if (to.path === LOGIN_PATH) {
      return true
    }
    return {
      path: LOGIN_PATH,
      query:
        to.fullPath === accessStore.homePath
          ? {}
          : { redirect: encodeURIComponent(to.fullPath) },
      replace: true,
    }
  }

  if (to.path === LOGIN_PATH) {
    return decodeURIComponent((to.query.redirect as string) || accessStore.homePath)
  }

  if (to.name === 'ConsoleRoot') {
    return accessStore.homePath
  }

  if (isFallbackRoute && normalizedTargetPath && accessStore.canAccessPath(normalizedTargetPath)) {
    const resolvedTarget = router.resolve({
      path: normalizedTargetPath,
      query: to.query,
      hash: to.hash,
    })

    if (!resolvedTarget.matched.some(record => record.path === '/:pathMatch(.*)*')) {
      return {
        path: normalizedTargetPath,
        query: to.query,
        hash: to.hash,
        replace: true,
      }
    }
  }

  if (!to.meta.public && to.path !== LOGIN_PATH) {
    const normalizedPath = normalizeConsolePath(to.path)
    if (accessStore.canAccessPath(normalizedPath)) {
      return true
    }

    if (knownConsolePaths.has(normalizedPath)) {
      return {
        path: FORBIDDEN_PATH,
        replace: true,
      }
    }
  }

  if (!to.matched.length) {
    return {
      path: NOT_FOUND_PATH,
      replace: true,
    }
  }

  return true
})

router.afterEach((to) => {
  if (
    to.meta.public
    || to.path === LOGIN_PATH
    || to.path === FORBIDDEN_PATH
    || to.path === NOT_FOUND_PATH
    || to.matched.some(record => record.path === '/:pathMatch(.*)*')
  ) {
    return
  }

  const accessStore = useAccessStore()
  const tabbarStore = useTabbarStore()
  tabbarStore.addTab({
    closable: to.path !== accessStore.homePath,
    fixed: to.path === accessStore.homePath,
    fullPath: to.fullPath,
    keepAlive: true,
    name: String(to.name || to.path),
    title: String(to.meta.title || '未命名页面'),
  })
})

export default router
