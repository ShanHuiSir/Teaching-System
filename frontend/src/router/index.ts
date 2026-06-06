import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getCookie } from '../utils/cookie'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginPage.vue'),
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('../views/DashboardPage.vue') },
      { path: 'review', name: 'Review', component: () => import('../views/ReviewPage.vue') },
      { path: 'classes', name: 'Classes', component: () => import('../views/ClassesPage.vue') },
      { path: 'assignments', name: 'Assignments', component: () => import('../views/AssignmentsPage.vue') },
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
  if (to.path === '/login' && hasToken) return '/'
  if (to.path === '/forbidden' && hasToken) return '/'
})

export default router
