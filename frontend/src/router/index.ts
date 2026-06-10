import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getCookie } from '../utils/cookie'

import LoginPage from '../views/LoginPage.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
  },
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/dashboard',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/DashboardPage.vue') },
    ],
  },
  {
    path: '/review',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', component: () => import('../views/ReviewPage.vue') },
    ],
  },
  {
    path: '/classes',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', component: () => import('../views/ClassesPage.vue') },
    ],
  },
  {
    path: '/assignments',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', component: () => import('../views/AssignmentsPage.vue') },
    ],
  },
  {
    path: '/forbidden',
    name: 'Forbidden',
    component: () => import('../views/ForbiddenPage.vue'),
  },
  {
    path: '/loading',
    name: 'Loading',
    component: () => import('../views/LoadingPage.vue'),
  },
  {
    path: '/preview/:submissionId',
    name: 'FilePreview',
    component: () => import('../views/FilePreviewPage.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundPage.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(to => {
  const hasToken = !!getCookie('auth_token')
  if (to.meta.requiresAuth && !hasToken) return '/forbidden'
  if (to.path === '/login' && hasToken) return '/dashboard'
  if (to.path === '/forbidden' && hasToken) return '/dashboard'
})

export default router
