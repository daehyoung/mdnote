<template>
  <v-container>
    <div class="d-flex align-center mb-4">
        <h1 class="text-h4">Category Management</h1>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="openDialog(null)">Add Root Category</v-btn>
    </div>

    <v-card class="pa-4">
        <v-list>
            <RecursiveCategoryList 
                v-for="cat in docStore.categories" 
                :key="cat.id" 
                :item="cat"
                @edit="openDialog"
                @remove="confirmDelete"
                @add-child="openDialog"
            />
        </v-list>
    </v-card>

    <!-- Dialog -->
    <v-dialog v-model="dialog" max-width="500px">
        <v-card>
            <v-card-title>{{ isEdit ? 'Edit Category' : 'New Category' }}</v-card-title>
            <v-card-text>
                <v-text-field v-model="editedItem.name" label="Name"></v-text-field>
                <p v-if="editedItem.parentId" class="text-caption">Parent ID: {{ editedItem.parentId }}</p>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn color="blue darken-1" text @click="closeDialog">Cancel</v-btn>
                <v-btn color="blue darken-1" text @click="save">Save</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useDocumentStore } from '../stores/document';
import RecursiveCategoryList from '../components/RecursiveCategoryList.vue';

const docStore = useDocumentStore();
const dialog = ref(false);
const isEdit = ref(false);
const editedItem = ref({ id: null, name: '', parentId: null });

onMounted(() => {
    docStore.fetchCategories();
});

const openDialog = (item, parentId = null) => {
    if (item && !parentId) {
        // Edit existing
        isEdit.value = true;
        editedItem.value = { ...item };
    } else {
        // Create new
        isEdit.value = false;
        // If adding child, parentId is passed. If root, parentId is null.
        editedItem.value = { id: null, name: '', parentId: parentId || (item ? item.id : null) };
    }
    dialog.value = true;
};

const closeDialog = () => {
    dialog.value = false;
    editedItem.value = { id: null, name: '', parentId: null };
};

const save = async () => {
    if (isEdit.value) {
        await docStore.updateCategory(editedItem.value.id, editedItem.value.name);
    } else {
        await docStore.createCategory(editedItem.value.name, editedItem.value.parentId);
    }
    closeDialog();
};

const confirmDelete = async (item) => {
    if (confirm(`Are you sure you want to delete ${item.name}?`)) {
        await docStore.deleteCategory(item.id);
    }
};
</script>
