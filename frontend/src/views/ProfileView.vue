<template>
  <v-container>
    <h1 class="text-h4 mb-4">User Profile</h1>
    <v-card class="pa-4 mb-4" :loading="loading">
        <v-card-title>Personal Information</v-card-title>
        <v-card-text>
            <v-text-field v-model="profile.username" label="Username" readonly disabled></v-text-field>
            <v-text-field v-model="profile.name" label="Name"></v-text-field>
            <v-text-field v-model="profile.email" label="Email"></v-text-field>
            <v-text-field 
                :model-value="profile.department ? profile.department.name : '-'" 
                label="Organization" 
                readonly 
                disabled
            ></v-text-field>
            <v-text-field v-model="profile.role" label="Role" readonly disabled></v-text-field>
        </v-card-text>
        <v-card-actions>
            <v-btn color="primary" @click="updateInfo">Update Info</v-btn>
        </v-card-actions>
    </v-card>

    <v-card class="pa-4">
        <v-card-title>Change Password</v-card-title>
        <v-card-text>
            <v-text-field 
                v-model="newPassword" 
                label="New Password" 
                type="password"
            ></v-text-field>
        </v-card-text>
        <v-card-actions>
            <v-btn color="warning" @click="changePassword">Change Password</v-btn>
        </v-card-actions>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../services/api';

const loading = ref(false);
const profile = ref({
    username: '',
    name: '',
    email: '',
    role: '',
    department: null
});
const newPassword = ref('');

const fetchProfile = async () => {
    loading.value = true;
    try {
        const response = await api.get('/profile');
        profile.value = response.data;
    } catch (e) {
        console.error("Failed to fetch profile", e);
    } finally {
        loading.value = false;
    }
};

const updateInfo = async () => {
    try {
        await api.put('/profile', {
            name: profile.value.name,
            email: profile.value.email
        });
        alert("Profile updated");
    } catch (e) {
        alert("Failed to update profile");
    }
};

const changePassword = async () => {
    if (!newPassword.value) return;
    try {
        await api.put('/profile/password', { password: newPassword.value });
        alert("Password changed");
        newPassword.value = '';
    } catch (e) {
        alert("Failed to change password");
    }
};

onMounted(() => {
    fetchProfile();
});
</script>
