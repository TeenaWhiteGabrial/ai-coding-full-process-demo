import type { RouteRecordSingleView } from 'vue-router'

const LEGACY_CONSOLE_PREFIX = '/console'
const CONSOLE_BASE_PATH = '/'
const LOGIN_PATH = '/login'
const FORBIDDEN_PATH = '/403'
const NOT_FOUND_PATH = '/404'

interface ConsoleRouteDefinition {
  componentKey: string
  fullPath: string
  name: string
  routePath: string
  title: string
  component: RouteRecordSingleView['component']
}

const consoleRouteDefinitions: ConsoleRouteDefinition[] = [
  {
    componentKey: 'Dashboard',
    fullPath: '/dashboard',
    name: 'ConsoleDashboard',
    routePath: 'dashboard',
    title: '工作台',
    component: () => import('@/views/Dashboard.vue'),
  },
  {
    componentKey: 'Users',
    fullPath: '/users',
    name: 'ConsoleUsers',
    routePath: 'users',
    title: '用户管理',
    component: () => import('@/views/Users.vue'),
  },
  {
    componentKey: 'Roles',
    fullPath: '/roles',
    name: 'ConsoleRoles',
    routePath: 'roles',
    title: '角色管理',
    component: () => import('@/views/Roles.vue'),
  },
  {
    componentKey: 'Menus',
    fullPath: '/menus',
    name: 'ConsoleMenus',
    routePath: 'menus',
    title: '菜单管理',
    component: () => import('@/views/Menus.vue'),
  },
  {
    componentKey: 'Orgs',
    fullPath: '/orgs',
    name: 'ConsoleOrgs',
    routePath: 'orgs',
    title: '组织管理',
    component: () => import('@/views/Orgs.vue'),
  },
  {
    componentKey: 'SiteManage',
    fullPath: '/site',
    name: 'ConsoleSiteManage',
    routePath: 'site',
    title: '网站管理',
    component: () => import('@/views/SiteManage.vue'),
  },
  {
    componentKey: 'Settings',
    fullPath: '/settings',
    name: 'ConsoleSettings',
    routePath: 'settings',
    title: '个人中心',
    component: () => import('@/views/Settings.vue'),
  },
]

const routeDefinitionByPath = new Map(
  consoleRouteDefinitions.map(item => [item.fullPath, item]),
)

const routeDefinitionByComponent = new Map(
  consoleRouteDefinitions.map(item => [item.componentKey.toLowerCase(), item]),
)

function normalizeConsolePath(path?: string) {
  if (!path) return ''

  if (path.startsWith(`${LEGACY_CONSOLE_PREFIX}/`)) {
    return path.slice(LEGACY_CONSOLE_PREFIX.length)
  }

  const normalized = path.startsWith('/') ? path : `/${path}`
  return normalized
}

function resolveConsoleRoute(options: { component?: string; path?: string }) {
  const normalizedPath = normalizeConsolePath(options.path)
  if (normalizedPath) {
    const routeByPath = routeDefinitionByPath.get(normalizedPath)
    if (routeByPath) return routeByPath
  }

  const componentKey = options.component?.trim().toLowerCase()
  if (!componentKey) return null

  return routeDefinitionByComponent.get(componentKey) || null
}

function createConsoleRouteRecord(
  definition: ConsoleRouteDefinition,
  title?: string,
): RouteRecordSingleView {
  return {
    path: definition.routePath,
    name: definition.name,
    component: definition.component,
    meta: {
      pathKey: definition.fullPath,
      requiresAuth: true,
      title: title || definition.title,
    },
  }
}

function getConsoleRouteDefinition(path?: string) {
  const normalizedPath = normalizeConsolePath(path)
  if (!normalizedPath) return null
  return routeDefinitionByPath.get(normalizedPath) || null
}

const knownConsolePaths = new Set(
  consoleRouteDefinitions.map(item => item.fullPath),
)

export {
  CONSOLE_BASE_PATH,
  FORBIDDEN_PATH,
  LOGIN_PATH,
  NOT_FOUND_PATH,
  createConsoleRouteRecord,
  getConsoleRouteDefinition,
  knownConsolePaths,
  normalizeConsolePath,
  resolveConsoleRoute,
}
