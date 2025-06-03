<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <form class="bg-white p-6 rounded shadow-lg w-full max-w-md">
      <h2 class="text-xl font-bold mb-4">Add New Bar</h2>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">Bar Name:</label>
        <input
          v-model="barName"
          type="text"
          class="w-full border rounded px-3 py-2"
          placeholder="Enter bar name"
          required
        />
      </div>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">Location:</label>
        <input
          v-model="barLocation"
          type="text"
          class="w-full border rounded px-3 py-2 required"
          placeholder="Enter location"
          required
        />
      </div>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700"
          >Stock Capacity:</label
        >
        <input
          v-model="barStockCapacity"
          type="number"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-green-400 text-lg"
          placeholder="Enter max stock capacity"
          required
          min="1"
          max="1000"
        />
      </div>
      <div class="flex justify-end">
        <button
          @click="$emit('close')"
          class="mr-2 bg-gray-300 px-4 py-2 rounded hover:bg-gray-400"
        >
          Cancel
        </button>
        <button
          @click="addBar"
          class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Save
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import api from "../services/api";

export default {
  name: "AddBarModal",
  props: {
    isOpen: Boolean,
  },
  data() {
    return {
      barName: "",
      barLocation: "",
      barStockCapacity: "",
    };
  },
  methods: {
    async addBar() {
      if (this.barName) {
        await api.addBar(this.barName, this.barLocation, this.barStockCapacity);
        this.$emit("close");
        this.$emit("bar-added");
        this.barName = "";
        this.barLocation = "";
        this.barStockCapacity = "";
      }
    },
  },
};
</script>
