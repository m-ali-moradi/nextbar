<template>
    <div>
      <DashboardSummary :barId="mode==='bar' ? selectedBarId : undefined" />
  
      <h3>Supply Dashboard</h3>
  
      <!-- Mode Toggle -->
      <div class="mode-toggle">
        <button
          :class="{ active: mode==='all' }"
          @click="switchMode('all')"
        >All Bars</button>
        <button
          :class="{ active: mode==='bar' }"
          @click="switchMode('bar')"
        >Bar Supply</button>
      </div>
  
      <!-- ── ALL BARS MODE ── -->
      <div v-if="mode==='all'" class="table-container">
        <table>
          <thead>
            <tr>
              <th>Bar ID</th>
              <th>Name</th>
              <th>Location</th>
              <th>Capacity</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="bar in bars" :key="bar.id">
              <td>{{ bar.id }}</td>
              <td>{{ bar.name }}</td>
              <td>{{ bar.location }}</td>
              <td>{{ bar.maxCapacity }}</td>
              <td>
                <button class="button view" @click="viewBar(bar.id)">
                  View Requests
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-if="bars.length===0">Loading bars…</p>
      </div>
  
      <!-- ── BAR SUPPLY MODE ── -->
      <div v-else>
        <div class="controls">
          <label for="bar-select">Bar:</label>
          <select
            id="bar-select"
            v-model="selectedBarId"
            @change="fetchSupply"
          >
            <option value="">-- choose a bar --</option>
            <option v-for="bar in bars" :key="bar.id" :value="bar.id">
              {{ bar.name }} ({{ bar.location }})
            </option>
          </select>
        </div>
  
        <div class="table-container" v-if="requests.length">
          <table>
            <thead>
              <tr>
                <th>Request ID</th>
                <th>Item</th>
                <th>Qty</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in requests" :key="req.id">
                <td>{{ req.id }}</td>
                <td>{{ req.items[0].productName }}</td>
                <td>{{ req.items[0].quantity }}</td>
                <td>{{ req.status }}</td>
                <td>
                  <!-- REQUESTED → IN_PROGRESS -->
                  <button
                    v-if="req.status==='REQUESTED'"
                    @click="update(req, 'REQUESTED', req.items[0].quantity)"
                    class="button process"
                  >Process</button>
                  <!-- REQUESTED → REJECTED -->
                  <button
                    v-if="req.status==='REQUESTED'"
                    @click="update(req, 'REQUESTED', 0)"
                    class="button reject"
                  >Reject</button>
                  <!-- IN_PROGRESS → DELIVERED -->
                  <button
                    v-else-if="req.status==='IN_PROGRESS'"
                    @click="update(req, 'IN_PROGRESS', req.items[0].quantity)"
                    class="button deliver"
                  >Deliver</button>
                  <span v-else>—</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
  
        <p v-else-if="!selectedBarId">Please select a bar to view requests.</p>
        <p v-else>No requests for this bar.</p>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import axios from 'axios';
  import DashboardSummary from './DashboardSummary.vue';
  
  interface BarDto {
    id: string; name: string; location: string; maxCapacity: number;
  }
  interface SupplyItemDto {
    productId: string; productName: string; quantity: number;
  }
  interface SupplyRequestDto {
    id: string; barId: string; items: SupplyItemDto[];
    status: string; createdAt: string;
  }
  interface ReplenishResponse { message: string; }
  
  const bars          = ref<BarDto[]>([]);
  const requests      = ref<SupplyRequestDto[]>([]);
  const selectedBarId = ref<string>('');
  const mode          = ref<'all'|'bar'>('all');
  
  onMounted(async () => {
    const { data } = await axios.get<BarDto[]>('http://localhost:8085/warehouse/bars');
    bars.value = data;
  });
  
  /**
   * Switch between All Bars / Bar Supply.
   * If switching to 'bar' and a bar is already selected, fetch its requests.
   */
  function switchMode(newMode: 'all'|'bar') {
    mode.value = newMode;
    requests.value = [];
    if (newMode==='bar' && selectedBarId.value) {
      fetchSupply();
    }
  }
  
  /** Handler for the “View Requests” button in All Bars table. */
  function viewBar(barId: string) {
    selectedBarId.value = barId;
    mode.value = 'bar';
    fetchSupply();
  }
  
  /** Fetch supply requests for the selected bar. */
  async function fetchSupply() {
    requests.value = [];
    if (!selectedBarId.value) return;
    const { data } = await axios.get<SupplyRequestDto[]>(
      `http://localhost:8085/warehouse/bars/${selectedBarId.value}/supply`
    );
    requests.value = data;
  }
  
  /**
   * Send a status update with the given quantity & status.
   * - REQUESTED → IN_PROGRESS: send full quantity
   * - REQUESTED → REJECTED: send 0
   * - IN_PROGRESS → DELIVERED: send full quantity
   */
  async function update(
    req: SupplyRequestDto,
    currentStatus: 'REQUESTED'|'IN_PROGRESS',
    quantity: number
  ) {
    const { data } = await axios.put<ReplenishResponse>(
      `http://localhost:8085/warehouse/bars/replenish/${req.barId}/${req.id}`,
      { beverageType: req.items[0].productName, quantity },
      { params: { currentStatus } }
    );
    alert(data.message);
    fetchSupply();
  }
  </script>
  
  <style scoped>
  .mode-toggle {
    margin: 1rem 0;
    display: flex;
    gap: 8px;
  }
  .mode-toggle button {
    padding: 6px 12px;
    border: 1px solid #ddd;
    background: #fff;
    cursor: pointer;
  }
  .mode-toggle button.active {
    background: #3182ce;
    color: #fff;
    border-color: #3182ce;
  }
  
  .controls {
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .button {
    padding: 4px 8px;
    margin-right: 4px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.85rem;
  }
  .button.view    { background: #4a5568; color: #fff; }
  .button.process { background: #3182ce; color: #fff; }
  .button.reject  { background: #e53e3e; color: #fff; }
  .button.deliver { background: #38a169; color: #fff; }
  </style>  