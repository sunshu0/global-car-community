import { requestWithCsrf } from './auth'
import type { Car } from './cars'

export async function getPendingCars(): Promise<Car[]> {
  const response = await fetch('/api/admin/cars', {
    credentials: 'same-origin',
  })

  if (response.status === 401) {
    throw new Error('Please sign in with an administrator account.')
  }

  if (response.status === 403) {
    throw new Error('This account does not have administrator access.')
  }

  if (!response.ok) {
    throw new Error(`Unable to load the review queue (status ${response.status}).`)
  }

  return response.json()
}

export async function approveCar(id: number): Promise<Car> {
  const response = await requestWithCsrf(
    `/api/admin/cars/${id}/approve`,
    'PATCH',
  )

  return response.json()
}
