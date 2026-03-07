import { vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';

// Proper Vuetify 3 setup for testing
const vuetify = createVuetify({
  components,
  directives,
});

// Mocking useTheme for components that use it
vi.mock('vuetify', async () => {
  const actual = await vi.importActual('vuetify');
  return {
    ...actual,
    useTheme: vi.fn(() => ({
      global: {
        current: {
          value: { dark: false }
        }
      }
    })),
  };
});

// Mocking Pinia
const pinia = createPinia();
setActivePinia(pinia);

// Mocking some browser APIs
global.ResizeObserver = vi.fn().mockImplementation(() => ({
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
}));

// Mocking Mermaid
vi.mock('mermaid', () => ({
  default: {
    initialize: vi.fn(),
    run: vi.fn(),
  },
  mermaid: {
    initialize: vi.fn(),
    run: vi.fn(),
  }
}));

export { vuetify, pinia };
