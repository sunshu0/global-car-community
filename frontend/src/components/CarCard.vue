<script setup lang="ts">
import { RouterLink } from 'vue-router'

defineProps<{
  id: number
  make: string
  model: string
  location: string
  imageUrl: string | null
}>()
</script>

<template>
  <RouterLink class="car-card-link" :to="`/cars/${id}`" :aria-label="`View ${make} ${model}`">
    <article class="car-card">
      <div class="car-media">
        <img
          v-if="imageUrl"
          class="car-image"
          :src="imageUrl"
          :alt="`${make} ${model}`"
          loading="lazy"
        />
        <div v-else class="car-image-placeholder">
          <span>World Garage</span>
          No image available
        </div>

        <span class="card-label">Garage entry</span>
      </div>

      <div class="card-copy">
        <p class="car-location">{{ location }}</p>
        <h3>
          {{ make }} <em>{{ model }}</em>
        </h3>
        <p class="card-note">Open garage entry →</p>
      </div>
    </article>
  </RouterLink>
</template>

<style scoped>
.car-card-link {
  display: block;
  color: inherit;
  text-decoration: none;
}

.car-card-link:focus-visible {
  outline: 3px solid #b82c22;
  outline-offset: 6px;
}

.car-card {
  min-width: 0;
  color: #171717;
}

.car-media {
  position: relative;
  overflow: hidden;
  background: #d4d0c7;
}

.car-image,
.car-image-placeholder {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
}

.car-image {
  object-fit: cover;
  filter: saturate(0.9) contrast(1.04);
  transition:
    transform 500ms cubic-bezier(0.2, 0.75, 0.3, 1),
    filter 300ms ease;
}

.car-card-link:hover .car-image {
  filter: saturate(1.08) contrast(1.03);
  transform: scale(1.025);
}

.car-image-placeholder {
  display: grid;
  place-content: center;
  gap: 8px;
  padding: 28px;
  background:
    linear-gradient(135deg, transparent 49.5%, rgb(255 255 255 / 35%) 50%, transparent 50.5%),
    #d7d2c8;
  color: #68635a;
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-align: center;
  text-transform: uppercase;
}

.car-image-placeholder span {
  color: #a12b23;
  font-size: 0.62rem;
  letter-spacing: 0.18em;
}

.card-label {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 8px 10px;
  background: #111;
  color: #f1eee5;
  font-size: 0.6rem;
  font-weight: 900;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.card-copy {
  padding-top: 20px;
  border-top: 4px solid #151515;
}

.car-location {
  margin: 0 0 10px;
  color: #8f2b23;
  font-size: 0.64rem;
  font-weight: 900;
  letter-spacing: 0.13em;
  text-transform: uppercase;
}

h3 {
  margin: 0;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(1.65rem, 2.4vw, 2.7rem);
  font-weight: 500;
  letter-spacing: -0.045em;
  line-height: 1;
}

h3 em {
  font-weight: 400;
}

.card-note {
  margin: 16px 0 0;
  padding-top: 13px;
  border-top: 1px solid #c6c1b6;
  color: #78736a;
  font-size: 0.62rem;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}
</style>
