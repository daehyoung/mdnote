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
            
            <v-card-title class="px-0">Appearance</v-card-title>
             <v-btn-toggle
                v-model="currentTheme"
                mandatory
                color="primary"
                @update:model-value="saveTheme"
            >
                <v-btn value="light">
                    <v-icon start>mdi-white-balance-sunny</v-icon>
                    Light
                </v-btn>
                <v-btn value="dark">
                    <v-icon start>mdi-weather-night</v-icon>
                    Dark
                </v-btn>
            </v-btn-toggle>
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
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const loading = ref(false);
const profile = ref({
    username: '',
    name: '',
    email: '',
    role: '',
    department: null
});
const newPassword = ref('');
const currentTheme = ref('light');

const fetchProfile = async () => {
    loading.value = true;
    try {
        const data = await authStore.fetchProfile();
        // Since store updates its own state, we could use that, 
        // but keeping local ref synced for form editing is fine too.
        profile.value = data;
        if (data.theme) {
            currentTheme.value = data.theme;
        }
    } catch (e) {
        console.error("Failed to fetch profile", e);
    } finally {
        loading.value = false;
    }
};

const saveTheme = async (val) => {
    try {
        await authStore.updateTheme(val);
    } catch (e) {
        // Revert on error?
    }
};

const updateInfo = async () => {
    try {
        await authStore.updateProfile(profile.value.name, profile.value.email);
        alert("Profile updated");
    } catch (e) {
        alert("Failed to update profile");
    }
};

const changePassword = async () => {
    if (!newPassword.value) return;
    try {
        await authStore.changePassword(newPassword.value);
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
