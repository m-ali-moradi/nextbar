module.exports = [
  {
    ignores: ['dist/**', 'node_modules/**', '**/*.vue', '**/*.ts'],
  },
  {
    files: ['**/*.{js,mjs,cjs}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
    },
    rules: {},
  },
];
