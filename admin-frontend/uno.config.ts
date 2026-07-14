import { defineConfig } from 'unocss'
import presetUno from '@unocss/preset-uno'

export default defineConfig({
  presets: [presetUno()],
  shortcuts: {
    'btn-primary': 'bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition',
    'btn-secondary': 'bg-gray-200 text-gray-700 px-4 py-2 rounded hover:bg-gray-300 transition',
    'btn-danger': 'bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition',
  },
})