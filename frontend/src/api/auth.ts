export interface CurrentUser {
  id: number
  email: string
  displayName: string
  role: 'USER' | 'ADMIN'
}

interface CsrfToken {
  headerName: string
  token: string
}

async function check(response: Response, path: string): Promise<void> {
  if (response.ok) return
  if (response.status === 401) {
    const message =
      path === '/api/auth/login'
        ? 'Email or password is incorrect. Please sign in again.'
        : 'Please sign in before submitting a car.'
    throw new Error(message)
  }
  if (response.status === 403) throw new Error('Your security token expired. Please try again.')
  if (response.status === 409) throw new Error('This email is already registered.')
  if (response.status === 400) {
    const body = await response.json().catch(() => null)
    if (body?.errors) throw new Error(Object.values(body.errors).join(' '))
  }
  throw new Error('Unable to complete the request. Please try again.')
}

// Fetch a fresh token for each write; login and logout rotate the CSRF secret.
export async function requestWithCsrf(
  path: string,
  method: 'POST' | 'PATCH',
  body?: object,
): Promise<Response> {
  const tokenResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin' })
  await check(tokenResponse, '/api/auth/csrf')
  const csrf: CsrfToken = await tokenResponse.json()
  const response = await fetch(path, {
    method,
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
    ...(body ? { body: JSON.stringify(body) } : {}),
  })
  await check(response, path)
  return response
}

export async function postWithCsrf(path: string, body?: object): Promise<Response> {
  return requestWithCsrf(path, 'POST', body)
}

export async function getCurrentUser(): Promise<CurrentUser | null> {
  const response = await fetch('/api/auth/me', { credentials: 'same-origin' })
  if (response.status === 401) return null
  await check(response, '/api/auth/me')
  return response.json()
}

export async function register(
  email: string,
  password: string,
  displayName: string,
): Promise<void> {
  await postWithCsrf('/api/auth/register', {
    email: email.trim(),
    password,
    displayName: displayName.trim(),
  })
}

export async function login(email: string, password: string): Promise<void> {
  await postWithCsrf('/api/auth/login', { email: email.trim(), password })
}

export async function logout(): Promise<void> {
  await postWithCsrf('/api/auth/logout')
}
