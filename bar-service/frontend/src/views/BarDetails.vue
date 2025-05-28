<template>
  <!-- Main container for the Bar Details page -->
  <div class="container mx-auto p-4">
    <!-- Page header with bar name, defaults to "Bar Details" if bar is null -->
    <h1 class="text-2xl font-bold mb-4">{{ bar?.name || "Bar Details" }}</h1>
    <!-- Navigation link to return to the Bars list -->
    <router-link to="/" class="text-blue-500 hover:underline mb-4 inline-block">
      Back to Bars
    </router-link>

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
              <!-- Product name with color-coded background based on product -->
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
              <!-- Quantity with conditional styling based on stock level -->
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
              <!-- Last updated timestamp -->
              <td class="border p-2 text-center align-middle">
                {{ formatDate(item.updatedAt) }}
              </td>
              <!-- Action buttons: Sell, Request, or show request status -->
              <td class="border p-2 text-center align-middle">
                <!-- Sell button, enabled only if quantity > 0 -->
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
                <!-- Show 'Request in Process' if quantity is 0 and a request is pending -->
                <button
                  v-if="
                    item.quantity === 0 && hasPendingRequest(item.productId)
                  "
                  disabled
                  class="bg-yellow-300 text-white px-2 py-1 rounded cursor-not-allowed"
                >
                  Request in Process
                </button>
                <!-- Show 'Request' button if quantity < 5 and no pending request -->
                <button
                  v-else-if="
                    item.quantity < 5 && !hasPendingRequest(item.productId)
                  "
                  @click="openRequestModal(item)"
                  class="bg-yellow-500 text-white px-2 py-1 rounded hover:bg-yellow-600"
                >
                  Request
                </button>
                <!-- Auto Supply Request Modal: Displays as a toast when quantity <= 5 and no pending request -->
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
      <!-- Message displayed when no stock is available -->
      <p v-else class="text-gray-500">No stock available.</p>
    </div>

    <!-- Side-by-Side Serve Log and Total Served Sections -->
    <div class="flex flex-col md:flex-row gap-6 mb-6">
      <!-- Serve Log Section: Displays history of served items -->
      <div class="bg-white p-4 rounded shadow flex-1">
        <h2 class="text-lg font-semibold mb-2">Serve Log</h2>
        <!-- Serve log table, shown only if usage logs exist -->
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
              <!-- Iterate over usage logs -->
              <tr v-for="log in usageLogs" :key="log.id">
                <td class="border p-2">{{ log.productName }}</td>
                <td class="border p-2">{{ log.quantity }}</td>
                <td class="border p-2">{{ formatDate(log.timestamp) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- Message displayed when no usage logs are available -->
        <p v-else class="text-gray-500">No serve log available.</p>
      </div>

      <!-- Total Served Section: Displays total served quantities per product -->
      <div class="bg-white p-4 rounded shadow flex-1">
        <h2 class="text-lg font-semibold mb-2">Total Served</h2>
        <!-- Total served table, shown only if usage logs exist -->
        <div v-if="usageLogs.length" class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead>
              <tr class="bg-gray-200">
                <th class="p-3 text-left">Name</th>
                <th class="p-3 text-left">Total</th>
              </tr>
            </thead>
            <tbody>
              <!-- Iterate over total served data -->
              <tr v-for="item in totalServed" :key="item.name">
                <td class="p-3">{{ item.name }}</td>
                <td class="p-3">{{ item.total }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- Message displayed when no items have been served -->
        <p v-else class="text-gray-500">No items served yet.</p>
      </div>
    </div>

    <!-- Supply Requests Section: Displays pending supply requests -->
    <div class="bg-white p-4 rounded shadow">
      <h2 class="text-lg font-semibold mb-2">Supply Requests</h2>
      <!-- Supply requests table, shown only if requests exist -->
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
            <!-- Iterate over supply requests -->
            <tr v-for="request in supplyRequests" :key="request.id">
              <td class="border p-2">
                {{ request.items[0]?.productName || "Unknown" }}
              </td>
              <td class="border p-2">{{ request.items[0]?.quantity || 0 }}</td>
              <td class="border p-2">{{ request.status }}</td>
              <td class="border p-2">{{ formatDate(request.createdAt) }}</td>
              <!-- Action column with Cancel button, enabled only for REQUESTED status -->
              <td class="border p-2">
                <button
                  :disabled="request.status !== 'REQUESTED'"
                  @click="cancelRequest(request.id)"
                  :class="{
                    'bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600':
                      request.status === 'REQUESTED',
                    'bg-gray-400 text-gray-200 px-2 py-1 rounded cursor-not-allowed':
                      request.status !== 'REQUESTED',
                  }"
                >
                  Cancel
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Message displayed when no supply requests are available -->
      <p v-else class="text-gray-500">No supply requests available.</p>
    </div>

    <!-- Modals for Sell and Supply Request actions -->
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
  </div>
</template>

<script>
// Importing required dependencies and components
import api from "../services/api";
import SellStockModal from "../components/SellStockModal.vue";
import SupplyRequestModal from "../components/SupplyRequestModal.vue";
import AutoSupplyRequestModal from "../components/AutoSupplyRequestModal.vue";

export default {
  name: "BarDetails",
  components: {
    SellStockModal,
    SupplyRequestModal,
    AutoSupplyRequestModal,
  },
  // Reactive data properties
  data() {
    return {
      bar: null, // Bar details
      stock: [], // List of stock items
      usageLogs: [], // Serve log entries
      supplyRequests: [], // Supply request entries
      totalServed: [], // Total served quantities per product
      showSellModal: false, // Controls visibility of SellStockModal
      showRequestModal: false, // Controls visibility of SupplyRequestModal
      selectedItem: null, // Currently selected stock item
    };
  },
  // Computed properties
  computed: {
    // Sort stock items by last updated date (descending)
    sortedStock() {
      return [...this.stock].sort(
        (a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)
      );
    },
  },
  // Methods
  methods: {
    // Fetch bar details, stock, usage logs, supply requests, and total served
    async fetchBarDetails() {
      try {
        const barId = this.$route.params.barId;
        this.bar = await api.getBar(barId);
        this.stock = await api.getStock(barId);
        this.usageLogs = await api.getUsageLogs(barId);
        this.supplyRequests = await api.getSupplyRequests(barId);
        // Transform totalServed array into a list of { name, total } objects
        const totalServedArray = await api.getTotalServed(barId);
        this.totalServed = totalServedArray.map((entry) => {
          const [name, total] = Object.entries(entry)[0];
          return { name, total };
        });
      } catch (error) {
        console.error("Error fetching bar details:", error);
      }
    },
    // Open the Sell Stock modal for the selected item
    openSellModal(item) {
      this.selectedItem = item;
      this.showSellModal = true;
    },
    // Open the Supply Request modal for the selected item
    openRequestModal(item) {
      this.selectedItem = item;
      this.showRequestModal = true;
    },
    // Handle supply request confirmation from AutoSupplyRequestModal
    async handleSupplyRequest({ productId, quantity }) {
      await api.createSupplyRequest(this.$route.params.barId, [
        { productId, quantity },
      ]);
      await this.fetchBarDetails();
    },
    // Cancel a supply request with confirmation
    async cancelRequest(requestId) {
      // Show confirmation dialog to the user
      const confirmed = window.confirm(
        "Are you sure you want to cancel this supply request?"
      );
      if (!confirmed) return;

      try {
        // Send DELETE request to cancel the supply request
        await api.cancelSupplyRequest(this.$route.params.barId, requestId);
        // Refresh data after cancellation
        await this.fetchBarDetails();
        // Show success alert
        alert("Supply request canceled successfully!");
      } catch (error) {
        console.error("Error canceling request:", error);
        // Show error alert
        alert("Failed to cancel supply request. Please try again.");
      }
    },
    // Format a date to display relative time (e.g., "Just now" or "5 min ago")
    formatDate(date) {
      const now = new Date();
      const updated = new Date(date);
      const diffInMinutes = Math.floor((now - updated) / (1000 * 60));
      return diffInMinutes <= 1 ? "Just now" : `${diffInMinutes} min ago`;
    },
    // Check if a product has a pending supply request
    hasPendingRequest(productId) {
      return this.supplyRequests.some((req) =>
        req.items.some(
          (item) =>
            item.productId === productId &&
            (req.status === "REQUESTED" || req.status === "IN_PROGRESS")
        )
      );
    },
  },
  // Lifecycle hook: Fetch data when the component is created
  created() {
    this.fetchBarDetails();
  },
};
</script>
