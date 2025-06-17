import { createRouter, createWebHistory } from 'vue-router'
import Beveragestock from '@/components/BeverageStock.vue'
import ReplenishForm from '@/components/ReplenishForm.vue'
import EmptyBottle from '@/components/EmptyBottle.vue'

const routes = [
  {
    path: '/beveragestock',
    name: 'BeverageStock',
    component: Beveragestock
  },
  {
    path: '/replenish',
    name: 'ReplenishForm',
    component: ReplenishForm
  },
  {
    path: '/emptybottle',
    name: 'EmptyBottle',
    component: EmptyBottle
  },
  {
    path: '/',
    redirect: '/beveragestock'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
