import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { fetchCurrentSession } from '../utils/session'

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

router.beforeEach(async to => {
  const needsSession = to.meta.requiresAuth || to.path === '/login' || to.path === '/forbidden'
  const session = needsSession ? await fetchCurrentSession() : null

  if (to.meta.requiresAuth && !session) return '/forbidden'
  if (to.path === '/login' && session) return '/dashboard'
  if (to.path === '/forbidden' && session) return '/dashboard'
})

export default router
