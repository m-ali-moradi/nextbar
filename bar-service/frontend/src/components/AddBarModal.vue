<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white p-6 rounded shadow-lg w-full max-w-md">
      <h2 class="text-xl font-bold mb-4">Add New Bar</h2>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">Bar Name:</label>
        <input v-model="barName" type="text" class="w-full border rounded px-3 py-2" placeholder="Enter bar name">
      </div>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">Location:</label>
        <input v-model="barLocation" type="text" class="w-full border rounded px-3 py-2" placeholder="Enter location">
      </div>
      <div class="flex justify-end">
        <button @click="$emit('close')" class="mr-2 bg-gray-300 px-4 py-2 rounded hover:bg-gray-400">Cancel</button>
        <button @click="addBar" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">Save</button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api';

export default {
  name: 'AddBarModal',
  props: {
    isOpen: Boolean
  },
  data() {
    return {
      barName: '',
      barLocation: ''
    };
  },
  methods: {
    async addBar() {
      if (this.barName) {
        await api.addBar(this.barName, this.barLocation);
        this.$emit('close');
        this.$emit('bar-added');
        this.barName = '';
        this.barLocation = '';
      }
    }
  }
};
</script>