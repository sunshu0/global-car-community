import type { Car } from './cars'

export interface PublicUserProfile {
  id: number
  displayName: string
  cars: Car[]
}

export async function getPublicUserProfile(id: number): Promise<PublicUserProfile> {
  const response = await fetch(`/api/users/${id}`)

  if (response.status === 404) {
    throw new Error('This garage owner could not be found.')
  }

  if (!response.ok) {
    throw new Error(`Unable to load this garage owner (status ${response.status}).`)
  }

  return response.json()
}
