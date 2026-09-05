import { createRouter, createWebHistory } from 'vue-router'
import GarageView from '../views/GarageView.vue'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/garage', name: 'garage', component: GarageView },
    {
      path: '/cars/:id',
      name: 'car-detail',
      component: () => import('../views/CarDetailView.vue'),
      props: true,
    },
    { path: '/submit', name: 'submit', component: () => import('../views/SubmitView.vue') },
    { path: '/account', name: 'account', component: () => import('../views/AccountView.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

export default router
