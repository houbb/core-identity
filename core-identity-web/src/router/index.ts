import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import Welcome from '@/pages/WelcomePage.vue'
import Unavailable from '@/pages/SystemUnavailablePage.vue'
import UpgradeRequired from '@/pages/UpgradeRequiredPage.vue'
import NotFound from '@/pages/NotFoundPage.vue'

const routes = [
  // Public routes
  { path: '/', name: 'welcome', component: Welcome },
  { path: '/register', name: 'register', component: () => import('@/pages/RegisterPage.vue') },
  { path: '/check-email', name: 'check-email', component: () => import('@/pages/CheckEmailPage.vue') },
  { path: '/verify-email', name: 'verify-email', component: () => import('@/pages/VerifyEmailPage.vue') },
  { path: '/login', name: 'login', component: () => import('@/pages/LoginPage.vue') },
  { path: '/forgot-password', name: 'forgot-password', component: () => import('@/pages/ForgotPasswordPage.vue') },
  { path: '/reset-password', name: 'reset-password', component: () => import('@/pages/ResetPasswordPage.vue') },

  // Invitation accept (semi-public)
  { path: '/invitations/:token', name: 'accept-invitation', component: () => import('@/pages/AcceptInvitationPage.vue') },

  // Auth required — with layout
  {
    path: '/organizations',
    component: () => import('@/components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'new', name: 'create-org', component: () => import('@/pages/CreateOrganizationPage.vue') },
      {
        path: ':organizationId',
        name: 'org-home',
        component: () => import('@/pages/OrganizationHomePage.vue')
      },
      {
        path: ':organizationId/settings',
        name: 'org-settings',
        component: () => import('@/pages/OrganizationSettingsPage.vue')
      },
      {
        path: ':organizationId/members',
        name: 'org-members',
        component: () => import('@/pages/MembersPage.vue')
      },
      {
        path: ':organizationId/invitations',
        name: 'org-invitations',
        component: () => import('@/pages/InvitationsPage.vue')
      },
      {
        path: ':organizationId/invite',
        name: 'org-invite',
        component: () => import('@/pages/InviteMemberPage.vue')
      },
      {
        path: ':organizationId/roles',
        name: 'org-roles',
        component: () => import('@/pages/RolesPage.vue')
      },
      {
        path: ':organizationId/roles/:roleId',
        name: 'org-role-detail',
        component: () => import('@/pages/RoleDetailPage.vue')
      },
      {
        path: ':organizationId/danger',
        name: 'org-danger',
        component: () => import('@/pages/DangerZonePage.vue')
      }
    ]
  },

  // Account pages (auth required)
  {
    path: '/account',
    component: () => import('@/components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'account', component: () => import('@/pages/AccountPage.vue') },
      { path: 'profile', name: 'profile', component: () => import('@/pages/ProfilePage.vue') },
      { path: 'security', name: 'security', component: () => import('@/pages/SecurityPage.vue') },
      { path: 'sessions', name: 'sessions', component: () => import('@/pages/SessionsPage.vue') }
    ]
  },

  // Personal space
  {
    path: '/personal',
    component: () => import('@/components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'personal-space', component: () => import('@/pages/PersonalSpacePage.vue') }
    ]
  },

  // System pages
  { path: '/system-unavailable', name: 'unavailable', component: Unavailable },
  { path: '/upgrade-required', name: 'upgrade', component: UpgradeRequired },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFound }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Global navigation guard
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      // Try to check session first
      await authStore.checkSession()
    }
    if (!authStore.isAuthenticated) {
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
  }

  next()
})

export default router
