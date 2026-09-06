import { getCsrfToken } from './auth'

interface ImageUploadResponse {
  imageUrl: string
}

export async function uploadImage(file: File): Promise<string> {
  const csrf = await getCsrfToken()
  const formData = new FormData()
  formData.append('image', file)

  const response = await fetch('/api/uploads', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      [csrf.headerName]: csrf.token,
    },
    body: formData,
  })

  if (response.status === 401) {
    throw new Error('Please sign in before uploading an image.')
  }

  if (response.status === 403) {
    throw new Error('Your security token expired. Please try again.')
  }

  if (response.status === 400) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? 'Please choose a JPEG, PNG, or WebP image.')
  }

  if (!response.ok) {
    throw new Error('Unable to upload the image. Please try again.')
  }

  const result: ImageUploadResponse = await response.json()
  return result.imageUrl
}
