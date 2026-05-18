export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          primary: '#6366F1',
          secondary: '#10B981',
          accent: '#FB7185',
          danger: '#EF4444',
          warning: '#F59E0B',
          surface: '#FFFFFF',
          background: '#F9FAFB',
          text: '#111827',
          muted: '#6B7280'
        },
        slate: {
          950: '#020617'
        }
      },
      borderRadius: {
        xl: '12px',
        '2xl': '20px'
      },
      boxShadow: {
        sm: '0 1px 2px 0 rgba(0,0,0,0.05)',
        md: '0 4px 6px -1px rgba(0,0,0,0.1)',
        lg: '0 10px 15px -3px rgba(0,0,0,0.1)'
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif']
      }
    }
  }
};

