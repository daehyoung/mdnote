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
  (error) => {
    if (error.response && error.response.status === 401) {
      // Clear token and redirect to login if unauthorized
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
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
