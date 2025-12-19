<template>
  <div class="flex min-h-screen">
    <Sidebar />
    <div class="flex-1 ml-64">
      <Navbar />
      <main class="p-6 bg-gray-100">
        <h1 class="text-2xl font-bold text-bar-primary mb-4">{{ barStore.currentBar?.name || 'Bar Details' }}</h1>
        <div class="flex justify-between mb-4">
          <router-link
            to="/bars"
            class="text-blue-500 hover:underline flex items-center"
          >
            <i class="fas fa-arrow-left mr-2"></i> Back to Bars
          </router-link>
          <button
            @click="showAddStockModal = true"
            class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 shadow-md transition-transform transform hover:scale-105"
          >
            Add Drink
          </button>
        </div>
        <div v-if="barStore.loading" class="text-center">Loading...</div>
        <div v-else-if="barStore.error" class="text-red-500 mb-4">{{ barStore.error }}</div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <!-- Stock Section -->
          <div class="bg-white p-6 rounded-lg shadow-lg col-span-1 md:col-span-2 lg:col-span-3">
            <h2 class="text-xl text-bar-primary mb-4">Stock</h2>
            <div v-if="barStore.stock.length" class="overflow-x-auto">
              <table class="w-full border-collapse table-auto">
                <thead>
                  <tr class="bg-gray-200">
                    <th class="border p-2 text-center align-middle">Name</th>
                    <th class="border p-2 text-center align-middle">Quantity</th>
                    <th class="border p-2 text-center align-middle">Last Update</th>
                    <th class="border p-2 text-center align-middle">Action</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in sortedStock" :key="item.id">
                    <td class="border p-2 text-center align-middle">
                      <span
                        :class="[
                          'inline-block px-6 py-2 rounded text-white text-md',
                          getProductColor(item.name),
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
                          'bg-yellow-400 text-white rounded px-2 py-1': item.quantity < 10,
                          'bg-green-500 text-white rounded px-2 py-1': item.quantity >= 10,
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
                          item.quantity === 0 ? 'bg-green-300 cursor-not-allowed' : 'bg-green-500 hover:bg-green-600 transition-transform transform hover:scale-105',
                        ]"
                      >
                        Sell
                      </button>
                      <button
                        v-if="item.quantity === 0 && !hasPendingRequest(item.productId)"
                        @click="openRequestModal(item)"
                        class="bg-yellow-500 text-white px-6 py-2 rounded hover:bg-yellow-600 transition-transform transform hover:scale-105"
                      >
                        Request Supply
                      </button>
                      <span
                        v-if="hasPendingRequest(item.productId)"
                        class="bg-yellow-300 text-white px-2 py-1 rounded cursor-not-allowed"
                      >
                        Pending Request
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p v-else class="text-gray-500">No stock available.</p>
          </div>

          <!-- Usage Logs Section -->
          <div class="bg-white p-6 rounded-lg shadow-lg">
            <h2 class="text-xl text-bar-primary mb-4">Usage Logs</h2>
            <ul v-if="barStore.usageLogs.length">
              <li v-for="log in barStore.usageLogs" :key="log.id" class="text-bar-primary mb-2 border-b pb-2">
                {{ log.productName }}: {{ log.quantity }} at {{ formatDate(log.timestamp) }}
              </li>
            </ul>
            <p v-else class="text-gray-500">No usage logs available.</p>
          </div>

          <!-- Total Served Section -->
          <div class="bg-white p-6 rounded-lg shadow-lg">
            <h2 class="text-xl text-bar-primary mb-4">Total Served</h2>
            <ul v-if="barStore.totalServed.length">
              <li v-for="served in barStore.totalServed" :key="served.name" class="text-bar-primary mb-2 border-b pb-2">
                {{ served.name }}: {{ served.total }}
              </li>
            </ul>
            <p v-else class="text-gray-500">No total served data available.</p>
          </div>

          <!-- Supply Requests Section -->
          <div class="bg-white p-6 rounded-lg shadow-lg">
            <h2 class="text-xl text-bar-primary mb-4">Supply Requests</h2>
            <ul v-if="barStore.supplyRequests.length">
              <li v-for="request in barStore.supplyRequests" :key="request.id" class="text-bar-primary mb-2 border-b pb-2">
                Status: {{ request.status }} (Created: {{ formatDate(request.createdAt) }})
                <button
                  v-if="request.status === 'REQUESTED'"
                  @click="cancelRequest(request.id)"
                  class="bg-red-500 text-white px-2 py-1 rounded ml-2 hover:bg-red-600"
                >
                  Cancel
                </button>
                <button
                  v-if="request.status === 'DELIVERED'"
                  @click="addToStock(request)"
                  class="bg-green-500 text-white px-2 py-1 rounded ml-2 hover:bg-green-600"
                >
                  Add to Stock
                </button>
              </li>
            </ul>
            <p v-else class="text-gray-500">No supply requests available.</p>
          </div>
        </div>
        <SellStockModal
          :isOpen="showSellModal"
          :item="selectedItem"
          :barId="route.params.barId"
          @close="showSellModal = false"
          @sale-confirmed="handleSell"
        />
        <SupplyRequestModal
          :isOpen="showRequestModal"
          :item="selectedItem"
          :barId="route.params.barId"
          @close="showRequestModal = false"
          @supply-requested="handleSupplyRequest"
        />
        <AddStockModal
          :is-open="showAddStockModal"
          @close="showAddStockModal = false"
          @stock-added="handleAddStock"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useBarStore } from '../../stores/barStore.ts';
import Sidebar from '../../components/common/Sidebar.vue';
import Navbar from '../../components/common/Navbar.vue';
import SellStockModal from '../../components/bars/SellStockModal.vue';
import SupplyRequestModal from '../../components/bars/SupplyRequestModal.vue'; 
import AutoSupplyRequestModal from '../../components/bars/AutoSupplyRequestModal.vue'; 
import AddStockModal from '../../components/bars/AddStockModal.vue'; 

const barStore = useBarStore();
const route = useRoute();
const showSellModal = ref(false);
const showRequestModal = ref(false);
const showAddStockModal = ref(false);
const selectedItem = ref(null);

onMounted(() => {
  const barId = route.params.barId;
  barStore.fetchBarDetails(barId);
});

const sortedStock = computed(() => {
  return [...barStore.stock].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
});

const getProductColor = (name) => {
  const lowerName = name.toLowerCase();
  if (lowerName === 'fanta') return 'bg-yellow-600';
  if (lowerName === 'coca zero') return 'bg-black';
  if (lowerName === 'red bull') return 'bg-red-500';
  if (lowerName === 'beer') return 'bg-yellow-400';
  if (lowerName === 'wine') return 'bg-purple-500';
  return 'bg-gray-500';
};

const formatDate = (date) => {
  const now = new Date();
  const updated = new Date(date);
  const diffInMinutes = Math.floor((now - updated) / (1000 * 60));
  return diffInMinutes <= 1 ? 'Just now' : `${diffInMinutes} min ago`;
};

const hasPendingRequest = (productId) => {
  return barStore.supplyRequests.some((req) =>
    req.items.some(
      (item) =>
        item.productId === productId &&
        (req.status === 'REQUESTED' || req.status === 'IN_PROGRESS' || req.status === 'DELIVERED')
    )
  );
};

const openSellModal = (item) => {
  selectedItem.value = item;
  showSellModal.value = true;
};

const openRequestModal = (item) => {
  selectedItem.value = item;
  showRequestModal.value = true;
};

const handleSell = async ({ quantity }) => {
  const barId = route.params.barId;
  const productId = selectedItem.value.productId;
  await barStore.reduceStock(barId, productId, quantity);
  showSellModal.value = false;
};

const handleSupplyRequest = async ({ productId, quantity }) => {
  const barId = route.params.barId;
  await barStore.createSupplyRequest(barId, [{ productId, quantity }]);
  showRequestModal.value = false;
};

const cancelRequest = async (requestId) => {
  if (confirm('Are you sure you want to cancel this supply request?')) {
    const barId = route.params.barId;
    await barStore.cancelSupplyRequest(barId, requestId);
  }
};

const addToStock = async (request) => {
  const barId = route.params.barId;
  const productId = request.items[0].productId;
  const quantity = request.items[0].quantity;
  await barStore.addStock(barId, productId, quantity);
  await barStore.updateSupplyRequest(barId, request.id, 'COMPLETED');
};

const handleAddStock = async ({ productId, quantity }) => {
  const barId = route.params.barId;
  await barStore.addStock(barId, productId, quantity);
  showAddStockModal.value = false;
};
</script>

<style scoped>
/* Tailwind handles styling */
</style>