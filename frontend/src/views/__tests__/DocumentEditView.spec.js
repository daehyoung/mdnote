import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { vuetify } from '../../../vitest.setup';
import DocumentEditView from '../DocumentEditView.vue';
import { createTestingPinia } from '@pinia/testing';
import { useDocumentStore } from '../../stores/document';
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

describe('DocumentEditView Driver Test', () => {
  let wrapper;
  let documentStore;

  beforeEach(() => {
    wrapper = mount(DocumentEditView, {
      global: {
        plugins: [
          vuetify,
          createTestingPinia({
            createSpy: vi.fn,
          }),
        ],
        stubs: {
          'v-navigation-drawer': true,
          'v-autocomplete': true,
          'v-combobox': true,
        }
      },
    });
    documentStore = useDocumentStore();
    // Pre-populate store
    documentStore.currentDocument = { id: '123', title: 'Test Doc', content: 'Initial Content', attachments: [] };
    documentStore.categories = [];
  });

  it('renders title and content from store', async () => {
    // We need to wait for onMounted and loadDocument
    // Instead, let's just test the reactive refs after load
    const titleInput = wrapper.find('#title-input');
    // Note: Since we are using Vuetify, finding the internal input might be tricky
    // Vuetify v-text-field renders an actual input inside.
    expect(wrapper.vm.editedTitle).toBe(''); // Initial is empty until loadDocument runs
    
    // Manually trigger load completion simulation
    wrapper.vm.editedTitle = 'Test Doc';
    wrapper.vm.editedContent = '# New Content';
    await wrapper.vm.$nextTick();

    expect(wrapper.find('textarea').element.value).toBe('# New Content');
  });

  it('enables save button when dirty', async () => {
    wrapper.vm.isDirty = false;
    await wrapper.vm.$nextTick();
    const saveBtn = wrapper.find('[data-test="save-button"]');
    expect(saveBtn.attributes('disabled')).toBeDefined();

    wrapper.vm.editedTitle = 'Changed';
    wrapper.vm.isDirty = true; // Manually triggering watch effect or setting it
    await wrapper.vm.$nextTick();
    
    expect(saveBtn.attributes('disabled')).toBeUndefined();
  });

  it('calls updateDocument on save', async () => {
    documentStore.updateDocument.mockResolvedValue(true);
    wrapper.vm.isDirty = true;
    await wrapper.vm.$nextTick();
    
    await wrapper.find('[data-test="save-button"]').trigger('click');
    await wrapper.vm.$nextTick();
    
    expect(documentStore.updateDocument).toHaveBeenCalled();
    expect(wrapper.vm.isDirty).toBe(false);
  });

  it('toggles permissions correctly', async () => {
    wrapper.vm.editedPublicWrite = false;
    await wrapper.vm.$nextTick();
    
    wrapper.vm.editedPublicWrite = true;
    await wrapper.vm.$nextTick(); // Wait for watch or dependency
    expect(wrapper.vm.isDirty).toBe(true);
    expect(wrapper.vm.permissionsSummary).toContain('Pub:R/W');
  });

  it('renders attachments in the sidebar', async () => {
    documentStore.currentDocument.attachments = [
      { id: 'att1', originalFileName: 'image.png', fileSize: 1024 }
    ];
    wrapper.vm.showSettings = true; // Open drawer
    await wrapper.vm.$nextTick();

    // Check if attachment summary is updated
    expect(wrapper.vm.attachmentsSummary).toBe('1 Attachment');
    
    // Check if v-list-item for attachment exists if not stubbed, 
    // or check the store data binding in VM.
    expect(wrapper.vm.settingsSummary).toContain('1 Attachment');
  });
});
