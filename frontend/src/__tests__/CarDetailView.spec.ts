import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'

import CarDetailView from '../views/CarDetailView.vue'

describe('CarDetailView', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('loads and displays an approved car by route id', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({
          id: 7,
          make: 'Nissan',
          model: '370Z',
          location: 'Auckland, New Zealand',
          imageUrl: 'https://example.com/370z.jpg',
          reviewStatus: 'APPROVED',
        }),
      }),
    )

    const wrapper = mount(CarDetailView, {
      props: { id: '7' },
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()

    expect(fetch).toHaveBeenCalledWith('/api/cars/7')
    expect(wrapper.text()).toContain('Nissan 370Z')
    expect(wrapper.text()).toContain('Auckland, New Zealand')
    expect(wrapper.get('img').attributes('src')).toBe('https://example.com/370z.jpg')
  })

  it('shows an unavailable state when the backend returns 404', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 404,
      }),
    )

    const wrapper = mount(CarDetailView, {
      props: { id: '99' },
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('Nothing to display here.')
    expect(wrapper.text()).toContain('This car is not available in the public garage.')
  })
})
