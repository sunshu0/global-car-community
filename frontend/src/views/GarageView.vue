<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { Car } from '../api/cars'
import CarCard from '../components/CarCard.vue'

const cars = ref<Car[]>([])
const isLoading = ref(true)
const loadError = ref('')
const searchQuery = ref('')
const selectedCountry = ref('All countries')

const filteredCars = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  return cars.value.filter((car) => {
    const searchableText = `${car.make} ${car.model} ${car.location}`.toLowerCase()
    const matchesSearch = query === '' || searchableText.includes(query)
    const matchesCountry =
      selectedCountry.value === 'All countries' || car.location.endsWith(selectedCountry.value)

    return matchesSearch && matchesCountry
  })
})

async function loadCars(): Promise<void> {
  try {
    const response = await fetch('/api/cars')
    if (!response.ok) throw new Error(`Request failed with status ${response.status}`)
    cars.value = await response.json()
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : 'Unable to load cars.'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadCars)
</script>

<template>
  <section id="garage" class="garage-section" aria-labelledby="garage-title">
    <div class="section-heading">
      <div>
        <p class="eyebrow eyebrow--dark">The public garage</p>
        <h2 id="garage-title">Cars worth a closer look.</h2>
      </div>
      <p class="section-deck">
        Browse approved cars from the community. Search by make, model, or the place each car calls
        home.
      </p>
    </div>

    <div class="filters" aria-label="Filter cars">
      <div class="field field--search">
        <label for="car-search">Search the archive</label>
        <input
          id="car-search"
          v-model="searchQuery"
          type="search"
          placeholder="Make, model, or location"
        />
      </div>
      <div class="field field--country">
        <label for="country-filter">Country</label>
        <select id="country-filter" v-model="selectedCountry">
          <option value="All countries">All countries</option>
          <option value="New Zealand">New Zealand</option>
          <option value="Japan">Japan</option>
          <option value="Germany">Germany</option>
          <option value="USA">USA</option>
          <option value="Italy">Italy</option>
        </select>
      </div>
      <p class="result-count" aria-live="polite">
        {{ isLoading ? 'Loading archive' : `${filteredCars.length} cars on display` }}
      </p>
    </div>

    <p v-if="searchQuery" class="search-summary">Search results for "{{ searchQuery }}"</p>
    <p v-if="isLoading" class="empty-state">Loading cars...</p>
    <p v-else-if="loadError" class="empty-state">Unable to load cars: {{ loadError }}</p>
    <p v-else-if="filteredCars.length === 0" class="empty-state">
      No cars found matching your search.
    </p>
    <div v-else class="car-grid">
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
</template>
