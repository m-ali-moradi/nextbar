<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white p-6 rounded shadow-lg w-full max-w-md">
      <h1 class="mb-4 align-middle text-center"> Request supply for
        <br>
        <span
          :class="[
            'inline-block px-4 py-1 rounded text-white text-lg',
            item.name.toLowerCase() === 'fanta'
              ? 'bg-yellow-600'
              : item.name.toLowerCase() === 'coca zero'
              ? 'bg-black'
              : item.name.toLowerCase() === 'red bull'
              ? 'bg-red-500'
              : item.name.toLowerCase() === 'beer'
              ? 'bg-yellow-500'
              : item.name.toLowerCase() === 'wine'
              ? 'bg-purple-500'
              : 'bg-gray-500',
          ]"
        >
          {{ item.name }}
        </span>
      </h1>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">Quantity:</label>
        <input
          v-model.number="quantity"
          type="number"
          min="1"
          max="50"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-green-400 text-lg"
          @input="validateQuantity"
        />
      </div>
      <div class="flex justify-end">
        <button
          @click="$emit('close')"
          class="mr-2 bg-gray-300 px-10 py-2 rounded hover:bg-gray-400"
        >
          Cancel
        </button>
        <button
          @click="requestSupply"
          :disabled="quantity < 1 || quantity > 50"
          :class="[
            'text-white px-10 py-2 rounded',
            quantity < 1 || quantity > 50
              ? 'bg-gray-400 cursor-not-allowed'
              : 'bg-yellow-500 hover:bg-yellow-600'
          ]"
        >
          Request
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api';

export default {
  name: 'SupplyRequestModal',
  props: {
    isOpen: Boolean,
    item: Object,
    barId: String
  },
  data() {
    return {
      quantity: 10
    };
  },
  methods: {
    validateQuantity() {
      if (this.quantity < 1) {
        this.quantity = 0;
      } else if (this.quantity > 50) {
        alert("You can only request up to 50 items at a time.");
        this.quantity = 50;
      }
    },
    async requestSupply() {
      if (this.quantity < 1 || this.quantity > 50) {
        alert("Invalid quantity. Please enter a value between 1 and 50.");
        return; // Prevent request if quantity is invalid
      }
      try {
        await api.createSupplyRequest(this.barId, [
          { productId: this.item.productId, quantity: this.quantity }
        ]);
        this.$emit('close');
        this.$emit('supply-requested');
        this.quantity = 10;
      } catch (error) {
        console.error("Error during supply request:", error);
        alert("Failed to request supply. Please try again later.");
      }
    }
  }
};
</script>