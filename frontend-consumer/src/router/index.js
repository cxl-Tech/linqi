import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/ConsumerLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'products', name: 'Products', component: () => import('../views/Products.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('../views/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { auth: true } },
      { path: 'orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { auth: true } },
      { path: 'chat', name: 'Chat', component: () => import('../views/Chat.vue'), meta: { auth: true } },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.auth && !localStorage.getItem('user_token')) {
    next('/login')
  } else {
    next()
  }
})

export default router
