<script setup lang="ts">
import { ref } from 'vue'
import { postWithCsrf } from '../api/auth'
import type { Car } from '../api/cars'

const newMake = ref('')
const newModel = ref('')
const newLocation = ref('')
const newImageUrl = ref('')
const isSubmitting = ref(false)
const submitError = ref('')
const statusMessage = ref('')

async function submitCar(): Promise<void> {
  const car = {
    make: newMake.value.trim(),
    model: newModel.value.trim(),
    location: newLocation.value.trim(),
    imageUrl: newImageUrl.value.trim() || null,
  }

  if (!car.make || !car.model || !car.location) {
    submitError.value = 'Please complete all fields.'
    return
  }

  isSubmitting.value = true
  submitError.value = ''
  statusMessage.value = ''

  try {
    const response = await postWithCsrf('/api/cars', car)
    const createdCar: Car = await response.json()
    window.dispatchEvent(new Event('world-garage:car-submitted'))

    newMake.value = ''
    newModel.value = ''
    newLocation.value = ''
    newImageUrl.value = ''
    statusMessage.value = `${createdCar.make} ${createdCar.model} was submitted for review.`
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : 'Unable to add car.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="submission-section" aria-labelledby="submission-title">
    <div class="submission-intro">
      <p class="eyebrow">Community submissions</p>
      <h2 id="submission-title">Put your car in the next issue.</h2>
      <p>
        Tell us what you drive and where it lives. Every submission enters review before it appears
        in the public garage.
      </p>
      <div class="review-note">
        <span>Editorial note</span>
        Submitting creates a pending entry. It does not publish the car immediately.
      </div>
    </div>

    <form class="car-form" @submit.prevent="submitCar">
      <div class="form-field">
        <label for="car-make">Make</label>
        <input id="car-make" v-model="newMake" type="text" placeholder="Nissan" required />
      </div>
      <div class="form-field">
        <label for="car-model">Model</label>
        <input id="car-model" v-model="newModel" type="text" placeholder="370Z" required />
      </div>
      <div class="form-field">
        <label for="car-location">Location</label>
        <input
          id="car-location"
          v-model="newLocation"
          type="text"
          placeholder="Auckland, New Zealand"
          required
        />
      </div>
      <div class="form-field">
        <label for="car-image-url">Image URL <span>Optional</span></label>
        <input
          id="car-image-url"
          v-model="newImageUrl"
          type="url"
          placeholder="https://example.com/car.jpg"
        />
      </div>
      <div class="form-footer">
        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? 'Submitting...' : 'Submit for review' }}
        </button>
        <p class="form-terms">Public only after administrator approval.</p>
      </div>
      <p v-if="submitError" class="form-message form-message--error" role="alert">
        Unable to add car: {{ submitError }}
      </p>
      <p v-if="statusMessage" class="form-message form-message--success" role="status">
        {{ statusMessage }}
      </p>
    </form>
  </section>
</template>
