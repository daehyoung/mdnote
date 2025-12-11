<template>
    <v-container fluid class="fill-height pa-0 ma-0" style="width: 100%; max-width: 100%;">
      <v-row no-gutters class="fill-height">
        <v-col cols="12" class="d-flex flex-column fill-height pa-0 ma-0">
            <div class="d-flex flex-column fill-height">
                 <!-- Header -->
                  <v-toolbar dense flat class="border-b" color="surface">
                       <v-btn icon @click="goBack" data-test="back-button">
                         <v-icon>mdi-arrow-left</v-icon>
                     </v-btn>
                    <v-toolbar-title class="d-flex align-center">
                        <span class="mr-3">{{ documentStore.currentDocument?.title }}</span>
                        <v-chip 
                            v-for="tag in documentStore.currentDocument?.tags" 
                            :key="tag.id" 
                            size="small" 
                            label 
                            color="primary" 
                            variant="outlined" 
                            class="mr-1"
                        >
                            {{ tag.name }}
                        </v-chip>
                    </v-toolbar-title>
                    
                    <v-spacer></v-spacer>
                    
                    <v-btn 
                        v-if="canEdit" 
                        color="primary" 
                        @click="goToEdit"
                        prepend-icon="mdi-pencil"
                        variant="tonal"
                        data-test="edit-document-button"
                    >
                        Edit
                    </v-btn>
                 </v-toolbar>

                <!-- Content Area -->
                <div class="d-flex flex-grow-1 flex-column" style="height: calc(100vh - 128px); overflow-y: auto;">
                    <!-- Preview Container -->
                    <div class="d-flex flex-grow-1">
                        <div 
                            class="pa-4 flex-grow-1 markdown-body"
                            v-html="compiledViewMarkdown"
                            style="min-height: 500px;"
                            :data-theme="markdownTheme"
                        ></div>
                    </div>
    
                    <!-- Meta Info Bar -->
                    <div v-if="documentStore.currentDocument" class="pa-4 border-t">
                        <div class="d-flex flex-wrap align-center">
                            <span class="text-caption font-weight-bold mr-2">Author:</span>
                            <span class="text-caption mr-4">{{ documentStore.currentDocument.author?.name || documentStore.currentDocument.author?.username }} 
                                <span v-if="documentStore.currentDocument.author?.department" class="text-grey">({{ documentStore.currentDocument.author.department.name }})</span>
                            </span>

                            <v-divider vertical class="mx-2"></v-divider>

                            <span class="text-caption font-weight-bold mr-2">Permissions:</span>
                            
                            <v-chip size="x-small" label class="mr-1" :color="documentStore.currentDocument.publicRead ? 'success' : 'grey'" variant="outlined">
                                <v-icon start size="small">mdi-web</v-icon> Public Read
                            </v-chip>
                            <v-chip size="x-small" label class="mr-1" :color="documentStore.currentDocument.publicWrite ? 'warning' : 'grey'" variant="outlined">
                                <v-icon start size="small">mdi-pencil</v-icon> Public Write
                            </v-chip>
                            
                            <v-chip size="x-small" label class="mr-1" :color="documentStore.currentDocument.groupRead ? 'success' : 'grey'" variant="outlined">
                                <v-icon start size="small">mdi-account-group</v-icon> Group Read
                            </v-chip>
                            <v-chip size="x-small" label class="mr-1" :color="documentStore.currentDocument.groupWrite ? 'warning' : 'grey'" variant="outlined">
                                <v-icon start size="small">mdi-account-edit</v-icon> Group Write
                            </v-chip>
                        </div>
                    </div>

                    <!-- Comments Section -->
                    <div class="pa-4" v-if="documentStore.currentDocument?.allowComments">
                        <div class="text-h6 mb-2">Comments</div>
                        <v-list class="bg-transparent rounded mb-2" density="compact">
                             <v-list-item v-for="comment in documentStore.comments" :key="comment.id" class="mb-1">
                                <template v-slot:prepend>
                                    <v-avatar color="secondary" size="small" variant="tonal" class="mr-2">
                                        {{ (comment.user?.name || comment.user?.username || '?').charAt(0).toUpperCase() }}
                                    </v-avatar>
                                </template>
                                <template v-slot:append>
                                      <!-- Allow delete if owner (simple check) -->
                                     <v-btn v-if="authStore.user && (authStore.user.id === comment.user?.id || authStore.user.role === 'ADMIN')" 
                                            icon size="x-small" variant="text" color="error" @click="removeComment(comment.id)">
                                        <v-icon>mdi-delete</v-icon>
                                    </v-btn>
                                </template>
                                <v-list-item-title class="text-body-2 font-weight-bold">
                                    {{ comment.user ? comment.user.name : 'Unknown' }} 
                                    <span class="text-caption text-grey ml-2">{{ new Date(comment.createdAt).toLocaleString() }}</span>
                                </v-list-item-title>
                                <v-list-item-subtitle class="text-body-1 text-high-emphasis pt-1" style="white-space: pre-wrap;">{{ comment.content }}</v-list-item-subtitle>
                            </v-list-item>
                            <v-list-item v-if="!documentStore.comments.length">
                                <v-list-item-title class="text-grey text-caption">No comments yet.</v-list-item-title>
                            </v-list-item>
                        </v-list>
                        
                        <div class="d-flex align-center">
                            <v-text-field
                                v-model="newComment"
                                label="Write a comment..."
                                variant="outlined"
                                density="compact"
                                hide-details
                                class="mr-2"
                                @keyup.enter="postComment"
                            ></v-text-field>
                            <v-btn color="primary" @click="postComment" :disabled="!newComment.trim()">
                                Post
                            </v-btn>
                        </div>
                    </div>
    
                    <!-- Attachments could go here if requested for view mode -->
                </div>
            </div>
        </v-col>
      </v-row>
    </v-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useDocumentStore } from '../stores/document';
import { useRouter, useRoute } from 'vue-router';
import { marked, mermaid } from '../utils/markdown';
import { useTheme } from 'vuetify';

const authStore = useAuthStore();
const documentStore = useDocumentStore();
const router = useRouter();
const route = useRoute();
const theme = useTheme();

const markdownTheme = computed(() => {
    return theme.global.current.value.dark ? 'dark' : 'light';
});

const newComment = ref('');

const compiledViewMarkdown = computed(() => {
    return marked(documentStore.currentDocument?.content || '');
});

// Watch for changes and render mermaid
watch(compiledViewMarkdown, async () => {
    await nextTick();
    try {
        await mermaid.run({
            nodes: document.querySelectorAll('.mermaid')
        });
    } catch (e) {
        console.error('Mermaid render error:', e);
    }
});

const canEdit = computed(() => {
    if (!documentStore.currentDocument) return false;
    const user = authStore.user;
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    
    const author = documentStore.currentDocument.author;
    if (!author) return false; 
    
    // Owner check
    if (author.username === user.username) return true;
    
    // Group Write
    if (documentStore.currentDocument.groupWrite && user.department && author.department && user.department.id === author.department.id) return true;
    
    // Public Write
    if (documentStore.currentDocument.publicWrite) return true;

    return false;
});

const loadDocument = async (docId) => {
    if (!docId) return;
    await documentStore.fetchDocument(docId);
    await documentStore.fetchComments(docId);
};

const postComment = async () => {
    if (!newComment.value || !documentStore.currentDocument) return;
    try {
        await documentStore.addComment(documentStore.currentDocument.id, newComment.value);
        newComment.value = '';
    } catch (e) {
        console.error("Failed to post comment", e);
    }
};

const removeComment = async (commentId) => {
    if (!confirm('Delete comment?')) return;
    try {
        await documentStore.deleteComment(documentStore.currentDocument.id, commentId);
    } catch (e) {
        // alert("Failed to delete comment");
        console.error("Failed to delete comment", e);
    }
};

const goBack = () => {
    router.push({ name: 'home' });
};

const goToEdit = () => {
    if (documentStore.currentDocument) {
        router.push({ name: 'document-edit', params: { id: documentStore.currentDocument.id } });
    }
};

onMounted(() => {
    if (route.params.id) {
        loadDocument(route.params.id);
    }
});

watch(() => route.params.id, (newId) => {
    loadDocument(newId);
});
</script>

<style scoped>
.markdown-body {
    box-sizing: border-box;
    min-width: 200px;
    padding: 10px;
    width: 100%;
}
</style>
