<template>
  <v-container>
    <h1 class="text-h4 mb-4">Search Results: "{{ query }}"</h1>

    <div v-if="loading" class="text-center">
        <v-progress-circular indeterminate></v-progress-circular>
    </div>
    
    <v-list v-else-if="results.length">
      <v-list-item
        v-for="doc in results"
        :key="doc.id"
        :title="doc.title"
        :subtitle="getSnippet(doc.content)"
        @click="openDocument(doc.id)"
        prepend-icon="mdi-file-document-outline"
      >
      </v-list-item>
    </v-list>
    
    <v-alert v-else type="info">No results found.</v-alert>
  </v-container>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDocumentStore } from '../stores/document';

const route = useRoute();
const router = useRouter();
const documentStore = useDocumentStore();

const query = ref('');
const results = computed(() => documentStore.documents);
const loading = computed(() => documentStore.loading);

const performSearch = async (q) => {
    if (!q) {
        documentStore.documents = []; // Clear results
        return;
    }
    await documentStore.searchDocuments(q);
};

const getSnippet = (content) => {
    if (!content) return '';
    return content.substring(0, 100) + '...';
};

const openDocument = (id) => {
    router.push(`/documents/${id}`);
};

onMounted(() => {
    query.value = route.query.q || '';
    performSearch(query.value);
});

watch(() => route.query.q, (newQ) => {
    query.value = newQ || '';
    performSearch(newQ);
});
</script>
