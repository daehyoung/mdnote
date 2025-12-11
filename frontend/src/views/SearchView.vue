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
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '../services/api';

const route = useRoute();
const router = useRouter();
const query = ref('');
const results = ref([]);
const loading = ref(false);

const performSearch = async (q) => {
    if (!q) return;
    loading.value = true;
    try {
        const response = await api.get(`/documents/search?q=${encodeURIComponent(q)}`);
        results.value = response.data;
    } catch (e) {
        console.error("Search failed", e);
    } finally {
        loading.value = false;
    }
};

const getSnippet = (content) => {
    if (!content) return '';
    return content.substring(0, 100) + '...';
};

const openDocument = (id) => {
    // Navigate home and select doc? Or just assume Home with query param?
    // For now, let's go Home but maybe we need logic to open specific doc
    // We can pass query param to home or just rely on manual navigation for now.
    // DocumentStore selection logic is client side inside HomeView based on ID?
    // Let's simpler: Store selected ID in store or URL.
    // Ideally routing should be /documents/:id.
    // But our HomeView is monolithic (sidebar + main).
    // Let's just push to home (which might reset state) but simpler for MVP.
    router.push('/'); 
    // Ideally we would want to signal HomeView to open this doc.
    // Can use query param ?docId=...
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
