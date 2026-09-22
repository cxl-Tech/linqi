import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/merchant-login', name: 'MerchantLogin', component: () => import('../views/MerchantLogin.vue') },
  { path: '/merchant-register', name: 'MerchantRegister', component: () => import('../views/MerchantRegister.vue') },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { role: 'admin' },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '数据看板', icon: 'DataAnalysis' } },
      { path: 'users', name: 'Users', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'merchants', name: 'Merchants', component: () => import('../views/MerchantManage.vue'), meta: { title: '商家管理', icon: 'Shop' } },
      { path: 'products', name: 'Products', component: () => import('../views/ProductManage.vue'), meta: { title: '商品管理', icon: 'Goods' } },
      { path: 'categories', name: 'Categories', component: () => import('../views/CategoryManage.vue'), meta: { title: '分类管理', icon: 'Menu' } },
      { path: 'orders', name: 'Orders', component: () => import('../views/OrderManage.vue'), meta: { title: '订单管理', icon: 'List' } },
      { path: 'aftersale', name: 'AdminAfterSale', component: () => import('../views/AdminAfterSale.vue'), meta: { title: '售后管理', icon: 'Service' } },
      { path: 'complaints', name: 'Complaints', component: () => import('../views/ComplaintManage.vue'), meta: { title: '投诉管理', icon: 'Warning' } },
      { path: 'banners', name: 'Banners', component: () => import('../views/BannerManage.vue'), meta: { title: '轮播图', icon: 'Picture' } },
      { path: 'announcements', name: 'Announcements', component: () => import('../views/AnnouncementManage.vue'), meta: { title: '公告管理', icon: 'Bell' } },
      { path: 'settings', name: 'Settings', component: () => import('../views/SystemSetting.vue'), meta: { title: '系统设置', icon: 'Setting' } },
    ]
  },
  {
    path: '/merchant',
    component: () => import('../layouts/MerchantLayout.vue'),
    redirect: '/merchant/dashboard',
    meta: { role: 'merchant' },
    children: [
      { path: 'dashboard', name: 'MerchantDashboard', component: () => import('../views/merchant/MerchantDashboard.vue') },
      { path: 'products', name: 'MerchantProducts', component: () => import('../views/merchant/MerchantProductManage.vue') },
      { path: 'orders', name: 'MerchantOrders', component: () => import('../views/merchant/MerchantOrderManage.vue') },
      { path: 'reviews', name: 'MerchantReviews', component: () => import('../views/merchant/MerchantReviewManage.vue') },
      { path: 'stock-warning', name: 'MerchantStockWarning', component: () => import('../views/merchant/MerchantStockWarning.vue') },
      { path: 'aftersale', name: 'MerchantAfterSale', component: () => import('../views/merchant/MerchantAfterSale.vue') },
      { path: 'chat', name: 'MerchantChat', component: () => import('../views/merchant/MerchantChat.vue') },
      { path: 'profile', name: 'MerchantProfile', component: () => import('../views/merchant/MerchantProfile.vue') },
    ]
  }
]

const publicPaths = ['/login', '/merchant-login', '/merchant-register']

const router = createRouter({ history: createWebHistory('/admin'), routes })

router.beforeEach((to, from, next) => {
  if (publicPaths.includes(to.path)) return next()
  if (to.path.startsWith('/merchant/') || to.path === '/merchant') {
    if (!localStorage.getItem('merchant_token')) return next('/merchant-login')
  } else {
    if (!localStorage.getItem('admin_token')) return next('/login')
  }
  next()
})

export default router
