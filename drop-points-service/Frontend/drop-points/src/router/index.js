import { createRouter, createWebHistory } from 'vue-router'
import Droppoint from '@/components/Droppoint.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'droppoint',
      component: Droppoint
    },
  ],
})

export default router
