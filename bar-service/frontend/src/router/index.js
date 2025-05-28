import { createRouter, createWebHistory } from 'vue-router';
import BarList from '../views/BarList.vue';
import BarDetails from '../views/BarDetails.vue';

const routes = [
  {
    path: '/',
    name: 'BarList',
    component: BarList
  },
  {
    path: '/bar/:barId',
    name: 'BarDetails',
    component: BarDetails
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;