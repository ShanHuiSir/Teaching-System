import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getCookie } from '../utils/cookie'

import LoginPage from '../views/LoginPage.vue'
import MainLayout from '../layouts/MainLayout.vue'
import DashboardPage from '../views/DashboardPage.vue'
import ReviewPage from '../views/ReviewPage.vue'
import ClassesPage from '../views/ClassesPage.vue'
import AssignmentsPage from '../views/AssignmentsPage.vue'
import ForbiddenPage from '../views/ForbiddenPage.vue'
import LoadingPage from '../views/LoadingPage.vue'
import NotFoundPage from '../views/NotFoundPage.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
  },
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/dashboard',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Dashboard', component: DashboardPage },
    ],
  },
  {
    path: '/review',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', component: ReviewPage },
    ],
  },
  {
    path: '/classes',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', component: ClassesPage },
    ],
  },
  {
    path: '/assignments',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', component: AssignmentsPage },
    ],
  },
  // Old path compatibility redirects
  { path: '/assignments/pending', redirect: '/review' },
  { path: '/assignments/ai-reviewed', redirect: '/review' },
  { path: '/assignments/completed', redirect: '/review' },
  { path: '/students', redirect: '/classes' },
  { path: '/submit', redirect: '/assignments' },
  { path: '/works', redirect: '/assignments' },
  { path: '/export', redirect: '/dashboard' },

  {
    path: '/forbidden',
    name: 'Forbidden',
    component: ForbiddenPage,
  },
  {
    path: '/loading',
    name: 'Loading',
    component: LoadingPage,
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFoundPage,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(to => {
  const hasToken = !!getCookie('auth_token')
  if (to.meta.requiresAuth && !hasToken) return '/login'
  if (to.path === '/login' && hasToken) return '/dashboard'
})

export default router
