<template>
  <v-layout class="rounded rounded-md">
    <v-app-bar :elevation="2" density="compact" scroll-behavior="hide">
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
          <v-btn value="VIEW" data-test="mode-view">
              <v-icon left>mdi-eye</v-icon> View
          </v-btn>
          <v-btn value="EDIT" data-test="mode-edit">
              <v-icon left>mdi-pencil</v-icon> Edit
          </v-btn>
      </v-btn-toggle>
      
      <v-btn v-if="authStore.isAdmin" icon :to="isAdminRoute ? '/' : '/admin'" :title="isAdminRoute ? 'Back to App' : 'Admin Dashboard'">
          <v-icon>{{ isAdminRoute ? 'mdi-home' : 'mdi-shield-account' }}</v-icon>
      </v-btn>
  
      <v-btn icon :to="isProfileRoute ? '/' : '/profile'" :title="isProfileRoute ? 'Back to App' : 'My Profile'" data-test="profile-button">
          <v-icon>{{ isProfileRoute ? 'mdi-home' : 'mdi-account' }}</v-icon>
      </v-btn>
  
      <v-btn icon @click="logout" title="Logout">
        <v-icon>mdi-logout</v-icon>
      </v-btn>
    </v-app-bar>
  
    <v-navigation-drawer v-model="drawer" width="300" v-if="!isAdminRoute">
      <!-- System Categories -->
      <v-list-subheader>System Categories</v-list-subheader>
      <v-list density="compact" nav class="sidebar-list">
          <v-list-item
              prepend-icon="mdi-folder-outline"
              title="All Documents"
              @click="filterByCategory(null)"
              :active="!documentStore.selectedCategoryId && !documentStore.selectedTag"
              color="primary"
          ></v-list-item>
           <RecursiveList 
              v-for="cat in documentStore.systemCategories" 
              :key="cat.id" 
              :item="cat" 
              @select="filterByCategory"
              :selectedId="documentStore.selectedCategoryId"
          />
      </v-list>

      <v-divider class="my-2"></v-divider>

      <!-- User Categories (My Documents) -->
      <div class="d-flex align-center px-4 pt-2">
          <v-list-subheader class="pl-0">My Documents</v-list-subheader>
          <v-spacer></v-spacer>
          <!-- Manage Button -->
          <v-btn icon size="x-small" variant="text" to="/my-categories" title="Manage Folders">
              <v-icon>mdi-cog</v-icon>
          </v-btn>
      </div>
      
      <v-list density="compact" nav class="sidebar-list">
           <RecursiveList 
              v-for="cat in documentStore.userCategories" 
              :key="cat.id" 
              :item="cat" 
              @select="filterByCategory"
              :selectedId="documentStore.selectedCategoryId"
          />
           <v-list-item v-if="documentStore.userCategories.length === 0" class="text-caption text-grey">
              No personal folders yet.
           </v-list-item>
      </v-list>

       <v-divider class="my-2"></v-divider>

       <!-- Tags -->
       <v-list-subheader>Tags</v-list-subheader>
       <v-chip-group column class="px-3" v-model="selectedTagIndex" @update:model-value="onTagSelected">
           <v-chip
             v-for="tag in documentStore.tags"
             :key="tag.name"
             :value="tag.name"
             filter
             variant="outlined"
             size="small"
             :class="{'bg-primary text-white': documentStore.selectedTag === tag.name}"
           >
             {{ tag.name }} ({{ tag.count }})
           </v-chip>
       </v-chip-group>
    </v-navigation-drawer>
    
    <v-main style="min-height: 300px;">
        <router-view />
    </v-main>
  </v-layout>
</template>

<style scoped>
/* Reduce gap between icon and text by half */
.sidebar-list :deep(.v-list-item__prepend) {
    margin-inline-end: 8px !important; /* Default is usually around 16px-32px depending on explicit width */
    width: 24px !important; /* Force tighter icon width if needed */
    min-width: 24px !important;
}

/* Reduce vertical padding/spacing by half */
.sidebar-list :deep(.v-list-item) {
    min-height: 32px !important; /* Reduce height */
    padding-top: 2px !important;
    padding-bottom: 2px !important;
}

/* Ensure nested RecursiveList doesn't double-pad or reset */
.sidebar-list :deep(.v-list-group__items .v-list-item) {
   padding-inline-start: 16px !important; /* Keep indent but tight vertically */
}
</style>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useDocumentStore } from '../stores/document';
import { useRouter, useRoute } from 'vue-router';
import RecursiveList from '../components/RecursiveList.vue';
import { computed } from 'vue';

const drawer = ref(true);
const authStore = useAuthStore();
const documentStore = useDocumentStore();
const router = useRouter();
const route = useRoute();

const isAdminRoute = computed(() => {
    return route.path.startsWith('/admin');
});

const isProfileRoute = computed(() => {
    return route.path.startsWith('/profile');
});

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
    router.push({ name: 'home' });
    await documentStore.fetchDocuments(catId);
};

const selectedTagIndex = ref(null); // Local ref for chip group, but sync with store

const onTagSelected = async (tag) => { 
    console.log("MainLayout: onTagSelected called with:", tag);
    if (tag) {
        documentStore.setSelectedTag(tag);
    } else {
        documentStore.setSelectedTag(null);
    }
    
    documentStore.currentDocument = null;
    router.push({ name: 'home' });
    console.log("MainLayout: Fetching documents after tag selection. Tag:", documentStore.selectedTag, "Category:", documentStore.selectedCategoryId);
    await documentStore.fetchDocuments(); 
};



    
onMounted(async () => {
    // Initial fetch of categories for the sidebar
    documentStore.fetchCategories();
    documentStore.fetchTags(); // Fetch tags
    // Ensure profile is loaded for UI logic (like Admin toggle)
    if (authStore.isAuthenticated && !authStore.user) {
        await authStore.fetchProfile();
    }
});
</script>
