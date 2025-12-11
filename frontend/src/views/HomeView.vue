<template>
    <v-container fluid class="fill-height pa-0 ma-0" style="width: 100%; max-width: 100%;">
      <v-row no-gutters class="fill-height">
        <!-- Editor/Viewer Area -->
        <v-col cols="12" class="d-flex flex-column fill-height pa-0 ma-0">
            <!-- List View -->
            <div v-if="!documentStore.currentDocument" class="d-flex flex-column fill-height pa-4">
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
                    
                    <!-- Status Filter (Edit Mode Only) -->
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
                        v-if="appMode === 'EDIT'"
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
                            <v-list-item-title class="font-weight-bold">
                                {{ doc.title || 'Untitled' }} 
                                <span class="text-caption text-grey ml-2" v-if="doc.author">by {{ doc.author.name || doc.author.username }}</span>
                                <v-chip x-small v-if="appMode==='EDIT' && doc.status" class="ml-2" size="x-small" color="secondary" variant="outlined">{{ doc.status }}</v-chip>
                            </v-list-item-title>
                            <v-list-item-subtitle>
                                <span class="text-caption mr-2">{{ new Date(doc.createdAt).toLocaleDateString() }}</span>
                                <v-chip size="x-small" label class="mr-1">{{ doc.category ? doc.category.name : 'Uncategorized' }}</v-chip>
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

            <!-- Detail View -->
            <div v-else class="d-flex flex-column fill-height">
                 <!-- Back Button Header -->
                  <v-toolbar dense flat class="border-b">
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
                        <v-btn color="success" text @click="saveDocument" :disabled="!isDirty || !canEdit" v-if="canEdit" data-test="save-button">
                            Save
                        </v-btn>
                        <v-btn color="error" icon @click="deleteDocument" v-if="canDelete">
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
                <div v-if="appMode === 'EDIT'" class="d-flex align-center pa-1">
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
                                class="pa-2 flex-grow-1 text-high-emphasis bg-transparent"
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
                    <div v-if="appMode === 'EDIT'" class="pa-4">
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
                            
                            <v-expansion-panel value="permissions" title="Permissions">
                                <v-expansion-panel-text>
                                    <v-row no-gutters>
                                        <v-col cols="12">
                                            <div class="text-subtitle-2 mb-2">Group (Department)</div>
                                            <v-checkbox v-model="editedGroupRead" label="Read" density="compact" hide-details></v-checkbox>
                                            <v-checkbox v-model="editedGroupWrite" label="Write" density="compact" hide-detailsMessages="Write permission includes delete"></v-checkbox>
                                        </v-col>
                                        <v-col cols="12" class="mt-2">
                                            <v-divider></v-divider>
                                            <div class="text-subtitle-2 mt-2 mb-2">General Public</div>
                                            <v-checkbox v-model="editedPublicRead" label="Read" density="compact" hide-details></v-checkbox>
                                            <v-checkbox v-model="editedPublicWrite" label="Write" density="compact" hide-detailsMessages="Write permission includes delete"></v-checkbox>
                                        </v-col>
                                    </v-row>
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

const openSidebarGroups = ref(['attachments', 'comments', 'permissions']);
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
const searchQueryInput = ref('');
const filterStatus = ref(null);

// Permission Refs
const editedGroupRead = ref(true);
const editedGroupWrite = ref(false);
const editedPublicRead = ref(true);
const editedPublicWrite = ref(false);


// Permission Logic
const canEdit = computed(() => {
    if (!documentStore.currentDocument) return false;
    // New doc (no ID) is always editable by creator
    if (!documentStore.currentDocument.id) return true;
    
    const user = authStore.user;
    if (!user) return false;
    if (user.role === 'ADMIN') return true;
    
    const author = documentStore.currentDocument.author;
    // If no author set (legacy), maybe allow? Or deny? Sticking to stricter: deny.
    // Wait, createNewDocument sets author on backend. 
    // If viewing a doc, author should be there.
    if (!author) return false; 
    
    // Owner check
    if (author.username === user.username) return true;
    
    // Group Write
    if (documentStore.currentDocument.groupWrite && user.department && author.department && user.department.id === author.department.id) return true;
    
    // Public Write
    if (documentStore.currentDocument.publicWrite) return true;

    return false;
});

const canDelete = computed(() => {
    // Delete is now same as Edit/Write permission
    return canEdit.value;
});

// Pagination Model
// We sync with store
const currentPage = computed({
    get: () => documentStore.page,
    set: (val) => documentStore.setPage(val)
});

const handleSearch = () => {
    if (searchQueryInput.value) {
        // Use Store search or Router push to Search View?
        // Current impl uses router push to 'search' view/route?
        // Wait, "search" route wasn't in list view.
        // User asked: "검색도 단어 입력후 검색 버튼 누르면 검색하도록 변경해줘"
        // Previous logic: doSearch() pushed to router query 'q'.
        // Let's keep that pattern if there is a separate search view or if it stays on home.
        // If stays on home, we might need SEARCH endpoint support in pagination?
        // User asked for "All Categories ... Paging".
        // If searching, we usually use the /search endpoint.
        // Does /search endpoint support pagination? Not yet.
        // BUT, if I implement search into the main list API (like filter), it would be better.
        // However, I updated SearchController separately.
        // Let's assume for now, Search is a separate mode/view or uses the separate API.
        // I will route to 'search' view or use store.searchDocuments logic.
        // Let's stick to existing:
        documentStore.searchDocuments(searchQueryInput.value); // This calls /search/ which is list (not paged yet)
        // Ideally Search should be paginated too, but user didn't explicitly ask for Paged Search, just "Paging for List".
        // They asked "Search -> Button Trigger".
        // I will trigger the store search action.
    } else {
        // Clear search, reload list
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

// Removed visibleDocuments computed property as we now display documentStore.documents directly (server-side paged)
// Only client-side logic remaining: none. We rely on store state.

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
    
    // Permissions
    editedGroupRead.value = fullDoc.groupRead !== undefined ? fullDoc.groupRead : true;
    editedGroupWrite.value = fullDoc.groupWrite || false;
    editedPublicRead.value = fullDoc.publicRead !== undefined ? fullDoc.publicRead : true;
    editedPublicWrite.value = fullDoc.publicWrite || false;
    
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
            attachments: documentStore.currentDocument.attachments, // Send attachments list to link them
            // Permissions
            groupRead: editedGroupRead.value,
            groupWrite: editedGroupWrite.value,
            publicRead: editedPublicRead.value,
            publicWrite: editedPublicWrite.value
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
// Watch for changes to track dirty state
watch([editedTitle, editedContent, editedTags, editedStatus, editedCategoryId, editedGroupRead, editedGroupWrite, editedPublicRead, editedPublicWrite], () => {
    if (documentStore.currentDocument) {
        isDirty.value = true;
    }
});

onMounted(async () => {
    console.log('HomeView Mounted');
    await fetchDocuments();
    console.log('HomeView fetchDocuments called. Documents:', documentStore.documents.length);
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
