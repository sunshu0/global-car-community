import { describe, expect, it } from 'vitest'
import { mount, RouterLinkStub } from '@vue/test-utils'

import CarCard from '../components/CarCard.vue'

describe('CarCard', () => {
  it('renders the car image when imageUrl is provided', () => {
    const wrapper = mount(CarCard, {
      props: {
        id: 4,
        make: 'Mazda',
        model: 'RX-7',
        location: 'Hiroshima, Japan',
        imageUrl: 'https://example.com/mazda-rx7.jpg',
      },
      global: { stubs: { RouterLink: RouterLinkStub } },
    })

    const image = wrapper.get('img.car-image')

    expect(image.attributes('src')).toBe('https://example.com/mazda-rx7.jpg')
    expect(image.attributes('alt')).toBe('Mazda RX-7')
    expect(wrapper.find('.car-image-placeholder').exists()).toBe(false)
  })
  it('renders a placeholder when imageUrl is null', () => {
    const wrapper = mount(CarCard, {
      props: {
        id: 2,
        make: 'Toyota',
        model: 'Supra',
        location: 'Tokyo, Japan',
        imageUrl: null,
      },
      global: { stubs: { RouterLink: RouterLinkStub } },
    })

    expect(wrapper.find('img.car-image').exists()).toBe(false)
    expect(wrapper.get('.car-image-placeholder').text()).toContain('No image available')
  })
})
