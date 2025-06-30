// vite.config.js
export default {
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8090', // backend port
                changeOrigin: true,
                secure: false,
            }
        }
    }
}
