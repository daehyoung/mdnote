import { defineStore } from 'pinia'
import axios from 'axios'
import api from '../services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    theme: localStorage.getItem('theme') || 'light' // Default to local storage or light
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    async login(username, password) {
      try {
        const response = await axios.post('/api/v1/auth/login', { username, password });
        this.token = response.data.token;
        this.theme = response.data.theme || 'light';
        localStorage.setItem('token', this.token);
        localStorage.setItem('theme', this.theme);
        
        // Fetch full profile to populate user state (roles, etc.)
        await this.fetchProfile();
        
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
      // Optional: clear theme or keep it? Keeping it is usually better UX.
      // localStorage.removeItem('theme'); 
    },
    async fetchProfile() {
        try {
            const response = await api.get('/profile');
            // Check if we need to store full profile in state or just return
            // For now, let's update a profile state if we had one, 
            // but the view was using local ref. Let's add profile to state.
            this.user = response.data;
            if (this.user.theme) {
                this.theme = this.user.theme;
                localStorage.setItem('theme', this.theme);
            }
            // Ensure we keep the user role
            return response.data;
        } catch (error) {
            console.error("Failed to fetch profile", error);
            throw error;
        }
    },
    async updateProfile(name, email) {
        try {
            await api.put('/profile', { name, email });
            // Update local state if needed (or fetchProfile again)
            if (this.user) {
                this.user.name = name;
                this.user.email = email;
            }
        } catch (error) {
            console.error("Failed to update profile", error);
            throw error;
        }
    },
    async updateTheme(newTheme) {
        try {
            await api.put('/profile', { theme: newTheme });
            if (this.user) {
                this.user.theme = newTheme;
            }
            this.theme = newTheme;
            localStorage.setItem('theme', newTheme);
        } catch (error) {
            console.error("Failed to update theme", error);
            throw error;
        }
    },
    async changePassword(password) {
        try {
            await api.put('/profile/password', { password });
        } catch (error) {
             console.error("Failed to change password", error);
             throw error;
        }
    }
  }
})
