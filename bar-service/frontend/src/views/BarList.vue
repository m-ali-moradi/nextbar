<template>
  <!-- Main container with Tailwind CSS classes for centering and padding -->
  <div class="container mx-auto p-4">
    <!-- Page title -->
    <h1 class="text-2xl font-bold mb-4">Coditects Bar</h1>

    <!-- Button to open the AddBarModal for creating a new bar -->
    <!-- Uses Tailwind CSS for styling and triggers modal visibility -->
    <div class="flex justify-between items-center mb-4">
      <button
        @click="showAddBarModal = true"
        class="bg-blue-500 text-white px-4 py-2 rounded mb-4 hover:bg-blue-600"
      >
        Add Bar
      </button>
      <button
        @click="showDrinkCatalogModal = true"
        class="bg-yellow-500 text-white px-4 py-2 rounded mb-4 hover:bg-yellow-600"
      >
        View drinks catalog
      </button>
    </div>

    <!-- add three dots in the top right side of the card, allowing to edit or delete the bar-->

    <!-- Conditional rendering: display bars if available -->
    <div v-if="bars.length" class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- Loop through bars array to display each bar -->
      <div
        v-for="bar in bars"
        :key="bar.id"
        class="bg-white p-4 rounded shadow relative"
      >
        <!-- Three dots menu -->
        <div class="absolute top-2 right-3">
          <div class="relative group">
            <button
              class="text-gray-500 hover:text-gray-700 focus:outline-none"
            >
              ⋮
            </button>
            <!-- Dropdown menu -->
            <div
              class="hidden group-hover:block absolute right-0 mt-1 w-32 bg-white border rounded shadow z-10"
            >
              <button
                @click="editBar(bar.id)"
                class="block w-full text-left px-4 py-2 text-sm hover:bg-gray-100"
              >
                Edit
              </button>
              <button
                @click="deleteBar(bar.id)"
                class="block w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-gray-100"
              >
                Delete
              </button>
            </div>
          </div>
        </div>

        <!-- Display bar name -->
        <h2 class="text-lg font-semibold">{{ bar.name }}</h2>
        <p class="text-gray-600">
          Location: {{ bar.location || "N/A" }} Capacity:
          {{ bar.maxCapacity || "N/A" }}
        </p>
        <!-- Link to view bar details -->
        <router-link
          :to="'/bar/' + bar.id"
          class="text-blue-500 hover:underline"
        >
          Open the bar
        </router-link>
      </div>
    </div>

    <!-- Display message if no bars are available -->
    <p v-else class="text-gray-500">
      No bars available. Add a new bar to get started!
    </p>

    <!-- AddBarModal component for adding new bars -->
    <!-- Props and events control modal visibility and bar addition -->
    <AddBarModal
      :is-open="showAddBarModal"
      @close="showAddBarModal = false"
      @bar-added="fetchBars"
    />
    <DrinkCatalogModal
      :is-open="showDrinkCatalogModal"
      @close="showDrinkCatalogModal = false"
    />
  </div>
</template>

<script>
// Import API service for making HTTP requests
import api from "../services/api";

// Import AddBarModal component for adding new bars
import AddBarModal from "../components/AddBarModal.vue";
import DrinkCatalogModal from "../components/DrinkCatalogModal.vue";

export default {
  name: "BarList",

  components: {
    AddBarModal,
    DrinkCatalogModal,
  },

  // Reactive data properties
  data() {
    return {
      bars: [], // Array to store list of bars fetched from API
      showAddBarModal: false,
      showDrinkCatalogModal: false,
    };
  },

  // Component methods
  methods: {
    // Fetch bars from API and update bars array
    async fetchBars() {
      try {
        // Call getBars from API service and store result in bars
        this.bars = await api.getBars();
      } catch (error) {
        // Log errors to console if API call fails
        console.error("Error fetching bars:", error);
      }
    },
    async deleteBar(barId) {
      // Call deleteBar from API service to remove a bar
      try {
        await api.deleteBar(barId);
        // Refresh bars list after deletion
        this.fetchBars();
      } catch (error) {
        console.error("Error deleting bar:", error);
      }
    },
    async editBar(barId) {
      // Navigate to the edit bar page with the selected bar ID
      this.$router.push({ name: "EditBar", params: { barId } });
    },
  },

  // Lifecycle hook: runs when component is created
  created() {
    // Fetch bars when component is initialized
    this.fetchBars();
  },
};
</script>
