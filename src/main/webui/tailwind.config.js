/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,svelte}",
  ],
  safelist: [
    {
      pattern: /bg-(red|green|yellow|gray|emerald|amber|slate|blue|purple|pink|indigo|rose)-(500|600|700)/,
    }
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#fef2f4',
          100: '#fde6e9',
          200: '#fbd0d8',
          300: '#f7a8b8',
          400: '#f27693',
          500: '#e6516f',
          600: '#d12f54',
          700: '#b02345',
          800: '#931f40',
          900: '#7d1e3b',
        },
        accent: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
        }
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic': 'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in-out',
        'slide-in': 'slideIn 0.3s ease-out',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideIn: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms')
  ],
}

