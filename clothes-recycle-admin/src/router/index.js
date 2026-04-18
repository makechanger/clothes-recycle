import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../views/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/layout/DashboardView.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'collectors',
        name: 'Collectors',
        component: () => import('../views/collector/CollectorView.vue'),
        meta: { title: '回收员管理' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/order/OrderView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'points-rules',
        name: 'PointsRules',
        component: () => import('../views/points/PointsRuleView.vue'),
        meta: { title: '积分规则' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/UserView.vue'),
        meta: { title: '用户管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫：未登录时拦截到登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth === false) {
    // 登录页：已登录则跳转仪表盘
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    // 需要登录的页面：未登录则跳转登录页
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
