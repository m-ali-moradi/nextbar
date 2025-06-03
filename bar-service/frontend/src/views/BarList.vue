<template>
  <!-- Main container with Tailwind CSS classes for centering and padding -->
  <div class="container mx-auto p-4">
    <!-- Page title -->
    <h1 class="text-2xl font-bold mb-4">Coditects Bar</h1>

    <!-- Button to open the AddBarModal for creating a new bar -->
    <!-- Uses Tailwind CSS for styling and triggers modal visibility -->
    <button
      @click="showAddBarModal = true"
      class="bg-blue-500 text-white px-4 py-2 rounded mb-4 hover:bg-blue-600"
    >
      Add Bar
    </button>

    <!-- Conditional rendering: display bars if available -->
    <div v-if="bars.length" class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- Loop through bars array to display each bar -->
      <div
        v-for="bar in bars"
        :key="bar.id"
        class="bg-white p-4 rounded shadow"
      >
        <!-- Display bar name -->
        <h2 class="text-lg font-semibold">{{ bar.name }}</h2>
        <!-- Display bar location, fallback to 'N/A' if not provided -->
        <p class="text-gray-600">Location: {{ bar.location || "N/A" }}</p>
        <!-- Link to view bar details, navigates to /bar/:id route -->
        <router-link
          :to="'/bar/' + bar.id"
          class="text-blue-500 hover:underline"
        >
          View Details
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
  </div>
</template>

<script>
// Import API service for making HTTP requests
import api from "../services/api";

// Import AddBarModal component for adding new bars
import AddBarModal from "../components/AddBarModal.vue";

export default {
  name: "BarList",

  components: {
    AddBarModal,
  },

  // Reactive data properties
  data() {
    return {
      bars: [], // Array to store list of bars fetched from API
      showAddBarModal: false,
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
  },

  // Lifecycle hook: runs when component is created
  created() {
    // Fetch bars when component is initialized
    this.fetchBars();
  },
};
</script>
