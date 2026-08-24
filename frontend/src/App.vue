<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CarCard from './components/CarCard.vue'

interface Car {
  id: number
  make: string
  model: string
  location: string
}

const cars = ref<Car[]>([])
const isLoading = ref<boolean>(true)
const loadError = ref<string>('')
const searchQuery = ref<string>('')
const selectedCountry = ref<string>('All countries')
const newMake = ref<string>('')
const newModel = ref<string>('')
const newLocation = ref<string>('')
const isSubmitting = ref<boolean>(false)
const submitError = ref<string>('')

const filteredCars = computed<Car[]>(() => {
  const query = searchQuery.value.trim().toLowerCase()

  return cars.value.filter((car) => {
    const searchableText = `${car.make} ${car.model} ${car.location}`.toLowerCase()
    const matchesSearch = query === '' || searchableText.includes(query)
    const matchesCountry =
      selectedCountry.value === 'All countries' || car.location.endsWith(selectedCountry.value)

    return matchesSearch && matchesCountry
  })
})

const siteName = 'WORLD GARAGE'
const tagline = 'Discover real cars from around the world'
const statusMessage = ref<string>('Ready to explore.')

async function loadCars(): Promise<void> {
  try {
    const response = await fetch('/api/cars')

    if (!response.ok) {
      throw new Error(`Request failed with status ${response.status}`)
    }

    cars.value = await response.json()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : 'Unable to load cars.'
  } finally {
    isLoading.value = false
  }
}

async function submitCar(): Promise<void> {
  const car = {
    make: newMake.value.trim(),
    model: newModel.value.trim(),
    location: newLocation.value.trim(),
  }

  if (!car.make || !car.model || !car.location) {
    submitError.value = 'Please complete all fields.'
    return
  }
  isSubmitting.value = true
  submitError.value = ''

  try {
    const response = await fetch('/api/cars', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(car),
    })

    if (!response.ok) {
      throw new Error(`Request failed with status ${response.status}`)
    }
    const createdCar: Car = await response.json()
    cars.value.push(createdCar)
    newMake.value = ''
    newModel.value = ''
    newLocation.value = ''
    searchQuery.value = ''
    selectedCountry.value = 'All countries'
    statusMessage.value = `${createdCar.make} ${createdCar.model} was added to the garage.`
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : 'Unable to add car.'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(loadCars)

function exploreCars(): void {
  statusMessage.value = 'The car gallery is coming next.'
}
</script>

<template>
  <main>
    <h1>{{ siteName }}</h1>
    <p>{{ tagline }}</p>

    <button type="button" @click="exploreCars">Explore</button>
    <p>{{ statusMessage }}</p>
    <form class="car-form" @submit.prevent="submitCar">
      <label for="car-make">Make</label>
      <input id="car-make" v-model="newMake" type="text" required />

      <label for="car-model">Model</label>
      <input id="car-model" v-model="newModel" type="text" required />

      <label for="car-location">Location</label>
      <input id="car-location" v-model="newLocation" type="text" required />

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? 'Adding car...' : 'Add car' }}
      </button>
      <p v-if="submitError" role="alert">Unable to add car: {{ submitError }}</p>
    </form>
    <section class="featured-section">
      <h2>Featured cars</h2>
      <label for="car-search">Search cars</label>

      <input
        id="car-search"
        v-model="searchQuery"
        type="text"
        placeholder="Search by make, model, or location"
      />

      <label for="country-filter">Country</label>

      <select id="country-filter" v-model="selectedCountry">
        <option value="All countries">All countries</option>
        <option value="New Zealand">New Zealand</option>
        <option value="Japan">Japan</option>
        <option value="Germany">Germany</option>
        <option value="USA">USA</option>
        <option value="Italy">Italy</option>
      </select>

      <p v-if="searchQuery">Search results for "{{ searchQuery }}"</p>

      <p v-if="isLoading">Loading cars...</p>
      <p v-else-if="loadError" class="empty-state">Unable to load cars: {{ loadError }}</p>
      <p v-else-if="filteredCars.length === 0" class="empty-state">
        No cars found matching your search.
      </p>
      <div class="car-grid">
        <CarCard
          v-for="car in filteredCars"
          :key="car.id"
          :make="car.make"
          :model="car.model"
          :location="car.location"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
:global(body) {
  margin: 0;
  min-width: 320px;
  background: #0b0d10;
  color: #f5f7fb;
  font-family: Arial, sans-serif;
}

main {
  max-width: 1120px;
  margin: 0 auto;
  padding: 64px 24px;
}

h1 {
  margin: 0;
  color: #e5484d;
  letter-spacing: 4px;
}
.featured-section {
  margin-top: 48px;
}
.car-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
}
</style>
