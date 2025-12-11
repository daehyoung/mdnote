<template>
    <v-container fluid class="fill-height pa-0 ma-0" style="width: 100%; max-width: 100%;">
      <v-row no-gutters class="fill-height">
        <!-- Editor/Viewer Area -->
        <v-col cols="12" class="d-flex flex-column fill-height pa-0 ma-0">
            <!-- List View -->
            <div v-if="!documentStore.currentDocument" class="d-flex flex-column fill-height pa-4">
                 <div class="d-flex align-center mb-4">
                    <h2 class="text-h5 mr-4">Documents</h2>
                    <v-text-field
                        v-model="searchQuery"
                        dense
                        outlined
                        hide-details
                        prepend-inner-icon="mdi-magnify"
                        label="Search Documents..."
                        style="max-width: 400px;"
                        clearable
                        class="mr-4"
                        id="search-input"
                    ></v-text-field>
                    <v-btn 
                        v-if="appMode === 'EDIT'"
                        color="primary" 
                        @click="createNewDocument"
                    >
                        <v-icon left>mdi-plus</v-icon> New Document
                    </v-btn>
                 </div>
                 
                 <v-card variant="flat" class="flex-grow-1">
                    <v-list v-if="documentStore.loading && !documentStore.documents.length">
                         <v-list-item>Loading...</v-list-item>
                    </v-list>
                    <v-list v-else lines="two">
                        <v-list-item
                            v-for="doc in visibleDocuments"
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
                            <v-list-item-title class="font-weight-bold">{{ doc.title || 'Untitled' }}</v-list-item-title>
                            <v-list-item-subtitle>
                                <span class="text-caption mr-2">{{ new Date(doc.createdAt).toLocaleDateString() }}</span>
                                <v-chip size="x-small" label>{{ doc.category ? doc.category.name : 'Uncategorized' }}</v-chip>
                            </v-list-item-subtitle>
                        </v-list-item>
                        <v-list-item v-if="!visibleDocuments.length">
                            <v-list-item-title class="text-grey">No documents found.</v-list-item-title>
                        </v-list-item>
                    </v-list>
                 </v-card>
            </div>

            <!-- Detail View -->
            <div v-else class="d-flex flex-column fill-height">
                 <!-- Back Button Header -->
                  <v-toolbar dense flat color="grey-lighten-4" class="border-b">
                       <v-btn icon @click="goBack" data-test="back-button">
                         <v-icon>mdi-arrow-left</v-icon>
                     </v-btn>
                    <v-toolbar-title v-if="appMode === 'VIEW'">{{ documentStore.currentDocument.title }}</v-toolbar-title>
                    
                    <template v-if="appMode === 'EDIT'">
                         <v-text-field
                            v-model="editedTitle"
                            label="Title"
                            single-line
                            hide-details
                            class="ml-0 mr-4"
                            density="compact"
                            id="title-input"
                        ></v-text-field>
                        <v-spacer></v-spacer>
                        <v-btn color="success" text @click="saveDocument" :disabled="!isDirty" data-test="save-button">
                            Save
                        </v-btn>
                        <v-btn color="error" icon @click="deleteDocument">
                            <v-icon>mdi-delete</v-icon>
                        </v-btn>
                        <input type="file" ref="fileInput" style="display: none" @change="handleFileUpload" id="file-input" />
                        <v-btn icon @click="triggerFileUpload" data-test="upload-button">
                            <v-icon>mdi-paperclip</v-icon>
                        </v-btn>
                        <v-btn @click="toggleEditMode">
                            {{ isEditMode ? 'Preview' : 'Edit' }}
                        </v-btn>
                    </template>
                 </v-toolbar>

                <!-- EDIT MODE Metadata -->
                <div v-if="appMode === 'EDIT'" class="d-flex align-center pa-1 bg-grey-lighten-5">
                    <v-autocomplete
                        v-model="editedCategoryId"
                        :items="flattenedCategories"
                        item-title="displayName"
                        item-value="id"
                        label="Category"
                        density="compact"
                        hide-details
                        class="mr-4"
                        style="max-width: 200px;"
                        clearable
                        placeholder="Select Category"
                    ></v-autocomplete>

                    <v-combobox
                    v-model="editedTags"
                    label="Tags"
                    chips
                    multiple
                    density="compact"
                    hide-details
                    placeholder="Add tags..."
                    :items="[]" 
                    class="mr-4"
                    style="max-width: 400px;"
                    ></v-combobox>

                    <v-select
                    v-model="editedStatus"
                    :items="['DRAFT', 'REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED']"
                    label="Status"
                    density="compact"
                    hide-details
                    style="max-width: 150px;"
                    ></v-select>
                </div>

                <!-- Content Area -->
                <div class="d-flex flex-grow-1 flex-column" style="height: calc(100vh - 128px); overflow-y: auto;">
                    <!-- Editor/Preview Container -->
                    <div class="d-flex flex-grow-1">
                        <!-- Editor (Only in Edit Mode and isEditMode=true) -->
                        <div v-if="appMode === 'EDIT' && isEditMode" class="flex-grow-1 d-flex">
                            <textarea
                                v-model="editedContent"
                                class="pa-2 flex-grow-1"
                                style="width: 100%; min-height: 500px; border: none; outline: none; resize: none; font-family: monospace;"
                                placeholder="Type your markdown here..."
                                id="content-editor"
                            ></textarea>
                        </div>
                        
                        <!-- Preview (In View Mode OR Edit Mode Preview) -->
                        <div 
                            v-else 
                            class="pa-2 flex-grow-1 markdown-body"
                            v-html="appMode === 'EDIT' ? compiledMarkdown : compiledViewMarkdown"
                            style="min-height: 500px;"
                        ></div>
                    </div>
    
                    <!-- Bottom Section: Attachments and Comments (Only in Edit Mode?) 
                         User said "Edit mode... currently like this". 
                         "View mode... markdown view only". 
                         So I will hide this in VIEW mode unless requested. 
                         "Markdown View Mode" usually implies read-only.
                         But comments might be useful in View mode.
                         User said "Select Document -> Display Markdown View (Read-only) only".
                         "Edit Mode -> ... currently modification possible".
                         I'll hide attachments/comments in VIEW mode for strictly "View Only".
                    -->
                    <div v-if="appMode === 'EDIT'" class="pa-4 bg-grey-lighten-4">
                        <v-expansion-panels multiple v-model="openSidebarGroups">
                            <v-expansion-panel value="attachments" title="Attachments">
                                 <v-expansion-panel-text>
                                    <v-list dense class="bg-transparent">
                                        <v-list-item v-for="att in documentStore.currentDocument?.attachments" :key="att.id" :title="att.originalFileName || att.fileName">
                                            <template v-slot:subtitle>
                                                {{ (att.fileSize / 1024).toFixed(2) }} KB
                                            </template>
                                            <template v-slot:append>
                                                <v-btn icon size="small" variant="text" :href="`/api/attachments/${att.id}`" target="_blank" class="mr-1">
                                                    <v-icon>mdi-download</v-icon>
                                                </v-btn>
                                                <v-btn icon size="small" variant="text" color="error" @click="removeAttachment(att.id)">
                                                    <v-icon>mdi-delete</v-icon>
                                                </v-btn>
                                            </template>
                                        </v-list-item>
                                        <v-list-item v-if="!documentStore.currentDocument?.attachments?.length">
                                            <v-list-item-subtitle>No attachments</v-list-item-subtitle>
                                        </v-list-item>
                                    </v-list>
                                 </v-expansion-panel-text>
                            </v-expansion-panel>
                            
                            <v-expansion-panel value="comments" title="Comments">
                                <v-expansion-panel-text>
                                    <v-list dense class="bg-transparent">
                                        <v-list-item v-for="comment in documentStore.comments" :key="comment.id">
                                            <template v-slot:append>
                                                <v-btn icon size="x-small" variant="text" color="error" @click="removeComment(comment.id)">
                                                    <v-icon>mdi-delete</v-icon>
                                                </v-btn>
                                            </template>
                                            <v-list-item-subtitle class="text-caption">
                                                {{ comment.user ? comment.user.name : 'Unknown' }} - {{ new Date(comment.createdAt).toLocaleString() }}
                                            </v-list-item-subtitle>
                                            <v-list-item-title class="text-wrap">{{ comment.content }}</v-list-item-title>
                                        </v-list-item>
                                    </v-list>
                                    <div class="mt-2">
                                        <v-text-field
                                            v-model="newComment"
                                            label="Add a comment"
                                            append-inner-icon="mdi-send"
                                            @click:append-inner="postComment"
                                            @keyup.enter="postComment"
                                            hide-details
                                            density="compact"
                                            variant="outlined"
                                        ></v-text-field>
                                    </div>
                                </v-expansion-panel-text>
                            </v-expansion-panel>
                        </v-expansion-panels>
                    </div>
                </div>
            </div>
        </v-col>
      </v-row>
    </v-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useDocumentStore } from '../stores/document';
import { useRouter, useRoute } from 'vue-router';
import { marked } from 'marked';
import { uploadFile } from '../services/api';

const authStore = useAuthStore();
const documentStore = useDocumentStore();
const router = useRouter();
const route = useRoute();

const openSidebarGroups = ref(['attachments', 'comments']);
// Using store state for app mode
const appMode = computed(() => documentStore.appMode);

const isEditMode = ref(true);
const editedTitle = ref('');
const editedContent = ref('');
const editedTags = ref([]);
const editedCategoryId = ref(null);
const editedStatus = ref('DRAFT');
const isDirty = ref(false);
const fileInput = ref(null);
const newComment = ref('');
const searchQuery = ref('');
// selectedCategoryId is managed by Store/MainLayout now, 
// but we might need it locally if we wanted to highlight logic here?
// Actual filtering happens via fetchDocuments(catId) triggered by MainLayout.
// We just need to access the store list.

const doSearch = () => {
    if (searchQuery.value) {
        router.push({ name: 'search', query: { q: searchQuery.value } });
    }
};

// Computed property to filter the ALREADY fetched documents (which are by category or all) locally by search query
const visibleDocuments = computed(() => {
    let docs = documentStore.documents;
    if (searchQuery.value) {
        const q = searchQuery.value.toLowerCase();
        docs = docs.filter(d => 
            d.title.toLowerCase().includes(q) 
        );
    }
    return docs;
});

const compiledMarkdown = computed(() => {
  return marked(editedContent.value || '');
});

const compiledViewMarkdown = computed(() => {
    return marked(documentStore.currentDocument?.content || '');
});

// Logout moved to Layout

const fetchDocuments = async () => {
    // Initial fetch handled by layout? Or we should do it here to be safe on direct load?
    // MainLayout handles initial category fetch.
    // If HomeView is mounted, it should probably fetch current view docs.
    // Use store category ID if present.
    await documentStore.fetchDocuments(documentStore.selectedCategoryId);
    // Categories fetched by Layout
};

// filterByCategory moved to Layout

const selectDocument = (doc) => {
    router.push({ name: 'document-detail', params: { id: doc.id } });
};

const loadDocument = async (docId) => {
    if (!docId) {
        documentStore.currentDocument = null;
        return;
    }
    
    // Fetch full document details (including attachments)
    const fullDoc = await documentStore.fetchDocument(docId);
    if (!fullDoc) return; // Error handling
    
    // setCurrentDocument is handled by fetchDocument
    editedTitle.value = fullDoc.title;
    editedContent.value = fullDoc.content;
    editedTags.value = fullDoc.tags ? fullDoc.tags.map(t => t.name) : [];
    editedCategoryId.value = fullDoc.category ? fullDoc.category.id : null;
    editedStatus.value = fullDoc.status || 'DRAFT';
    
    isEditMode.value = false; // Default to preview mode on select
    if (appMode.value === 'EDIT') {
        isEditMode.value = true;
    }
    isDirty.value = false;
    await documentStore.fetchComments(fullDoc.id);
};

const createNewDocument = async () => {
    try {
        const newDoc = await documentStore.createDocument('Untitled', '', documentStore.selectedCategoryId);
        router.push({ name: 'document-detail', params: { id: newDoc.id } });
        isEditMode.value = true; // Switch to edit mode for new doc
    } catch (e) {
        console.error("Failed to create", e);
    }
};

const saveDocument = async () => {
    if (!documentStore.currentDocument) return;
    
    try {
        await documentStore.updateDocument(documentStore.currentDocument.id, {
            title: editedTitle.value,
            content: editedContent.value,
            // Preserve other fields if needed
            category: editedCategoryId.value ? { id: editedCategoryId.value } : null,
            status: editedStatus.value,
            tags: editedTags.value.map(name => ({ name })), // Send as objects
            attachments: documentStore.currentDocument.attachments // Send attachments list to link them
        });
        isDirty.value = false;
        // alert('Document saved successfully'); // Simple feedback
    } catch (e) {
        console.error("Failed to save", e);
    }
};

const deleteDocument = async () => {
    if (!documentStore.currentDocument) return;
    if (!confirm('Are you sure you want to delete this document?')) return;
    
    try {
        await documentStore.deleteDocument(documentStore.currentDocument.id);
        router.push({ name: 'home' });
    } catch (e) {
        console.error("Failed to delete", e);
    }
};

const toggleEditMode = () => {
    isEditMode.value = !isEditMode.value;
};

const triggerFileUpload = () => {
    fileInput.value.click();
};

const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
        const response = await uploadFile(file);
        const attachment = response.data;
        // Insert markdown image syntax
        // attachment.filePath is absolute URL in our simple case, but we need relative for reverse proxy if used differently
        // Current impl returns full URL from backend including host.
        // Let's use relative path if possible, or just use what backend gave. 
        // Backend gave: http://backend:8080/api/attachments/UUID_name
        // Frontend needs: /api/attachments/UUID_name
        
        // Let's handle this by just using the filename to construct relative path for now to be safe with proxy
        const relativePath = `/api/attachments/${attachment.fileName}`; // Wait, backend controller stores full URL?
        // Let's look at controller: ServletUriComponentsBuilder... includes scheme/host.
        // We might want to fix backend to return relative, or just parse here.
        // For now, let's just append the filename to /api/attachments/ 
        // Note: The backend logic for download uses "fileName" containing UUID? No, storeFile adds UUID.
        // The Controller returns the original filename in setFileName?? 
        // Wait, controller does: attachment.setFileName(file.getOriginalFilename());
        // But storeFile returns generated fileName (UUID+Original).
        // Controller download uses "fileName".
        // ERROR in controller logic potentially. 
        // storeFile returns the stored name (UUID+Original).
        // logic in uploadFile: 
        // String fileName = fileStorageService.storeFile(file); // This is UUID+Orig
        // path builder uses fileName.
        // Saved Attachment entity uses file.getOriginalFilename().
        // If we request /api/attachments/{originalName}, it won't find it if it was renamed.
        
        // I need to fix the Controller to save the STORED filename in the entity or return it.
        // Let's assume I fix the controller next.
        
        // For now stub frontend logic:
        const markdownImage = `\n![${file.name}](/api/attachments/${attachment.id})`;
        editedContent.value += markdownImage;
        // Update store logic to show attachment immediately
        documentStore.updateCurrentDocumentAttachments(attachment);
        
        isDirty.value = true;
    } catch (e) {
        console.error("Upload failed", e);
        alert("File upload failed");
    }
};

const flattenCats = (cats, level = 0) => {
    let flat = [];
    cats.forEach(c => {
        flat.push({ ...c, displayName: '-'.repeat(level) + ' ' + c.name });
        if (c.children && c.children.length) {
            flat = flat.concat(flattenCats(c.children, level + 1));
        }
    });
    return flat;
};

const flattenedCategories = computed(() => {
    return flattenCats(documentStore.categories);
});



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
        alert("Failed to delete comment");
    }
};

const removeAttachment = async (attId) => {
    if (!confirm('Delete attachment?')) return;
    try {
        await documentStore.deleteAttachment(attId);
    } catch (e) {
        alert("Failed to delete attachment");
    }
};

const goBack = () => {
    router.push({ name: 'home' });
};

// Watch for changes to track dirty state
watch([editedTitle, editedContent, editedTags, editedStatus, editedCategoryId], () => {
    if (documentStore.currentDocument) {
        isDirty.value = true;
    }
});

onMounted(() => {
    fetchDocuments();
    if (route.params.id) {
        loadDocument(route.params.id);
    }
});

watch(() => route.params.id, (newId) => {
    loadDocument(newId);
});
</script>

<style scoped>
.scrollable {
    overflow-y: auto;
}
.markdown-body {
    box-sizing: border-box;
    min-width: 200px;
    /* max-width: 980px; Removed for wider view */
    /* margin: 0 auto; */
    padding: 10px;
    width: 100%; /* Force full width */
}
</style>
