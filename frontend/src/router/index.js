import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import DashboardView from '@/views/DashboardView.vue'
import BillListView from '@/views/BillListView.vue'
import BillFormView from '@/views/BillFormView.vue'
import PaymentHistoryView from '@/views/PaymentHistoryView.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: LoginView, meta: { guest: true } },
  { path: '/register', component: RegisterView, meta: { guest: true } },
  { path: '/dashboard', component: DashboardView, meta: { requiresAuth: true } },
  { path: '/bills', component: BillListView, meta: { requiresAuth: true } },
  { path: '/bills/new', component: BillFormView, meta: { requiresAuth: true } },
  { path: '/bills/:id/edit', component: BillFormView, meta: { requiresAuth: true } },
  { path: '/payments', component: PaymentHistoryView, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.guest && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
