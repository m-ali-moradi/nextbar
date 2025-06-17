# Drop point service

The **Drop-points service (DS)** is a Vue 3 application designed to streamline and optimize drop points operations. It includes functionalities such as managing empties, ensuring efficient workflow and data management.

---

## Features

- **Drop point Management**: Add, edit, and delete drop-points empties.

---

## Project Structure

```
Droppoint Management System
├── package-lock.json
├── package.json
├── public
│   └── favicon.ico
├── src
│   ├── App.vue
│   ├── assets
│   │   ├── base.css
│   │   ├── logo.svg
│   │   └── main.css
│   ├── components
│   │   ├── Droppoint.vue
│   │   ├── __tests__
│   │   │   └── HelloWorld.spec.js
│   │   └── icons
│   │       ├── IconCommunity.vue
│   │       ├── IconDocumentation.vue
│   │       ├── IconEcosystem.vue
│   │       ├── IconSupport.vue
│   │       └── IconTooling.vue
│   ├── main.js
│   ├── router
│   │   └── index.js
│   ├── stores
│   │   ├── droppoint.js
│   └── views
│       ├── DroppointView.vue
├── vite.config.js
└── vitest.config.js
```

---

## Technologies Used

- **Vue 3**: Frontend framework.
- **Pinia**: State management library.
- **Vite**: Build tool for faster development.
- **Axios**: For making HTTP requests.
- **Vitest**: Unit testing framework.
- **ESLint**: Linter for maintaining code quality.

---

## Installation

Follow these steps to set up and run the project locally:

### Prerequisites

Ensure you have the following installed:

- [Node.js](https://nodejs.org/) (v14 or higher)
- [npm](https://www.npmjs.com/)

### Steps

1. Clone the repository:

   ```sh
   git clone https://github.com/Team-Coditects/dmsa-sose25-coditects.git
   ```

2. Navigate to the project directory:

   ```sh
   cd drop-points-service/Frontend/drop-points
   ```

3. Install dependencies:

   ```sh
   npm install
   ```

4. Start the development server:
   ```sh
   npm run dev
   ```

---

## Scripts

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Compile and Minify for Production

```sh
npm run build
```

### Run Unit Tests

```sh
npm run test:unit
```

### Lint with ESLint

```sh
npm run lint
```

---

## Recommended Tools

- [VSCode](https://code.visualstudio.com/)
- [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (disable Vetur)

---

## Future Enhancements

- **Analytics Dashboard**: Visualize key metrics like stock levels, order trends, etc.
- **Role-Based Access Control**: Enhance security by implementing user roles.
- **Notifications**: Real-time alerts for low stock or order updates.

---

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).

---

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

---

## Contact

For inquiries or support, contact [allen.mwandunga001@stud.fh-dortmund.de].
