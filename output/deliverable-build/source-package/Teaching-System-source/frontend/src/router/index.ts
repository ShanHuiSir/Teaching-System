import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { fetchCurrentSession } from '../utils/session'

import LoginPage from '../views/LoginPage.vue'
import MainLayout from '../layouts/MainLayout.vue'
import DashboardPage from '../views/DashboardPage.vue'
import ReviewPage from '../views/ReviewPage.vue'
import ClassesPage from '../views/ClassesPage.vue'
import AssignmentsPage from '../views/AssignmentsPage.vue'
import ForbiddenPage from '../views/ForbiddenPage.vue'
import FilePreviewPage from '../views/FilePreviewPage.vue'
import NotFoundPage from '../views/NotFoundPage.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
  },
  {
    path: '/',
    redirect: () => (window as any).__initialSession ? '/dashboard' : '/login',
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
  {
    path: '/forbidden',
    name: 'Forbidden',
    component: ForbiddenPage,
  },
  {
    path: '/preview/:submissionId',
    name: 'FilePreview',
    component: FilePreviewPage,
    meta: { requiresAuth: true },
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

router.beforeEach(async to => {
  const needsSession = to.meta.requiresAuth || to.path === '/login' || to.path === '/forbidden'
  const session = needsSession ? await fetchCurrentSession() : null

  if (to.meta.requiresAuth && !session) return '/forbidden'
  if (to.path === '/login' && session) return '/dashboard'
})

export default router
