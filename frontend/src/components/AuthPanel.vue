<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getCurrentUser, login, logout, register, type CurrentUser } from '../api/auth'

const user = ref<CurrentUser | null>(null)
const mode = ref<'login' | 'register'>('login')
const email = ref('')
const password = ref('')
const displayName = ref('')
const busy = ref(false)
const loading = ref(true)
const error = ref('')
const message = ref('')

function errorMessage(cause: unknown): string {
  return cause instanceof Error ? cause.message : 'Unable to connect. Please try again.'
}

async function restore(): Promise<void> {
  loading.value = true
  error.value = ''
  try {
    user.value = await getCurrentUser()
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    loading.value = false
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
    message.value = 'You are signed out.'
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    busy.value = false
  }
}

onMounted(restore)
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
