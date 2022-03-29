import { defineConfig, loadEnv } from 'vite'
import { createVuePlugin } from 'vite-plugin-vue2'
import components from 'unplugin-vue-components/vite'
import { VuetifyResolver } from 'unplugin-vue-components/resolvers'

import path from 'path'
import fs from 'fs'

const ABS_ROOT_DIR = path.resolve(__dirname, 'src/')

// sass/scss custom variables
let sassAdditionalData
if (fs.existsSync(path.resolve(ABS_ROOT_DIR, 'styles/variables.sass'))) {
  sassAdditionalData = '\n@import \'@/styles/variables.sass\'\n'
}

let scssAdditionalData
if (fs.existsSync(path.resolve(ABS_ROOT_DIR, 'styles/variables.scss'))) {
  scssAdditionalData = '\n@import \'@/styles/variables.scss\'\n'
}

// console.log(sassAdditionalData, scssAdditionalData)
// https://vitejs.dev/config/
export default defineConfig(() => {
  return {
    plugins: [
      createVuePlugin(/* options */),
      components({
        resolvers: [
          VuetifyResolver(),
        ],
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    css: {
      preprocessorOptions: {
        sass: {
          additionalData: sassAdditionalData,
        },
        scss: {
          additionalData: scssAdditionalData,
        },
      },
    },
    server: {
      port: 8080,
    },
  }
})
