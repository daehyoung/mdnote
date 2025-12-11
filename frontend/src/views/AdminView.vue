<template>
  <v-layout class="fill-height">
    <!-- Admin Sidebar -->
    <v-navigation-drawer permanent>
        <v-list>
            <v-list-item title="Admin Dashboard" subtitle="Management Console"></v-list-item>
        </v-list>
        
        <v-divider></v-divider>

        <v-list density="compact" nav>
            <v-list-item 
                prepend-icon="mdi-account-group" 
                title="User Management" 
                to="/admin" 
                exact
            ></v-list-item>
            
            <v-list-item 
                prepend-icon="mdi-domain" 
                title="Organization Management" 
                to="/admin/organization"
            ></v-list-item>
            
            <v-list-item 
                prepend-icon="mdi-shape" 
                title="Category Management" 
                to="/admin/categories"
            ></v-list-item>
        </v-list>

        <template v-slot:append>
             <div class="pa-2">
                <v-btn block color="secondary" to="/">
                    <v-icon start>mdi-arrow-left</v-icon>
                    Back to App
                </v-btn>
             </div>
        </template>
    </v-navigation-drawer>

    <!-- Main Admin Content -->
    <v-main>
        <router-view></router-view>
    </v-main>
  </v-layout>
</template>

<script setup>
// Admin Layout Logic if any
</script>
