import { describe, it, expect } from 'vitest'

import { mount } from '@vue/test-utils'
import App from '../App.vue'

describe('App', () => {
  it('mounts renders properly', () => {
    const wrapper = mount(App)
    expect(wrapper.text()).toContain('WORLD GARAGE')
    expect(wrapper.text()).toContain('Discover real cars from around the world')
  })
  it('updates status message when explore button is clicked', async () => {
    const wrapper = mount(App)

    await wrapper.get('button').trigger('click')

    expect(wrapper.text()).toContain('The car gallery is coming next.')
  })
  it('filters cars based on search query', async () => {
    const wrapper = mount(App)
    const searchInput = wrapper.get('#car-search')

    await searchInput.setValue('Nissan')

    expect(wrapper.text()).toContain('Nissan 370Z')
    expect(wrapper.text()).not.toContain('Toyota Supra')
    expect(wrapper.text()).not.toContain('Mercedes-Benz A45 AMG')
  })
  it('shows an empty state when no cars match', async () => {
    const wrapper = mount(App)
    const searchInput = wrapper.get('#car-search')

    await searchInput.setValue('Nonexistent Car')

    expect(wrapper.text()).toContain('No cars found matching your search.')
    expect(wrapper.findAll('.car-card')).toHaveLength(0)
  })
})
