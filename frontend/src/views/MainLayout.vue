<template>
  <v-layout class="rounded rounded-md">
    <v-app-bar color="primary" dark>
      <v-app-bar-nav-icon @click="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title>MD Note</v-toolbar-title>
      
      <v-btn-toggle
          v-model="appMode"
          mandatory
          dense
          rounded
          class="mx-4"
          @update:model-value="updateAppMode"
      >
          <v-btn value="VIEW">
              <v-icon left>mdi-eye</v-icon> View
          </v-btn>
          <v-btn value="EDIT">
              <v-icon left>mdi-pencil</v-icon> Edit
          </v-btn>
      </v-btn-toggle>
      
      <v-btn icon to="/admin" title="Admin Dashboard">
          <v-icon>mdi-shield-account</v-icon>
      </v-btn>
  
      <v-btn icon to="/profile" title="My Profile">
          <v-icon>mdi-account</v-icon>
      </v-btn>
  
      <v-btn icon @click="logout" title="Logout">
        <v-icon>mdi-logout</v-icon>
      </v-btn>
    </v-app-bar>
  
    <v-navigation-drawer v-model="drawer" width="300">
      <div class="pa-2">
      </div>
      <v-divider></v-divider>
      <v-list-subheader>Categories</v-list-subheader>
      <v-list dense nav>
          <v-list-item
              prepend-icon="mdi-folder-outline"
              title="All Categories"
              @click="filterByCategory(null)"
              :active="!documentStore.selectedCategoryId"
              color="primary"
          ></v-list-item>
           <RecursiveList 
              v-for="cat in documentStore.categories" 
              :key="cat.id" 
              :item="cat" 
              @select="filterByCategory"
              :selectedId="documentStore.selectedCategoryId"
          />
      </v-list>
    </v-navigation-drawer>
    
    <v-main class="d-flex align-center justify-center" style="min-height: 300px;">
        <router-view />
    </v-main>
  </v-layout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useDocumentStore } from '../stores/document';
import { useRouter } from 'vue-router';
import RecursiveList from '../components/RecursiveList.vue';

const drawer = ref(true);
const authStore = useAuthStore();
const documentStore = useDocumentStore();
const router = useRouter();

// Sync with store state
const appMode = ref(documentStore.appMode);

// Helper to update store when toggle changes
const updateAppMode = (val) => {
    documentStore.setAppMode(val);
};

const logout = () => {
  authStore.logout();
  router.push('/login');
};

const filterByCategory = async (category) => {
    const catId = category ? category.id : null;
    documentStore.setSelectedCategoryId(catId);
    documentStore.currentDocument = null; // Clear view to show list
    await documentStore.fetchDocuments(catId);
};



onMounted(() => {
    // Initial fetch of categories for the sidebar
    documentStore.fetchCategories();
});
</script>
