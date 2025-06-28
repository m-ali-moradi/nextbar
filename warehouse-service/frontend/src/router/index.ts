// import { createRouter, createWebHistory } from 'vue-router'
// import Beveragestock from '@/components/BeverageStock.vue'
// import ReplenishForm from '@/components/ReplenishForm.vue'
// import EmptyBottle from '@/components/EmptyBottle.vue'

// const routes = [
//   {
//     path: '/beveragestock',
//     name: 'BeverageStock',
//     component: Beveragestock
//   },
//   {
//     path: '/replenish',
//     name: 'ReplenishForm',
//     component: ReplenishForm
//   },
//   {
//     path: '/emptybottle',
//     name: 'EmptyBottle',
//     component: EmptyBottle
//   },
//   {
//     path: '/',
//     redirect: '/beveragestock'
//   }
// ]

// const router = createRouter({
//   history: createWebHistory(import.meta.env.BASE_URL),
//   routes
// })

// export default router

import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import BeverageStock from '../components/BeverageStock.vue';
import SupplyRequests from '../components/SupplyRequests.vue';
import DropPointDashboard from '../components/DropPointDashboard.vue';
import EmptiesCollected from '../components/EmptiesCollected.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/stock' },
  { path: '/stock', component: BeverageStock },
  { path: '/supply', component: SupplyRequests },
  { path: '/drop-points', component: DropPointDashboard },
  { path: '/empties', component: EmptiesCollected }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;