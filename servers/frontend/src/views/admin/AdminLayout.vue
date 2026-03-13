<template>
  <div>
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">User Management</h1>
            <p class="text-slate-500 mt-1">Administration, roles, and access control</p>
          </div>
          <button
            v-if="$route.name === 'AdminUsers'"
            @click="$router.push({ name: 'AdminUsers', query: { create: '1' } })"
            class="btn-primary"
          >
            <i class="fas fa-user-plus"></i>
            <span>Create User</span>
          </button>
        </div>

        <!-- Sub-Navigation -->
        <div class="flex items-center gap-1 mb-6 p-1 bg-slate-100 rounded-xl w-fit">
          <router-link
            :to="{ name: 'AdminUsers' }"
            class="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200"
            :class="$route.name === 'AdminUsers'
              ? 'bg-white text-slate-900 shadow-sm'
              : 'text-slate-500 hover:text-slate-700'"
          >
            <i class="fas fa-users text-xs"></i>
            <span>Users</span>
            <span
              v-if="userCount > 0"
              class="ml-1 px-2 py-0.5 rounded-full text-xs font-bold"
              :class="$route.name === 'AdminUsers'
                ? 'bg-primary-100 text-primary-700'
                : 'bg-slate-200 text-slate-600'"
            >
              {{ userCount }}
            </span>
          </router-link>
          <router-link
            :to="{ name: 'AdminRoles' }"
            class="flex items-center gap-2 px-5 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200"
            :class="$route.name === 'AdminRoles'
              ? 'bg-white text-slate-900 shadow-sm'
              : 'text-slate-500 hover:text-slate-700'"
          >
            <i class="fas fa-shield-alt text-xs"></i>
            <span>Roles & Services</span>
          </router-link>
        </div>

        <!-- Child Route Content -->
        <router-view
          @user-count="handleUserCount"
        />
  </div>
</template>

<script setup>
import { ref } from 'vue';

const userCount = ref(0);

function handleUserCount(count) {
  userCount.value = count;
}
</script>
