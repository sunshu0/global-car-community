import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'

import App from '../App.vue'
import GarageView from '../views/GarageView.vue'
import HomeView from '../views/HomeView.vue'
import SubmitView from '../views/SubmitView.vue'

const cars = [
  {
    id: 1,
    make: 'Nissan',
    model: '370Z',
    location: 'Auckland, New Zealand',
    imageUrl: null,
  },
  { id: 2, make: 'Toyota', model: 'Supra', location: 'Tokyo, Japan', imageUrl: null },
  { id: 3, make: 'BMW', model: 'M3', location: 'Munich, Germany', imageUrl: null },
]

describe('World Garage pages', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => cars,
      }),
    )
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders the home page through the router', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: HomeView }],
    })
    await router.push('/')
    await router.isReady()

    const wrapper = mount(App, { global: { plugins: [router] } })

    expect(wrapper.get('.wordmark').text()).toBe('WORLDGARAGE')
    expect(wrapper.text()).toContain('Discover real cars from around the world')
    expect(wrapper.get('.hero-actions a').attributes('href')).toBe('#garage')
    expect(wrapper.text()).toContain('Cars worth a closer look.')
  })

  it('loads cars from the backend endpoint', async () => {
    const wrapper = mount(GarageView)
    await flushPromises()

    expect(fetch).toHaveBeenCalledWith('/api/cars')
    expect(wrapper.text()).toContain('Nissan 370Z')
  })

  it('filters cars based on search query', async () => {
    const wrapper = mount(GarageView)
    await flushPromises()
    await wrapper.get('#car-search').setValue('Nissan')

    expect(wrapper.text()).toContain('Nissan 370Z')
    expect(wrapper.text()).not.toContain('Toyota Supra')
    expect(wrapper.text()).not.toContain('BMW M3')
  })

  it('filters cars by selected country', async () => {
    const wrapper = mount(GarageView)
    await flushPromises()
    await wrapper.get('#country-filter').setValue('Germany')

    const carCards = wrapper.findAll('.car-card')
    expect(carCards).toHaveLength(1)
    expect(carCards[0]?.text()).toContain('Germany')
  })

  it('shows an empty state when no cars match', async () => {
    const wrapper = mount(GarageView)
    await flushPromises()
    await wrapper.get('#car-search').setValue('Nonexistent Car')

    expect(wrapper.text()).toContain('No cars found matching your search.')
    expect(wrapper.findAll('.car-card')).toHaveLength(0)
  })

  it('submits a new car for review', async () => {
    const wrapper = mount(SubmitView)
    vi.mocked(fetch)
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({ headerName: 'X-CSRF-TOKEN', token: 'car-token' }),
      } as Response)
      .mockResolvedValueOnce({
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
    expect(wrapper.text()).toContain('Mazda RX-7 was submitted for review.')
  })

  it('tells anonymous users to sign in before submitting', async () => {
    const wrapper = mount(SubmitView)
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
