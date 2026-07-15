import { createRouter, createWebHistory } from 'vue-router'
import Welcome from '@/pages/WelcomePage.vue'
import Unavailable from '@/pages/SystemUnavailablePage.vue'
import UpgradeRequired from '@/pages/UpgradeRequiredPage.vue'
import NotFound from '@/pages/NotFoundPage.vue'

const routes = [
  { path: '/', name: 'welcome', component: Welcome },
  { path: '/register', name: 'register', component: () => import('@/pages/RegisterPage.vue') },
  { path: '/check-email', name: 'check-email', component: () => import('@/pages/CheckEmailPage.vue') },
  { path: '/verify-email', name: 'verify-email', component: () => import('@/pages/VerifyEmailPage.vue') },
  { path: '/login', name: 'login', component: () => import('@/pages/LoginPage.vue') },
  { path: '/forgot-password', name: 'forgot-password', component: () => import('@/pages/ForgotPasswordPage.vue') },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/pages/ResetPasswordPage.vue') },
  {
    path: '/account',
    name: 'account',
    component: () => import('@/pages/AccountPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/account/profile',
    name: 'profile',
    component: () => import('@/pages/ProfilePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/account/security',
    name: 'security',
    component: () => import('@/pages/SecurityPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/account/sessions',
    name: 'sessions',
    component: () => import('@/pages/SessionsPage.vue'),
    meta: { requiresAuth: true }
  },
  { path: '/system-unavailable', name: 'unavailable', component: Unavailable },
  { path: '/upgrade-required', name: 'upgrade', component: UpgradeRequired },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFound }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router