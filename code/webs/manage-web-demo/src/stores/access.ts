import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { RouteMeta, RouteRecordNameGeneric } from 'vue-router'

import { getStoredToken, setStoredToken } from '@/utils/session'

interface AccessibleRouteLike {
  name?: RouteRecordNameGeneric
  meta?: RouteMeta
}

export const useAccessStore = defineStore('access', () => {
  const accessToken = ref(getStoredToken())
  const accessiblePaths = ref<string[]>([])
  const accessibleRouteNames = ref<string[]>([])
  const accessReady = ref(false)

  const homePath = computed(() => accessiblePaths.value[0] || '/login')

  function setAccessToken(token: string) {
    accessToken.value = token
    if (token) {
      setStoredToken(token)
    }
  }

  function setAccessibleRoutes(routes: AccessibleRouteLike[]) {
    accessibleRouteNames.value = routes
      .map(route => (typeof route.name === 'string' ? route.name : ''))
      .filter(Boolean)

    accessiblePaths.value = routes
      .map(route => String(route.meta?.pathKey || ''))
      .filter(Boolean)
  }

  function canAccessPath(path: string) {
    if (['/login', '/403', '/404'].includes(path)) return true
    return accessiblePaths.value.includes(path)
  }

  function markAccessReady(value: boolean) {
    accessReady.value = value
  }

  function resetAccess() {
    accessToken.value = ''
    accessiblePaths.value = []
    accessibleRouteNames.value = []
    accessReady.value = false
  }

  return {
    accessReady,
    accessToken,
    accessiblePaths,
    accessibleRouteNames,
    canAccessPath,
    homePath,
    markAccessReady,
    resetAccess,
    setAccessibleRoutes,
    setAccessToken,
  }
})
