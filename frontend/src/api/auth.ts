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

async function check(response: Response): Promise<void> {
  if (response.ok) return
  if (response.status === 401)
    throw new Error('Email or password is incorrect. Please sign in again.')
  if (response.status === 403) throw new Error('Your security token expired. Please try again.')
  if (response.status === 409) throw new Error('This email is already registered.')
  if (response.status === 400) {
    const body = await response.json().catch(() => null)
    if (body?.errors) throw new Error(Object.values(body.errors).join(' '))
  }
  throw new Error('Unable to complete the request. Please try again.')
}

// Fetch a fresh token for each write; login and logout rotate the CSRF secret.
async function write(path: string, body?: object): Promise<void> {
  const tokenResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin' })
  await check(tokenResponse)
  const csrf: CsrfToken = await tokenResponse.json()
  const response = await fetch(path, {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
    ...(body ? { body: JSON.stringify(body) } : {}),
  })
  await check(response)
}

export async function getCurrentUser(): Promise<CurrentUser | null> {
  const response = await fetch('/api/auth/me', { credentials: 'same-origin' })
  if (response.status === 401) return null
  await check(response)
  return response.json()
}

export function register(email: string, password: string, displayName: string): Promise<void> {
  return write('/api/auth/register', {
    email: email.trim(),
    password,
    displayName: displayName.trim(),
  })
}

export function login(email: string, password: string): Promise<void> {
  return write('/api/auth/login', { email: email.trim(), password })
}

export function logout(): Promise<void> {
  return write('/api/auth/logout')
}
