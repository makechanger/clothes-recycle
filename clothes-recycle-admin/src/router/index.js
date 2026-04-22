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
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/UserView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'collectors',
        name: 'Collectors',
        component: () => import('../views/collector/CollectorView.vue'),
        meta: { title: '回收员管理' }
      },
      {
        path: 'admins',
        name: 'Admins',
        component: () => import('../views/admin/AdminManagerView.vue'),
        meta: { title: '管理员管理', requiresSuperAdmin: true }
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
        path: 'reviews',
        name: 'Reviews',
        component: () => import('../views/review/ReviewView.vue'),
        meta: { title: '评价管理' }
      },
      {
        path: 'complaints',
        name: 'Complaints',
        component: () => import('../views/complaint/ComplaintView.vue'),
        meta: { title: '申诉管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫：未登录时拦截到登录页，operator 不能访问管理员管理
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth === false) {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else if (to.meta.requiresSuperAdmin && localStorage.getItem('admin_role') !== 'admin') {
      next('/dashboard')
    } else {
      next()
    }
  }
})

export default router
