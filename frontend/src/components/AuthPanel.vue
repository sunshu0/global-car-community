<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getCurrentUser, login, logout, register, type CurrentUser } from '../api/auth'
import { getMyCars, type Car } from '../api/cars'

const user = ref<CurrentUser | null>(null)
const mode = ref<'login' | 'register'>('login')
const email = ref('')
const password = ref('')
const displayName = ref('')
const busy = ref(false)
const loading = ref(true)
const error = ref('')
const message = ref('')
const myCars = ref<Car[]>([])
const carsLoading = ref(false)
const carsError = ref('')

function errorMessage(cause: unknown): string {
  return cause instanceof Error ? cause.message : 'Unable to connect. Please try again.'
}

async function restore(): Promise<void> {
  loading.value = true
  error.value = ''
  try {
    user.value = await getCurrentUser()
    if (user.value) {
      await loadMyCars()
    } else {
      myCars.value = []
    }
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    loading.value = false
  }
}

async function loadMyCars(): Promise<void> {
  if (!user.value || carsLoading.value) return
  carsLoading.value = true
  carsError.value = ''
  try {
    myCars.value = await getMyCars()
  } catch (cause) {
    carsError.value = errorMessage(cause)
  } finally {
    carsLoading.value = false
  }
}

function switchMode(): void {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  password.value = ''
  error.value = ''
  message.value = ''
}

async function submit(): Promise<void> {
  if (busy.value) return
  busy.value = true
  error.value = ''
  message.value = ''
  try {
    if (mode.value === 'register') {
      await register(email.value, password.value, displayName.value)
      mode.value = 'login'
      message.value = 'Account created. Sign in with your new password.'
    } else {
      await login(email.value, password.value)
      user.value = await getCurrentUser()
      if (!user.value) throw new Error('Your session expired. Please sign in again.')
      await loadMyCars()
      message.value = 'You are signed in.'
    }
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    password.value = ''
    busy.value = false
  }
}

async function signOut(): Promise<void> {
  if (busy.value) return
  busy.value = true
  error.value = ''
  message.value = ''
  try {
    await logout()
    user.value = null
    myCars.value = []
    message.value = 'You are signed out.'
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    busy.value = false
  }
}

function handleCarSubmitted(): void {
  void loadMyCars()
}

onMounted(() => {
  window.addEventListener('world-garage:car-submitted', handleCarSubmitted)
  void restore()
})

onBeforeUnmount(() => {
  window.removeEventListener('world-garage:car-submitted', handleCarSubmitted)
})
</script>

<template>
  <section id="account" class="account-panel" aria-labelledby="account-title">
    <div>
      <p class="account-kicker">The people behind the machines</p>
      <h2 id="account-title">Your World Garage.</h2>
      <p>One account. Your place in the garage.</p>
    </div>
    <div class="account-content" :aria-busy="loading || busy">
      <p v-if="loading" role="status">Checking your session…</p>
      <div v-else-if="user">
        <h3>Welcome, {{ user.displayName }}</h3>
        <p>{{ user.email }}</p>
        <button type="button" :disabled="busy" @click="signOut">
          {{ busy ? 'Signing out…' : 'Sign out' }}
        </button>
        <div class="my-garage">
          <div class="my-garage__heading">
            <h3>My vehicles</h3>
            <button type="button" class="refresh-cars" :disabled="carsLoading" @click="loadMyCars">
              {{ carsLoading ? 'Refreshing…' : 'Refresh' }}
            </button>
          </div>
          <p v-if="carsLoading && myCars.length === 0" role="status">Loading your vehicles…</p>
          <p v-else-if="carsError" class="cars-error" role="alert">{{ carsError }}</p>
          <p v-else-if="myCars.length === 0">No submissions yet.</p>
          <ul v-else class="my-car-list">
            <li v-for="car in myCars" :key="car.id">
              <div>
                <strong>{{ car.make }} {{ car.model }}</strong>
                <span>{{ car.location }}</span>
              </div>
              <span
                class="review-status"
                :class="`review-status--${car.reviewStatus?.toLowerCase()}`"
              >
                {{ car.reviewStatus }}
              </span>
            </li>
          </ul>
        </div>
      </div>
      <form v-else class="auth-form" @submit.prevent="submit">
        <h3>{{ mode === 'login' ? 'Sign in' : 'Create an account' }}</h3>
        <label v-if="mode === 'register'" for="account-name"
          >Display name
          <input
            id="account-name"
            v-model="displayName"
            autocomplete="nickname"
            required
            maxlength="50"
            :disabled="busy"
          />
        </label>
        <label for="account-email"
          >Email
          <input
            id="account-email"
            v-model="email"
            type="email"
            autocomplete="username"
            required
            maxlength="320"
            :disabled="busy"
          />
        </label>
        <label for="account-password"
          >Password
          <input
            id="account-password"
            v-model="password"
            type="password"
            :autocomplete="mode === 'register' ? 'new-password' : 'current-password'"
            :minlength="mode === 'register' ? 8 : undefined"
            maxlength="72"
            required
            :disabled="busy"
          />
        </label>
        <p v-if="mode === 'register'" class="account-hint">
          8–72 characters. Password recovery is not available yet.
        </p>
        <button type="submit" :disabled="busy">
          {{ busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account' }}
        </button>
        <button type="button" class="account-switch" :disabled="busy" @click="switchMode">
          {{ mode === 'login' ? 'New here? Create an account' : 'Already registered? Sign in' }}
        </button>
      </form>
      <p v-if="message" role="status">{{ message }}</p>
      <div v-if="error" role="alert">
        <p>{{ error }}</p>
        <button type="button" :disabled="busy || loading" @click="restore">
          Check session again
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.account-panel {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  padding: 3rem;
  margin: 2rem auto;
  max-width: 1280px;
  background: #e9e5dc;
  color: #202322;
  border-top: 3px solid #202322;
}
.account-kicker {
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.75rem;
}
h2 {
  font-size: clamp(2rem, 4vw, 3.5rem);
  line-height: 1.1;
  margin: 1rem 0;
}
h3 {
  margin-top: 0;
}
.account-content {
  min-width: 0;
  overflow-wrap: anywhere;
}
.auth-form,
label {
  display: grid;
  gap: 0.6rem;
}
.auth-form {
  gap: 1rem;
}
input {
  box-sizing: border-box;
  width: 100%;
  min-width: 0;
  padding: 0.8rem;
  border: 1px solid #777;
  background: #fff;
  color: #202322;
  font: inherit;
}
button {
  padding: 0.8rem 1rem;
  border: 1px solid #202322;
  background: #202322;
  color: white;
  font: inherit;
  cursor: pointer;
}
button:disabled {
  opacity: 0.6;
  cursor: wait;
}
.account-switch {
  background: transparent;
  color: #202322;
}
.account-hint {
  font-size: 0.85rem;
  margin: 0;
}
.my-garage {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #aaa49a;
}
.my-garage__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}
.my-garage__heading h3 {
  margin: 0;
}
.refresh-cars {
  padding: 0.45rem 0.75rem;
  background: transparent;
  color: #202322;
}
.my-car-list {
  display: grid;
  gap: 0;
  padding: 0;
  margin: 1rem 0 0;
  list-style: none;
  border-top: 1px solid #aaa49a;
}
.my-car-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid #aaa49a;
}
.my-car-list li div,
.my-car-list li span {
  display: grid;
  gap: 0.25rem;
}
.review-status {
  flex: 0 0 auto;
  padding: 0.3rem 0.5rem;
  border: 1px solid currentColor;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.08em;
}
.review-status--pending {
  color: #9b5b11;
}
.review-status--approved {
  color: #28643c;
}
.cars-error {
  color: #942727;
}
[role='alert'] {
  color: #942727;
  margin-top: 1rem;
}
@media (max-width: 700px) {
  .account-panel {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    padding: 1.5rem;
  }
}
</style>
