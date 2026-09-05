export interface Car {
  id: number
  make: string
  model: string
  location: string
  imageUrl: string | null
  reviewStatus?: 'PENDING' | 'APPROVED'
}

export async function getMyCars(): Promise<Car[]> {
  const response = await fetch('/api/cars/mine', { credentials: 'same-origin' })

  if (response.status === 401) {
    throw new Error('Your session expired. Please sign in again.')
  }

  if (!response.ok) {
    throw new Error(`Unable to load your cars (status ${response.status}).`)
  }

  return response.json()
}
