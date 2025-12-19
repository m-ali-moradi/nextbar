import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/auth/LoginPage.vue';
import Register from '../views/auth/RegisterPage.vue';
import BarsDashboard from '../views/bars/BarsDashboard.vue';
import BarDetailsView from '@/views/bars/BarDetailsView.vue';
import AdminDashboard from '../views/admin/AdminDashboard.vue';
import DroppointsView from '../views/droppoints/DroppointsView.vue';
import WarehouseView from '../views/warehouse/WarehouseView.vue';
import EventsListView from '../views/eventplanner/EventsListView.vue';
import EventFormView from '../views/eventplanner/EventFormView.vue';
import { useAuthStore } from '../stores/authStore';

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  {
    path: '/bars',
    name: 'BarsDashboard',
    component: BarsDashboard,
    meta: { requiresAuth: true },
  },
  {
    path: '/bars/:barId',
    name: 'BarDetails',
    component: BarDetailsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/droppoints',
    name: 'Droppoints',
    component: DroppointsView,
    meta: { requiresAuth: true },
  },
  {
    path: '/warehouse',
    name: 'Warehouse',
    component: WarehouseView,
    meta: { requiresAuth: true },
  },
  {
    path: '/events',
    name: 'Events',
    component: EventsListView,
    meta: { requiresAuth: true },
  },
  {
    path: '/events/new',
    name: 'EventCreate',
    component: EventFormView,
    meta: { requiresAuth: true },
  },
  {
    path: '/events/:id/edit',
    name: 'EventEdit',
    component: EventFormView,
    meta: { requiresAuth: true },
  },
  {
    path: '/events/:id',
    name: 'EventDetails',
    component: EventFormView,
    meta: { requiresAuth: true },
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  { path: '/', redirect: '/bars' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.token) {
    next('/login');
  } else if (to.meta.requiresAdmin && !authStore.user?.isAdmin) {
    next('/bars');
  } else {
    next();
  }
});

export default router;



// import { createRouter, createWebHistory } from 'vue-router';
// import BarList from '../views/BarList.vue';
// import BarDetails from '../views/BarDetails.vue';
//
// const routes = [
//   {
//     path: '/',
//     name: 'BarList',
//     component: BarList
//   },
//   {
//     path: '/bar/:barId',
//     name: 'BarDetails',
//     component: BarDetails
//   }
// ];
//
// const router = createRouter({
//   history: createWebHistory(),
//   routes
// });
//
// export default router;