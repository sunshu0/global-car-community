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
})
