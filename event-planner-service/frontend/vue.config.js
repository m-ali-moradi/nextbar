/*
const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true
})
*/

// vue.config.js
module.exports = {
  devServer: {
    proxy: {
      // any request that starts with /api will be forwarded to your Spring Boot
      '/api': {
        target: 'http://localhost:8080',  // adjust if your backend runs on a different port
        changeOrigin: true,
        // optional: if your backend uses a context path, you can rewrite it:
        // pathRewrite: { '^/api': '' }
      },
    },
  },
};

