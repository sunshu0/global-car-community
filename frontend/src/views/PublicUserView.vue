<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getPublicUserProfile, type PublicUserProfile } from '../api/users'
import CarCard from '../components/CarCard.vue'

const props = defineProps<{ id: string }>()

const profile = ref<PublicUserProfile | null>(null)
const isLoading = ref(true)
const error = ref('')

async function loadProfile(): Promise<void> {
  const userId = Number(props.id)

  if (!Number.isInteger(userId) || userId < 1) {
    error.value = 'This garage owner could not be found.'
    isLoading.value = false
    return
  }

  try {
    profile.value = await getPublicUserProfile(userId)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'Unable to load this garage owner.'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <section class="profile-page">
    <p v-if="isLoading" class="profile-state">Loading owner profile...</p>

    <div v-else-if="error" class="profile-state">
      <p class="eyebrow">Owner unavailable</p>
      <h1>Garage not found.</h1>
      <p>{{ error }}</p>
      <RouterLink class="profile-link" to="/garage">Return to the public garage</RouterLink>
    </div>

    <template v-else-if="profile">
      <header class="profile-header">
        <RouterLink class="profile-link" to="/garage">← The public garage</RouterLink>
        <p class="eyebrow">Community member · Owner {{ profile.id }}</p>
        <h1>{{ profile.displayName }}</h1>
        <p>
          {{ profile.cars.length }} approved
          {{ profile.cars.length === 1 ? 'car' : 'cars' }} in the public collection.
        </p>
      </header>

      <div v-if="profile.cars.length" class="profile-car-grid">
        <CarCard
          v-for="car in profile.cars"
          :key="car.id"
          :id="car.id"
          :make="car.make"
          :model="car.model"
          :location="car.location"
          :image-url="car.imageUrl"
        />
      </div>

      <p v-else class="profile-empty">This owner has no approved cars on display yet.</p>
    </template>
  </section>
</template>

<style scoped>
.profile-page {
  min-height: calc(100vh - 88px);
  padding: clamp(52px, 8vw, 120px) clamp(24px, 6vw, 110px);
  background: #eeeae0;
  color: #171717;
}

.profile-header,
.profile-car-grid,
.profile-empty {
  max-width: 1500px;
  margin-right: auto;
  margin-left: auto;
}

.profile-header {
  padding-bottom: clamp(52px, 7vw, 92px);
  border-bottom: 1px solid #bdb8ad;
}

.profile-header h1,
.profile-state h1 {
  margin: 0 0 24px;
  font-size: clamp(3.8rem, 8vw, 9rem);
  letter-spacing: -0.07em;
  line-height: 0.85;
}

.profile-header > p:last-child {
  color: #615c54;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 1.05rem;
}

.profile-link {
  display: inline-block;
  margin-bottom: 54px;
  border-bottom: 1px solid #8e887d;
  color: #5b564e;
  font-size: 0.68rem;
  font-weight: 900;
  letter-spacing: 0.13em;
  text-decoration: none;
  text-transform: uppercase;
}

.profile-car-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(30px, 4vw, 68px) clamp(18px, 2.5vw, 42px);
  padding-top: clamp(52px, 7vw, 92px);
}

.profile-empty,
.profile-state {
  padding: 70px 24px;
  border: 1px solid #bdb8ad;
  color: #625e56;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 1.15rem;
  text-align: center;
}

.profile-state {
  max-width: 900px;
  margin: clamp(50px, 10vh, 130px) auto;
}

.profile-state .profile-link {
  margin: 28px 0 0;
}

@media (max-width: 1000px) {
  .profile-car-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .profile-car-grid {
    grid-template-columns: 1fr;
  }
}
</style>
