<script setup lang="ts">
defineProps<{
  make: string
  model: string
  location: string
}>()
</script>

<template>
  <article class="car-card">
    <h3>{{ make }} {{ model }}</h3>
    <p>{{ location }}</p>
  </article>
</template>

<style scoped>
.car-card {
  padding: 24px;
  border: 1px solid #2a3038;
  border-radius: 16px;
  background: #15191f;
}
</style>
