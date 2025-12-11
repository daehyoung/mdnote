<template>
    <v-container fluid class="fill-height pa-0 ma-0" style="width: 100%; max-width: 100%;">
      <v-row no-gutters class="fill-height">
        <v-col cols="12" class="d-flex flex-column fill-height pa-0 ma-0">
            <div class="d-flex flex-column fill-height">
                 <!-- Header -->
                  <v-toolbar dense flat class="border-b">
                       <v-btn icon @click="goBack" data-test="back-button">
                         <v-icon>mdi-arrow-left</v-icon>
                     </v-btn>
                    
                    <v-text-field
                        v-model="editedTitle"
                        label="Title"
                        single-line
                        hide-details
                        class="ml-0 mr-4"
                        density="compact"
                        style="max-width: 400px;" 
                        id="title-input"
                    ></v-text-field>
                    
                    <v-spacer></v-spacer>
                    
                    <v-btn color="success" text @click="saveDocument" :disabled="!isDirty" class="mr-2" data-test="save-button">
                        Save
                    </v-btn>
                    <v-btn color="error" icon @click="deleteDocument" v-if="canDelete">
                        <v-icon>mdi-delete</v-icon>
                    </v-btn>
                    <input type="file" ref="fileInput" style="display: none" @change="handleFileUpload" id="file-input" />
                    <v-btn icon @click="triggerFileUpload" title="Upload Attachment">
                        <v-icon>mdi-paperclip</v-icon>
                    </v-btn>
                    <v-btn @click="togglePreview" class="ml-2">
                        {{ showPreview ? 'Edit' : 'Preview' }}
                    </v-btn>
                 </v-toolbar>

                <!-- EDIT MODE Metadata -->
                <div class="d-flex align-center pa-1 border-b">
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
                        id="status-selector"
                    ></v-select>
                </div>

                <!-- Content Area -->
                <div class="d-flex flex-grow-1 flex-column" style="height: calc(100vh - 180px); overflow-y: auto;">
                    <div class="d-flex flex-grow-1">
                        <!-- Editor -->
                        <div v-show="!showPreview" class="flex-grow-1 d-flex">
                            <textarea
                                v-model="editedContent"
                                class="pa-4 flex-grow-1 text-high-emphasis bg-transparent"
                                style="width: 100%; min-height: 500px; border: none; outline: none; resize: none; font-family: monospace;"
                                placeholder="Type your markdown here..."
                                id="content-editor"
                            ></textarea>
                        </div>
                        
                        <!-- Preview -->
                        <div 
                            v-show="showPreview" 
                            class="pa-4 flex-grow-1 markdown-body"
                            v-html="compiledMarkdown"
                            style="min-height: 500px;"
                            :data-theme="markdownTheme"
                        ></div>
                    </div>
    
                    <!-- Metadata and Comments Panel -->
                    <div class="pa-4">
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
                                        <v-col cols="12" class="mt-2">
                                            <v-divider></v-divider>
                                            <div class="text-subtitle-2 mt-2 mb-2">Settings</div>
                                            <v-switch v-model="editedAllowComments" label="Allow Comments" density="compact" hide-details color="primary"></v-switch>
                                        </v-col>
                                    </v-row>
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
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useDocumentStore } from '../stores/document';
import { useRouter, useRoute } from 'vue-router';
import { uploadFile } from '../services/api';
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

const openSidebarGroups = ref(['attachments', 'comments', 'permissions']);
const showPreview = ref(false);

const editedTitle = ref('');
const editedContent = ref('');
const editedTags = ref([]);
const editedCategoryId = ref(null);
const editedStatus = ref('DRAFT');
const isDirty = ref(false);
const fileInput = ref(null);
const newComment = ref('');

// Permission Refs
const editedGroupRead = ref(true);
const editedGroupWrite = ref(false);
const editedPublicRead = ref(true);
const editedPublicWrite = ref(false);
const editedAllowComments = ref(true);

const compiledMarkdown = computed(() => {
  return marked(editedContent.value || '');
});

const canDelete = computed(() => {
    // Basic check here, the real check happens in permission guard and backend
    if (!documentStore.currentDocument) return false;
    // ... duplicate logic from DocumentDetailView or simplified since if we are here we should have write access?
    // Let's rely on the router/view guard verification mostly.
    return true; 
});

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

const loadDocument = async (docId) => {
    if (!docId) return;
    
    // Fetch full document details (including attachments)
    const fullDoc = await documentStore.fetchDocument(docId);
    if (!fullDoc) {
        // Maybe redirect if not found?
        return;
    }
    
    // Permission check for access to edit view
    // Note: We might want to do this in the router guard, but having it here acts as a double check.
    
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
    editedAllowComments.value = fullDoc.allowComments !== undefined ? fullDoc.allowComments : true;
    
    isDirty.value = false;
    await documentStore.fetchComments(fullDoc.id);
};

const saveDocument = async () => {
    if (!documentStore.currentDocument) return;
    
    try {
        await documentStore.updateDocument(documentStore.currentDocument.id, {
            title: editedTitle.value,
            content: editedContent.value,
            category: editedCategoryId.value ? { id: editedCategoryId.value } : null,
            status: editedStatus.value,
            tags: editedTags.value.map(name => ({ name })),
            attachments: documentStore.currentDocument.attachments,
            groupRead: editedGroupRead.value,
            groupWrite: editedGroupWrite.value,
            publicRead: editedPublicRead.value,
            publicWrite: editedPublicWrite.value,
            allowComments: editedAllowComments.value
        });
        isDirty.value = false;
        // Maybe toast notification?
    } catch (e) {
        console.error("Failed to save", e);
        alert("Failed to save changes");
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
        alert("Failed to delete document");
    }
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
        const markdownImage = `\n![${file.name}](/api/attachments/${attachment.id})`;
        editedContent.value += markdownImage;
        documentStore.updateCurrentDocumentAttachments(attachment);
        isDirty.value = true;
    } catch (e) {
        console.error("Upload failed", e);
        alert("File upload failed");
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

const togglePreview = () => {
    showPreview.value = !showPreview.value;
};

const goBack = () => {
    if (isDirty.value) {
        if (!confirm('You have unsaved changes. Leave anyway?')) return;
    }
    // Go back to details view if possible
    if (documentStore.currentDocument) {
         router.push({ name: 'document-detail', params: { id: documentStore.currentDocument.id } });
    } else {
         router.push({ name: 'home' });
    }
};

// Dirty tracking
watch([editedTitle, editedContent, editedTags, editedStatus, editedCategoryId, editedGroupRead, editedGroupWrite, editedPublicRead, editedPublicWrite, editedAllowComments], () => {
    if (documentStore.currentDocument) {
        isDirty.value = true;
    }
});

// Watch for changes and render mermaid in preview
watch([compiledMarkdown, showPreview], async () => {
    if (showPreview.value) {
        await nextTick();
        try {
            await mermaid.run({
                nodes: document.querySelectorAll('.mermaid')
            });
        } catch (e) {
            console.error('Mermaid render error:', e);
        }
    }
});

onMounted(() => {
    if (route.params.id) {
        loadDocument(route.params.id);
    }
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
