import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'

import PublicUserView from '../views/PublicUserView.vue'

describe('PublicUserView', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('loads an owner and displays the public profile', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({
          id: 3,
          displayName: 'Garage Owner',
          cars: [
            {
              id: 7,
              make: 'Nissan',
              model: '370Z',
              location: 'Auckland, New Zealand',
              imageUrl: 'https://example.com/370z.jpg',
            },
          ],
        }),
      }),
    )

    const wrapper = mount(PublicUserView, {
      props: { id: '3' },
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()

    expect(fetch).toHaveBeenCalledWith('/api/users/3')
    expect(wrapper.text()).toContain('Garage Owner')
    expect(wrapper.text()).toContain('1 approved car')
    expect(wrapper.text()).toContain('Nissan 370Z')
  })

  it('shows an unavailable state when the owner does not exist', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 404,
      }),
    )

    const wrapper = mount(PublicUserView, {
      props: { id: '99' },
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('Garage not found.')
    expect(wrapper.text()).toContain('This garage owner could not be found.')
  })
})
