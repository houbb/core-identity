import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/admin/system' },
      { path: 'system', name: 'system-overview', component: () => import('@/pages/SystemOverviewPage.vue') },
      { path: 'system/health', name: 'system-health', component: () => import('@/pages/SystemHealthPage.vue') },
      { path: 'system/contracts', name: 'system-contracts', component: () => import('@/pages/SystemContractsPage.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/admin/system' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router