<template>
  <div>
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Warehouse Management</h1>
            <p class="text-slate-500 mt-1">Inventory, supply requests, and bottle collections</p>
          </div>
        </div>

        <!-- Sub-Navigation -->
        <div class="flex items-center gap-1 mb-6 p-1 bg-slate-100 rounded-xl w-fit">
          <router-link
            :to="{ name: 'WarehouseStock' }"
            class="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200"
            :class="route.name === 'WarehouseStock'
              ? 'bg-white text-slate-900 shadow-sm'
              : 'text-slate-500 hover:text-slate-700'"
          >
            <i class="fas fa-boxes text-xs"></i>
            <span>Stock</span>
          </router-link>
          <router-link
            :to="{ name: 'WarehouseSupply' }"
            class="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200"
            :class="route.name === 'WarehouseSupply'
              ? 'bg-white text-slate-900 shadow-sm'
              : 'text-slate-500 hover:text-slate-700'"
          >
            <i class="fas fa-truck text-xs"></i>
            <span>Supply Requests</span>
            <span
              v-if="pendingCount > 0"
              class="ml-1 px-2 py-0.5 rounded-full text-xs font-bold"
              :class="route.name === 'WarehouseSupply'
                ? 'bg-amber-100 text-amber-700'
                : 'bg-slate-200 text-slate-600'"
            >
              {{ pendingCount }}
            </span>
          </router-link>
          <router-link
            :to="{ name: 'WarehouseCollections' }"
            class="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200"
            :class="route.name === 'WarehouseCollections'
              ? 'bg-white text-slate-900 shadow-sm'
              : 'text-slate-500 hover:text-slate-700'"
          >
            <i class="fas fa-recycle text-xs"></i>
            <span>Empty Collections</span>
          </router-link>
        </div>

        <!-- Child Route Content -->
        <router-view @pending-count="c => pendingCount = c" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const pendingCount = ref(0);
</script>
