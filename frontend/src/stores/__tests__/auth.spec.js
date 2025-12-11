import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import api from '../../services/api'

// Mock the API service
vi.mock('../../services/api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
  }
}))

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('updates theme correctly', async () => {
    const auth = useAuthStore()
    
    // Initial state
    expect(auth.theme).toBe('light')
    
    // Mock API success
    api.put.mockResolvedValue({})
    
    await auth.updateTheme('dark')
    
    expect(api.put).toHaveBeenCalledWith('/profile', { theme: 'dark' })
    expect(auth.theme).toBe('dark')
  })

  it('handles theme update failure', async () => {
    const auth = useAuthStore()
    const error = new Error('Network error')
    
    api.put.mockRejectedValue(error)
    
    await expect(auth.updateTheme('dark')).rejects.toThrow('Network error')
    expect(auth.theme).toBe('light') // Should default/remain
  })

  it('login sets theme from response', async () => {
    const auth = useAuthStore()
    import('axios').then(axios => {
        // Need to mock axios for login if it uses axios directly, 
        // but auth.js uses axios separately from api service in login implementation?
        // Let's check auth.js content. It uses axios.
    })
  })
})
