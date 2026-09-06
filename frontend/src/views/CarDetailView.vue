<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getCarById, type Car } from '../api/cars'

const props = defineProps<{ id: string }>()

const car = ref<Car | null>(null)
const isLoading = ref(true)
const error = ref('')

async function loadCar(): Promise<void> {
  const carId = Number(props.id)

  if (!Number.isInteger(carId) || carId < 1) {
    error.value = 'This car is not available in the public garage.'
    isLoading.value = false
    return
  }

  try {
    car.value = await getCarById(carId)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'Unable to load this car.'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadCar)
</script>

<template>
  <section class="car-detail">
    <p v-if="isLoading" class="detail-state">Loading garage entry...</p>

    <div v-else-if="error" class="detail-state detail-state--error">
      <p class="eyebrow">Garage entry unavailable</p>
      <h1>Nothing to display here.</h1>
      <p>{{ error }}</p>
      <RouterLink class="detail-link" to="/garage">Return to the garage</RouterLink>
    </div>

    <article v-else-if="car" class="detail-layout">
      <div class="detail-copy">
        <RouterLink class="detail-back" to="/garage">← The public garage</RouterLink>
        <p class="eyebrow">Community collection · Entry {{ car.id }}</p>
        <h1>
          {{ car.make }} <em>{{ car.model }}</em>
        </h1>
        <p class="detail-location">{{ car.location }}</p>
        <p class="detail-intro">
          An approved submission
          <template v-if="car.owner">from {{ car.owner.displayName }}</template>
          <template v-else>from the World Garage archive</template>.
        </p>
        <dl class="detail-facts">
          <div>
            <dt>Owner</dt>
            <dd>
              <RouterLink v-if="car.owner" class="owner-link" :to="`/users/${car.owner.id}`">
                {{ car.owner.displayName }} →
              </RouterLink>
              <template v-else>Archive entry</template>
            </dd>
          </div>
          <div>
            <dt>Make</dt>
            <dd>{{ car.make }}</dd>
          </div>
          <div>
            <dt>Model</dt>
            <dd>{{ car.model }}</dd>
          </div>
          <div>
            <dt>Location</dt>
            <dd>{{ car.location }}</dd>
          </div>
        </dl>
      </div>

      <figure class="detail-visual">
        <img v-if="car.imageUrl" :src="car.imageUrl" :alt="`${car.make} ${car.model}`" />
        <div v-else class="detail-placeholder">
          <span>World Garage</span>
          Image story coming soon
        </div>
        <figcaption>Owner-submitted garage entry</figcaption>
      </figure>
    </article>
  </section>
</template>

<style scoped>
.car-detail {
  min-height: calc(100vh - 88px);
  padding: clamp(42px, 6vw, 90px) clamp(24px, 6vw, 110px);
  background: #eeeae0;
  color: #171717;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(300px, 0.72fr) minmax(0, 1.28fr);
  gap: clamp(45px, 7vw, 120px);
  max-width: 1500px;
  margin: 0 auto;
}

.detail-copy {
  align-self: center;
}

.detail-back,
.detail-link {
  display: inline-block;
  margin-bottom: clamp(52px, 9vw, 120px);
  border-bottom: 1px solid #8e887d;
  color: #5b564e;
  font-size: 0.68rem;
  font-weight: 900;
  letter-spacing: 0.13em;
  text-decoration: none;
  text-transform: uppercase;
}

.detail-copy h1,
.detail-state h1 {
  margin: 0 0 24px;
  font-size: clamp(4rem, 7vw, 8.5rem);
  letter-spacing: -0.07em;
  line-height: 0.82;
}

.detail-copy h1 em {
  display: block;
  color: #b82c22;
  font-weight: 400;
}

.detail-location {
  margin-bottom: 28px;
  color: #8f2b23;
  font-size: 0.72rem;
  font-weight: 900;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.detail-intro {
  max-width: 490px;
  color: #5f5a52;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 1.05rem;
  line-height: 1.7;
}

.detail-facts {
  margin: 45px 0 0;
  border-top: 1px solid #bdb8ad;
}

.detail-facts div {
  display: grid;
  grid-template-columns: 110px 1fr;
  padding: 15px 0;
  border-bottom: 1px solid #cbc6bb;
}

.detail-facts dt {
  color: #8b857b;
  font-size: 0.65rem;
  font-weight: 900;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.detail-facts dd {
  margin: 0;
}

.owner-link {
  color: #8f2b23;
  font-weight: 700;
  text-decoration-thickness: 1px;
  text-underline-offset: 4px;
}

.detail-visual {
  position: relative;
  min-height: 660px;
  margin: 0;
  background: #d6d1c7;
}

.detail-visual img,
.detail-placeholder {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 660px;
}

.detail-visual img {
  object-fit: cover;
}

.detail-placeholder {
  display: grid;
  place-content: center;
  gap: 12px;
  background:
    linear-gradient(135deg, transparent 49.8%, rgb(255 255 255 / 40%) 50%, transparent 50.2%),
    #d7d2c8;
  color: #676158;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-align: center;
  text-transform: uppercase;
}

.detail-placeholder span {
  color: #a12b23;
}

.detail-visual figcaption {
  position: absolute;
  right: 18px;
  bottom: 18px;
  padding: 12px 15px;
  background: #111;
  color: #f1eee5;
  font-size: 0.62rem;
  font-weight: 900;
  letter-spacing: 0.13em;
  text-transform: uppercase;
}

.detail-state {
  max-width: 850px;
  margin: clamp(80px, 14vh, 180px) auto;
  font-family: Georgia, 'Times New Roman', serif;
  text-align: center;
}

.detail-state--error .detail-link {
  margin: 30px 0 0;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-back {
    margin-bottom: 52px;
  }

  .detail-visual,
  .detail-visual img,
  .detail-placeholder {
    min-height: 55vh;
  }
}
</style>
