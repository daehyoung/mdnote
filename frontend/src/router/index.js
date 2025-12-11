import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
       path: '/login',
       name: 'login',
       component: () => import('../views/LoginView.vue')
    },
    {
      path: '/',
      component: () => import('../views/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/HomeView.vue')
        },
        {
          path: 'documents/:id',
          name: 'document-detail',
          component: () => import('../views/HomeView.vue')
        },
        {
          path: 'search',
          name: 'search',
          component: () => import('../views/SearchView.vue')
        },
        {
          path: 'admin',
          component: () => import('../views/AdminView.vue'),
          children: [
            {
                path: '',
                name: 'admin-users',
                component: () => import('../views/UserManageView.vue')
            },
            {
                path: 'organization',
                name: 'organization',
                component: () => import('../views/OrganizationView.vue')
            },
            {
                path: 'categories',
                name: 'categories',
                component: () => import('../views/CategoryManageView.vue')
            }
          ]
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue')
        }
      ]
    }
  ]
})

// Navigation Guard
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token');
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export default router
