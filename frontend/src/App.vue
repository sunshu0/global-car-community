<script setup lang="ts">
import { computed, ref } from 'vue'
interface Car {
  id: number
  make: string
  model: string
  location: string
}
const cars: Car[] = [
  {
    id: 1,
    make: 'Nissan',
    model: '370Z',
    location: 'Auckland, New Zealand',
  },
  {
    id: 2,
    make: 'Toyota',
    model: 'Supra',
    location: 'Tokyo, Japan',
  },
  {
    id: 3,
    make: 'Mercedes-Benz',
    model: 'A45 AMG',
    location: 'Berlin, Germany',
  },
]
const searchQuery = ref<string>('')
const filteredCars = computed<Car[]>(() => {
  const query = searchQuery.value.trim().toLowerCase()

  if (query === '') {
    return cars
  }

  return cars.filter((car) => {
    const searchableText = `${car.make} ${car.model} ${car.location}`.toLowerCase()

    return searchableText.includes(query)
  })
})
const siteName = 'WORLD GARAGE'
const tagline = 'Discover real cars from around the world'
const statusMessage = ref<string>('Ready to explore.')

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
    <section class="featured-section">
      <h2>Featured cars</h2>
      <label for="car-search">Search cars</label>

      <input
        id="car-search"
        v-model="searchQuery"
        type="text"
        placeholder="Search by make, model, or location"
      />

      <p v-if="searchQuery">Search results for "{{ searchQuery }}"</p>

      <p v-if="filteredCars.length === 0" class="empty-state">
        No cars found matching your search.
      </p>
      <div class="car-grid">
        <article v-for="car in filteredCars" :key="car.id" class="car-card">
          <h3>{{ car.make }} {{ car.model }}</h3>
          <p>{{ car.location }}</p>
        </article>
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

.car-card {
  padding: 24px;
  border: 1px solid #2a3038;
  border-radius: 16px;
  background: #15191f;
}
</style>
