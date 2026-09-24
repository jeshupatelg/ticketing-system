/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        theme: {
          bg: 'var(--color-bg)',
          surface: 'var(--color-surface)',
          surfaceHover: 'var(--color-surface-hover)',
          border: 'var(--color-border)',
          text: 'var(--color-text)',
          muted: 'var(--color-muted)',
          primary: 'var(--color-primary)',
          primaryHover: 'var(--color-primary-hover)',
          accent: 'var(--color-accent)',
        }
      }
    },
  },
  plugins: [],
}
