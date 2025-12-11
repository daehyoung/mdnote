import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null
  }),
  getters: {
    isAuthenticated: (state) => !!state.token
  },
  actions: {
    async login(username, password) {
      try {
        const response = await axios.post('/api/v1/auth/login', { username, password });
        this.token = response.data.token;
        localStorage.setItem('token', this.token);
        // this.user = ... fetch user info
        return true;
      } catch (error) {
        console.error('Login failed', error);
        return false;
      }
    },
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('token');
    }
  }
})
