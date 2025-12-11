<template>
  <v-list-group v-if="item.children && item.children.length" :value="item.id">
    <template v-slot:activator="{ props }">
      <v-list-item
        v-bind="props"
         prepend-icon="mdi-folder-open-outline"
        :title="item.name"
      >
        <template v-slot:append>
             <v-btn icon size="x-small" @click.stop="$emit('add-child', null, item.id)" title="Add Child">
                <v-icon>mdi-plus</v-icon>
            </v-btn>
            <v-btn icon size="x-small" @click.stop="$emit('edit', item)" title="Edit" class="ml-1">
                <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn icon size="x-small" @click.stop="$emit('remove', item)" title="Delete" class="ml-1">
                <v-icon>mdi-delete</v-icon>
            </v-btn>
        </template>
      </v-list-item>
    </template>

    <div class="pl-4">
        <RecursiveCategoryList 
            v-for="child in item.children" 
            :key="child.id" 
            :item="child" 
            @edit="(i) => $emit('edit', i)"
            @remove="(i) => $emit('remove', i)"
            @add-child="(i, pid) => $emit('add-child', i, pid)"
        />
    </div>
  </v-list-group>

  <v-list-item
    v-else
    prepend-icon="mdi-folder-outline"
    :title="item.name"
    :value="item.id"
  >
        <template v-slot:append>
             <v-btn icon size="x-small" @click.stop="$emit('add-child', null, item.id)" title="Add Child">
                <v-icon>mdi-plus</v-icon>
            </v-btn>
            <v-btn icon size="x-small" @click.stop="$emit('edit', item)" title="Edit" class="ml-1">
                <v-icon>mdi-pencil</v-icon>
            </v-btn>
             <v-btn icon size="x-small" @click.stop="$emit('remove', item)" title="Delete" class="ml-1">
                <v-icon>mdi-delete</v-icon>
            </v-btn>
        </template>
  </v-list-item>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['edit', 'remove', 'add-child']);
</script>

<script>
export default {
    name: 'RecursiveCategoryList'
}
</script>
