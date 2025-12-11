<template>
    <v-container fluid class="fill-height pa-0 ma-0" style="width: 100%; max-width: 100%;">
      <v-row no-gutters class="fill-height">
        <v-col cols="12" class="d-flex flex-column fill-height pa-0 ma-0">
             <!-- List View -->
            <div class="d-flex flex-column fill-height pa-4">
                 <div class="d-flex align-center mb-4 flex-wrap">
                    <h2 class="text-h5 mr-4">Documents</h2>
                    
                    <!-- Search Input with Explicit Button -->
                    <v-text-field
                        v-model="searchQueryInput"
                        dense
                        outlined
                        hide-details
                        label="Search..."
                        style="max-width: 300px;"
                        clearable
                        class="mr-2"
                        @keyup.enter="handleSearch"
                        id="search-input"
                    ></v-text-field>
                    <v-btn icon @click="handleSearch" class="mr-4" title="Search">
                        <v-icon>mdi-magnify</v-icon>
                    </v-btn>
                    
                    <!-- Status Filter -->
                     <v-select
                        v-if="appMode === 'EDIT'" 
                        v-model="filterStatus"
                        :items="['DRAFT', 'REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED']"
                        label="Status"
                        dense
                        outlined
                        hide-details
                        clearable
                        style="max-width: 150px;"
                        class="mr-4"
                        @update:modelValue="handleStatusFilter"
                    ></v-select>

                    <v-spacer></v-spacer>

                    <v-btn 
                        v-if="isAuthorizedForNewDoc && appMode === 'EDIT'"
                        color="primary" 
                        @click="createNewDocument"
                    >
                        <v-icon left>mdi-plus</v-icon> New Document
                    </v-btn>
                 </div>
                 
                 <v-card variant="flat" class="flex-grow-1 d-flex flex-column">
                    <v-list v-if="documentStore.loading && !documentStore.documents.length">
                         <v-list-item>Loading...</v-list-item>
                    </v-list>
                    <v-list v-else lines="two" class="flex-grow-1">
                        <v-list-item
                            v-for="doc in documentStore.documents"
                            :key="doc.id"
                            :value="doc.id"
                            @click="selectDocument(doc)"
                            border
                            class="mb-2 rounded"
                        >
                            <template v-slot:prepend>
                                <v-avatar color="primary" variant="tonal">
                                    <v-icon>mdi-file-document-outline</v-icon>
                                </v-avatar>
                            </template>
                            <v-list-item-title class="font-weight-bold d-flex align-center flex-wrap">
                                {{ doc.title || 'Untitled' }} 
                                <span class="text-caption text-grey ml-2" v-if="doc.author">
                                    by {{ doc.author.name || doc.author.username }}
                                    <span v-if="doc.author?.department && doc.author?.department?.name">({{ doc.author.department.name }})</span>
                                </span>
                                <v-chip x-small v-if="doc.status" class="ml-2" size="x-small" color="secondary" variant="outlined">{{ doc.status }}</v-chip>
                            </v-list-item-title>
                            <v-list-item-subtitle class="d-flex align-center mt-1 flex-wrap">
                                <span class="text-caption mr-2">{{ new Date(doc.createdAt).toLocaleDateString() }}</span>
                                <v-chip size="x-small" label class="mr-1" v-if="doc.category">{{ doc.category.name }}</v-chip>
                                <v-chip size="x-small" label class="mr-1" v-else>Uncategorized</v-chip>
                                
                                <v-chip 
                                    v-for="tag in doc.tags" 
                                    :key="tag.id" 
                                    size="x-small" 
                                    label 
                                    variant="outlined" 
                                    class="mr-1"
                                >
                                    {{ tag.name }}
                                </v-chip>

                                <v-spacer></v-spacer>

                                <!-- Permission Icons (Mini) -->
                                <v-icon size="small" class="mr-2" :color="doc.publicRead ? 'success' : 'grey-lighten-2'" :title="doc.publicRead ? 'Public Read Class: Allowed' : 'Public Read: Denied'">mdi-web</v-icon>
                                <v-icon size="small" class="mr-2" :color="doc.publicWrite ? 'warning' : 'grey-lighten-2'" :title="doc.publicWrite ? 'Public Write: Allowed' : 'Public Write: Denied'">mdi-pencil</v-icon>
                                <v-icon size="small" class="mr-2" :color="doc.groupRead ? 'success' : 'grey-lighten-2'" :title="doc.groupRead ? 'Group Read: Allowed' : 'Group Read: Denied'">mdi-account-group</v-icon>
                                <v-icon size="small" :color="doc.groupWrite ? 'warning' : 'grey-lighten-2'" :title="doc.groupWrite ? 'Group Write: Allowed' : 'Group Write: Denied'">mdi-account-edit</v-icon>
                            </v-list-item-subtitle>
                        </v-list-item>
                        <v-list-item v-if="!documentStore.documents.length">
                            <v-list-item-title class="text-grey">No documents found.</v-list-item-title>
                        </v-list-item>
                    </v-list>

                    <!-- Pagination -->
                    <div class="d-flex justify-center pa-2 border-t" v-if="documentStore.totalPages > 1">
                        <v-pagination
                            v-model="currentPage"
                            :length="documentStore.totalPages"
                            :total-visible="7"
                            @update:modelValue="handlePageChange"
                            density="compact"
                        ></v-pagination>
                    </div>
                 </v-card>
            </div>
        </v-col>
      </v-row>
    </v-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useDocumentStore } from '../stores/document';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const documentStore = useDocumentStore();
const router = useRouter();

const searchQueryInput = ref('');
const filterStatus = ref(null);

const appMode = computed(() => documentStore.appMode); // Might remove appMode dependence later, or keep for simple toggle

const isAuthorizedForNewDoc = computed(() => {
    // Basic check: user must be logged in. 
    return !!authStore.user;
});

const currentPage = computed({
    get: () => documentStore.page,
    set: (val) => documentStore.setPage(val)
});

const handleSearch = () => {
    if (searchQueryInput.value) {
        documentStore.searchDocuments(searchQueryInput.value); 
    } else {
        documentStore.setSearchQuery('');
        fetchDocuments(); 
    }
};

const handlePageChange = () => {
    fetchDocuments();
};

const handleStatusFilter = () => {
    documentStore.setFilterStatus(filterStatus.value);
    fetchDocuments();
};

const fetchDocuments = async () => {
    await documentStore.fetchDocuments(documentStore.selectedCategoryId);
};

const selectDocument = (doc) => {
    router.push({ name: 'document-detail', params: { id: doc.id } });
};

const createNewDocument = async () => {
    try {
        const newDoc = await documentStore.createDocument('Untitled', '', documentStore.selectedCategoryId);
        // Navigate to EDIT view for new document
        router.push({ name: 'document-edit', params: { id: newDoc.id } });
    } catch (e) {
        console.error("Failed to create", e);
    }
};

onMounted(async () => {
    console.log('HomeView Mounted');
    await fetchDocuments();
});
</script>

<style scoped>
/* Scoped styles if any */
</style>
