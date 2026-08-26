<script setup lang="ts">
defineProps<{
  make: string
  model: string
  location: string
  imageUrl: string | null
}>()
</script>

<template>
  <article class="car-card">
    <img
      v-if="imageUrl"
      class="car-image"
      :src="imageUrl"
      :alt="`${make} ${model}`"
      loading="lazy"
    />
    <div v-else class="car-image-placeholder">No image available</div>

    <h3>{{ make }} {{ model }}</h3>
    <p>{{ location }}</p>
  </article>
</template>

<style scoped>
.car-card {
  min-width: 0;
  overflow: hidden;
  border: 1px solid #2a3038;
  border-radius: 16px;
  background: #15191f;
}

.car-image,
.car-image-placeholder {
  display: block;
  width: 100%;
  aspect-ratio: 16 / 10;
}

.car-image {
  object-fit: cover;
}

.car-image-placeholder {
  display: grid;
  place-items: center;
  box-sizing: border-box;
  padding: 24px;
  background: linear-gradient(135deg, #1d222a, #101318);
  color: #747c89;
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h3 {
  margin: 20px 22px 0;
  font-size: 1.15rem;
}

p {
  margin: 8px 22px 22px;
  color: #aeb4c0;
  line-height: 1.5;
}
</style>
