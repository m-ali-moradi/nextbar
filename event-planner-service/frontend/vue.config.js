// vue.config.js
module.exports = {
  devServer: {
    port: 5173,             // ← serve your Vue app on 3000
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // your Spring Boot
        changeOrigin: true,
        secure: false
      }
    }
  }
};
