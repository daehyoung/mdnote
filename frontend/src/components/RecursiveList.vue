<template>
  <v-list-group v-if="item.children && item.children.length" :value="item.id">
    <template v-slot:activator="{ props }">
      <v-list-item
        v-bind="props"
        prepend-icon="mdi-folder-open-outline"
        :title="item.name"
        @click="onSelect(item)"
        :active="selectedId === item.id"
        color="primary"
      ></v-list-item>
    </template>

    <div class="pl-4">
        <RecursiveList 
            v-for="child in item.children" 
            :key="child.id" 
            :item="child" 
            @select="onSelect"
            :selectedId="selectedId"
        />
    </div>
  </v-list-group>

  <v-list-item
    v-else
    prepend-icon="mdi-folder-outline"
    :title="item.name"
    @click="onSelect(item)"
    :value="item.id"
    :active="selectedId === item.id"
    color="primary"
  ></v-list-item>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  item: {
    type: Object,
    required: true,
  },
  selectedId: {
      type: Number,
      default: null
  }
});

const emit = defineEmits(['select']);

const onSelect = (item) => {
    emit('select', item);
};
</script>

<script>
// Necessary for recursive component in Vue 3 SFC? 
// <script setup> usually handles it if name is implied or registered manually.
// But recursive usage often needs explicit name or self-reference.
// Let's name it explicitly.
export default {
    name: 'RecursiveList'
}
</script>
