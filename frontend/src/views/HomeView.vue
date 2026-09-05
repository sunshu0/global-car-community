<script setup lang="ts">
import { RouterLink } from 'vue-router'
import heroImage from '../assets/world-garage-hero.jpg'
import GarageView from './GarageView.vue'

const tagline = 'Discover real cars from around the world'
</script>

<template>
  <section class="hero" aria-labelledby="hero-title">
    <div class="hero-copy">
      <p class="eyebrow">Issue 001 · New Zealand</p>
      <h1 id="hero-title" class="publication-title">
        <span>World</span>
        <span>Garage</span>
      </h1>
      <p class="hero-headline">Built to be driven.<br />Shared to be remembered.</p>
      <p class="hero-intro">
        {{ tagline }}, told through the machines, places, and people that make every garage
        personal.
      </p>
      <div class="hero-actions">
        <a class="button button--primary" href="#garage">Explore the collection</a>
        <RouterLink class="button button--quiet" to="/submit">Submit your car</RouterLink>
      </div>
      <p class="issue-note">A living archive of owner-submitted cars.</p>
    </div>

    <figure class="hero-visual">
      <img
        :src="heroImage"
        alt="Blue Nissan 370Z parked against a black-and-white New Zealand landscape"
      />
      <figcaption><span>Owner's spotlight</span>Nissan 370Z · New Zealand</figcaption>
    </figure>
  </section>

  <section class="editorial-strip" aria-label="World Garage principles">
    <p><span>01</span> Independent builds</p>
    <p><span>02</span> Worldwide perspective</p>
    <p><span>03</span> Reviewed submissions</p>
  </section>

  <GarageView />
</template>
