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