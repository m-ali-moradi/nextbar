<template>
  <!-- Main container for the Bar Details page -->
  <div class="container mx-auto p-4">
    <!-- Page header with bar name, defaults to "Bar Details" if bar is null -->
    <h1 class="text-2xl font-bold mb-4">{{ bar?.name || "Bar Details" }}</h1>
    
    <!--align back to bars with add drink button in a div-->
    <div class="flex justify-between mb-4">
      <!-- Navigation link to return to the Bars list -->
      <router-link to="/" class="text-blue-500 hover:underline">
        Close the bar
      </router-link>
      <!-- Add Drink button -->
      <button @click="openAddStockModal"
              class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 mb-4">
        Add Drink
      </button>
    </div>
    <!-- Stock Section: Displays current stock levels -->
    <div class="bg-white p-4 rounded shadow mb-6">
      <h2 class="text-lg font-semibold mb-2">Stock</h2>
      <!-- Stock table, shown only if stock data exists -->
      <div v-if="stock.length" class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="bg-gray-200">
              <th class="border p-2 text-center align-middle">Name</th>
              <th class="border p-2 text-center align-middle">Quantity</th>
              <th class="border p-2 text-center align-middle">Last Update</th>
              <th class="border p-2 text-center align-middle">Action</th>
            </tr>
          </thead>
          <tbody>
            <!-- Iterate over stock items -->
            <tr v-for="item in stock" :key="item.id">
              <td class="border p-2 text-center align-middle">
                <span
                  :class="[
                    'inline-block px-6 py-2 rounded text-white text-md',
                    item.name.toLowerCase() === 'fanta'
                      ? 'bg-yellow-600'
                      : item.name.toLowerCase() === 'coca zero'
                      ? 'bg-black'
                      : item.name.toLowerCase() === 'red bull'
                      ? 'bg-red-500'
                      : item.name.toLowerCase() === 'beer'
                      ? 'bg-yellow-400'
                      : item.name.toLowerCase() === 'wine'
                      ? 'bg-purple-500'
                      : 'bg-gray-500',
                  ]"
                >
                  {{ item.name }}
                </span>
              </td>
              <td class="border p-2 text-center align-middle">
                <span
                  v-if="item.quantity === 0"
                  class="bg-red-500 text-white rounded px-2 py-1"
                >
                  Empty
                </span>
                <span
                  v-else
                  :class="{
                    'bg-yellow-400 text-white rounded px-2 py-1':
                      item.quantity < 10,
                    'bg-green-500 text-white rounded px-2 py-1':
                      item.quantity >= 10,
                  }"
                >
                  {{ item.quantity }}
                </span>
              </td>
              <td class="border p-2 text-center align-middle">
                {{ formatDate(item.updatedAt) }}
              </td>
              <td class="border p-2 text-center align-middle">
                <button
                  v-if="item.quantity > 0"
                  @click="openSellModal(item)"
                  :disabled="item.quantity === 0"
                  :class="[
                    'px-6 py-2 rounded mr-2 text-white text-md',
                    item.quantity === 0
                      ? 'bg-green-300 cursor-not-allowed'
                      : 'bg-green-500 hover:bg-green-600',
                  ]"
                >
                  Sell
                </button>
                <button
                  v-if="
                    item.quantity === 0 && hasPendingRequest(item.productId)
                  "
                  disabled
                  class="bg-yellow-300 text-white px-2 py-1 rounded cursor-not-allowed"
                >
                  Pending Request
                </button>
                <button
                  v-else-if="
                    item.quantity < 5 && !hasPendingRequest(item.productId)
                  "
                  @click="openRequestModal(item)"
                  class="bg-yellow-500 text-white px-2 py-1 rounded hover:bg-yellow-600"
                >
                  Request
                </button>
                <AutoSupplyRequestModal
                  v-if="
                    item.quantity <= 5 && !hasPendingRequest(item.productId)
                  "
                  :item="item"
                  :duration="10"
                  @cancel="fetchBarDetails"
                  @confirm="handleSupplyRequest"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else class="text-gray-500">No stock available.</p>
    </div>

    <div class="flex flex-col md:flex-row gap-6 mb-6">
      <div class="bg-white p-4 rounded shadow flex-1">
        <h2 class="text-lg font-semibold mb-2">Serve Log</h2>
        <div v-if="usageLogs.length" class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="bg-gray-200">
                <th class="border p-2">Name</th>
                <th class="border p-2">Quantity</th>
                <th class="border p-2">Timestamp</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in usageLogs" :key="log.id">
                <td class="border p-2 text-center align-middle">{{ log.productName }}</td>
                <td class="border p-2 text-center align-middle">{{ log.quantity }}</td>
                <td class="border p-2 text-center align-middle">{{ formatDate(log.timestamp) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="text-gray-500">No serve log available.</p>
      </div>

      <div class="bg-white p-4 rounded shadow flex-1">
        <h2 class="text-lg font-semibold mb-2">Total Served</h2>
        <div v-if="usageLogs.length" class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="bg-gray-200">
                <th class="p-3 text-left">Name</th>
                <th class="p-3 text-left">Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in totalServed" :key="item.name">
                <td class="p-3">{{ item.name }}</td>
                <td class="p-3">{{ item.total }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="text-gray-500">No items served yet.</p>
      </div>
    </div>

    <div class="bg-white p-4 rounded shadow">
      <h2 class="text-lg font-semibold mb-2">Supply Requests</h2>
      <div v-if="supplyRequests.length" class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="bg-gray-200">
              <th class="border p-2">Name</th>
              <th class="border p-2">Quantity</th>
              <th class="border p-2">Status</th>
              <th class="border p-2">Request Time</th>
              <th class="border p-2">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="request in supplyRequests" :key="request.id">
              <td class="border p-2 text-center align-middle">
                {{ request.items[0]?.productName || "Unknown" }}
              </td>
              <td class="border p-2 text-center align-middle">{{ request.items[0]?.quantity || 0 }}</td>
              <td class="border p-2 text-center align-middle">{{ request.status }}</td>
              <td class="border p-2 text-center align-middle">{{ formatDate(request.createdAt) }}</td>
              <td class="border p-2 text-center align-middle">
                <!-- Cancel button for REQUESTED status -->
                <button
                  v-if="request.status === 'REQUESTED'"
                  :disabled="request.status !== 'REQUESTED'"
                  @click="cancelRequest(request.id)"
                  class="bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600"
                >
                  Cancel
                </button>
                <!-- Add to Stock button for DELIVERED status -->
                 <button
                  v-else-if="request.status === 'IN_PROGRESS'"

                  class="bg-yellow-500 text-white px-2 py-1 rounded hover:bg-yellow-600"
                >
                  In Progress
                </button>
                <button
                  v-else-if="request.status === 'DELIVERED'"
                  @click="addToStock(request)"
                  class="bg-green-500 text-white px-2 py-1 rounded hover:bg-green-600"
                >
                  Add to Stock
                </button>
                <!-- Completed label for COMPLETED status -->
                <!-- <span
                  v-else-if="request.status === 'COMPLETED'"
                  class="bg-gray-500 text-white px-2 py-1 rounded"
                >
                  Completed
                </span> -->
                <!-- Disabled state for other statuses -->
                <!-- <button
                  v-else
                  disabled
                  class="bg-gray-400 text-gray-200 px-2 py-1 rounded cursor-not-allowed"
                >
                  -
                </button> -->
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else class="text-gray-500">No supply requests available.</p>
    </div>

    <SellStockModal
      :is-open="showSellModal"
      :item="selectedItem"
      :bar-id="$route.params.barId"
      @close="showSellModal = false"
      @sale-confirmed="fetchBarDetails"
    />
    <SupplyRequestModal
      :is-open="showRequestModal"
      :item="selectedItem"
      :bar-id="$route.params.barId"
      @close="showRequestModal = false"
      @supply-requested="fetchBarDetails"
    />
    <AddStockModal
      :is-open="showAddStockModal"
      @close="showAddStockModal = false"
      @stock-added="fetchBarDetails"
    />
  </div>
</template>

<script>
// Importing required dependencies and components
import api from "../services/api";
import SellStockModal from "../components/SellStockModal.vue";
import SupplyRequestModal from "../components/SupplyRequestModal.vue";
import AutoSupplyRequestModal from "../components/AutoSupplyRequestModal.vue";
import AddStockModal from "../components/AddStockModal.vue";

export default {
  name: "BarDetails",
  components: {
    SellStockModal,
    SupplyRequestModal,
    AutoSupplyRequestModal,
    AddStockModal,
  },
  data() {
    return {
      bar: null,
      stock: [],
      usageLogs: [],
      supplyRequests: [],
      totalServed: [],
      showSellModal: false,
      showRequestModal: false,
      selectedItem: null,
      showAddStockModal: false,
    };
  },
  computed: {
    sortedStock() {
      return [...this.stock].sort(
        (a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)
      );
    },
  },
  methods: {
    async fetchBarDetails() {
      try {
        const barId = this.$route.params.barId;
        this.bar = await api.getBar(barId);
        this.stock = await api.getStock(barId);
        this.usageLogs = await api.getUsageLogs(barId);
        this.supplyRequests = await api.getSupplyRequests(barId);
        const totalServedArray = await api.getTotalServed(barId);
        this.totalServed = totalServedArray.map((entry) => {
          const [name, total] = Object.entries(entry)[0];
          return { name, total };
        });
      } catch (error) {
        console.error("Error fetching bar details:", error);
      }
    },
    openSellModal(item) {
      this.selectedItem = item;
      this.showSellModal = true;
    },
    openRequestModal(item) {
      this.selectedItem = item;
      this.showRequestModal = true;
    },
    openAddStockModal() {
      this.showAddStockModal = true;
    },
    async handleSupplyRequest({ productId, quantity }) {
      await api.createSupplyRequest(this.$route.params.barId, [
        { productId, quantity },
      ]);
      await this.fetchBarDetails();
    },
    async cancelRequest(requestId) {
      const confirmed = window.confirm(
        "Are you sure you want to cancel this supply request?"
      );
      if (!confirmed) return;

      try {
        await api.cancelSupplyRequest(this.$route.params.barId, requestId);
        await this.fetchBarDetails();
        alert("Supply request canceled successfully!");
      } catch (error) {
        console.error("Error canceling request:", error);
        alert("Failed to cancel supply request. Please try again.");
      }
    },
    async addToStock(request) {
      try {
        const barId = this.$route.params.barId;
        const productId = request.items[0].productId;
        const quantity = request.items[0].quantity;

        // Send addStock request
        await api.addStock(barId, productId, quantity);

        // Update request status to COMPLETED
        await api.updateSupplyRequest(barId, request.id, "COMPLETED");

        // Refresh data
        await this.fetchBarDetails();
        alert("Stock added successfully!");
      } catch (error) {
        console.error("Error adding stock:", error);
        alert("Failed to add stock. Please try again.");
      }
    },
    formatDate(date) {
      const now = new Date();
      const updated = new Date(date);
      const diffInMinutes = Math.floor((now - updated) / (1000 * 60));
      return diffInMinutes <= 1 ? "Just now" : `${diffInMinutes} min ago`;
    },
    hasPendingRequest(productId) {
      return this.supplyRequests.some((req) =>
        req.items.some(
          (item) =>
            item.productId === productId &&
            (req.status === "REQUESTED" || req.status === "IN_PROGRESS" || req.status === "DELIVERED")
        )
      );
    },
    requestCompleted(productId) {
      return this.supplyRequests.some((req) =>
        req.items.some(
          (item) =>
            item.productId === productId &&
            (req.status === "COMPLETED")
        )
      );
    },
  },
  created() {
    this.fetchBarDetails();
  },
};
</script>