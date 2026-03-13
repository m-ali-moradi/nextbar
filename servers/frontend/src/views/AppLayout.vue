<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />

    <div class="flex-1 ml-72 min-w-0">
      <Navbar />

      <main class="p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/common/Sidebar.vue';
import Navbar from '@/components/common/Navbar.vue';
</script>
