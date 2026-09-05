export interface OwnerSummary {
  id: number
  displayName: string
}

export interface Car {
  id: number
  make: string
  model: string
  location: string
  imageUrl: string | null
  reviewStatus?: 'PENDING' | 'APPROVED'
  owner?: OwnerSummary | null
}

export async function getCarById(id: number): Promise<Car> {
  const response = await fetch(`/api/cars/${id}`)

  if (response.status === 404) {
    throw new Error('This car is not available in the public garage.')
  }

  if (!response.ok) {
    throw new Error(`Unable to load this car (status ${response.status}).`)
  }

  return response.json()
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
