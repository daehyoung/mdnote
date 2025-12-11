import { defineStore } from 'pinia';
import api from '../services/api';

export const useAdminStore = defineStore('admin', {
  state: () => ({
    users: [],
    loading: false,
    error: null,
  }),
  actions: {
    async fetchUsers() {
      this.loading = true;
      try {
        const response = await api.get('/admin/users');
        this.users = response.data;
      } catch (error) {
        this.error = 'Failed to fetch users';
        console.error("Failed to fetch users", error);
      } finally {
        this.loading = false;
      }
    },
    async toggleUserStatus(user) {
        const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
        try {
            await api.put(`/admin/users/${user.id}/status`, newStatus, {
                headers: { 'Content-Type': 'text/plain' }
            });
            // Update local state
            const target = this.users.find(u => u.id === user.id);
            if (target) {
                target.status = newStatus;
            }
        } catch (error) {
            console.error("Failed to update status", error);
            throw error;
        }
    },
    async createUser(userData) {
        try {
            await api.post('/admin/users', userData);
            await this.fetchUsers(); // Refresh list to get new user with ID
        } catch (error) {
            console.error("Failed to create user", error);
            throw error;
        }
    },
    async updateUser(id, userData) {
        try {
            await api.put(`/admin/users/${id}`, userData);
            const target = this.users.find(u => u.id === id);
            if (target) {
                // Optimistic update
                Object.assign(target, userData);
            }
        } catch (error) {
            console.error("Failed to update user", error);
            throw error;
        }
    },
    async resetPassword(id, newPassword) {
        try {
            await api.put(`/admin/users/${id}/password`, newPassword, {
                headers: { 'Content-Type': 'text/plain' }
            });
        } catch (error) {
            console.error("Failed to reset password", error);
            throw error;
        }
    }
  }
});
