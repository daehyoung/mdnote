<template>
  <v-app>
    <router-view />
  </v-app>
</template>

<script setup>
  import { useTheme } from 'vuetify';
  import { useAuthStore } from './stores/auth';
  import { watch } from 'vue';

  const theme = useTheme();
  const authStore = useAuthStore();

  // Watch for store changes
  watch(() => authStore.theme, (newTheme) => {
      theme.global.name.value = newTheme;
  }, { immediate: true });
</script>
