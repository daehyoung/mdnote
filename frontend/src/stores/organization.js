import { defineStore } from 'pinia';
import api from '../services/api';

export const useOrganizationStore = defineStore('organization', {
  state: () => ({
    departments: [], // Tree
    loading: false,
    error: null,
  }),
  actions: {
    async fetchDepartments() {
        this.loading = true;
        try {
            const response = await api.get('/departments/tree');
            this.departments = response.data;
        } catch (error) {
            console.error(error);
            this.error = "Failed to fetch departments";
        } finally {
            this.loading = false;
        }
    },
    async createDepartment(name, parentId) {
        try {
            await api.post('/departments', { name, parentId });
            await this.fetchDepartments();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },
    async updateDepartment(id, name) {
        try {
            await api.put(`/departments/${id}`, { name });
            await this.fetchDepartments();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },
    async deleteDepartment(id) {
        try {
            await api.delete(`/departments/${id}`);
            await this.fetchDepartments();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },
    async assignUser(userId, deptId) {
        try {
             // DeptId can be null
            await api.put(`/admin/users/${userId}/department`, deptId); 
        } catch (error) {
            console.error(error);
            throw error;
        }
    }
  },
});
