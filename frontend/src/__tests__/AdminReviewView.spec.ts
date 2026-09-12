import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils'

import AdminReviewView from '../views/AdminReviewView.vue'

function response(status: number, body: unknown = null): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as Response
}

afterEach(() => vi.unstubAllGlobals())

describe('AdminReviewView', () => {
  it('loads pending cars for an administrator', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        response(200, {
          id: 34,
          email: 'admin@example.com',
          displayName: 'Admin',
          role: 'ADMIN',
        }),
      )
      .mockResolvedValueOnce(
        response(200, [
          {
            id: 74,
            make: 'Nissan',
            model: '370Z',
            location: 'Auckland, New Zealand',
            imageUrl: '/owner-z.jpg',
            reviewStatus: 'PENDING',
          },
        ]),
      )

    vi.stubGlobal('fetch', fetchMock)

    const wrapper = mount(AdminReviewView, {
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/cars', {
      credentials: 'same-origin',
    })
    expect(wrapper.text()).toContain('Nissan 370Z')
    expect(wrapper.text()).toContain('Approve for public garage')
    expect(wrapper.text()).toContain('Reject submission')
  })

  it('approves a car and removes it from the queue', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        response(200, {
          id: 34,
          email: 'admin@example.com',
          displayName: 'Admin',
          role: 'ADMIN',
        }),
      )
      .mockResolvedValueOnce(
        response(200, [
          {
            id: 75,
            make: 'Mazda',
            model: 'RX-7',
            location: 'Hiroshima, Japan',
            imageUrl: null,
            reviewStatus: 'PENDING',
          },
        ]),
      )
      .mockResolvedValueOnce(
        response(200, {
          headerName: 'X-CSRF-TOKEN',
          token: 'fresh-token',
        }),
      )
      .mockResolvedValueOnce(
        response(200, {
          id: 75,
          make: 'Mazda',
          model: 'RX-7',
          location: 'Hiroshima, Japan',
          imageUrl: null,
          reviewStatus: 'APPROVED',
        }),
      )

    vi.stubGlobal('fetch', fetchMock)

    const wrapper = mount(AdminReviewView, {
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()
    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/cars/75/approve', {
      method: 'PATCH',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': 'fresh-token',
      },
    })
    expect(wrapper.text()).toContain('Mazda RX-7 is now public.')
    expect(wrapper.text()).toContain('The review queue is empty.')
  })

  it('blocks the page for a non-admin account', async () => {
    const fetchMock = vi.fn().mockResolvedValueOnce(
      response(200, {
        id: 33,
        email: 'owner@example.com',
        displayName: 'Owner',
        role: 'USER',
      }),
    )

    vi.stubGlobal('fetch', fetchMock)

    const wrapper = mount(AdminReviewView, {
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toContain(
      'does not have administrator access',
    )
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('rejects a car and removes it from the queue', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        response(200, {
          id: 34,
          email: 'admin@example.com',
          displayName: 'Admin',
          role: 'ADMIN',
        }),
      )
      .mockResolvedValueOnce(
        response(200, [
          {
            id: 76,
            make: 'Honda',
            model: 'S2000',
            location: 'Tokyo, Japan',
            imageUrl: null,
            reviewStatus: 'PENDING',
          },
        ]),
      )
      .mockResolvedValueOnce(
        response(200, {
          headerName: 'X-CSRF-TOKEN',
          token: 'fresh-token',
        }),
      )
      .mockResolvedValueOnce(
        response(200, {
          id: 76,
          make: 'Honda',
          model: 'S2000',
          location: 'Tokyo, Japan',
          imageUrl: null,
          reviewStatus: 'REJECTED',
        }),
      )

    vi.stubGlobal('fetch', fetchMock)

    const wrapper = mount(AdminReviewView, {
      global: { stubs: { RouterLink: RouterLinkStub } },
    })
    await flushPromises()

    const rejectButton = wrapper
      .findAll('button')
      .find((button) => button.text() === 'Reject submission')

    expect(rejectButton).toBeDefined()
    await rejectButton!.trigger('click')
    await flushPromises()

    expect(fetchMock).toHaveBeenCalledWith('/api/admin/cars/76/reject', {
      method: 'PATCH',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': 'fresh-token',
      },
    })
    expect(wrapper.text()).toContain('Honda S2000 was rejected.')
    expect(wrapper.text()).toContain('The review queue is empty.')
  })
})
