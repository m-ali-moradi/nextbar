<template>
  <router-view />
</template>

<script setup>
/**
 * ============================================================
 * Root App Component
 * ============================================================
 * 
 * This is the root component that provides routing via router-view.
 * 
 * WebSocket Connection:
 * The useWebSocketEvents composable auto-connects when imported.
 * Components use the composable directly to subscribe to events.
 */

// The useWebSocketEvents composable auto-connects on module load,
// so no explicit initialization is needed here.
// Components like WarehouseView import it and use watch() to react to events.
</script>

<style scoped>
/* Tailwind handles styling */
</style>
