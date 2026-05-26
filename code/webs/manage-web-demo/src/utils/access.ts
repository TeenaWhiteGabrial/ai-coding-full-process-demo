import type { App, Directive } from 'vue'

import { useAccessStore } from '@/stores/access'
import { useUserStore } from '@/stores/user'

interface AccessRequirement {
  mode?: 'all' | 'any'
  paths?: string[]
  roles?: string[]
}

function matchesRequirement(expected: string[] | undefined, actual: string[], mode: 'all' | 'any') {
  if (!expected?.length) return true
  if (!actual.length) return false

  return mode === 'all'
    ? expected.every(item => actual.includes(item))
    : expected.some(item => actual.includes(item))
}

function hasAccess(requirement?: AccessRequirement) {
  if (!requirement) return true

  const userStore = useUserStore()
  const accessStore = useAccessStore()
  const mode = requirement.mode || 'any'
  const roleMatched = matchesRequirement(requirement.roles, userStore.userInfo?.roles || [], mode)
  const pathMatched = matchesRequirement(requirement.paths, accessStore.accessiblePaths, mode)

  return roleMatched && pathMatched
}

function updateAccessState(el: HTMLElement, requirement?: AccessRequirement) {
  el.style.display = hasAccess(requirement) ? '' : 'none'
}

const accessDirective: Directive<HTMLElement, AccessRequirement | undefined> = {
  mounted(el, binding) {
    updateAccessState(el, binding.value)
  },
  updated(el, binding) {
    updateAccessState(el, binding.value)
  },
}

function installAccessDirective(app: App) {
  app.directive('access', accessDirective)
}

export { installAccessDirective, type AccessRequirement, hasAccess }
