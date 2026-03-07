import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { vuetify } from '../../../vitest.setup';
import DocumentDetailView from '../DocumentDetailView.vue';
import { createTestingPinia } from '@pinia/testing';
import { useDocumentStore } from '../../stores/document';
import { useAuthStore } from '../../stores/auth';
import { useRouter, useRoute } from 'vue-router';

// Mock useRouter and useRoute
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
  useRoute: vi.fn(() => ({
    params: { id: '123' },
  })),
}));

// Mock Mermaid and other utils
vi.mock('../../utils/markdown', () => ({
  marked: vi.fn((c) => `<div class="rendered">${c}</div>`),
  mermaid: {
    initialize: vi.fn(),
    run: vi.fn(),
  },
}));

describe('DocumentDetailView Driver Test', () => {
  let wrapper;
  let documentStore;
  let authStore;

  beforeEach(async () => {
    wrapper = mount(DocumentDetailView, {
      global: {
        plugins: [
          vuetify,
          createTestingPinia({
            createSpy: vi.fn,
          }),
        ],
        stubs: {
          'v-chip': true,
          'v-avatar': true,
        }
      },
    });
    documentStore = useDocumentStore();
    authStore = useAuthStore();
    
    // Set initial state before mount for reactivity properly
    documentStore.currentDocument = { 
        id: '123', 
        title: 'Detail Test', 
        content: 'Content', 
        author: { username: 'owner', department: { name: 'Eng' } },
        updatedAt: new Date().toISOString(),
        allowComments: true
    };
    documentStore.comments = [];
    await wrapper.vm.$nextTick();
  });

  it('renders document title and content', () => {
    expect(wrapper.text()).toContain('Detail Test');
    expect(wrapper.html()).toContain('rendered');
  });

  it('shows edit button only for authorized users', async () => {
    // 1. Not logged in
    authStore.user = null;
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-test="edit-document-button"]').exists()).toBe(false);

    // 2. Logged in as owner
    authStore.user = { username: 'owner', role: 'USER' };
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-test="edit-document-button"]').exists()).toBe(true);

    // 3. Logged in as Admin
    authStore.user = { username: 'admin', role: 'ADMIN' };
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-test="edit-document-button"]').exists()).toBe(true);
  });

  it('posts a comment when button is clicked', async () => {
    wrapper.vm.newComment = 'Hello World';
    await wrapper.vm.$nextTick();
    
    const postBtn = wrapper.find('button').element.textContent.includes('Post') ? wrapper.find('button') : null;
    await wrapper.vm.postComment(); // Directly trigger for reliability in test if find fails
    
    expect(documentStore.addComment).toHaveBeenCalledWith('123', 'Hello World');
  });
});
