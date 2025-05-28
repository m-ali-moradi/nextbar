<template>
  <!-- Transition effect for sliding in and out of the modal -->
  <transition name="fade-slide">
    <!-- Modal container, visible only when 'visible' is true -->
    <div
      v-if="visible"
      class="fixed top-4 right-4 bg-yellow-200 shadow-lg rounded-lg p-4 w-80 z-50"
      @mouseenter="pauseTimer"
      @mouseleave="resumeTimer"
    >
      <!-- Header section with title and close button -->
      <div class="flex justify-between items-center mb-2">
        <h3 class="text-lg font-semibold text-gray-800">
          Low Stock: {{ item.name }}
        </h3>
        <!-- Close button, triggers cancel on click -->
        <button @click="cancel" class="text-gray-500 hover:text-red-500">
          ✕
        </button>
      </div>
      <!-- Status message showing current quantity and countdown -->
      <p class="text-gray-600 mb-2">
        Current quantity: {{ item.quantity }}. Requesting supply in
        {{ countdown }}s...
      </p>
      <!-- Quantity input section -->
      <div class="flex items-center mb-2">
        <label for="quantity" class="mr-2 text-gray-700">Quantity:</label>
        <!-- Input for user to set requested quantity, with validation -->
        <input
          id="quantity"
          type="number"
          v-model.number="quantity"
          min="1"
          max="50"
          class="border rounded px-2 py-1 w-20"
        />
      </div>
      <!-- Action button section -->
      <div class="flex justify-end">
        <!-- Confirm button to submit the supply request -->
        <button
          @click="confirm"
          class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          OK
        </button>
      </div>
    </div>
  </transition>
</template>

<script>
// Vue component for the auto supply request modal
export default {
  name: "AutoSupplyRequestModal",
  // Define props for the component
  props: {
    item: {
      type: Object, // Expect an object containing item details
      required: true, // Item prop is mandatory
    },
    duration: {
      type: Number, // Duration of the countdown in seconds
      default: 10, // Default countdown duration is 10 seconds
    },
  },
  // Reactive data properties
  data() {
    return {
      visible: true, // Controls modal visibility
      countdown: this.duration, // Countdown timer value
      quantity: 20, // Default requested quantity
      timer: null, // Reference to the interval timer
      paused: false, // Flag to pause the countdown
    };
  },
  // Lifecycle hook: Start the timer when the component is mounted
  mounted() {
    this.startTimer();
  },
  // Component methods
  methods: {
    // Start the countdown timer
    startTimer() {
      this.timer = setInterval(() => {
        if (!this.paused) {
          this.countdown--; // Decrement countdown
          if (this.countdown <= 0) {
            this.confirm(); // Automatically confirm when countdown reaches zero
          }
        }
      }, 1000); // Update every second
    },
    // Pause the countdown timer
    pauseTimer() {
      this.paused = true;
    },
    // Resume the countdown timer
    resumeTimer() {
      this.paused = false;
    },
    // Cancel the supply request and close the modal
    cancel() {
      clearInterval(this.timer); // Clear the timer
      this.visible = false; // Hide the modal
      this.$emit("cancel"); // Emit cancel event to parent
    },
    // Confirm the supply request and close the modal
    confirm() {
      clearInterval(this.timer); // Clear the timer
      this.visible = false; // Hide the modal
      // Emit confirm event with productId and quantity
      this.$emit("confirm", {
        productId: this.item.productId,
        quantity: this.quantity,
      });
    },
  },
};
</script>

<style scoped>
/* Define transition effects for the modal slide animation */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.5s ease; /* Smooth transition over 0.5 seconds */
}

/* Define starting and ending states for the slide animation */
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0; /* Fully transparent at start/end */
  transform: translateX(100%); /* Slide in from the right */
}
</style>
