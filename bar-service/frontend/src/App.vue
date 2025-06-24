<template>
  <div class="min-h-screen bg-gray-100 font-sans">
    <router-view></router-view>
    <footer class="text-center py-4 bg-gray-200 mt-8">
      © 2025 Coditects Bar. All rights reserved.
    </footer>
  </div>
</template>

<script>
export default {
  name: "App"
};
</script>