<template>
  <div class="flex min-h-screen">
    <Sidebar />
    <div class="flex-1 ml-64">
      <Navbar />
      <main class="p-6 bg-gray-100">
        <h1 class="text-2xl font-bold text-bar-primary mb-4">Bars Dashboard</h1>
        <div class="flex justify-between mb-4">
          <button
            @click="showAddBarModal = true"
            class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 shadow-md transition-transform transform hover:scale-105"
          >
            Add Bar
          </button>
          <button
            @click="showDrinkCatalogModal = true"
            class="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600 shadow-md transition-transform transform hover:scale-105"
          >
            View Drinks Catalog
          </button>
        </div>
        <div v-if="barStore.loading" class="text-center">Loading...</div>
        <div v-else-if="barStore.error" class="text-red-500 mb-4">{{ barStore.error }}</div>
        <div v-else-if="barStore.bars.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="bar in barStore.bars"
            :key="bar.id"
            class="bg-white p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow relative overflow-hidden border border-bar-secondary hover:border-bar-primary"
          >
            <div class="absolute top-2 right-3">
              <div class="relative group">
                <button class="text-gray-500 hover:text-gray-700 focus:outline-none">⋮</button>
                <div class="hidden group-hover:block absolute right-0 mt-1 w-32 bg-white border rounded shadow z-10">
                  <button
                    @click="editBar(bar.id)"
                    class="block w-full text-left px-4 py-2 text-sm hover:bg-gray-100"
                  >
                    Edit
                  </button>
                  <button
                    @click="deleteBar(bar.id)"
                    class="block w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-gray-100"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
            <h2 class="text-lg font-semibold text-bar-primary mb-2">{{ bar.name }}</h2>
            <p class="text-gray-600 mb-4">Location: {{ bar.location || 'N/A' }} | Capacity: {{ bar.maxCapacity || 'N/A' }}</p>
            <router-link
              :to="`/bars/${bar.id}`"
              class="text-blue-500 hover:underline font-medium flex items-center"
            >
              <i class="fas fa-door-open mr-2"></i> Open the Bar
            </router-link>
          </div>
        </div>
        <p v-else class="text-gray-500">No bars available. Add a new bar to get started!</p>
        <AddBarModal
          :is-open="showAddBarModal"
          @close="showAddBarModal = false"
          @bar-added="barStore.fetchBars"
        />
        <DrinkCatalogModal
          :is-open="showDrinkCatalogModal"
          @close="showDrinkCatalogModal = false"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useBarStore } from '../../stores/barStore.ts';
import Sidebar from '../../components/common/Sidebar.vue';
import Navbar from '../../components/common/Navbar.vue';
import AddBarModal from '../../components/bars/AddBarModal.vue';
import DrinkCatalogModal from '../../components/bars/DrinkCatalogModal.vue';

const barStore = useBarStore();
const showAddBarModal = ref(false);
const showDrinkCatalogModal = ref(false);

console.log('Bar Store:'+JSON.stringify(barStore.bars));
onMounted(() => {
  barStore.fetchBars();
});

const editBar = (barId) => {
  // Implement edit logic, e.g., open modal or navigate to edit page
  console.log('Edit bar:', barId);
};

const deleteBar = async (barId) => {
  if (confirm('Are you sure you want to delete this bar?')) {
    await barStore.deleteBar(barId);
  }
};
</script>

<style scoped>
/* Tailwind handles styling */
</style>