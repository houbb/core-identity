import { createRouter, createWebHistory } from 'vue-router'
import Welcome from '@/pages/WelcomePage.vue'
import Unavailable from '@/pages/SystemUnavailablePage.vue'
import UpgradeRequired from '@/pages/UpgradeRequiredPage.vue'
import NotFound from '@/pages/NotFoundPage.vue'

const routes = [
  { path: '/', name: 'welcome', component: Welcome },
  { path: '/login', name: 'login', component: () => import('@/pages/LoginPlaceholder.vue') },
  { path: '/account', name: 'account', component: () => import('@/pages/AccountPlaceholder.vue') },
  { path: '/system-unavailable', name: 'unavailable', component: Unavailable },
  { path: '/upgrade-required', name: 'upgrade', component: UpgradeRequired },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFound }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router