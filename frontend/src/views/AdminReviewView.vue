<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { approveCar, getPendingCars, rejectCar } from '../api/admin'
import { getCurrentUser } from '../api/auth'
import type { Car } from '../api/cars'

const cars = ref<Car[]>([])
const isLoading = ref(true)
const reviewingId = ref<number | null>(null)
const reviewAction = ref<'approve' | 'reject' | null>(null)
const error = ref('')
const message = ref('')

function errorMessage(cause: unknown): string {
  return cause instanceof Error ? cause.message : 'Unable to connect. Please try again.'
}

async function loadQueue(): Promise<void> {
  isLoading.value = true
  error.value = ''

  try {
    const user = await getCurrentUser()

    if (!user) {
      throw new Error('Please sign in with an administrator account.')
    }

    if (user.role !== 'ADMIN') {
      throw new Error('This account does not have administrator access.')
    }

    cars.value = await getPendingCars()
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    isLoading.value = false
  }
}

async function approve(id: number): Promise<void> {
  if (reviewingId.value !== null) return

  reviewingId.value = id
  reviewAction.value = 'approve'
  error.value = ''
  message.value = ''

  try {
    const approved = await approveCar(id)
    cars.value = cars.value.filter((car) => car.id !== id)
    message.value = `${approved.make} ${approved.model} is now public.`
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    reviewingId.value = null
    reviewAction.value = null
  }
}

async function reject(id: number): Promise<void> {
  if (reviewingId.value !== null) return

  reviewingId.value = id
  reviewAction.value = 'reject'
  error.value = ''
  message.value = ''

  try {
    const rejected = await rejectCar(id)
    cars.value = cars.value.filter((car) => car.id !== id)
    message.value = `${rejected.make} ${rejected.model} was rejected.`
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    reviewingId.value = null
    reviewAction.value = null
  }
}

onMounted(loadQueue)
</script>

<template>
  <section class="review-page">
    <header class="review-heading">
      <div>
        <p class="eyebrow">Administrator desk</p>
        <h1>Review queue.</h1>
      </div>
      <p>
        Check each community submission before it enters the public garage.
      </p>
    </header>

    <p v-if="isLoading" class="review-state" role="status">Loading pending submissions...</p>

    <div v-else-if="error" class="review-state review-state--error" role="alert">
      <p>{{ error }}</p>
      <RouterLink to="/account">Go to your account</RouterLink>
    </div>

    <template v-else>
      <p v-if="message" class="review-message" role="status">{{ message }}</p>
      <p v-if="cars.length === 0" class="review-state">The review queue is empty.</p>

      <div v-else class="review-grid">
        <article v-for="car in cars" :key="car.id" class="review-card">
          <div class="review-media">
            <img
              v-if="car.imageUrl"
              :src="car.imageUrl"
              :alt="`${car.make} ${car.model}`"
            />
            <div v-else class="review-placeholder">No image submitted</div>
          </div>
          <div class="review-copy">
            <p>Submission {{ car.id }} · {{ car.location }}</p>
            <h2>{{ car.make }} <em>{{ car.model }}</em></h2>
            <div class="review-actions">
              <button
                type="button"
                :disabled="reviewingId !== null"
                @click="approve(car.id)"
              >
                {{
                  reviewingId === car.id && reviewAction === 'approve'
                    ? 'Approving...'
                    : 'Approve for public garage'
                }}
              </button>
              <button
                type="button"
                class="reject-button"
                :disabled="reviewingId !== null"
                @click="reject(car.id)"
              >
                {{
                  reviewingId === car.id && reviewAction === 'reject'
                    ? 'Rejecting...'
                    : 'Reject submission'
                }}
              </button>
            </div>
          </div>
        </article>
      </div>
    </template>
  </section>
</template>

<style scoped>
.review-page {
  min-height: calc(100vh - 88px);
  padding: clamp(58px, 8vw, 120px) clamp(24px, 6vw, 110px);
  background: #eeeae0;
  color: #171717;
}

.review-heading {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(260px, 0.5fr);
  gap: 50px;
  align-items: end;
  max-width: 1500px;
  margin: 0 auto clamp(44px, 6vw, 80px);
  padding-bottom: 32px;
  border-bottom: 1px solid #bdb8ad;
}

.review-heading h1 {
  margin: 0;
  font-size: clamp(4rem, 8vw, 9rem);
  letter-spacing: -0.07em;
  line-height: 0.82;
}

.review-heading > p {
  color: #625d55;
  font-family: Georgia, 'Times New Roman', serif;
  line-height: 1.7;
}

.review-grid {
  display: grid;
  gap: 32px;
  max-width: 1500px;
  margin: 0 auto;
}

.review-card {
  display: grid;
  grid-template-columns: minmax(280px, 0.75fr) minmax(300px, 1.25fr);
  background: #171717;
  color: #eeeae0;
}

.review-media {
  min-height: 320px;
  background: #d4d0c7;
}

.review-media img,
.review-placeholder {
  width: 100%;
  height: 100%;
  min-height: 320px;
  object-fit: cover;
}

.review-placeholder {
  display: grid;
  place-content: center;
  color: #676158;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.review-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: clamp(30px, 5vw, 70px);
}

.review-copy > p {
  color: #a9a49a;
  font-size: 0.67rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.review-copy h2 {
  margin: 12px 0 38px;
  font-size: clamp(2.5rem, 5vw, 6rem);
  letter-spacing: -0.06em;
  line-height: 0.9;
}

.review-copy h2 em {
  color: #d83a2e;
  font-weight: 400;
}

.review-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.review-copy button {
  align-self: flex-start;
  min-height: 50px;
  padding: 0 22px;
  border: 0;
  background: #d83a2e;
  color: #fff;
  cursor: pointer;
  font-size: 0.7rem;
  font-weight: 900;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.review-copy .reject-button {
  border: 1px solid #706c64;
  background: transparent;
  color: #eeeae0;
}

.review-copy button:disabled {
  cursor: wait;
  opacity: 0.55;
}

.review-state,
.review-message {
  max-width: 1500px;
  margin: 0 auto;
  padding: 54px 24px;
  border: 1px solid #bdb8ad;
  color: #625e56;
  font-family: Georgia, 'Times New Roman', serif;
  text-align: center;
}

.review-state a {
  color: #9d2e25;
}

.review-message {
  margin-bottom: 24px;
  padding: 18px 24px;
  border-color: #7a9a7e;
  color: #28643c;
}

@media (max-width: 800px) {
  .review-heading,
  .review-card {
    grid-template-columns: 1fr;
  }
}
</style>
