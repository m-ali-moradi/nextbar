<!-- <template>
  <div id="app">
    <nav class="navbar">
      <RouterLink to="/beveragestock">Beverage Stock</RouterLink>
      <RouterLink to="/replenish">Replenish</RouterLink>
      <RouterLink to="/emptybottle">Empty Bottles</RouterLink>
    </nav>
    <main class="container">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.navbar {
  background: #2c3e50;
  padding: 1rem;
  display: flex;
  justify-content: center;
  gap: 2rem;
}
.navbar a {
  color: #ecf0f1;
  text-decoration: none;
  font-weight: bold;
}
.navbar a.router-link-active {
  border-bottom: 2px solid #fff;
}
.container {
  padding: 2rem;
  max-width: 900px;
  margin: auto;
}
</style> -->
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
      <!-- <header class="header">
        <h2>Warehouse Dashboard</h2>
        <button class="button process">Logout</button>
      </header> -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
// no script needed here
</script>