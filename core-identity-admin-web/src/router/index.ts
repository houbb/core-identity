import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/pages/AdminLoginPage.vue')
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/system' },
      { path: 'system', name: 'system-overview', component: () => import('@/pages/SystemOverviewPage.vue') },
      { path: 'system/health', name: 'system-health', component: () => import('@/pages/SystemHealthPage.vue') },
      { path: 'system/contracts', name: 'system-contracts', component: () => import('@/pages/SystemContractsPage.vue') },
      { path: 'users', name: 'admin-users', component: () => import('@/pages/AdminUsersPage.vue') },
      { path: 'users/:userId', name: 'admin-user-detail', component: () => import('@/pages/AdminUserDetailPage.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/admin/system' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Simple auth guard
router.beforeEach(async (to) => {
  if (to.meta.requiresAdmin) {
    try {
      const axios = (await import('axios')).default
      const { data } = await axios.get('/admin-api/v1/identity/auth/me')
      if (!data.active) return '/admin/login'
    } catch {
      return '/admin/login'
    }
  }
})

export default router