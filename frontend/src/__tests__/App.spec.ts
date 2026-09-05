import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { flushPromises, mount } from '@vue/test-utils'
import App from '../App.vue'

// Account behavior has its own tests; keep car request mocks isolated.
vi.mock('../components/AuthPanel.vue', () => ({
  default: { template: '<section id="account"></section>' },
}))

describe('App', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => [
          {
            id: 1,
            make: 'Nissan',
            model: '370Z',
            location: 'Auckland, New Zealand',
            imageUrl: null,
          },
          { id: 2, make: 'Toyota', model: 'Supra', location: 'Tokyo, Japan', imageUrl: null },
          { id: 3, make: 'BMW', model: 'M3', location: 'Munich, Germany', imageUrl: null },
        ],
      }),
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('mounts renders properly', () => {
    const wrapper = mount(App)
    expect(wrapper.get('.wordmark').text()).toBe('WORLDGARAGE')
    expect(wrapper.text()).toContain('Discover real cars from around the world')
  })

  it('links the hero action to the public garage', () => {
    const wrapper = mount(App)

    const exploreLink = wrapper.get('.hero-actions a[href="#garage"]')
    expect(exploreLink.text()).toContain('Explore the collection')
  })

  it('filters cars based on search query', async () => {
    const wrapper = mount(App)
    await flushPromises()
    const searchInput = wrapper.get('#car-search')

    await searchInput.setValue('Nissan')

    expect(wrapper.text()).toContain('Nissan 370Z')
    expect(wrapper.text()).not.toContain('Toyota Supra')
    expect(wrapper.text()).not.toContain('BMW M3')
  })

  it('filters cars by selected country', async () => {
    const wrapper = mount(App)
    await flushPromises()
    const countryFilter = wrapper.get('#country-filter')

    await countryFilter.setValue('Germany')

    const carCards = wrapper.findAll('.car-card')

    expect(carCards).toHaveLength(1)
    expect(carCards.every((card) => card.text().includes('Germany'))).toBe(true)
  })

  it('shows an empty state when no cars match', async () => {
    const wrapper = mount(App)
    await flushPromises()
    const searchInput = wrapper.get('#car-search')

    await searchInput.setValue('Nonexistent Car')

    expect(wrapper.text()).toContain('No cars found matching your search.')
    expect(wrapper.findAll('.car-card')).toHaveLength(0)
  })

  it('loads cars from the backend endpoint', async () => {
    const wrapper = mount(App)

    await flushPromises()

    expect(fetch).toHaveBeenCalledWith('/api/cars')
    expect(wrapper.text()).toContain('Nissan 370Z')
  })

  it('submits a new car for review without displaying it publicly', async () => {
    const wrapper = mount(App)
    await flushPromises()

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({ headerName: 'X-CSRF-TOKEN', token: 'car-token' }),
    } as Response)
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      status: 201,
      json: async () => ({
        id: 4,
        make: 'Mazda',
        model: 'RX-7',
        location: 'Hiroshima, Japan',
        imageUrl: 'https://example.com/mazda-rx7.jpg',
        reviewStatus: 'PENDING',
      }),
    } as Response)

    await wrapper.get('#car-make').setValue('Mazda')
    await wrapper.get('#car-model').setValue('RX-7')
    await wrapper.get('#car-location').setValue('Hiroshima, Japan')
    await wrapper.get('#car-image-url').setValue('https://example.com/mazda-rx7.jpg')

    await wrapper.get('form.car-form').trigger('submit')
    await flushPromises()

    expect(fetch).toHaveBeenLastCalledWith('/api/cars', {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': 'car-token',
      },
      body: JSON.stringify({
        make: 'Mazda',
        model: 'RX-7',
        location: 'Hiroshima, Japan',
        imageUrl: 'https://example.com/mazda-rx7.jpg',
      }),
    })

    expect(wrapper.get('.car-grid').text()).not.toContain('Mazda RX-7')
    expect(wrapper.text()).toContain('Mazda RX-7 was submitted for review.')
  })

  it('tells anonymous users to sign in before submitting', async () => {
    const wrapper = mount(App)
    await flushPromises()

    vi.mocked(fetch)
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({ headerName: 'X-CSRF-TOKEN', token: 'car-token' }),
      } as Response)
      .mockResolvedValueOnce({ ok: false, status: 401 } as Response)

    await wrapper.get('#car-make').setValue('Nissan')
    await wrapper.get('#car-model').setValue('370Z')
    await wrapper.get('#car-location').setValue('Auckland, New Zealand')
    await wrapper.get('form.car-form').trigger('submit')
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toContain(
      'Please sign in before submitting a car.',
    )
  })
})
