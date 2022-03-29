module.exports = {
  // https://eslint.org/docs/user-guide/configuring#configuration-cascading-and-hierarchy
  // This option interrupts the configuration hierarchy at this file
  // Remove this if you have an higher level ESLint config file (it usually happens into a monorepos)
  root: true,

  parserOptions: {
    parser: '@typescript-eslint/parser',
    ecmaVersion: 2020, // Allows for the parsing of modern ECMAScript features
    sourceType: 'module' // Allows for the use of imports
  },


  // Rules order is important, please avoid shuffling them
  extends: [
    // Base ESLint recommended rules
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
  ],

  plugins: [
    // required to apply rules which need type information
    '@typescript-eslint',
    'import',
  ],
  // add your custom rules here
  rules: {
    // allow async-await
    'generator-star-spacing': 'off',
    // allow paren-less arrow functions
    'arrow-parens': 'off',
    'one-var': 'off',
    'no-void': 'off',
    'multiline-ternary': 'off',

    'import/first': 'off',
    'import/named': 'error',
    'import/namespace': 'error',
    'import/default': 'error',
    'import/export': 'error',
    'import/extensions': 'off',
    'import/no-unresolved': 'off',
    'import/no-extraneous-dependencies': 'off',
    'prefer-promise-reject-errors': 'off',
    'no-useless-return': 'error',

    "no-unused-vars": "off",
    "@typescript-eslint/no-unused-vars": ["error"],
    '@typescript-eslint/no-non-null-assertion': 'warn',
    // typescript-eslint rules
    // note you must disable the base rule as it can report incorrect errors
    '@typescript-eslint/ban-ts-comment': 'off',

    '@typescript-eslint/member-delimiter-style': ['error', {
      multiline: {
        delimiter: 'none',
      },
      singleline: {
        delimiter: 'comma',
        requireLast: false,
      },
    }],

    'indent': 'off',
    '@typescript-eslint/indent': ['error', 2 ],
    'quotes': 'off',
    '@typescript-eslint/quotes': ['error', 'single' ],
    'semi': 'off',
    '@typescript-eslint/semi': ['error', 'never'],

    'brace-style': 'off',
    '@typescript-eslint/brace-style': ['error' ],
    'object-curly-spacing': 'off',
    '@typescript-eslint/object-curly-spacing': ['error', 'always'],
    'comma-spacing': 'off',
    '@typescript-eslint/comma-spacing': ['error' ],
    'comma-dangle': 'off',
    '@typescript-eslint/comma-dangle': ['error', 'always-multiline' ],
    

    'space-before-function-paren': 'off',
    '@typescript-eslint/space-before-function-paren': ['error' ],

    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/explicit-function-return-type': ['error', {
      "allowExpressions": true,
    }],
    '@typescript-eslint/prefer-optional-chain': 'warn',
    '@typescript-eslint/no-non-null-assertion': 'warn',

    'no-console': 'off',
    // allow debugger during development only
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off'
  }
}
