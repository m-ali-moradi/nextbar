import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import {
  isAdmin,
  hasService,
  hasManagerRole,
  hasAnyManagerRole,
  canAccessDroppoints,
  hasResourceAccess,
} from '@/composables/useAccessControl';

// Auth pages are eagerly loaded (entry points)
import LoginPage from '@/views/auth/LoginPage.vue';

// All other views are lazy-loaded for code splitting
const AppLayout = () => import('@/views/AppLayout.vue');
const BarsDashboard = () => import('@/views/bars/BarsDashboard.vue');
const BarDetailsView = () => import('@/views/bars/BarDetailsView.vue');
const DroppointsView = () => import('@/views/droppoints/DroppointsView.vue');
const AdminLayout = () => import('@/views/admin/AdminLayout.vue');
const ManageAccounts = () => import('@/views/admin/ManageAccounts.vue');
const RolesManagement = () => import('@/views/admin/RolesManagement.vue');
const WarehouseLayout = () => import('@/views/warehouse/WarehouseLayout.vue');
const StockView = () => import('@/views/warehouse/StockView.vue');
const SupplyRequestsView = () => import('@/views/warehouse/SupplyRequestsView.vue');
const CollectionsView = () => import('@/views/warehouse/CollectionsView.vue');
const EventsListView = () => import('@/views/eventplanner/EventsListView.vue');
const EventFormView = () => import('@/views/eventplanner/EventFormView.vue');
const EventDetailsView = () => import('@/views/eventplanner/EventDetailsView.vue');
const ProfileView = () => import('@/views/ProfileView.vue');

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean;
    requiresAdmin?: boolean;
    requiresService?: string;
    requiresManager?: boolean;
    requiresAnyManagerServices?: string[];
    requiresDroppointsAccess?: boolean;
    requiresResourceParam?: string;
  }
}

/**
 * Application router with route definitions and global guards for authentication and access control.
 */
const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: LoginPage },
  {
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: () => {
          const authStore = useAuthStore();
          authStore.syncUserFromToken();
          if (!authStore.token) return '/login';
          return authStore.getDefaultRoute();
        },
      },
      {
        path: 'bars',
        name: 'BarsDashboard',
        component: BarsDashboard,
        meta: { requiresService: 'BAR' },
      },
      {
        path: 'bars/:barId',
        name: 'BarDetails',
        component: BarDetailsView,
        meta: { requiresService: 'BAR', requiresResourceParam: 'barId' },
      },
      {
        path: 'droppoints',
        name: 'Droppoints',
        component: DroppointsView,
        meta: { requiresDroppointsAccess: true },
      },
      {
        path: 'warehouse',
        component: WarehouseLayout,
        meta: { requiresService: 'WAREHOUSE' },
        children: [
          { path: '', redirect: { name: 'WarehouseStock' } },
          { path: 'stock', name: 'WarehouseStock', component: StockView },
          { path: 'supply', name: 'WarehouseSupply', component: SupplyRequestsView },
          { path: 'collections', name: 'WarehouseCollections', component: CollectionsView },
        ],
      },
      {
        path: 'events',
        name: 'Events',
        component: EventsListView,
        meta: { requiresService: 'EVENT', requiresManager: true },
      },
      {
        path: 'events/new',
        name: 'EventCreate',
        component: EventFormView,
        meta: { requiresService: 'EVENT', requiresManager: true },
      },
      {
        path: 'events/:id/edit',
        name: 'EventEdit',
        component: EventFormView,
        meta: { requiresService: 'EVENT', requiresManager: true },
      },
      {
        path: 'events/:id',
        name: 'EventDetails',
        component: EventDetailsView,
        meta: { requiresService: 'EVENT', requiresManager: true },
      },
      {
        path: 'admin',
        component: AdminLayout,
        meta: { requiresAdmin: true },
        children: [
          { path: '', redirect: { name: 'AdminUsers' } },
          { path: 'users', name: 'AdminUsers', component: ManageAccounts },
          { path: 'roles', name: 'AdminRoles', component: RolesManagement },
        ],
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfileView,
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore();
  authStore.syncUserFromToken();

  if (to.meta.requiresAuth && !authStore.token) return next('/login');

  const user = authStore.user;

  if (to.meta.requiresAdmin && !isAdmin(user)) return next(authStore.getDefaultRoute());

  if (to.meta.requiresService && !hasService(user, to.meta.requiresService)) {
    return next(authStore.getDefaultRoute());
  }

  if (to.meta.requiresManager && !hasManagerRole(user, to.meta.requiresService as string)) {
    return next(authStore.getDefaultRoute());
  }

  if (to.meta.requiresAnyManagerServices && !hasAnyManagerRole(user, to.meta.requiresAnyManagerServices)) {
    return next(authStore.getDefaultRoute());
  }

  if (to.meta.requiresDroppointsAccess && !canAccessDroppoints(user)) {
    return next(authStore.getDefaultRoute());
  }

  if (to.meta.requiresResourceParam) {
    const resourceId = to.params?.[to.meta.requiresResourceParam];
    if (!hasResourceAccess(user, to.meta.requiresService as string, resourceId as string)) {
      return next(authStore.getDefaultRoute());
    }
  }

  return next();
});

export default router;
