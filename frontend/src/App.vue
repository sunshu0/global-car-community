<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import CarCard from './components/CarCard.vue'

interface Car {
  id: number
  make: string
  model: string
  location: string
  imageUrl: string | null
}

const cars = ref<Car[]>([])
const isLoading = ref<boolean>(true)
const loadError = ref<string>('')
const searchQuery = ref<string>('')
const selectedCountry = ref<string>('All countries')
const newMake = ref<string>('')
const newModel = ref<string>('')
const newLocation = ref<string>('')
const newImageUrl = ref<string>('')
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
    imageUrl: newImageUrl.value.trim() || null,
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
    newMake.value = ''
    newModel.value = ''
    newLocation.value = ''
    newImageUrl.value = ''
    searchQuery.value = ''
    selectedCountry.value = 'All countries'
    statusMessage.value =
      `${createdCar.make} ${createdCar.model} was submitted for review.`
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
      <label for="car-image-url">Image URL</label>
      <input
        id="car-image-url"
        v-model="newImageUrl"
        type="url"
        placeholder="https://example.com/car.jpg"
      />

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
          :image-url="car.imageUrl"
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
:global(body) {
  background: radial-gradient(circle at 80% 0%, rgb(229 72 77 / 14%), transparent 32rem), #0b0d10;
  font-family:
    Inter,
    ui-sans-serif,
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
}

main {
  padding: 72px 24px;
}

h1 {
  color: #f5f7fb;
  font-size: clamp(3rem, 10vw, 6.5rem);
  line-height: 0.9;
  letter-spacing: -0.055em;
}

main > p:first-of-type {
  max-width: 560px;
  margin: 24px 0 0;
  color: #aeb4c0;
  font-size: clamp(1.1rem, 2vw, 1.35rem);
  line-height: 1.6;
}

button,
input,
select {
  font: inherit;
}

button {
  border: 0;
  cursor: pointer;
}

main > button,
.car-form > button {
  border-radius: 10px;
  background: #e5484d;
  color: #fff;
  font-weight: 800;
  transition:
    transform 160ms ease,
    background 160ms ease;
}

main > button {
  margin-top: 30px;
  padding: 12px 18px;
}

main > button:hover,
.car-form > button:hover:not(:disabled) {
  background: #f2555a;
  transform: translateY(-1px);
}

main > p:nth-of-type(2) {
  display: inline-block;
  margin: 0 0 0 18px;
  color: #8d94a1;
  font-size: 0.9rem;
}

.car-form {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 18px;
  align-items: end;
  margin-top: 56px;
  padding: 30px;
  border: 1px solid #252a33;
  border-radius: 18px;
  background: rgb(18 21 26 / 88%);
  box-shadow: 0 24px 70px rgb(0 0 0 / 24%);
}

.car-form label {
  color: #cbd0d9;
  font-size: 0.82rem;
  font-weight: 700;
}

.car-form label[for='car-make'] {
  grid-column: 1;
  grid-row: 1;
}

.car-form label[for='car-model'] {
  grid-column: 2;
  grid-row: 1;
}

.car-form label[for='car-location'] {
  grid-column: 3;
  grid-row: 1;
}

.car-form input {
  width: 100%;
  box-sizing: border-box;
  padding: 12px 13px;
  border: 1px solid #343a45;
  border-radius: 9px;
  outline: none;
  background: #0e1116;
  color: #f5f7fb;
  transition:
    border-color 160ms ease,
    box-shadow 160ms ease;
}

.car-form input:focus {
  border-color: #e5484d;
  box-shadow: 0 0 0 3px rgb(229 72 77 / 14%);
}

#car-make {
  grid-column: 1;
  grid-row: 2;
}

#car-model {
  grid-column: 2;
  grid-row: 2;
}

#car-location {
  grid-column: 3;
  grid-row: 2;
}

.car-form > button {
  grid-column: 1 / -1;
  justify-self: start;
  min-width: 132px;
  margin-top: 10px;
  padding: 12px 22px;
}

.car-form > button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.car-form > p {
  grid-column: 1 / -1;
  margin: 0;
  color: #ff8f93;
  font-size: 0.9rem;
}

.featured-section {
  margin-top: 64px;
}

@media (max-width: 760px) {
  main {
    padding: 40px 18px;
  }

  main > p:nth-of-type(2) {
    display: block;
    margin: 12px 0 0;
  }

  .car-form {
    grid-template-columns: 1fr;
    padding: 22px;
  }

  .car-form label,
  .car-form input {
    grid-column: 1;
  }

  .car-form label[for='car-make'] {
    grid-row: 1;
  }

  #car-make {
    grid-row: 2;
  }

  .car-form label[for='car-model'] {
    grid-row: 3;
    margin-top: 6px;
  }

  #car-model {
    grid-row: 4;
  }

  .car-form label[for='car-location'] {
    grid-row: 5;
    margin-top: 6px;
  }

  #car-location {
    grid-row: 6;
  }

  .car-form > button {
    grid-row: 7;
  }
}
</style>
