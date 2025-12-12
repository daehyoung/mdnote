import axios from 'axios';

const api = axios.create({
  baseURL: '/api', // Proxy handles redirection to backend:8080
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors (e.g., 401)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Dynamic import to avoid circular dependency/pinia init issues
    // Or just import store if we are sure Pinia is ready. 
    // Usually safe in interceptors as they run after app mount.
    // However, best practice in some setups is to import inside or use getActivePinia.
    // Let's assume standard import works if we are cautious, but dynamic import is safer if api.js is imported early.
    // For simplicity with Pinia in Vue 3:
    // We need to import the store definition and use it.
    
    // We can't use useSnackbarStore() outside of a component setup or after Pinia is installed.
    // Since api.js is imported in components, we can import the store file at the top,
    // but we must call useSnackbarStore() inside the interceptor.
    
    if (error.response) {
        if (error.response.status === 401) {
            // Unauthorized - Clear token and redirect
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        } else {
             // Other errors (400, 403, 404, 500)
             // We need to access the store safely.
             // Since this runs during runtime, Pinia should be active.
             try {
                 const { useSnackbarStore } = await import('../stores/snackbar');
                 const snackbarStore = useSnackbarStore();
                 
                 const msg = error.response.data?.message || error.message || 'An error occurred';
                 snackbarStore.showError(`Error (${error.response.status}): ${msg}`);
             } catch (e) {
                 console.error("Failed to show snackbar", e);
             }
        }
    } else {
        // Network error
         try {
             const { useSnackbarStore } = await import('../stores/snackbar');
             const snackbarStore = useSnackbarStore();
             snackbarStore.showError('Network Error: Check your connection.');
         } catch (e) {
             console.error("Failed to show snackbar", e);
         }
    }
    return Promise.reject(error);
  }
);

export const uploadFile = async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/attachments/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};

export default api;
