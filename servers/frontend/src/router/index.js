import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/auth/LoginPage.vue';
import Register from '../views/auth/RegisterPage.vue';
import BarsDashboard from '../views/bars/BarsDashboard.vue';
import BarDetailsView from '@/views/bars/BarDetailsView.vue';
import AdminDashboard from '../views/admin/ManageAccounts.vue';
import DroppointsView from '../views/droppoints/DroppointsView.vue';
import WarehouseView from '../views/warehouse/WarehouseView.vue';
import EventsListView from '../views/eventplanner/EventsListView.vue';
import EventFormView from '../views/eventplanner/EventFormView.vue';
import { useAuthStore } from '../stores/authStore';

function isAdmin(user) {
  return !!user?.isAdmin;
}

function hasService(user, serviceCode) {
  if (!user) return false;
  if (isAdmin(user)) return true;
  return Array.isArray(user.roles) && user.roles.some((r) => r?.service === serviceCode);
}

function hasManagerRole(user, serviceCode) {
  if (!user) return false;
  if (isAdmin(user)) return true;
  return Array.isArray(user.roles)
    && user.roles.some((r) => r?.service === serviceCode && r?.role === 'MANAGER');
}

function hasResourceAccess(user, serviceCode, resourceId) {
  if (!user) return false;
  if (isAdmin(user)) return true;
  if (!Array.isArray(user.roles)) return false;

  // MANAGER => all resources for that service.
  if (user.roles.some((r) => r?.service === serviceCode && r?.role === 'MANAGER')) return true;

  // OPERATOR => only assigned resource.
  return user.roles.some(
    (r) => r?.service === serviceCode && r?.role === 'OPERATOR' && String(r?.resourceId) === String(resourceId)
  );
}

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  {
    path: '/bars',
    name: 'BarsDashboard',
    component: BarsDashboard,
    meta: { requiresAuth: true, requiresService: 'BAR' },
  },
  {
    path: '/bars/:barId',
    name: 'BarDetails',
    component: BarDetailsView,
    meta: { requiresAuth: true, requiresService: 'BAR', requiresResourceParam: 'barId' }
  },
  {
    path: '/droppoints',
    name: 'Droppoints',
    component: DroppointsView,
    meta: { requiresAuth: true, requiresService: 'DROP_POINT' },
  },
  {
    path: '/warehouse',
    name: 'Warehouse',
    component: WarehouseView,
    meta: { requiresAuth: true, requiresService: 'WAREHOUSE' },
  },
  {
    path: '/events',
    name: 'Events',
    component: EventsListView,
    meta: { requiresAuth: true, requiresService: 'EVENT' },
  },
  {
    path: '/events/new',
    name: 'EventCreate',
    component: EventFormView,
    meta: { requiresAuth: true, requiresService: 'EVENT', requiresManager: true },
  },
  {
    path: '/events/:id/edit',
    name: 'EventEdit',
    component: EventFormView,
    meta: { requiresAuth: true, requiresService: 'EVENT', requiresManager: true },
  },
  {
    path: '/events/:id',
    name: 'EventDetails',
    component: EventFormView,
    meta: { requiresAuth: true, requiresService: 'EVENT' },
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

  if (to.meta.requiresAuth && !authStore.token) return next('/login');

  const user = authStore.user;

  if (to.meta.requiresAdmin && !isAdmin(user)) return next(authStore.getDefaultRoute());

  if (to.meta.requiresService && !hasService(user, to.meta.requiresService)) {
    return next(authStore.getDefaultRoute());
  }

  if (to.meta.requiresManager && !hasManagerRole(user, to.meta.requiresService)) {
    return next(authStore.getDefaultRoute());
  }

  if (to.meta.requiresResourceParam) {
    const resourceId = to.params?.[to.meta.requiresResourceParam];
    if (!hasResourceAccess(user, to.meta.requiresService, resourceId)) {
      return next(authStore.getDefaultRoute());
    }
  }

  return next();
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