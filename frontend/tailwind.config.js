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
        // Dark theme colors
        background: '#0a0a0a',
        surface: '#111111',
        card: '#1a1a1a',
        border: '#2a2a2a',
        
        // Blue accent colors
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        
        // Text colors for dark theme
        foreground: '#ffffff',
        muted: '#a1a1aa',
        'muted-foreground': '#71717a',
      },
    },
  },
  plugins: [],
}