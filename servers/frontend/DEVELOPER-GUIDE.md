# Developer Guide - Unified Frontend

## Project Structure

```
bar-service/frontend/src/
├── api/                    # API service layer
│   ├── index.ts           # Main API service & interceptors
│   ├── authApi.ts         # Authentication endpoints
│   ├── barApi.ts          # Bar management endpoints
│   ├── droppointApi.ts    # Drop point endpoints
│   ├── warehouseApi.ts    # Warehouse endpoints
│   └── eventApi.ts        # Event planner endpoints
│
├── stores/                # Pinia stores (Composition API)
│   ├── authStore.ts       # Authentication state
│   ├── barStore.ts        # Bar management state
│   ├── droppointStore.ts  # Drop point state
│   ├── warehouseStore.ts  # Warehouse state
│   └── eventStore.ts      # Event planner state
│
├── views/                 # Page components
│   ├── auth/             # Login & Registration
│   ├── bars/             # Bar management pages
│   ├── droppoints/       # Drop point management
│   ├── warehouse/        # Warehouse management
│   ├── eventplanner/     # Event planning pages
│   └── admin/            # Admin dashboard
│
├── components/           # Reusable components
│   └── common/
│       └── Sidebar.vue   # Main navigation sidebar
│
├── router/              # Vue Router configuration
│   └── index.js
│
└── main.ts             # Application entry point
```

## Running the Application

### Development
```bash
cd bar-service/frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`

### Build for Production
```bash
npm run build
```

### Run Tests
```bash
npm run test:unit
```

## Module Overview

### 1. Drop Points Module

**Routes:**
- `/droppoints` - Manage drop points

**Key Features:**
- Create/Edit/Delete drop points
- Add empty bottles to drop points
- Notify warehouse when full
- Verify transfer to warehouse

**Store:** `useDroppointStore`
**API:** `droppointApi`

### 2. Warehouse Module

**Routes:**
- `/warehouse` - Warehouse management dashboard

**Key Features:**
- View and replenish stock inventory
- Manage supply requests from bars
- Track empties collected from drop points
- Process, deliver, or reject supply requests

**Store:** `useWarehouseStore`
**API:** `warehouseApi`

### 3. Event Planner Module

**Routes:**
- `/events` - List all events
- `/events/new` - Create new event
- `/events/:id/edit` - Edit existing event
- `/events/:id` - View event details

**Key Features:**
- Create/Edit/Delete events
- Select beverages for events
- Configure multiple bars with stock
- Add multiple drop points per event

**Store:** `useEventStore`
**API:** `eventApi`

### 4. Bar Module (Existing)

**Routes:**
- `/bars` - List all bars
- `/bars/:barId` - Bar details and management

**Store:** `useBarStore`
**API:** `barApi`

## Development Patterns

### Creating a New API Service

```typescript
// src/api/myServiceApi.ts
import ApiService from './index';

export interface MyData {
  id: number;
  name: string;
}

export const myServiceApi = {
  getData: () => ApiService.get<MyData[]>('/api/myservice'),
  getItem: (id: number) => ApiService.get<MyData>(`/api/myservice/${id}`),
  create: (data: Partial<MyData>) => ApiService.post<MyData>('/api/myservice', data),
  update: (id: number, data: Partial<MyData>) => 
    ApiService.put<MyData>(`/api/myservice/${id}`, data),
  delete: (id: number) => ApiService.delete(`/api/myservice/${id}`),
};

// Export from src/api/index.ts
export { myServiceApi } from './myServiceApi';
```

### Creating a New Store

```typescript
// src/stores/myStore.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { myServiceApi, MyData } from '../api/myServiceApi';

export const useMyStore = defineStore('myStore', () => {
  // State
  const items = ref<MyData[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Getters
  const itemCount = computed(() => items.value.length);

  // Actions
  async function fetchItems() {
    loading.value = true;
    error.value = null;
    try {
      const response = await myServiceApi.getData();
      items.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch items';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return {
    // State
    items,
    loading,
    error,
    // Getters
    itemCount,
    // Actions
    fetchItems,
  };
});
```

### Creating a New View Component

```vue
<!-- src/views/myservice/MyView.vue -->
<template>
  <div class="container">
    <header class="page-header">
      <h1 class="text-3xl font-bold">My Service</h1>
      <button @click="doAction" class="btn-primary">Action</button>
    </header>

    <div v-if="loading" class="loading-state">Loading...</div>
    
    <div v-else class="content">
      <!-- Your content here -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useMyStore } from '@/stores/myStore';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

const myStore = useMyStore();

// Computed
const items = computed(() => myStore.items);
const loading = computed(() => myStore.loading);

// Methods
const doAction = async () => {
  try {
    // Your action logic
    toast.success('Action completed!');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Action failed';
    toast.error(errorMessage);
  }
};

// Lifecycle
onMounted(() => {
  myStore.fetchItems();
});
</script>

<style scoped>
.container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.btn-primary:hover {
  background-color: #2563eb;
}

.loading-state {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}
</style>
```

## Styling Guidelines

### Tailwind CSS Classes
The project uses Tailwind CSS. Common utility classes:

- **Spacing**: `p-4`, `m-4`, `px-2`, `py-4`, `gap-4`
- **Colors**: `bg-blue-500`, `text-gray-800`, `border-gray-300`
- **Typography**: `text-xl`, `font-bold`, `text-center`
- **Layout**: `flex`, `grid`, `grid-cols-3`, `items-center`
- **Responsive**: `sm:`, `md:`, `lg:`, `xl:`

### Custom Scoped Styles
For component-specific styles, use scoped styles:

```vue
<style scoped>
.my-component {
  /* Component-specific styles */
}
</style>
```

## Toast Notifications

```typescript
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

// Success
toast.success('Operation successful!');

// Error
toast.error('Operation failed!');

// Warning
toast.warning('Warning message');

// Info
toast.info('Information message');
```

## Router Navigation

```typescript
import { useRouter } from 'vue-router';

const router = useRouter();

// Navigate to a route
router.push('/droppoints');
router.push({ name: 'Droppoints' });

// Navigate with params
router.push(`/events/${eventId}`);
router.push({ name: 'EventDetails', params: { id: eventId } });

// Go back
router.back();
```

## API Gateway Configuration

All API calls go through the gateway at `http://localhost:8080`:

```
┌─────────────┐
│   Frontend  │
│ :5173       │
└─────┬───────┘
      │
      ▼
┌─────────────┐
│   Gateway   │
│ :8080       │
└──┬──┬──┬──┬─┘
   │  │  │  │
   ▼  ▼  ▼  ▼
 Bar DP WH Event
 Svc Svc Svc Svc
```

## Authentication

The app uses JWT token authentication:
- Token stored in `localStorage` as `authToken`
- Auto-attached to all requests via interceptor
- Auto-redirect to login on 401 errors

## Error Handling

All API calls should be wrapped in try-catch:

```typescript
try {
  await someStore.someAction();
  toast.success('Success!');
} catch (err) {
  const errorMessage = err instanceof Error ? err.message : 'Operation failed';
  toast.error(errorMessage);
}
```

## TypeScript Tips

1. **Define interfaces** for all data structures
2. **Use computed properties** for derived state
3. **Type your props** with `defineProps<{ ... }>()`
4. **Avoid `any`** - use proper types or `unknown`
5. **Use optional chaining** for safe property access: `item?.property`

## Common Issues & Solutions

### Issue: CORS errors
**Solution:** Ensure backend Gateway has CORS configured for `http://localhost:5173`

### Issue: 401 Unauthorized
**Solution:** Check if JWT token is valid, re-login if needed

### Issue: API not found (404)
**Solution:** Verify backend service is running and registered with Gateway

### Issue: Type errors
**Solution:** Run `npm run build` to check all TypeScript errors

## Best Practices

1. ✅ Use Composition API (`<script setup>`)
2. ✅ Define TypeScript interfaces for all data
3. ✅ Use Pinia stores for state management
4. ✅ Show loading states during async operations
5. ✅ Handle errors gracefully with toast notifications
6. ✅ Use computed properties for derived data
7. ✅ Keep components focused and single-purpose
8. ✅ Use Tailwind utilities before custom CSS
9. ✅ Add confirmations for destructive actions
10. ✅ Test on different screen sizes

## Useful Commands

```bash
# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build

# Run linting
npm run lint

# Format code
npm run format

# Run tests
npm run test:unit
```

## Resources

- [Vue 3 Docs](https://vuejs.org/)
- [Pinia Docs](https://pinia.vuejs.org/)
- [Vue Router Docs](https://router.vuejs.org/)
- [Tailwind CSS Docs](https://tailwindcss.com/)
- [TypeScript Docs](https://www.typescriptlang.org/)
- [Vite Docs](https://vitejs.dev/)

## Getting Help

For questions or issues:
1. Check this guide
2. Review similar existing code
3. Check browser console for errors
4. Verify backend is running
5. Check network tab in DevTools
