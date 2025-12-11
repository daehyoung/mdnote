<template>
  <v-container>
    <div class="d-flex align-center mb-4">
        <h1 class="text-h4">User Management</h1>
        <v-spacer></v-spacer>
        <v-btn color="primary" prepend-icon="mdi-account-plus" @click="openCreateUserDialog">Add User</v-btn>
    </div>
    
    <v-card>
        <v-data-table
            :headers="headers"
            :items="users"
            :loading="loading"
        >
            <template v-slot:item.role="{ item }">
                <v-chip :color="item.role === 'ADMIN' ? 'red' : item.role === 'MANAGER' ? 'orange' : 'blue'">{{ item.role }}</v-chip>
            </template>
            <template v-slot:item.department="{ item }">
                {{ item.department ? item.department.name : '-' }}
            </template>
            <template v-slot:item.status="{ item }">
                <v-chip :color="item.status === 'ACTIVE' ? 'green' : 'grey'">{{ item.status }}</v-chip>
            </template>
            <template v-slot:item.actions="{ item }">
                 <v-menu>
                    <template v-slot:activator="{ props }">
                         <v-btn icon="mdi-dots-vertical" variant="text" size="small" v-bind="props"></v-btn>
                    </template>
                    <v-list>
                        <v-list-item @click="openEditUserDialog(item)">
                            <v-list-item-title>Edit User</v-list-item-title>
                        </v-list-item>
                         <v-list-item @click="openPasswordDialog(item)">
                            <v-list-item-title>Reset Password <v-icon size="small" color="red">mdi-lock-reset</v-icon></v-list-item-title>
                        </v-list-item>
                         <v-list-item @click="openAssignDialog(item)">
                            <v-list-item-title>Assign Organization</v-list-item-title>
                        </v-list-item>
                         <v-divider></v-divider>
                         <v-list-item @click="toggleStatus(item)">
                            <v-list-item-title :class="item.status === 'ACTIVE' ? 'text-red' : 'text-green'">
                                {{ item.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }}
                            </v-list-item-title>
                        </v-list-item>
                    </v-list>
                 </v-menu>
            </template>
        </v-data-table>
    </v-card>

    <!-- Create/Edit User Dialog -->
    <v-dialog v-model="userDialog" max-width="500">
        <v-card>
            <v-card-title>{{ dialogMode === 'create' ? 'Add User' : 'Edit User' }}</v-card-title>
            <v-card-text>
                <v-form @submit.prevent="saveUser">
                    <v-text-field
                        v-model="userForm.username"
                        label="Username"
                        :disabled="dialogMode === 'edit'"
                        required
                    ></v-text-field>
                    <v-text-field
                        v-model="userForm.name"
                        label="Full Name"
                    ></v-text-field>
                    <v-select
                        v-model="userForm.role"
                        :items="roles"
                        label="Role"
                        required
                    ></v-select>
                    <v-text-field
                        v-if="dialogMode === 'create'"
                        v-model="userForm.passwordHash"
                        label="Password"
                        type="password"
                        required
                    ></v-text-field>
                </v-form>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn text @click="userDialog = false">Cancel</v-btn>
                <v-btn color="primary" @click="saveUser">Save</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>

    <!-- Password Reset Dialog -->
    <v-dialog v-model="passwordDialog" max-width="400">
        <v-card>
            <v-card-title>Reset Password</v-card-title>
             <v-card-subtitle>For user: {{ selectedUser?.username }}</v-card-subtitle>
            <v-card-text>
                <v-text-field
                    v-model="passwordForm.newPassword"
                    label="New Password"
                    type="password"
                ></v-text-field>
            </v-card-text>
            <v-card-actions>
                <v-spacer></v-spacer>
                 <v-btn text @click="passwordDialog = false">Cancel</v-btn>
                 <v-btn color="error" @click="savePassword">Reset Password</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>

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
import { useAdminStore } from '../stores/admin';
import { useOrganizationStore } from '../stores/organization';

const adminStore = useAdminStore();
const orgStore = useOrganizationStore();

// Use store state
const users = computed(() => adminStore.users);
const loading = computed(() => adminStore.loading);

const assignDialog = ref(false);
const userDialog = ref(false);
const passwordDialog = ref(false);

const dialogMode = ref('create'); // 'create' or 'edit'

const selectedUser = ref(null);
const selectedDeptId = ref(null);

// Form Data for Create/Edit
const userForm = ref({
    username: '',
    name: '',
    role: 'USER',
    passwordHash: '', // Only for create
});

// Form Data for Password Reset
const passwordForm = ref({
    newPassword: ''
});

const headers = [
    { title: 'ID', key: 'id' },
    { title: 'Username', key: 'username' },
    { title: 'Name', key: 'name' },
    { title: 'Role', key: 'role' },
    { title: 'Organization', key: 'department' },
    { title: 'Status', key: 'status' },
    { title: 'Actions', key: 'actions', sortable: false },
];

const roles = ['USER', 'ADMIN', 'MANAGER'];

const fetchUsers = async () => {
    await adminStore.fetchUsers();
};

const toggleStatus = async (user) => {
    try {
        await adminStore.toggleUserStatus(user);
    } catch (e) {
        // Error handled in store, or show toast
    }
};

// --- Create / Edit User ---
const openCreateUserDialog = () => {
    dialogMode.value = 'create';
    userForm.value = { username: '', name: '', role: 'USER', passwordHash: '' };
    userDialog.value = true;
};

const openEditUserDialog = (user) => {
    dialogMode.value = 'edit';
    selectedUser.value = user;
    userForm.value = { 
        username: user.username, 
        name: user.name, 
        role: user.role, 
        passwordHash: '' // Not used in edit
    };
    userDialog.value = true;
};

const saveUser = async () => {
    try {
        if (dialogMode.value === 'create') {
            await adminStore.createUser(userForm.value);
        } else {
            await adminStore.updateUser(selectedUser.value.id, {
                name: userForm.value.name,
                role: userForm.value.role
            });
        }
        await fetchUsers();
        userDialog.value = false;
    } catch (e) {
        alert("Failed to save user");
    }
};

// --- Reset Password ---
const openPasswordDialog = (user) => {
    selectedUser.value = user;
    passwordForm.value.newPassword = '';
    passwordDialog.value = true;
};

const savePassword = async () => {
    if (!selectedUser.value || !passwordForm.value.newPassword) return;
    try {
        await adminStore.resetPassword(selectedUser.value.id, passwordForm.value.newPassword);
        alert("Password reset successful");
        passwordDialog.value = false;
    } catch (e) {
        alert("Failed to reset password");
    }
};


// --- Assign Org ---
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
