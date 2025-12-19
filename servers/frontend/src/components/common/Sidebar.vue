<template>
  <aside class="w-64 bg-gray-800 text-white h-screen fixed">
    <div class="p-4">
      <h1 class="text-2xl font-bold">Bottle Drop</h1>
    </div>
    <nav class="mt-4">
      <router-link
        to="/bars"
        class="flex items-center p-4 hover:bg-bar-secondary"
        active-class="bg-bar-primary"
      >
        <i class="fas fa-glass-martini mr-2"></i> Bars
      </router-link>
      <router-link
        to="/droppoints"
        class="flex items-center p-4 hover:bg-droppoint-secondary"
        active-class="bg-droppoint-primary"
      >
        <i class="fas fa-map-marker-alt mr-2"></i> Drop Points
      </router-link>
      <router-link
        to="/events"
        class="flex items-center p-4 hover:bg-event-secondary"
        active-class="bg-event-primary"
      >
        <i class="fas fa-calendar mr-2"></i> Events
      </router-link>
      <router-link
        to="/warehouse"
        class="flex items-center p-4 hover:bg-warehouse-secondary"
        active-class="bg-warehouse-primary"
      >
        <i class="fas fa-warehouse mr-2"></i> Warehouse
      </router-link>
      <router-link
        to="/admin"
        class="flex items-center p-4 hover:bg-admin-secondary"
        active-class="bg-admin-primary"
      >
        <i class="fas fa-user-shield mr-2"></i> Admin
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
import '@fortawesome/fontawesome-free/css/all.css';
</script>

<style scoped>
/* Tailwind handles styling */
</style>