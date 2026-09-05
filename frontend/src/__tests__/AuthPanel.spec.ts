import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AuthPanel from '../components/AuthPanel.vue'

const owner = { id: 7, email: 'owner@example.com', displayName: 'Garage Owner', role: 'USER' }
function response(status: number, body: unknown = null): Response {
  return { ok: status >= 200 && status < 300, status, json: async () => body } as Response
}
function setup(initial = response(401)) {
  const fetchMock = vi.fn().mockResolvedValueOnce(initial)
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}
function token() {
  return response(200, { headerName: 'X-CSRF-TOKEN', token: 'fresh-token' })
}
async function fill(wrapper: ReturnType<typeof mount>) {
  await wrapper.get('#account-email').setValue('owner@example.com')
  await wrapper.get('#account-password').setValue('Garage@2026')
}
afterEach(() => vi.unstubAllGlobals())

describe('account authentication', () => {
  it('restores the user on mount', async () => {
    const fetchMock = setup(response(200, owner))
    const wrapper = mount(AuthPanel)
    await flushPromises()
    expect(wrapper.text()).toContain('Welcome, Garage Owner')
    expect(fetchMock).toHaveBeenCalledWith('/api/auth/me', { credentials: 'same-origin' })
    expect(wrapper.find('form').exists()).toBe(false)
  })

  it('logs in with CSRF and refreshes identity', async () => {
    const fetchMock = setup()
    const wrapper = mount(AuthPanel)
    await flushPromises()
    fetchMock
      .mockResolvedValueOnce(token())
      .mockResolvedValueOnce(response(200))
      .mockResolvedValueOnce(response(200, owner))
    await fill(wrapper)
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': 'fresh-token' },
      body: JSON.stringify({ email: 'owner@example.com', password: 'Garage@2026' }),
    })
    expect(wrapper.text()).toContain('Welcome, Garage Owner')
  })

  it('registers and prompts for login without pretending to be signed in', async () => {
    const fetchMock = setup()
    const wrapper = mount(AuthPanel)
    await flushPromises()
    await wrapper.get('.account-switch').trigger('click')
    await wrapper.get('#account-name').setValue('Garage Owner')
    await fill(wrapper)
    fetchMock.mockResolvedValueOnce(token()).mockResolvedValueOnce(response(201))
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/auth/register',
      expect.objectContaining({
        body: JSON.stringify({
          email: 'owner@example.com',
          password: 'Garage@2026',
          displayName: 'Garage Owner',
        }),
      }),
    )
    expect(wrapper.text()).toContain('Account created.')
    expect((wrapper.get('#account-password').element as HTMLInputElement).value).toBe('')
    expect(wrapper.text()).not.toContain('Welcome,')
  })

  it('shows incorrect credentials and clears password', async () => {
    const fetchMock = setup()
    const wrapper = mount(AuthPanel)
    await flushPromises()
    await fill(wrapper)
    fetchMock.mockResolvedValueOnce(token()).mockResolvedValueOnce(response(401))
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(wrapper.get('[role="alert"]').text()).toContain('Email or password is incorrect')
    expect((wrapper.get('#account-password').element as HTMLInputElement).value).toBe('')
  })

  it('logs out with a freshly obtained token', async () => {
    const fetchMock = setup(response(200, owner))
    const wrapper = mount(AuthPanel)
    await flushPromises()
    fetchMock.mockResolvedValueOnce(token()).mockResolvedValueOnce(response(204))
    await wrapper.get('button').trigger('click')
    await flushPromises()
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/auth/logout',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': 'fresh-token' },
      }),
    )
    expect(wrapper.text()).toContain('You are signed out.')
    expect(wrapper.find('form').exists()).toBe(true)
  })

  it('does not claim logout succeeded when the network fails', async () => {
    const fetchMock = setup(response(200, owner))
    const wrapper = mount(AuthPanel)
    await flushPromises()
    fetchMock.mockRejectedValueOnce(new Error('Network unavailable'))
    await wrapper.get('button').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('Welcome, Garage Owner')
    expect(wrapper.get('[role="alert"]').text()).toContain('Network unavailable')
  })

  it('shows token expiry without retrying a write automatically', async () => {
    const fetchMock = setup()
    const wrapper = mount(AuthPanel)
    await flushPromises()
    await fill(wrapper)
    fetchMock.mockResolvedValueOnce(token()).mockResolvedValueOnce(response(403))
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(wrapper.get('[role="alert"]').text()).toContain('security token expired')
    expect(fetchMock).toHaveBeenCalledTimes(3)
  })

  it('shows duplicate email errors', async () => {
    const fetchMock = setup()
    const wrapper = mount(AuthPanel)
    await flushPromises()
    await wrapper.get('.account-switch').trigger('click')
    await wrapper.get('#account-name').setValue('Owner')
    await fill(wrapper)
    fetchMock.mockResolvedValueOnce(token()).mockResolvedValueOnce(response(409))
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(wrapper.get('[role="alert"]').text()).toContain('already registered')
  })
})
