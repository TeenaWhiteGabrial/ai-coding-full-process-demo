import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const loginPath = '/login'

  if (!token && to.path !== loginPath) {
    localStorage.setItem('redirectUrl', encodeURIComponent(to.fullPath))
    return loginPath
  }

  if (token && to.path === loginPath) {
    return '/'
  }
})

export default router
