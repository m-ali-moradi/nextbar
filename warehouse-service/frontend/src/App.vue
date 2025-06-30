
<template>
  <div>
    <!-- Sidebar -->
    <aside class="sidebar">
      <h1>Warehouse</h1>
      <nav>
        <router-link to="/stock" class="nav-link" active-class="active">Beverage Stock</router-link>
        <router-link to="/supply" class="nav-link" active-class="active">Supply Requests</router-link>
        <router-link to="/drop-points" class="nav-link" active-class="active">Drop Points</router-link>
        <router-link to="/empties"     class="nav-link" active-class="active">Empties Collected</router-link>
      </nav>
    </aside>

    <!-- Main content -->
    <div class="main">

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>