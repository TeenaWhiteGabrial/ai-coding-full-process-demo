import router from '@/utils/auth'
import Layout from '@/layout/index.vue'
import Login from '@/views/login/index.vue'

router.addRoute({
  path: '/console/login',
  name: 'ConsoleLogin',
  component: Login,
  meta: { title: '登录' },
})

router.addRoute({
  path: '/console',
  component: Layout,
  redirect: '/console/dashboard',
  children: [
    {
      path: '/console/dashboard',
      name: 'ConsoleDashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { title: '工作台' },
    },
    {
      path: '/console/users',
      name: 'ConsoleUsers',
      component: () => import('@/views/Users.vue'),
      meta: { title: '用户管理' },
    },
    {
      path: '/console/roles',
      name: 'ConsoleRoles',
      component: () => import('@/views/Roles.vue'),
      meta: { title: '角色管理' },
    },
    {
      path: '/console/menus',
      name: 'ConsoleMenus',
      component: () => import('@/views/Menus.vue'),
      meta: { title: '菜单管理' },
    },
    {
      path: '/console/settings',
      name: 'ConsoleSettings',
      component: () => import('@/views/Settings.vue'),
      meta: { title: '个人设置' },
    },
  ],
})

router.addRoute({
  path: '/:pathMatch(.*)*',
  redirect: '/console/dashboard',
})

export default router
