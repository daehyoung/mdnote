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
          component: () => import('../views/DocumentDetailView.vue')
        },
        {
          path: 'documents/:id/edit',
          name: 'document-edit',
          component: () => import('../views/DocumentEditView.vue'),
          meta: { requiresEditPermission: true }
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
                name: 'admin-categories', // Renamed for clarity key
                component: () => import('../views/CategoryManageView.vue'),
                props: { scope: 'SYSTEM' }
            }
          ]
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue')
        },
        {
          path: 'my-categories',
          name: 'my-categories',
          component: () => import('../views/CategoryManageView.vue'),
          props: { scope: 'USER' },
          meta: { requiresAuth: true }
        }
      ]
    }
  ]
})

import { useAuthStore } from '../stores/auth';
import { useDocumentStore } from '../stores/document';

// Navigation Guard
router.beforeEach(async (to, from, next) => {
  const isAuthenticated = localStorage.getItem('token');
  const authStore = useAuthStore(); 
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
    return;
  }
  
  // Hydrate user profile if token exists but user state is null
  if (isAuthenticated && !authStore.user) {
    try {
      await authStore.fetchProfile();
    } catch (e) {
      console.error("Failed to hydrate profile in guard", e);
      // If profile fetch fails (e.g. invalid/expired token), force logout/login
      authStore.logout();
      next('/login');
      return;
    }
  }

  if (to.path.startsWith('/admin')) {
      if (authStore.user && authStore.user.role !== 'ADMIN') {
          next('/');
          return;
      }
  }

  // Edit Permission Guard
  if (to.matched.some(record => record.meta.requiresEditPermission)) {
      const docId = to.params.id;
      const documentStore = useDocumentStore();
      
      // Use existing currentDocument if it matches ID, otherwise fetch
      let doc = documentStore.currentDocument;
      if (!doc || doc.id !== docId) {
          try {
              // Note: fetchDocument returns the document object
              doc = await documentStore.fetchDocument(docId);
          } catch (e) {
              console.error("Failed to fetch doc for permission check", e);
              next('/'); 
              return;
          }
      }
      
      if (!doc) {
          next('/'); // Doc not found
          return;
      }
      
      // Ensure user profile is loaded for check
      if (!authStore.user && isAuthenticated) {
         try {
             await authStore.fetchProfile();
         } catch(e) { /* ignore */ }
      }

      const user = authStore.user;
      let canEdit = false;
      if (user) {
          if (user.role === 'ADMIN') {
              canEdit = true;
          } else if (doc.author && doc.author.username === user.username) {
              canEdit = true;
          } else if (doc.groupWrite && user.department && doc.author.department && user.department.id === doc.author.department.id) {
              canEdit = true;
          } else if (doc.publicWrite) {
              canEdit = true;
          }
      }
      
      if (!canEdit) {
          console.warn("User attempted to access edit route without permission.");
          // Redirect to view mode
          next({ name: 'document-detail', params: { id: docId } });
          return;
      }
  }

  next();
});

export default router
