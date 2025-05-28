<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white p-6 rounded shadow-lg w-full max-w-md">
      <h1 class="mb-4 align-middle text-center">
        <span
          :class="[
            'inline-block px-10 py-2 rounded text-white text-2xl font-semibold',
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
      <p class="mb-4"></p>
      <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700">Quantity:</label>
        <input
          v-model.number="quantity"
          type="number"
          :max="item.quantity"
          min="1"
          :placeholder="`Available: ${item.quantity}`"
          :disabled="item.quantity === 0"
          @input="validateQuantity"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-green-400 text-lg"
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
          @click="confirmSale"
          :disabled="quantity === 0"
          :class="[
            'text-md text-white px-10 py-2 rounded',
            quantity === 0
              ? 'bg-gray-400 cursor-not-allowed'
              : 'bg-green-500 hover:bg-green-600',
          ]"
        >
          Sell
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from "../services/api";

export default {
  name: "SellStockModal",
  props: {
    isOpen: Boolean,
    item: Object,
    barId: String,
  },
  data() {
    return {
      quantity: 1,
    };
  },

  methods: {
    validateQuantity() {
      if (this.quantity < 1) {
        this.quantity = 0;
      } else if (this.quantity > this.item.quantity) {
        alert("Cannot sell more than available stock!");
        this.quantity = this.item.quantity;
      }
    },
  
    async confirmSale() {
      if (this.quantity === 0) {
        return;
      }
      if (this.quantity > this.item.quantity) {
        alert("Cannot sell more than available stock!");
        return;
      }
      try {
        await api.reduceStock(this.barId, this.item.productId, this.quantity);
        await api.logDrink(this.barId, this.item.productId, this.quantity);
        this.$emit("close");
        this.$emit("sale-confirmed");
        this.quantity = 1;
      } catch (error) {
        console.error("Error during sale:", error);
      }
    },
  },
   
};
</script>
