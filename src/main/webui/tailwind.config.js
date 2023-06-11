/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,svelte}",
  ],
  safelist: [
    {
      pattern: /bg-(red|green|yellow|gray)-500/,
    }
  ],
  theme: {
    extend: {},
  },
  plugins: [
    require('@tailwindcss/forms')
  ],
}

