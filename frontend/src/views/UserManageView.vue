<template>
  <v-container>
    <div class="d-flex align-center mb-4">
        <h1 class="text-h4">User Management</h1>
        <v-spacer></v-spacer>
    </div>
    
    <v-card>
        <v-data-table
            :headers="headers"
            :items="users"
            :loading="loading"
        >
            <template v-slot:item.role="{ item }">
                <v-chip :color="item.role === 'ADMIN' ? 'red' : 'blue'">{{ item.role }}</v-chip>
            </template>
            <template v-slot:item.department="{ item }">
                {{ item.department ? item.department.name : '-' }}
            </template>
            <template v-slot:item.status="{ item }">
                <v-chip :color="item.status === 'ACTIVE' ? 'green' : 'grey'">{{ item.status }}</v-chip>
            </template>
            <template v-slot:item.actions="{ item }">
                <v-btn size="small" variant="text" @click="toggleStatus(item)">
                    {{ item.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }}
                </v-btn>
                <v-btn size="small" variant="text" @click="openAssignDialog(item)">
                    Assign Org
                </v-btn>
            </template>
        </v-data-table>
    </v-card>

    <!-- Assign Org Dialog -->
    <v-dialog v-model="assignDialog" max-width="400">
        <v-card>
            <v-card-title>Assign Organization</v-card-title>
            <v-card-text>
                <v-select
                    v-model="selectedDeptId"
                    :items="flatDepartments"
                    item-title="name"
                    item-value="id"
                    label="Organization"
                    clearable
                ></v-select>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn text @click="assignDialog = false">Cancel</v-btn>
                <v-btn color="primary" @click="saveAssignment">Save</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '../services/api';
import { useOrganizationStore } from '../stores/organization';

const orgStore = useOrganizationStore();
const users = ref([]);
const loading = ref(false);
const assignDialog = ref(false);
const selectedUser = ref(null);
const selectedDeptId = ref(null);

const headers = [
    { title: 'ID', key: 'id' },
    { title: 'Username', key: 'username' },
    { title: 'Name', key: 'name' },
    { title: 'Role', key: 'role' },
    { title: 'Organization', key: 'department' },
    { title: 'Status', key: 'status' },
    { title: 'Actions', key: 'actions', sortable: false },
];

const fetchUsers = async () => {
    loading.value = true;
    try {
        const response = await api.get('/admin/users');
        users.value = response.data;
    } catch (e) {
        console.error("Failed to fetch users", e);
    } finally {
        loading.value = false;
    }
};

const toggleStatus = async (user) => {
    const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
        await api.put(`/admin/users/${user.id}/status`, newStatus, {
            headers: { 'Content-Type': 'text/plain' }
        });
        user.status = newStatus;
    } catch (e) {
        console.error("Failed to update status", e);
    }
};

const openAssignDialog = (user) => {
    selectedUser.value = user;
    selectedDeptId.value = user.department ? user.department.id : null;
    orgStore.fetchDepartments(); 
    assignDialog.value = true;
};

const saveAssignment = async () => {
    if (!selectedUser.value) return;
    try {
        await orgStore.assignUser(selectedUser.value.id, selectedDeptId.value);
        await fetchUsers(); // Refresh list
        assignDialog.value = false;
    } catch (e) {
        alert("Failed to assign organization");
    }
};

const flattenDepts = (depts) => {
    let flat = [];
    depts.forEach(d => {
        flat.push(d);
        if (d.children && d.children.length) {
            flat = flat.concat(flattenDepts(d.children));
        }
    });
    return flat;
};

const flatDepartments = computed(() => {
    return flattenDepts(orgStore.departments);
});

onMounted(() => {
    fetchUsers();
});
</script>
