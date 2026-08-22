<script setup lang="ts">
import { ref } from 'vue'
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
  </main>
</template>

<style scoped>
h1 {
  color: #e5484d;
}
</style>
