<script setup lang="ts">
import { computed, ref } from 'vue'
import CarCard from './components/CarCard.vue'
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
  {
    id: 4,
    make: 'Ford',
    model: 'Mustang GT',
    location: 'Los Angeles, USA',
  },
  {
    id: 5,
    make: 'Chevrolet',
    model: 'Camaro SS',
    location: 'Detroit, USA',
  },
  {
    id: 6,
    make: 'BMW',
    model: 'M3',
    location: 'Munich, Germany',
  },
  {
    id: 7,
    make: 'Audi',
    model: 'RS5',
    location: 'Ingolstadt, Germany',
  },
  {
    id: 8,
    make: 'Porsche',
    model: '911 Carrera S',
    location: 'Stuttgart, Germany',
  },
  {
    id: 9,
    make: 'Lamborghini',
    model: 'Huracan EVO',
    location: "Sant'Agata Bolognese, Italy",
  },
  {
    id: 10,
    make: 'Ferrari',
    model: '488 Pista',
    location: 'Maranello, Italy',
  },
]
const searchQuery = ref<string>('')
const selectedCountry = ref<string>('All countries')
const filteredCars = computed<Car[]>(() => {
  const query = searchQuery.value.trim().toLowerCase()

  return cars.filter((car) => {
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

      <p v-if="filteredCars.length === 0" class="empty-state">
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
