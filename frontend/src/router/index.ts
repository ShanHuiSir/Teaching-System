import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginPage.vue'),
  },
  {
    path: '/class-selection',
    name: 'ClassSelection',
    component: () => import('../views/ClassSelectionPage.vue'),
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/assignments/pending' },
      { path: 'students', name: 'Students', component: () => import('../views/StudentList.vue') },
      { path: 'submit', name: 'Submit', component: () => import('../views/SubmitAssignment.vue') },
      { path: 'works', name: 'Works', component: () => import('../views/WorksPage.vue') },
      { path: 'assignments/pending', name: 'Pending', component: () => import('../views/PendingPage.vue') },
      { path: 'assignments/ai-reviewed', name: 'AiReviewed', component: () => import('../views/AiReviewedPage.vue') },
      { path: 'assignments/completed', name: 'Completed', component: () => import('../views/CompletedPage.vue') },
      { path: 'evaluation/:submissionId', name: 'Evaluation', component: () => import('../views/EvaluationPage.vue') },
      { path: 'export', name: 'Export', component: () => import('../views/ExportPage.vue') },
      { path: 'classes', name: 'Classes', component: () => import('../views/ClassManagementPage.vue') },
    ],
  },
];

export default createRouter({
  history: createWebHistory(),
  routes,
});
