<template>
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
</style>
