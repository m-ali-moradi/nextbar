<!-- Add Stock Modal with beautiful input for product name and quantity -->
<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white p-6 rounded shadow-lg w-full max-w-md">
      <h2 class="text-xl font-bold mb-4">Add Stock</h2>
      <form>
        <div class="mb-4">
          <label for="product" class="block text-sm font-medium text-gray-700"
            >Product</label
          >

          <!--style this input with beautiful styles-->
          <select
            v-model="selectedProduct"
            id="product"
            required
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-green-400 text-lg"
          >
            <option
              v-for="product in products"
              :key="product.id"
              :value="product.id"
              required
            >
              {{ product.name }} - {{ product.unitType }}
            </option>
          </select>
        </div>
        <div class="mb-4">
          <label for="quantity" class="block text-sm font-medium text-gray-700"
            >Quantity</label
          >
          <input
            :required="true"
            type="number"
            v-model="quantity"
            id="quantity"
            required
            min="1"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-400 focus:border-green-400 text-lg"
          />
        </div>

        <!--align close button left, open button right-->
        <div class="flex justify-between">
          <button
            @click="$emit('close')"
            class="bg-gray-300 px-4 py-2 rounded hover:bg-gray-400"
          >
            Close
          </button>
          <button
            @click="handleSubmit"
            class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            Add
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import api from "../../api";

// add all fetch Products and handleSubmit methods
export default {
  name: "AddStockModal",
  props: {
    isOpen: Boolean,
  },
  data() {
    return {
      products: [],
      selectedProduct: null,
      quantity: 1,
    };
  },
  watch: {
    isOpen(newVal) {
      if (newVal) {
        this.fetchProducts();
      }
    },
  },
  methods: {
    async handleSubmit() {
      if (this.selectedProduct && this.quantity > 0) {
        try {
          console.log("Adding stock:", {
            barId: this.$route.params.barId,
            productId: this.selectedProduct,
            quantity: this.quantity,
          });
          await api.addStock(
            this.$route.params.barId,
            this.selectedProduct,
            this.quantity
          );
          this.$emit("close");
          this.$emit("stockAdded", { productId: this.selectedProduct, quantity: this.quantity });
        } catch (error) {
          console.error("Error adding stock:", error);
        }
      }
    },
    async fetchProducts() {
      try {
        const products = await api.getProducts();
        this.products = products;
      } catch (error) {
        console.error("Error fetching products:", error);
      }
    },
  },
};
</script>
