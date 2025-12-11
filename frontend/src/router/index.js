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

import { useAuthStore } from '../stores/auth';

// Navigation Guard
router.beforeEach(async (to, from, next) => {
  const isAuthenticated = localStorage.getItem('token');
  const authStore = useAuthStore(); // Check if this works outside component setup. Pinia usually allows it if app is created.
  // Note: Pinia instance is needed. Usually router is used app.use(router) after app.use(pinia).
  // Safest way is to access store inside guard if pinia is installed.
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
    return;
  }
  
  if (to.path.startsWith('/admin')) {
      if (!authStore.user && isAuthenticated) {
          try {
              await authStore.fetchProfile();
          } catch (e) {
              console.error("Failed to fetch profile in guard", e);
              // If fetch fails, maybe token is invalid? 
              // Let's allow redirect to login or home?
              // If we can't get profile, we can't verify admin.
              // Safer to redirect to home or login.
              next('/login');
              return;
          }
      }
      
      if (authStore.user && authStore.user.role !== 'ADMIN') {
          next('/');
          return;
      }
  }

  next();
});

export default router
