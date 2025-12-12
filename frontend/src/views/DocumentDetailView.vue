<template>
    <v-container fluid class="pa-0 ma-0" style="width: 100%; max-width: 100%;">
      <v-row no-gutters>
        <v-col cols="12" class="d-flex flex-column pa-0 ma-0">
            <div class="d-flex flex-column">
                 <!-- Header -->
                  <v-toolbar density="compact" flat class="border-b" color="surface" style="position: sticky; top: 0; z-index: 100;">
                       <v-btn icon @click="goBack" data-test="back-button">
                         <v-icon>mdi-arrow-left</v-icon>
                     </v-btn>
                    <v-toolbar-title style="flex: 0 1 auto; min-width: 0;" class="mr-4">
                        <span class="text-truncate d-block">{{ documentStore.currentDocument?.title }}</span>
                    </v-toolbar-title>

                    <!-- Metadata Group -->
                    <div class="d-flex align-center hidden-xs-only" style="flex: 1 1 auto; min-width: 0; overflow: hidden;">
                         <!-- Tags -->
                        <v-chip 
                            v-for="tag in documentStore.currentDocument?.tags" 
                            :key="tag.id" 
                            size="x-small" 
                            label 
                            color="primary" 
                            variant="outlined" 
                            class="mr-1"
                        >
                            {{ tag.name }}
                        </v-chip>

                        <v-divider vertical class="mx-2 hidden-sm-and-down" v-if="documentStore.currentDocument?.author"></v-divider>

                        <!-- Author Info -->
                        <div class="d-flex flex-column ml-1 hidden-sm-and-down" v-if="documentStore.currentDocument?.author" style="white-space: nowrap;">
                            <span class="text-caption font-weight-bold">
                                {{ documentStore.currentDocument.author?.name || documentStore.currentDocument.author?.username }}
                                <span v-if="documentStore.currentDocument.author?.department" class="text-grey">({{ documentStore.currentDocument.author.department.name }})</span>
                            </span>
                        </div>

                         <v-divider vertical class="mx-2 hidden-md-and-down"></v-divider>

                        <!-- Permissions (Mini) -->
                        <div class="d-flex align-center hidden-md-and-down" v-if="documentStore.currentDocument">
                            <v-chip size="x-small" density="comfortable" label class="mr-1" :color="documentStore.currentDocument.publicRead ? 'success' : 'grey'" variant="text" :prepend-icon="documentStore.currentDocument.publicRead ? 'mdi-check' : 'mdi-close'">
                                Public Read
                            </v-chip>
                             <v-chip v-if="documentStore.currentDocument.groupRead" size="x-small" density="comfortable" label class="mr-1" color="success" variant="text" prepend-icon="mdi-account-group">
                                Group Read
                            </v-chip>
                        </div>

                        <v-divider vertical class="mx-2 hidden-md-and-down"></v-divider>

                        <!-- Updated Date -->
                        <span class="text-caption text-grey hidden-sm-and-down" style="white-space: nowrap;">
                            {{ new Date(documentStore.currentDocument.updatedAt).toLocaleDateString() }}
                        </span>
                    </div>
                    
                    <v-spacer></v-spacer>

                    <v-btn
                        icon
                        @click="downloadMarkdown"
                        class="mr-2"
                        title="Download Markdown"
                    >
                        <v-icon>mdi-download</v-icon>
                    </v-btn>
                    
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
                <div class="d-flex flex-grow-1 flex-column">
                    <div class="d-flex flex-grow-1">
                        <div 
                            class="pa-4 flex-grow-1 markdown-body"
                            v-html="compiledViewMarkdown"
                            style="min-height: 500px;"
                            :data-theme="markdownTheme"
                            :key="markdownTheme"
                        ></div>
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

const downloadMarkdown = () => {
    const doc = documentStore.currentDocument;
    if (!doc) return;

    const blob = new Blob([doc.content], { type: 'text/markdown' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${doc.title || 'document'}.md`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
};

const newComment = ref('');

const compiledViewMarkdown = computed(() => {
    return marked(documentStore.currentDocument?.content || '');
});

// Watch for changes and render mermaid
watch([compiledViewMarkdown, markdownTheme], async () => {
    // 1. Initialize Mermaid with correct theme
    mermaid.initialize({ 
        startOnLoad: false,
        theme: markdownTheme.value === 'dark' ? 'dark' : 'default',
        securityLevel: 'loose'
    });

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
