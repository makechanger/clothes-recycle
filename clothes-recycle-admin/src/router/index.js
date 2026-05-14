import { createRouter, createWebHistory } from 'vue-router'
import { getLoginRole } from '../utils/request'

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
      // ========== 管理员路由 ==========
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/layout/DashboardView.vue'),
        meta: { title: '仪表盘', role: 'admin' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/UserView.vue'),
        meta: { title: '用户管理', role: 'admin' }
      },
      {
        path: 'collectors',
        name: 'Collectors',
        component: () => import('../views/collector/CollectorView.vue'),
        meta: { title: '回收员管理', role: 'admin' }
      },
      {
        path: 'admins',
        name: 'Admins',
        component: () => import('../views/admin/AdminManagerView.vue'),
        meta: { title: '管理员管理', role: 'admin', requiresSuperAdmin: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('../views/order/OrderView.vue'),
        meta: { title: '订单管理', role: 'admin' }
      },
      {
        path: 'points-rules',
        name: 'PointsRules',
        component: () => import('../views/points/PointsRuleView.vue'),
        meta: { title: '积分规则', role: 'admin' }
      },
      {
        path: 'reviews',
        name: 'Reviews',
        component: () => import('../views/review/ReviewView.vue'),
        meta: { title: '评价管理', role: 'admin' }
      },
      {
        path: 'complaints',
        name: 'Complaints',
        component: () => import('../views/complaint/ComplaintView.vue'),
        meta: { title: '申诉管理', role: 'admin' }
      },
      // ========== 机构路由 ==========
      {
        path: 'inst-dashboard',
        name: 'InstDashboard',
        component: () => import('../views/institution/InstDashboard.vue'),
        meta: { title: '数据看板', role: 'institution' }
      },
      {
        path: 'inst-orders',
        name: 'InstOrders',
        component: () => import('../views/institution/InstOrders.vue'),
        meta: { title: '订单管理', role: 'institution' }
      },
      {
        path: 'inst-receive',
        name: 'InstReceive',
        component: () => import('../views/institution/InstReceive.vue'),
        meta: { title: '扫码接收', role: 'institution' }
      },
      {
        path: 'inst-info',
        name: 'InstInfo',
        component: () => import('../views/institution/InstInfo.vue'),
        meta: { title: '机构信息', role: 'institution' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function getDefaultRoute(role) {
  return role === 'institution' ? '/inst-dashboard' : '/dashboard'
}

router.beforeEach((to, from, next) => {
  const loginRole = getLoginRole()

  if (to.meta.requiresAuth === false) {
    if (loginRole) {
      next(getDefaultRoute(loginRole))
    } else {
      next()
    }
    return
  }

  if (!loginRole) {
    next('/login')
    return
  }

  if (to.meta.role && to.meta.role !== loginRole) {
    next(getDefaultRoute(loginRole))
    return
  }

  if (to.meta.requiresSuperAdmin && localStorage.getItem('admin_role') !== 'admin') {
    next('/dashboard')
    return
  }

  if (to.path === '/' || to.path === '/dashboard') {
    if (loginRole === 'institution') {
      next('/inst-dashboard')
      return
    }
  }

  next()
})

export default router
