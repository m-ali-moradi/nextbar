<template>
  <div class="flex min-h-screen">
    <Sidebar />
    <div class="flex-1 ml-64">
      <Navbar />
      <main class="p-6 bg-gray-100">
        <h1 class="text-2xl font-bold text-admin-primary mb-4">Admin Dashboard</h1>
        <div v-if="loading" class="text-center">Loading...</div>
        <div v-else-if="error" class="text-red-500 mb-4">{{ error }}</div>
        <div v-else class="bg-white p-6 rounded-lg shadow-lg">
          <h2 class="text-xl mb-4">Manage Users</h2>
          <table class="w-full table-auto">
            <thead>
            <tr class="bg-admin-primary text-white">
              <th class="p-2">ID</th>
              <th class="p-2">Username</th>
              <th class="p-2">Email</th>
              <th class="p-2">Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="user in users" :key="user.id" class="border-b">
              <td class="p-2">{{ user.id }}</td>
              <td class="p-2">{{ user.username }}</td>
              <td class="p-2">{{ user.email }}</td>
              <td class="p-2">
                <button
                  class="bg-admin-primary text-white px-2 py-1 rounded mr-2 hover:bg-admin-secondary"
                  @click="editUser(user)"
                >
                  Edit
                </button>
                <button
                  class="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600"
                  @click="deleteUser(user.id)"
                >
                  Delete
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useQuery, useMutation } from '@tanstack/vue-query';
import { authApi } from '@/api';
import Sidebar from '../../components/common/Sidebar.vue';
import Navbar from '../../components/common/Navbar.vue';

const users = ref([]);
const { isLoading: loading, error, data } = useQuery({
  queryKey: ['users'],
  queryFn: () => authApi.getUsers(),
  onSuccess: (response) => {
    users.value = response.data;
  },
});

const deleteUserMutation = useMutation({
  mutationFn: (userId) => authApi.deleteUser(userId),
  onSuccess: (_data, userId) => {
    // Remove deleted user from the list
    users.value = users.value.filter((user) => user.id !== userId);
  },
});

const editUser = (user) => {
  // Implement edit modal or form
  console.log('Edit user:', user);
};

const deleteUser = (userId) => {
  if (confirm('Are you sure you want to delete this user?')) {
    deleteUserMutation.mutate(userId);
  }
};
</script>

<style scoped>
/* Tailwind handles styling */
</style>