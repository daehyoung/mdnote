import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { vuetify } from '../../../vitest.setup';
import LoginView from '../LoginView.vue';
import { createTestingPinia } from '@pinia/testing';
import { useAuthStore } from '../../stores/auth';
import { useRouter } from 'vue-router';

const pushSpy = vi.fn();
// Mock useRouter
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: pushSpy,
  })),
}));

describe('LoginView Driver Test', () => {
  let wrapper;
  let authStore;
  let router;

  beforeEach(() => {
    wrapper = mount(LoginView, {
      global: {
        plugins: [
          vuetify,
          createTestingPinia({
            createSpy: vi.fn,
          }),
        ],
      },
    });
    authStore = useAuthStore();
    router = useRouter();
  });

  it('renders login form correctly', () => {
    expect(wrapper.find('input[name="username"]').exists()).toBe(true);
    expect(wrapper.find('input[name="password"]').exists()).toBe(true);
    expect(wrapper.find('.v-btn').text()).toContain('Login');
  });

  it('calls authStore.login and redirects on success', async () => {
    authStore.login.mockResolvedValue(true);
    
    wrapper.vm.username = 'testuser';
    wrapper.vm.password = 'password';
    await wrapper.vm.handleLogin();
    
    expect(authStore.login).toHaveBeenCalledWith('testuser', 'password');
    expect(pushSpy).toHaveBeenCalledWith('/');
  });

  it('shows alert on login failure', async () => {
    authStore.login.mockResolvedValue(false);
    window.alert = vi.fn();
    
    wrapper.vm.username = 'wrong';
    wrapper.vm.password = 'wrong';
    await wrapper.vm.handleLogin();

    expect(window.alert).toHaveBeenCalledWith('Authentication failed');
  });
});
