<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white p-6 rounded shadow-lg w-full max-w-md">
      <h2 class="text-xl font-bold mb-4">Available drinks at our bar</h2>
      <p class="mb-4">You can make stock out of these drinks for Bars.</p>
      <table class="min-w-full border-collapse border border-gray-300">
        <thead>
          <tr>
            <th class="px-4 py-2">ID</th>
            <th class="px-4 py-2">Product Name</th>
            <th class="px-4 py-2">Type</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td class="border px-4 py-2">{{ product.id }}</td>
            <td class="border px-4 py-2">{{ product.name }}</td>
            <td class="border px-4 py-2">{{ product.unitType }}</td>
          </tr>
        </tbody>
      </table>
    <div class="flex justify-end mt-4">
            <button @click="$emit('close')" class="bg-gray-300 px-4 py-2 rounded hover:bg-gray-400">Close</button>
    </div>
    </div>
  </div>
</template>
<script>
import api from '../services/api';

export default {
  name: 'DrinkCatalogModal',
  props: {
    isOpen: Boolean
  },
  data() {
    return {
        products: [],
        };
  },
  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.fetchProducts();
      }
    }
  },
  methods: {
    async fetchProducts() {
      try {
        const products = await api.getProducts(); 
        this.products = products;
      } catch (error) {
        console.error('Error fetching products:', error);
      }
    }
  }
};
</script>