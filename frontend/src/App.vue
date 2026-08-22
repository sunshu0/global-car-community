<script setup lang="ts">
const siteName: string = 'WORLD GARAGE'
const tagline: string = 'Discover real cars from around the world'
</script>

<template>
  <main>
    <h1>{{ siteName }}</h1>
    <p>{{ tagline }}</p>
  </main>
</template>

<style scoped>
h1 {
  color: #e5484d;
}
</style>
