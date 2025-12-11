import { defineStore } from 'pinia';
import api from '../services/api';

export const useDocumentStore = defineStore('document', {
  state: () => ({
    documents: [],
    currentDocument: null,
    comments: [], // New state for comments
    categories: [], // Tree structure
    loading: false,
    error: null,
    appMode: 'VIEW', // Added shared state for UI Mode
    // Pagination & Filter State
    page: 1, // 1-based for Vuetify pagination
    size: 10,
    totalPages: 0,
    totalElements: 0,
    filterStatus: null, // For status filtering
    selectedCategoryId: null, // For category filtering
  }),
  actions: {
    async fetchDocuments(categoryId = null) {
      console.log('Store: fetchDocuments called with category:', categoryId);
      this.loading = true;
      try {
        // Construct query params
        const params = new URLSearchParams();
        // Handle Category
        // Note: categoryId argument overrides state if provided (for consistent valid usage)
        // Check if we are "All Categories" (null) or specific
        const targetCatId = categoryId !== undefined ? categoryId : this.selectedCategoryId;
        if (targetCatId) {
            params.append('categoryId', targetCatId);
        }
        
        // Handle Pagination (Backend matches 0-based, Vuetify is 1-based)
        params.append('page', this.page - 1); 
        params.append('size', this.size);
        
        // Handle Status Filter
        if (this.filterStatus) {
            params.append('status', this.filterStatus);
        }

        const response = await api.get(`/documents?${params.toString()}`);
        
        // Handle Page response structure
        if (response.data.content) {
            this.documents = response.data.content;
            this.totalPages = response.data.totalPages;
            this.totalElements = response.data.totalElements;
        } else {
            // Fallback if backend returns list (shouldn't happen with new backend)
            this.documents = response.data;
            this.totalPages = 1; 
        }
      } catch (error) {
        this.error = 'Failed to fetch documents';
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    async createDocument(title, content, categoryId = null) {
        this.loading = true;
        try {
            const payload = { title, content };
            if (categoryId) {
                payload.category = { id: categoryId };
            }
            const response = await api.post('/documents', payload);
            this.documents.push(response.data);
            return response.data;
        } catch (error) {
            this.error = 'Failed to create document';
            throw error;
        } finally {
            this.loading = false;
        }
    },
    async updateDocument(id, document) {
        this.loading = true;
        try {
            const response = await api.put(`/documents/${id}`, document);
            const index = this.documents.findIndex(d => d.id === id);
            if (index !== -1) {
                this.documents[index] = response.data;
            }
            if (this.currentDocument && this.currentDocument.id === id) {
                this.currentDocument = response.data;
            }
        } catch (error) {
             this.error = 'Failed to update document';
             throw error;
        } finally {
            this.loading = false;
        }
    },
    async deleteDocument(id) {
        try {
            await api.delete(`/documents/${id}`);
            this.documents = this.documents.filter(d => d.id !== id);
            if (this.currentDocument && this.currentDocument.id === id) {
                this.currentDocument = null;
            }
        } catch (error) {
            this.error = 'Failed to delete document';
            throw error;
        }
    },
    setCurrentDocument(document) {
      this.currentDocument = document;
      this.comments = []; // Reset comments when switching doc
    },
    async fetchComments(docId) {
        try {
            const response = await api.get(`/documents/${docId}/comments`);
            this.comments = response.data;
        } catch (error) {
            console.error("Failed to fetch comments", error);
        }
    },
    async fetchCategories() {
        try {
            const response = await api.get('/categories/tree');
            this.categories = response.data;
        } catch (error) {
            console.error("Failed to fetch categories", error);
        }
    },
    async createCategory(name, parentId) {
        try {
            await api.post('/categories', { name, parentId });
            await this.fetchCategories();
        } catch (error) {
             console.error("Failed to create category", error);
             throw error;
        }
    },
    async updateCategory(id, name) {
        try {
            await api.put(`/categories/${id}`, { name });
            await this.fetchCategories();
        } catch (error) {
             console.error("Failed to update category", error);
             throw error;
        }
    },
    async deleteCategory(id) {
        try {
             await api.delete(`/categories/${id}`);
             await this.fetchCategories();
        } catch (error) {
             console.error("Failed to delete category", error);
             throw error;
        }
    },
    async addComment(docId, content) {
        try {
            const response = await api.post(`/documents/${docId}/comments`, { content });
            this.comments.push(response.data);
        } catch (error) {
            console.error("Failed to add comment", error);
            throw error;
        }
    },
    async deleteComment(docId, commentId) {
        try {
            await api.delete(`/documents/${docId}/comments/${commentId}`);
            await this.fetchComments(docId);
        } catch (error) {
            console.error("Failed to delete comment", error);
            throw error;
        }
    },
    async deleteAttachment(attachmentId) {
        try {
            await api.delete(`/attachments/${attachmentId}`);
            // Refresh doc? Attachments are usually part of doc.
            // But current doc store might not update automatically.
            // Ideally re-fetch current document.
            if (this.currentDocument) {
                await this.fetchDocument(this.currentDocument.id);
            }
        } catch (error) {
            console.error("Failed to delete attachment", error);
            throw error;
        }
    },
    async fetchDocument(id) {
        try {
            const response = await api.get(`/documents/${id}`);
            this.setCurrentDocument(response.data);
            return response.data;
        } catch (error) {
            console.error(error);
        }
    },
    // Helper to refresh current document state manually without full fetch
    updateCurrentDocumentAttachments(attachment) {
        if (this.currentDocument) {
             if (!this.currentDocument.attachments) {
                 this.currentDocument.attachments = [];
             }
             this.currentDocument.attachments.push(attachment);
        }
    },
    setAppMode(mode) {
        this.appMode = mode;
    },
    setSelectedCategoryId(id) {
        this.selectedCategoryId = id;
        this.page = 1; // Reset to first page when category changes
    },
    setFilterStatus(status) {
        this.filterStatus = status;
        this.page = 1; // Reset to first page
    },
    setPage(page) {
        this.page = page;
    },
    async searchDocuments(query) {
        this.loading = true;
        try {
            const response = await api.get(`/search?q=${query}`);
            this.documents = response.data;
        } catch (error) {
            this.error = 'Failed to search documents';
            console.error(error);
        } finally {
            this.loading = false;
        }
    }
  },
});
