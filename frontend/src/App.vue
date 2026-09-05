<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import heroImage from './assets/world-garage-hero.jpg'
import CarCard from './components/CarCard.vue'
import AuthPanel from './components/AuthPanel.vue'
import { postWithCsrf } from './api/auth'
import type { Car } from './api/cars'

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
const statusMessage = ref<string>('')

const tagline = 'Discover real cars from around the world'

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
  statusMessage.value = ''

  try {
    const response = await postWithCsrf('/api/cars', car)

    const createdCar: Car = await response.json()
    window.dispatchEvent(new Event('world-garage:car-submitted'))

    newMake.value = ''
    newModel.value = ''
    newLocation.value = ''
    newImageUrl.value = ''
    searchQuery.value = ''
    selectedCountry.value = 'All countries'
    statusMessage.value = `${createdCar.make} ${createdCar.model} was submitted for review.`
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : 'Unable to add car.'
  } finally {
    isSubmitting.value = false
  }
}

onMounted(loadCars)
</script>

<template>
  <header class="site-header">
    <a class="wordmark" href="#top" aria-label="World Garage home">
      <span>WORLD</span>
      <span>GARAGE</span>
    </a>

    <nav aria-label="Primary navigation">
      <a href="#garage">The Garage</a>
      <a href="#submit">Submit a car</a>
      <a href="#account">Your account</a>
    </nav>

    <span class="edition-label">Independent motor culture</span>
  </header>

  <main id="top">
    <section class="hero" aria-labelledby="hero-title">
      <div class="hero-copy">
        <p class="eyebrow">Issue 001 · New Zealand</p>
        <h1 id="hero-title">Built to be driven.<br />Shared to be remembered.</h1>
        <p class="hero-intro">
          {{ tagline }}, told through the machines, places, and people that make every garage
          personal.
        </p>

        <div class="hero-actions">
          <a class="button button--primary" href="#garage">Explore the collection</a>
          <a class="button button--quiet" href="#submit">Submit your car</a>
        </div>

        <p class="issue-note">A living archive of owner-submitted cars.</p>
      </div>

      <figure class="hero-visual">
        <img
          :src="heroImage"
          alt="Blue Nissan 370Z parked against a black-and-white New Zealand landscape"
        />
        <figcaption>
          <span>Owner's spotlight</span>
          Nissan 370Z · New Zealand
        </figcaption>
      </figure>
    </section>

    <section class="editorial-strip" aria-label="World Garage principles">
      <p><span>01</span> Independent builds</p>
      <p><span>02</span> Worldwide perspective</p>
      <p><span>03</span> Reviewed submissions</p>
    </section>

    <section id="garage" class="garage-section" aria-labelledby="garage-title">
      <div class="section-heading">
        <div>
          <p class="eyebrow eyebrow--dark">The public garage</p>
          <h2 id="garage-title">Cars worth a closer look.</h2>
        </div>
        <p class="section-deck">
          Browse approved cars from the community. Search by make, model, or the place each car
          calls home.
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

    <section id="submit" class="submission-section" aria-labelledby="submission-title">
      <div class="submission-intro">
        <p class="eyebrow">Community submissions</p>
        <h2 id="submission-title">Put your car in the next issue.</h2>
        <p>
          Tell us what you drive and where it lives. Every submission enters review before it
          appears in the public garage.
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
    <AuthPanel />
  </main>

  <footer class="site-footer">
    <a class="wordmark wordmark--footer" href="#top">
      <span>WORLD</span>
      <span>GARAGE</span>
    </a>
    <p>Cars, culture, and the stories between destinations.</p>
    <p>World Garage · 2026</p>
  </footer>
</template>

<style scoped>
:global(*) {
  box-sizing: border-box;
}

:global(html) {
  scroll-behavior: smooth;
}

:global(body) {
  margin: 0;
  min-width: 320px;
  background: #0b0b0b;
  color: #f3f0e8;
  font-family: Arial, Helvetica, sans-serif;
  text-rendering: optimizeLegibility;
}

:global(button),
:global(input),
:global(select) {
  font: inherit;
}

:global(a) {
  color: inherit;
}

.site-header {
  position: relative;
  z-index: 10;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  min-height: 88px;
  padding: 18px clamp(24px, 4vw, 72px);
  border-bottom: 1px solid #282828;
  background: #0b0b0b;
}

.wordmark {
  display: inline-flex;
  flex-direction: column;
  width: max-content;
  color: #f3f0e8;
  font-size: 0.85rem;
  font-weight: 900;
  letter-spacing: 0.16em;
  line-height: 0.95;
  text-decoration: none;
}

.wordmark span:last-child {
  color: #d83a2e;
}

nav {
  display: flex;
  justify-content: center;
  gap: clamp(20px, 4vw, 54px);
}

nav a {
  color: #bdb9af;
  font-size: 0.76rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-decoration: none;
  text-transform: uppercase;
}

nav a:hover,
nav a:focus-visible {
  color: #fff;
}

.edition-label {
  color: #77746d;
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero {
  display: grid;
  grid-template-columns: minmax(350px, 0.72fr) minmax(0, 1.28fr);
  min-height: calc(100vh - 88px);
  background: #0b0b0b;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: clamp(56px, 8vw, 126px) clamp(28px, 5vw, 84px);
}

.eyebrow {
  margin: 0 0 24px;
  color: #d83a2e;
  font-size: 0.7rem;
  font-weight: 900;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

h1,
h2,
p {
  margin-top: 0;
}

h1,
h2 {
  font-family: Georgia, 'Times New Roman', serif;
  font-weight: 500;
}

h1 {
  max-width: 700px;
  margin-bottom: 30px;
  font-size: clamp(3.3rem, 6vw, 7.4rem);
  letter-spacing: -0.065em;
  line-height: 0.88;
}

.hero-intro {
  max-width: 580px;
  margin-bottom: 36px;
  color: #aaa69d;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(1rem, 1.4vw, 1.24rem);
  line-height: 1.65;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 18px;
}

.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 50px;
  padding: 0 24px;
  font-size: 0.73rem;
  font-weight: 900;
  letter-spacing: 0.11em;
  text-decoration: none;
  text-transform: uppercase;
  transition: 160ms ease;
}

.button--primary {
  background: #d83a2e;
  color: #fff;
}

.button--primary:hover,
.button--primary:focus-visible {
  background: #ef4a3c;
  transform: translateY(-2px);
}

.button--quiet {
  border-bottom: 1px solid #5e5b55;
  color: #d6d2c9;
}

.button--quiet:hover,
.button--quiet:focus-visible {
  border-color: #fff;
  color: #fff;
}

.issue-note {
  margin: clamp(48px, 10vh, 110px) 0 0;
  color: #64615b;
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.13em;
  text-transform: uppercase;
}

.hero-visual {
  position: relative;
  min-width: 0;
  min-height: 620px;
  margin: 0;
  overflow: hidden;
  background: #161616;
}

.hero-visual::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgb(11 11 11 / 24%), transparent 24%);
  content: '';
  pointer-events: none;
}

.hero-visual img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: 53% center;
}

.hero-visual figcaption {
  position: absolute;
  right: 28px;
  bottom: 28px;
  z-index: 1;
  padding: 14px 18px;
  background: rgb(8 8 8 / 84%);
  color: #d8d4cb;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-visual figcaption span {
  display: block;
  margin-bottom: 6px;
  color: #d83a2e;
  font-weight: 900;
}

.editorial-strip {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-top: 1px solid #272727;
  border-bottom: 1px solid #272727;
  background: #111;
}

.editorial-strip p {
  margin: 0;
  padding: 26px clamp(24px, 4vw, 70px);
  color: #9b978e;
  font-size: 0.7rem;
  font-weight: 800;
  letter-spacing: 0.13em;
  text-transform: uppercase;
}

.editorial-strip p + p {
  border-left: 1px solid #272727;
}

.editorial-strip span {
  margin-right: 12px;
  color: #d83a2e;
}

.garage-section {
  padding: clamp(80px, 10vw, 150px) clamp(24px, 6vw, 110px);
  background: #eeeae0;
  color: #161616;
}

.section-heading {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.6fr);
  gap: 60px;
  align-items: end;
  max-width: 1500px;
  margin: 0 auto 64px;
}

.eyebrow--dark {
  color: #b82c22;
}

.section-heading h2,
.submission-intro h2 {
  margin-bottom: 0;
  font-size: clamp(3rem, 6.6vw, 7.4rem);
  letter-spacing: -0.06em;
  line-height: 0.9;
}

.section-deck {
  margin-bottom: 4px;
  color: #5d5952;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 1.04rem;
  line-height: 1.7;
}

.filters {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(190px, 0.35fr) auto;
  gap: 18px;
  align-items: end;
  max-width: 1500px;
  margin: 0 auto 34px;
  padding: 24px 0;
  border-top: 1px solid #bdb8ad;
  border-bottom: 1px solid #bdb8ad;
}

.field {
  display: grid;
  gap: 9px;
}

.field label,
.form-field label {
  font-size: 0.68rem;
  font-weight: 900;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.field input,
.field select {
  width: 100%;
  min-height: 48px;
  border: 0;
  border-bottom: 1px solid #777168;
  border-radius: 0;
  outline: none;
  background: transparent;
  color: #161616;
}

.field input:focus,
.field select:focus {
  border-color: #b82c22;
}

.result-count {
  justify-self: end;
  margin: 0 0 15px;
  color: #706b62;
  font-size: 0.68rem;
  font-weight: 800;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.search-summary {
  max-width: 1500px;
  margin: 0 auto 24px;
  color: #6d685f;
  font-family: Georgia, 'Times New Roman', serif;
}

.empty-state {
  max-width: 1500px;
  margin: 50px auto;
  padding: 70px 24px;
  border: 1px solid #bdb8ad;
  color: #625e56;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 1.15rem;
  text-align: center;
}

.car-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: clamp(30px, 4vw, 68px) clamp(18px, 2.5vw, 42px);
  max-width: 1500px;
  margin: 0 auto;
}

.submission-section {
  display: grid;
  grid-template-columns: minmax(300px, 0.8fr) minmax(0, 1.2fr);
  gap: clamp(50px, 8vw, 140px);
  padding: clamp(80px, 10vw, 150px) clamp(24px, 7vw, 130px);
  background: #121212;
}

.submission-intro h2 {
  margin-bottom: 30px;
  font-size: clamp(3rem, 5.5vw, 6.5rem);
}

.submission-intro > p:not(.eyebrow) {
  max-width: 590px;
  color: #aaa69d;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 1.08rem;
  line-height: 1.7;
}

.review-note {
  max-width: 540px;
  margin-top: 48px;
  padding: 22px 0;
  border-top: 1px solid #373737;
  border-bottom: 1px solid #373737;
  color: #89857d;
  font-size: 0.78rem;
  line-height: 1.6;
}

.review-note span {
  display: block;
  margin-bottom: 8px;
  color: #d83a2e;
  font-size: 0.66rem;
  font-weight: 900;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.car-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 34px 28px;
  align-content: start;
  padding: clamp(28px, 4vw, 58px);
  border: 1px solid #303030;
  background: #171717;
}

.form-field {
  display: grid;
  gap: 12px;
}

.form-field:nth-child(3),
.form-field:nth-child(4) {
  grid-column: 1 / -1;
}

.form-field label {
  color: #d0ccc3;
}

.form-field label span {
  color: #68655f;
  font-weight: 600;
}

.form-field input {
  width: 100%;
  min-height: 52px;
  padding: 0;
  border: 0;
  border-bottom: 1px solid #4a4844;
  border-radius: 0;
  outline: none;
  background: transparent;
  color: #fff;
  transition: border-color 160ms ease;
}

.form-field input::placeholder {
  color: #65625c;
}

.form-field input:focus {
  border-color: #d83a2e;
}

.form-footer {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 18px;
  margin-top: 8px;
}

.form-footer button {
  min-height: 52px;
  padding: 0 26px;
  border: 0;
  border-radius: 0;
  background: #d83a2e;
  color: #fff;
  cursor: pointer;
  font-size: 0.72rem;
  font-weight: 900;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.form-footer button:hover:not(:disabled),
.form-footer button:focus-visible:not(:disabled) {
  background: #ef4a3c;
}

.form-footer button:disabled {
  cursor: wait;
  opacity: 0.55;
}

.form-terms {
  margin: 0;
  color: #6f6b64;
  font-size: 0.7rem;
}

.form-message {
  grid-column: 1 / -1;
  margin: 0;
  padding-top: 18px;
  border-top: 1px solid #353535;
  font-size: 0.82rem;
}

.form-message--error {
  color: #ff8278;
}

.form-message--success {
  color: #9ccc8f;
}

.site-footer {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: end;
  gap: 40px;
  padding: 52px clamp(24px, 6vw, 110px);
  border-top: 1px solid #292929;
  background: #0b0b0b;
  color: #68655f;
  font-size: 0.7rem;
}

.site-footer p {
  margin: 0;
}

.site-footer p:nth-child(2) {
  text-align: center;
}

.wordmark--footer {
  font-size: 0.72rem;
}

@media (max-width: 1100px) {
  .hero {
    grid-template-columns: minmax(320px, 0.85fr) 1.15fr;
  }

  .hero-copy {
    padding-right: 36px;
    padding-left: 36px;
  }

  .car-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .submission-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 780px) {
  .site-header {
    grid-template-columns: 1fr auto;
    min-height: 76px;
  }

  nav {
    justify-content: flex-end;
    gap: 18px;
  }

  .edition-label {
    display: none;
  }

  .hero {
    display: flex;
    flex-direction: column-reverse;
    min-height: auto;
  }

  .hero-visual {
    min-height: 54vh;
  }

  .hero-copy {
    padding: 60px 24px 70px;
  }

  h1 {
    font-size: clamp(3.4rem, 14vw, 5.7rem);
  }

  .issue-note {
    margin-top: 50px;
  }

  .editorial-strip {
    grid-template-columns: 1fr;
  }

  .editorial-strip p + p {
    border-top: 1px solid #272727;
    border-left: 0;
  }

  .section-heading {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .result-count {
    justify-self: start;
    margin-top: 8px;
  }

  .car-grid {
    grid-template-columns: 1fr;
  }

  .car-form {
    grid-template-columns: 1fr;
  }

  .form-field:nth-child(n) {
    grid-column: 1;
  }

  .site-footer {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .site-footer p:nth-child(2) {
    text-align: left;
  }
}

@media (max-width: 520px) {
  .site-header {
    padding: 16px 18px;
  }

  nav a:first-child {
    display: none;
  }

  .hero-visual {
    min-height: 44vh;
  }

  .hero-visual figcaption {
    right: 14px;
    bottom: 14px;
    left: 14px;
  }

  .hero-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .button {
    width: 100%;
  }

  .garage-section,
  .submission-section {
    padding-right: 18px;
    padding-left: 18px;
  }

  .section-heading h2,
  .submission-intro h2 {
    font-size: clamp(3rem, 15vw, 4.6rem);
  }
}
</style>
