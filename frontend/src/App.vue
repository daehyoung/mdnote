<template>
  <v-app>
    <router-view />
    <v-snackbar
      v-model="snackbarStore.show"
      :color="snackbarStore.color"
      :timeout="snackbarStore.timeout"
      location="top"
    >
      {{ snackbarStore.message }}
      
      <template v-slot:actions>
        <v-btn
          color="white"
          variant="text"
          @click="snackbarStore.show = false"
        >
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>

<script setup>
  import { useTheme } from 'vuetify';
  import { useAuthStore } from './stores/auth';
  import { useSnackbarStore } from './stores/snackbar';
  import { watch } from 'vue';

  const theme = useTheme();
  const authStore = useAuthStore();
  const snackbarStore = useSnackbarStore();

  // Watch for store changes
  watch(() => authStore.theme, (newTheme) => {
      theme.global.name.value = newTheme;
  }, { immediate: true });
</script>
