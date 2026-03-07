<template>
    <v-container fluid class="pa-0 ma-0" style="width: 100%; max-width: 100%;">
      <v-row no-gutters>
        <v-col cols="12" class="d-flex flex-column pa-0 ma-0">
            <div class="d-flex flex-column">
                 <!-- Header -->
                  <v-toolbar density="compact" flat class="border-b" style="position: sticky; top: 0; z-index: 100;">
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
                        data-test="title-input"
                    ></v-text-field>
                    
                    <v-spacer></v-spacer>
                    
                    <v-btn color="success" text @click="saveDocument" :disabled="!isDirty" class="mr-2" data-test="save-button">
                        Save
                    </v-btn>
                    <v-btn color="error" icon @click="deleteDocument" v-if="canDelete" data-test="delete-button">
                        <v-icon>mdi-delete</v-icon>
                    </v-btn>
                    <input type="file" ref="fileInput" style="display: none" @change="handleFileUpload" id="file-input" />
                    <v-btn icon @click="triggerFileUpload" title="Upload Attachment">
                        <v-icon>mdi-paperclip</v-icon>
                    </v-btn>
                    <v-btn @click="toggleViewMode" class="ml-2">
                        {{ viewModeLabel }}
                    </v-btn>
                </v-toolbar>

                <!-- EDIT MODE Metadata -->
                <div class="d-flex align-center py-1 px-2 border-b">
                    <v-autocomplete
                        v-model="editedCategoryId"
                        :items="flattenedCategories"
                        item-title="displayName"
                        item-value="id"
                        label="Category"
                        density="compact"
                        hide-details
                        class="mr-2"
                        style="flex: 2; min-width: 150px;"
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
                        class="mr-2"
                        style="flex: 3; min-width: 200px;"
                    ></v-combobox>

                    <v-select
                        v-model="editedStatus"
                        :items="['DRAFT', 'REVIEW', 'APPROVED', 'PUBLISHED', 'ARCHIVED']"
                        label="Status"
                        density="compact"
                        hide-details
                        style="flex: 1; min-width: 120px;"
                        id="status-selector"
                        class="mr-2"
                    ></v-select>

                    <!-- Settings Toggle Summary -->
                    <v-text-field
                        :model-value="settingsSummary"
                        label="Info"
                        readonly
                        density="compact"
                        hide-details
                        variant="filled"
                        :append-inner-icon="showSettings ? 'mdi-chevron-right' : 'mdi-chevron-left'"
                        @click="showSettings = !showSettings"
                        class="cursor-pointer"
                        style="flex: 1; min-width: 180px; max-width: 300px;" 
                    ></v-text-field>
                </div>

                <!-- Editor/Preview Area -->
                <div class="d-flex flex-grow-1">
                    <!-- Editor -->
                    <div class="d-flex flex-column" v-if="viewMode === 'EDIT' || viewMode === 'SPLIT'" :class="viewMode === 'SPLIT' ? 'w-50 border-r' : 'w-100'">
                        <div class="flex-grow-1" style="min-height: 500px;">
                            <textarea 
                                v-model="editedContent" 
                                class="pa-4" 
                                style="width: 100%; height: 100%; min-height: 500px; resize: none; border: none; outline: none; font-family: monospace; font-size: 14px; background-color: transparent; color: inherit;"
                                placeholder="# Write markdown here..."
                                @input="handleInput"
                            ></textarea>
                        </div>
                    </div>
                        <!-- Preview -->
                        <div 
                            v-if="viewMode === 'PREVIEW' || viewMode === 'SPLIT'" 
                            class="pa-4 flex-grow-1 markdown-body"
                            :class="viewMode === 'SPLIT' ? 'w-50' : 'w-100'"
                            v-html="compiledMarkdown"
                            style="min-height: 500px;"
                            :data-theme="markdownTheme"
                            :key="markdownTheme"
                        ></div>
                    
    
                <!-- Sidebar Drawer -->
                <v-navigation-drawer
                    v-model="showSettings"
                    location="right"
                    width="350"
                    disable-resize-watcher
                    border="s"
                >
                    <div class="pa-2">
                        <div class="d-flex align-center justify-space-between mb-2">
                            <div class="text-subtitle-1 font-weight-bold">Settings</div>
                            <v-btn icon="mdi-close" variant="text" size="x-small" @click="showSettings = false"></v-btn>
                        </div>
                        <v-expansion-panels multiple v-model="openSidebarGroups" density="compact" variant="accordion">
                            <v-expansion-panel value="attachments" title="Attachments">
                                 <v-expansion-panel-text class="pa-0">
                                    <v-list density="compact" class="bg-transparent pa-0">
                                        <v-list-item v-for="att in documentStore.currentDocument?.attachments" :key="att.id" :title="att.originalFileName || att.fileName" class="min-h-0 py-0">
                                            <template v-slot:subtitle>
                                                <span class="text-caption">{{ (att.fileSize / 1024).toFixed(2) }} KB</span>
                                            </template>
                                            <template v-slot:append>
                                                <v-btn icon size="x-small" variant="text" :href="`/api/attachments/${att.id}`" target="_blank" class="mr-1">
                                                    <v-icon size="small">mdi-download</v-icon>
                                                </v-btn>
                                                <v-btn icon size="x-small" variant="text" color="error" @click="removeAttachment(att.id)">
                                                    <v-icon size="small">mdi-delete</v-icon>
                                                </v-btn>
                                            </template>
                                        </v-list-item>
                                        <v-list-item v-if="!documentStore.currentDocument?.attachments?.length" density="compact" class="min-h-0 py-1">
                                            <v-list-item-subtitle class="text-caption">No attachments</v-list-item-subtitle>
                                        </v-list-item>
                                    </v-list>
                                 </v-expansion-panel-text>
                            </v-expansion-panel>
                            
                            <v-expansion-panel value="permissions" title="Permissions">
                                <v-expansion-panel-text class="pa-0">
                                    <v-row no-gutters class="pa-0">
                                        <v-col cols="12" class="mb-1">
                                            <div class="text-caption font-weight-bold mb-0">Group (Department)</div>
                                            <v-checkbox v-model="editedGroupRead" label="Read" density="compact" hide-details class="ma-0 pa-0" style="min-height: 24px;"></v-checkbox>
                                            <v-checkbox v-model="editedGroupWrite" label="Write" density="compact" hide-detailsMessages="Write permission includes delete" class="ma-0 pa-0" style="min-height: 24px;"></v-checkbox>
                                        </v-col>
                                        <v-col cols="12" class="mt-1 mb-1">
                                            <v-divider class="my-1"></v-divider>
                                            <div class="text-caption font-weight-bold mb-0">General Public</div>
                                            <v-checkbox v-model="editedPublicRead" label="Read" density="compact" hide-details class="ma-0 pa-0" style="min-height: 24px;"></v-checkbox>
                                            <v-checkbox v-model="editedPublicWrite" label="Write" density="compact" hide-detailsMessages="Write permission includes delete" class="ma-0 pa-0" style="min-height: 24px;"></v-checkbox>
                                        </v-col>
                                        <v-col cols="12" class="mt-1">
                                            <v-divider class="my-1"></v-divider>
                                            <div class="text-caption font-weight-bold mb-0">Settings</div>
                                            <v-switch v-model="editedAllowComments" label="Allow Comments" density="compact" hide-details color="primary" class="ma-0 pa-0" style="min-height: 24px;"></v-switch>
                                        </v-col>
                                    </v-row>
                                </v-expansion-panel-text>

                            </v-expansion-panel>
                        </v-expansion-panels>
                    </div>
                </v-navigation-drawer>
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
const viewMode = ref('EDIT'); // 'EDIT', 'SPLIT', 'PREVIEW'
const showSettings = ref(false);

const permissionsSummary = computed(() => {
    const parts = [];
    if (editedPublicRead.value) parts.push('Pub:R' + (editedPublicWrite.value ? '/W' : ''));
    if (editedGroupRead.value) parts.push('Grp:R' + (editedGroupWrite.value ? '/W' : ''));
    return parts.length ? parts.join(', ') : 'Private';
});

const attachmentsSummary = computed(() => {
    const count = documentStore.currentDocument?.attachments?.length || 0;
    return count === 0 ? 'No Attachments' : `${count} Attachment${count > 1 ? 's' : ''}`;
});

const settingsSummary = computed(() => {
    return `Perms: ${permissionsSummary.value} | ${attachmentsSummary.value}`;
});

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

const viewModeLabel = computed(() => {
    switch (viewMode.value) {
        case 'EDIT': return 'Split View';
        case 'SPLIT': return 'Preview Only';
        case 'PREVIEW': return 'Edit Only';
        default: return 'View';
    }
});

const toggleViewMode = () => {
    if (viewMode.value === 'EDIT') viewMode.value = 'SPLIT';
    else if (viewMode.value === 'SPLIT') viewMode.value = 'PREVIEW';
    else viewMode.value = 'EDIT';
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

watch(editedTags, (newTags) => {
    if (!newTags) return;
    const cleaned = newTags.map(t => typeof t === 'string' ? t.trim() : t).filter(t => t !== '');
    
    // Check if changes are needed to avoid infinite loop
    let needsUpdate = false;
    if (cleaned.length !== newTags.length) {
        needsUpdate = true;
    } else {
        for (let i = 0; i < cleaned.length; i++) {
            if (cleaned[i] !== newTags[i]) {
                needsUpdate = true;
                break;
            }
        }
    }

    if (needsUpdate) {
        nextTick(() => {
            editedTags.value = cleaned;
        });
    }
}, { deep: true });

// Watch for changes and render mermaid in preview
watch([compiledMarkdown, viewMode, markdownTheme], async () => {
    if (viewMode.value === 'PREVIEW' || viewMode.value === 'SPLIT') {
        // Initialize Mermaid with correct theme
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
