/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{vue,js,ts}'],
  theme: {
    extend: {
      colors: {
        bar: {
          primary: '#1E40AF', // Blue for bars
          secondary: '#60A5FA',
        },
        droppoint: {
          primary: '#15803D', // Green for drop points
          secondary: '#4ADE80',
        },
        event: {
          primary: '#7E22CE', // Purple for events
          secondary: '#A78BFA',
        },
        warehouse: {
          primary: '#C2410C', // Orange for warehouse
          secondary: '#F59E0B',
        },
        admin: {
          primary: '#374151', // Gray for admin
          secondary: '#9CA3AF',
        },
      },
    },
  },
  plugins: [],
};